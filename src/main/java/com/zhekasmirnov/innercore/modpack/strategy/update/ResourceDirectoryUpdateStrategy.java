package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.*;

public class ResourceDirectoryUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
        FileUtils.clearFileTree(getAssignedDirectory().getLocation(), true);
        getAssignedDirectory().getLocation().mkdirs();
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {
        File file = new File(getAssignedDirectory().getLocation(), path);
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = new FileOutputStream(file)) {
            FileTools.inStreamToOutStream(stream, outputStream);
        }
    }

    @Override
    public void finishUpdate() throws IOException {
    }
}
