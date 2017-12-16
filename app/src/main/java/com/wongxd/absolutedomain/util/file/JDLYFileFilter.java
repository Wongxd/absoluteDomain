package com.wongxd.absolutedomain.util.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class JDLYFileFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
        // TODO Auto-generated method stub  
//      return false;  

        if (file.isDirectory())
            return true;
        else {
            String name = file.getName();
            return name.endsWith(".JDLY") || name.endsWith(".jdly");
        }

    }


    /**
     * get all the music file in the rootpath.
     *
     * @param rootPath
     */
    public static List<File> getAllFilePath(String rootPath) {

        List<File> results = new ArrayList<>();

        File file = new File(rootPath);
        File[] files = file.listFiles(new JDLYFileFilter());
        for (File f : files) {
            if (f.isDirectory()) {
                getAllFilePath(f.getPath());
            } else {
                results.add(f);
            }
        }

        return results;

    }


}  