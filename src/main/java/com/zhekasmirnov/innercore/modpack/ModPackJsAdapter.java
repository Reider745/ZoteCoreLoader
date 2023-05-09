package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.NativeArray;

import java.io.File;

public class ModPackJsAdapter {
    private final ModPack modPack;

    public ModPackJsAdapter(ModPack modPack) {
        this.modPack = modPack;
    }

    public ModPack getModPack() {
        return modPack;
    }

    public File getRootDirectory() {
        return modPack.getRootDirectory();
    }

    public String getRootDirectoryPath() {
        return modPack.getRootDirectory().getAbsolutePath();
    }

    public String getModsDirectoryPath() {
        ModPackDirectory directory = modPack.getDirectoryOfType(ModPackDirectory.DirectoryType.MODS);
        if (directory != null) {
            return directory.getLocation().getAbsolutePath();
        } else {
            ICLog.i("ERROR", "Currently selected modpack has no mod directory, falling back to default one");
            return new File(modPack.getRootDirectory(), "mods").getAbsolutePath(); // fallback
        }
    }

    public ModPackManifest getManifest() {
        return modPack.getManifest();
    }

    public ModPackPreferences getPreferences() {
        return modPack.getPreferences();
    }

    public DirectorySetRequestHandler getRequestHandler(String type) {
        return modPack.getRequestHandler(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase()));
    }

    public NativeArray getAllDirectories() {
        return ScriptableObjectHelper.createArray(modPack.getAllDirectories());
    }

    public NativeArray getDirectoriesOfType(String type) {
        return ScriptableObjectHelper.createArray(modPack.getDirectoriesOfType(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase())));
    }

    public ModPackDirectory getDirectoryOfType(String type) {
        return modPack.getDirectoryOfType(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase()));
    }
}
