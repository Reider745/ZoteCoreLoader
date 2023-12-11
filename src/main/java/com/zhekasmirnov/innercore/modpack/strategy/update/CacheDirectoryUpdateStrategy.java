package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.horizon.util.FileUtils;

import java.io.*;

public class CacheDirectoryUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
        FileUtils.clearFileTree(getAssignedDirectory().getLocation(), true);
        getAssignedDirectory().getLocation().mkdirs();
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {
    }

    @Override
    public void finishUpdate() throws IOException {
    }
}
