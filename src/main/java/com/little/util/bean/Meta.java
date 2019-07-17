package com.little.util.bean;

import lombok.Data;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * @author nic
 * @version 1.0
 * @date 19-7-17
 */
@Data
public class Meta {
    private String format;
    private Long musicId;
    private String musicName;
    private List<List<String>> artist;
    private String album;
    private Long albumId;
    private Long albumPicDocId;
    private String albumPic;
    private Long mvId;
    private int flag;
    private int bitrate;
    private Long duration;
    private List<String> alias;
    private List<String> transNames;

    private String artists;

    public void parserArtists(){
        StringBuilder buff = new StringBuilder();
        Iterator<List<String>> it = artist.iterator();
        while (it.hasNext()){
            buff.append(it.next().get(0));
            if (it.hasNext()){
                buff.append(",");
            }
        }
        artists = buff.toString();
    }

    public String getFilePath(String basePath){
        return basePath + File.separator + artists + "-" + musicName + "." + format;
    }
}
