package com.zhekasmirnov.innercore.modpack.strategy.update;

import java.io.IOException;
import java.io.InputStream;

public class DirectoryDeniedUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
        throw new IOException(
                "update denied for directory " + getAssignedDirectory() + ", following calls will be ignored");
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {
    }

    @Override
    public void finishUpdate() throws IOException {
    }
}
