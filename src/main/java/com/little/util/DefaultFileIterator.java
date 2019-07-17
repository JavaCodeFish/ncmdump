package com.little.util;

import java.io.File;
import java.io.FileFilter;

/**
 * @author created by qingchuan.xia
 */
public class DefaultFileIterator implements FileIterator {
    @Override
    public void iterator(File rootFile, final FileFilter filter, Executable<File> executable) {
        //对文件进行处理
        if (!rootFile.isDirectory()){
            if (filter.accept(rootFile)) {
                executable.execute(rootFile);
                return;
            }
            return;
        }
        //对目录进行遍历
        File[] files = rootFile.listFiles(pathname -> pathname.isDirectory() || filter.accept(pathname));
        if (files != null && files.length > 0){
            for (File file : files){
                iterator(file,filter,executable);
            }
        }
    }
}
