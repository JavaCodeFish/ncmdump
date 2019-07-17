package com.little.util;

import com.google.gson.Gson;
import com.little.util.bean.Meta;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author nic
 * @version 1.0
 * @date 19-7-17
 */
public class DefaultNcmConvert implements BaseNcmConvert {

    private NcmFileStream stream;
    private final static String HEADER_FLAG = "CTENFDAM";
    private final static String CORE_KEY = "hzHRAmso5kInbaxW";
    private final static String META_KEY = "#14ljk_!\\]&0U<'(";

    @Override
    public void convert(File file) {
        file = file.getAbsoluteFile();
        stream = new NcmFileStream();
        try {
            stream.setFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        byte[] header = getHeader();
        if (!new String(header).equals(HEADER_FLAG)){
            System.out.println("文件类型错误!");
            return;
        }
        stream.skip(2);
        byte[] keyData = getKey();
        String metaData = getMeta();
        int crc32 = getCrc32();
        stream.skip(5);
        byte[] coverData = getAlbumCover();
        byte[] musicData = getMusic(keyData);
        Meta meta = new Gson().fromJson(metaData,Meta.class);
        meta.parserArtists();
        File saveFile = new File(meta.getFilePath(file.getParent()));
        stream.printFile(saveFile,musicData);
        try {
            AudioFile music = AudioFileIO.read(saveFile);
            Tag tag = music.getTag();
            tag.deleteArtworkField();
            tag.setField(FieldKey.ALBUM,meta.getAlbum());
            tag.setField(FieldKey.ARTIST,meta.getArtists());
            tag.setField(FieldKey.TITLE,meta.getMusicName());
            Artwork artwork = new Artwork();
            artwork.setBinaryData(coverData);
            artwork.setMimeType("image/jpeg");
            artwork.setPictureType(3);
            tag.setField(artwork);
            music.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getHeader() {
        NcmFileStream.ValidByte header = stream.read(8);
        return header.getValidByte();
    }

    private byte[] getKey(){
        int keyLength = getLength();
        NcmFileStream.ValidByte keyData = stream.read(keyLength);
        byte[] encryptedKey = keyData.getValidByte();
        for (int i = 0; i < encryptedKey.length; i++) {
            encryptedKey[i] ^= 0x64;
        }
        byte[] bytes = AesUtils.decrypt(CORE_KEY.getBytes(), encryptedKey);
        if (bytes == null) {
            throw new RuntimeException("无法解密");
        }
        String key = new String(bytes).trim().replaceAll("[\r\n]","").substring(17);
        bytes = key.getBytes();
        byte[] arr = new byte[256];
        for (int i = 0;i < arr.length;i++){
            arr[i] = (byte) i;
        }
        for (int i = 0, j = 0; i < arr.length; i++) {
            j = (j + arr[i] + bytes[i % bytes.length]) & 0xFF;
            byte tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }

    private String getMeta(){
        int metaLength = getLength();
        NcmFileStream.ValidByte metaData = stream.read(metaLength);
        byte[] bytes = metaData.getValidByte();
        for (int i = 0;i < bytes.length;i++){
            bytes[i] ^= 0x63;
        }
        bytes = Arrays.copyOfRange(bytes,22,bytes.length);
        bytes = Base64.getDecoder().decode(bytes);
        bytes = AesUtils.decrypt(META_KEY.getBytes(), bytes);
        if (bytes == null){
            throw new RuntimeException("歌曲信息获取失败!");
        }
        return new String(bytes).trim().replaceAll("[\r\n]","").substring(6);
    }

    private int getCrc32(){
        return getLength();
    }

    private byte[] getAlbumCover(){
        int coverLength = getLength();
        NcmFileStream.ValidByte coverData = stream.read(coverLength);
        return coverData.getValidByte();
    }

    private byte[] getMusic(byte[] key){
        NcmFileStream.ValidByte musicData = stream.read();
        byte[] musicBytes = musicData.getValidByte();
        byte[] resultKey = new byte[key.length];
        for (int i = 0; i < key.length; i++) {
            resultKey[i] = key[(key[i] + key[(i + key[i]) & 0xFF]) & 0xFF];
        }
        for (int i = 0;i < musicBytes.length;i++){
            byte cursor = resultKey[(i + 1) % resultKey.length];
            musicBytes[i] ^= cursor;
        }
        return musicBytes;
    }

    private int getLength(){
        NcmFileStream.ValidByte data = stream.read(4);
        byte[] byteData = data.getValidByte();
        for (int i = 0,j = byteData.length - 1;i < j;i++,j--){
            byte tmp = byteData[i];
            byteData[i] = byteData[j];
            byteData[j] = tmp;
        }
        int length = 0;
        for (byte b : byteData){
            length <<= 8;
            length |= (b & 0xff);
        }
        return length;
    }
}
