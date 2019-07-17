package com.little.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.Arrays;

/**
 * @author nic
 * @version 1.0
 * @date 19-7-17
 */
public class NcmFileStream {

    private FileInputStream fis;
    private File file;

    @Getter
    public static class ValidByte {
        private int length;
        private byte[] data;

        public ValidByte(int length) {
            this.length = length;
            data = new byte[length];
        }

        public byte[] getValidByte() {
            return length == 0 ? new byte[0] : Arrays.copyOfRange(data, 0, length);
        }
    }

    public void setFile(File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("文件:\"" + file.getName() + "\"不存在!");
        }
        if (fis == null) {
            fis = new FileInputStream(file);
        }
        this.file = file;
    }

    public ValidByte read(){
        return read((int) file.length());
    }

    public ValidByte read(int length) {
        if (fis == null){
            throw  new IllegalArgumentException(this.getClass().getName() + "未初始化!");
        }
        ValidByte data = new ValidByte(length);
        try {
            data.length = fis.read(data.data);
        } catch (Exception e){
            close();
        } finally {
            if (data.length == -1){
                close();
                fis = null;
            }
        }
        return data;
    }

    public void skip(int length) {
        if (fis == null) {
            throw new IllegalArgumentException(this.getClass().getName() + "未初始化!");
        }
        try {
            fis.skip(length);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printFile(File targetFile,byte[] data){
        try {
            if (targetFile.exists()){
                throw new RuntimeException("文件:\"" + targetFile.getName() + "\"已存在!");
            }
            if (!targetFile.createNewFile()){
                throw new RuntimeException("无法创建文件:\"" + targetFile.getName() + "\"!");
            }
            try (FileOutputStream stream = new FileOutputStream(targetFile)){
                stream.write(data);
                stream.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
