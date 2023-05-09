package com.zhekasmirnov.innercore.modpack.strategy.request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ConfigDirectoryRequestStrategy extends DirectoryRequestStrategy {
    private static String normalizeFileName(String name) {
        return name.replaceAll("[\\\\/\\s]", "-");
    }

    @Override
    public File get(String location, String name) {
        location = normalizeFileName(location);
        if (name.startsWith("config/")) {
            return new File(new File(getAssignedDirectory().getLocation(), location), name.substring(7));
        } else {
            return new File(getAssignedDirectory().getLocation(), location + "-" + normalizeFileName(name));
        }
    }

    @Override
    public File get(String location) {
        return getAssignedDirectory().getLocation(); // locations are not directories, so return root
    }

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
    public List<File> getAll(String location) {
        List<File> result = new ArrayList<>();
        File[] allFiles = getAssignedDirectory().getLocation().listFiles();
        if (allFiles != null) {
            String prefix = location.toLowerCase() + "-";
            for (File file : allFiles) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase(location)) {
                    addAllRecursive(file, result, null);
                } else if (file.isFile() && file.getName().toLowerCase().startsWith(prefix)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAllLocations() {
        Set<String> locationSet = new HashSet<>();
        locationSet.add("innercore");

        File[] allFiles = getAssignedDirectory().getLocation().listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                String name = file.getName().toLowerCase();
                if (file.isDirectory() && !name.equals(".keep-unchanged")) {
                    locationSet.add(name);
                } else if (file.isFile()) {
                    int separator = name.indexOf('-');
                    if (separator != -1) {
                        locationSet.add(name.substring(0, separator));
                    }
                }
            }
        }
        return new ArrayList<>(locationSet);
    }
}
