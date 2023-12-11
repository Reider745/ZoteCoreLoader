package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AllFilesDirectoryExtractStrategy extends DirectoryExtractStrategy {
    protected void addAllRecursive(File file, List<File> files, Predicate<File> filter) {
        if (file.isDirectory()) {
            File[] filesInDir = file.listFiles();
            if (filesInDir != null) {
                for (File child : filesInDir) {
                    addAllRecursive(child, files, filter);
                }
            }
        } else if (file.isFile() && (filter == null || filter.test(file))) {
            files.add(file);
        }
    }

    @Override
    public List<File> getFilesToExtract() {
        List<File> files = new ArrayList<>();
        addAllRecursive(getAssignedDirectory().getLocation(), files, null);
        return files;
    }

    @Override
    public String getEntryName(String relativePath, File file) {
        return relativePath;
    }
}
