package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import java.io.File;

public abstract class DirectoryBasedMod extends ApparatusMod {
    private final File directory;

    public DirectoryBasedMod(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public void initDependencies() {
        File cfg = new File(directory, "dependency-config.json");
        if (cfg.isFile()) {
            try {
                getDependencies().loadFrom(FileUtils.readJSON(cfg));
            } catch (Exception e) {
                ICLog.e("MOD-LOADER", "failed to load " + cfg.getAbsolutePath(), e);
            }
        }
    }
}
