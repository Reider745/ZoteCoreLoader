package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;

import java.io.IOException;
import java.io.InputStream;

public abstract class DirectoryUpdateStrategy {
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

    public abstract void beginUpdate() throws IOException;

    public abstract void updateFile(String path, InputStream stream) throws IOException;

    public abstract void finishUpdate() throws IOException;
}
