package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllIgnoredDirectoryExtractStrategy extends DirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        return new ArrayList<>();
    }

    @Override
    public String getEntryName(String relativePath, File file) {
        return null; // should not be called
    }
}
