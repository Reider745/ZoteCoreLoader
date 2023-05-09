package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.*;

public class CacheDirectoryUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
       // FileUtils.clearFileTree(getAssignedDirectory().getLocation(), true);
        getAssignedDirectory().getLocation().mkdirs();
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {

    }

    @Override
    public void finishUpdate() throws IOException {

    }
}
