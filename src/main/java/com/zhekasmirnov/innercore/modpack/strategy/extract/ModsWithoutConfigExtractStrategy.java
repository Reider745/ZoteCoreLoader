package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModsWithoutConfigExtractStrategy extends AllFilesDirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        List<File> files = new ArrayList<>();
        addAllRecursive(getAssignedDirectory().getLocation(), files, file ->
                !file.getName().toLowerCase().matches("(config(.info)?.json|.staticids)") &&
                        !file.getParentFile().getName().equalsIgnoreCase("config"));
        return files;
    }
}
