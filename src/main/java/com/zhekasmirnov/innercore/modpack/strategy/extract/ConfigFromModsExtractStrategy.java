package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigFromModsExtractStrategy extends AllFilesDirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        List<File> result = new ArrayList<>();
        File directory = getAssignedDirectory().getLocation();
        File[] filesInDir = directory.listFiles();
        if (filesInDir != null) {
            for (File modDir : filesInDir) {
                if (modDir.isDirectory()) {
                    File[] filesInModDir = modDir.listFiles();
                    if (filesInModDir != null) {
                        for (File file : filesInModDir) {
                            if (file.isFile() && file.getName().toLowerCase().matches("config(.info)?.json")) {
                                result.add(file);
                            } else if (file.isDirectory() && file.getName().equalsIgnoreCase("config")) {
                                addAllRecursive(file, result, null);
                            }
                        }
                    }
                } else if (modDir.isFile() && modDir.getName().equalsIgnoreCase(".staticids")) {
                    result.add(modDir);
                }
            }
        }
        return result;
    }

    private static String normalizeFileName(String name) {
        return name.replaceAll("[\\\\/\\s]", "-");
    }

    @Override
    public String getEntryName(String relativePath, File file) {
        if (relativePath.equalsIgnoreCase(".staticids")) {
            return "innercore-ids.json";
        }

        int sepIndex = relativePath.indexOf('/');
        if (sepIndex == -1) {
            return relativePath;
        }
        String modName = normalizeFileName(relativePath.substring(0, sepIndex));
        String path = relativePath.substring(sepIndex + 1);

        if (path.startsWith("config/")) {
            return modName + "/" + path.substring(7);
        } else {
            return modName + "-" + normalizeFileName(path);
        }
    }
}
