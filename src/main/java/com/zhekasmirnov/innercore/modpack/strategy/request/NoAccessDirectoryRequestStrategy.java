package com.zhekasmirnov.innercore.modpack.strategy.request;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoAccessDirectoryRequestStrategy extends DirectoryRequestStrategy {
    @Override
    public File get(String location, String name) {
        return null;
    }

    @Override
    public File get(String location) {
        return null;
    }

    @Override
    public List<File> getAll(String location) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getAllLocations() {
        return new ArrayList<>();
    }
}
