package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigDirectoryExtractStrategy extends AllFilesDirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        List<File> files = new ArrayList<>();
        addAllRecursive(getAssignedDirectory().getLocation(), files,
                file -> !file.getAbsolutePath().toLowerCase().contains(".keep-unchanged"));
        return files;
    }
}
