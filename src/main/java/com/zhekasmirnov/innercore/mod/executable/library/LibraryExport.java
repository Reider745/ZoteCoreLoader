package com.zhekasmirnov.innercore.mod.executable.library;

/**
 * Created by zheka on 24.02.2018.
 */

public class LibraryExport {
    public final String name;
    public final Object value;

    public LibraryExport(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    private int targetVersion = -1;

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public boolean hasTargetVersion() {
        return targetVersion != -1;
    }
}
