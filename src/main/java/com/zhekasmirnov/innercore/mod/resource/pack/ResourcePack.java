package com.zhekasmirnov.innercore.mod.resource.pack;

import com.zhekasmirnov.innercore.mod.resource.types.ResourceFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zheka on 03.07.2017.
 */

public class ResourcePack implements IResourcePack {
    private String dir;
    public ArrayList<ResourceFile> resourceFiles = new ArrayList<ResourceFile>();

    public ResourcePack(String dir) {
        this.dir = dir;
    }

    public String getAbsolutePath() {
        return dir;
    }

    public String getPackName() {
        return dir.substring(dir.lastIndexOf("/") + 1);
    }

    public boolean isLoaded = false;

    public void readAllFiles() {
        resourceFiles.clear();
        try {
            findFilesInDir(new File(dir), resourceFiles);
            isLoaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findFilesInDir(File dir, ArrayList<ResourceFile> files) throws IOException {
        File[] filesInDir = dir.listFiles();
        for (File file : filesInDir) {
            if (file.isDirectory())
                findFilesInDir(file, files);
            else
                files.add(new ResourceFile(this, file));
        }
    }

}
