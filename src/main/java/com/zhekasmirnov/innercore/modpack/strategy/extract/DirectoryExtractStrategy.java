package com.zhekasmirnov.innercore.modpack.strategy.extract;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.util.List;

public abstract class DirectoryExtractStrategy {
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

    // return all files, that could be extracted from directory
    public abstract List<File> getFilesToExtract();

    // parses relative path to get entry name
    public abstract String getEntryName(String relativePath, File file);

    public String getFullEntryName(File file) {
        String path = file.getAbsolutePath();
        String dir = getAssignedDirectory().getLocation().getAbsolutePath();
        if (!path.startsWith(dir)) {
            throw new IllegalArgumentException("getEntryNameForFile got file, not contained in assigned directory");
        }
        String relative = FileTools.cleanupPath(path.substring(dir.length()));
        return (getAssignedDirectory().getPathPattern() + "/" + FileTools.cleanupPath(getEntryName(relative, file))).replaceAll("//", "/");
    }
}
