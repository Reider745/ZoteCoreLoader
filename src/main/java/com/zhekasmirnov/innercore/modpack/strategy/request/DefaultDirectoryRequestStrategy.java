package com.zhekasmirnov.innercore.modpack.strategy.request;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultDirectoryRequestStrategy extends DirectoryRequestStrategy {
    @Override
    public File get(String location, String name) {
        return new File(get(location), name);
    }

    @Override
    public File get(String location) {
        return new File(getAssignedDirectory().getLocation(), location);
    }

    @Override
    public List<File> getAll(String location) {
        List<File> result = new ArrayList<>();
        File[] files = new File(getAssignedDirectory().getLocation(), location).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAllLocations() {
        List<String> result = new ArrayList<>();
        File[] files = getAssignedDirectory().getLocation().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.add(file.getName());
                }
            }
        }
        return result;
    }
}
