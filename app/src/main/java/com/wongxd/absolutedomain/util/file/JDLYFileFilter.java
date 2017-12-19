package com.wongxd.absolutedomain.util.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
        if (!file.exists()) try {
            File dir = file.getParentFile();
            dir.mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] files = file.listFiles(new JDLYFileFilter());
        if (files == null) return null;
        for (File f : files) {
            if (f == null) continue;
            if (f.isDirectory()) {
                getAllFilePath(f.getPath());
            } else {
                results.add(f);
            }
        }

        return results;

    }


}  