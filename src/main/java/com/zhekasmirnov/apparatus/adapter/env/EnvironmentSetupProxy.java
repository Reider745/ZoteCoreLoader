package com.zhekasmirnov.apparatus.adapter.env;

import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;

import java.io.File;

public interface EnvironmentSetupProxy {
    void addResourceDirectory(ApparatusMod mod, File directory);

    void addGuiAssetsDirectory(ApparatusMod mod, File directory);

    void addNativeDirectory(ApparatusMod mod, File directory);

    void addJavaDirectory(ApparatusMod mod, File directory);

    void addResourcePackDirectory(ApparatusMod mod, File directory);

    void addBehaviorPackDirectory(ApparatusMod mod, File directory);
}
