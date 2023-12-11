package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.mod.build.Mod;

/**
 * Created by zheka on 24.02.2018.
 */

public class LibraryDependency {
    public final String libName;
    public final int minVersion;

    private Mod parentMod;

    public LibraryDependency(String libName, int minVersion) {
        this.libName = libName;
        this.minVersion = minVersion;
    }

    public LibraryDependency(String formattedString) {
        String[] parts = formattedString.split(":");

        if (parts.length == 1) {
            libName = formattedString;
            minVersion = -1;
            return;
        }

        if (parts.length > 2) {
            throw new IllegalArgumentException("invalid library dependency " + formattedString
                    + ", it should be formatted as <name>:<versionCode>");
        }

        try {
            libName = parts[0];
            minVersion = Integer.valueOf(parts[1]);
        } catch (NumberFormatException err) {
            throw new IllegalArgumentException("invalid library dependency " + formattedString
                    + ", it should be formatted as <name>:<versionCode>");
        }
    }

    public void setParentMod(Mod parentMod) {
        this.parentMod = parentMod;
    }

    public Mod getParentMod() {
        return parentMod;
    }

    public boolean isMatchesLib(Library lib) {
        return libName.equals(lib.getLibName()) && lib.getVersionCode() >= minVersion;
    }

    public boolean hasTargetVersion() {
        return minVersion != -1;
    }

    @Override
    public String toString() {
        return libName + (hasTargetVersion() ? ":" + minVersion : "");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
