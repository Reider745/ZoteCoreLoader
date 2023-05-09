package com.zhekasmirnov.innercore.modpack.strategy.request;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class DirectoryRequestStrategy {
    private ModPackDirectory directory;

    public void assignToDirectory(ModPackDirectory directory) {
        if (this.directory != null) {
            throw new IllegalStateException();
        }
        this.directory = directory;
    }

    public ModPackDirectory getAssignedDirectory() {
        return directory;
    }

    // returns file for given location and name
    public abstract File get(String location, String name);

    // returns file for given location (usually directory)
    public abstract File get(String location);

    // returns all files in given location
    public abstract List<File> getAll(String location);

    // returns all location names
    public abstract List<String> getAllLocations();

    // same as get(location, name), but assures parent directory for file
    public File assure(String location, String name) {
        File file = get(location, name);
        file.getParentFile().mkdirs();
        return file;
    }

    // deletes file, returned by get(location, name)
    public boolean remove(String location, String name) {
        File file = get(location, name);
        return file.delete();
    }

    // returns all files, that can be requested
    public List<File> getAllFiles() {
        List<File> result = new ArrayList<>();
        for (String location : getAllLocations()) {
            result.addAll(getAll(location));
        }
        return result;
    }
}
