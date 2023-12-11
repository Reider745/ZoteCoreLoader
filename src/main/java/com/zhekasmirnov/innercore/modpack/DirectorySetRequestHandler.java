package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirectorySetRequestHandler {
    private final List<ModPackDirectory> directories = new ArrayList<>();
    private final List<DirectoryRequestStrategy> strategies = new ArrayList<>();

    public DirectorySetRequestHandler() {
    }

    public DirectorySetRequestHandler(ModPackDirectory... directories) {
        for (ModPackDirectory directory : directories) {
            add(directory);
        }
    }

    public DirectorySetRequestHandler(Collection<ModPackDirectory> directories) {
        for (ModPackDirectory directory : directories) {
            add(directory);
        }
    }

    public List<ModPackDirectory> getDirectories() {
        return directories;
    }

    public void add(ModPackDirectory directory) {
        directories.add(directory);
        strategies.add(directory.getRequestStrategy());
    }

    public File get(String location, String name) {
        File firstNonNull = null;
        for (DirectoryRequestStrategy strategy : strategies) {
            File file = strategy.get(location, name);
            if (file != null) {
                if (file.exists()) {
                    return file;
                }
                if (firstNonNull == null) {
                    firstNonNull = file;
                }
            }
        }
        return firstNonNull;
    }

    public File get(String location) {
        File firstNonNull = null;
        for (DirectoryRequestStrategy strategy : strategies) {
            File file = strategy.get(location);
            if (file != null) {
                if (file.exists()) {
                    return file;
                }
                if (firstNonNull == null) {
                    firstNonNull = file;
                }
            }
        }
        return firstNonNull;
    }

    public List<File> getAllAtLocation(String location) {
        List<File> result = new ArrayList<>();
        for (DirectoryRequestStrategy strategy : strategies) {
            result.addAll(strategy.getAll(location));
        }
        return result;
    }

    public List<String> getAllLocations() {
        List<String> result = new ArrayList<>();
        for (DirectoryRequestStrategy strategy : strategies) {
            result.addAll(strategy.getAllLocations());
        }
        return result;
    }
}
