package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.minecraft.version.ResourceGameVersion;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.BuildConfig;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModBuilder;
import com.zhekasmirnov.innercore.modpack.ModPack;

import java.io.File;

public class LegacyInnerCoreMod extends DirectoryBasedMod {
    private final ModPack modPack;
    private final String locationName;
    private Mod legacyModInstance = null;

    public LegacyInnerCoreMod(File directory, ModPack modPack, String locationName) {
        super(directory);
        this.modPack = modPack;
        this.locationName = locationName;
    }

    public LegacyInnerCoreMod(Mod mod) {
        super(new File(mod.dir));
        legacyModInstance = mod;
        this.modPack = null;
        this.locationName = null;
    }

    public Mod getLegacyModInstance() {
        return legacyModInstance;
    }

    @Override
    public void init() {
        if (legacyModInstance == null) {
            ICLog.d("INNERCORE-MOD", "building and importing mod: " + getDirectory().getName());
            legacyModInstance = ModBuilder.buildModForDir(getDirectory().getAbsolutePath() + "/", modPack, locationName);
            if (legacyModInstance != null) {
                legacyModInstance.onImport();
            } else {
                ICLog.i("INNERCORE-MOD", "failed to build mod: build.config file could not be parsed");
            }
        }
        if (legacyModInstance != null) {
            getInfo().pullLegacyModProperties(legacyModInstance);
        }
    }

    @Override
    public boolean isEnabledAndAbleToRun() {
        if (legacyModInstance == null) {
            return false;
        }
        if (!legacyModInstance.getConfig().getBool("enabled")) {
            return false;
        }
        return legacyModInstance.buildConfig.defaultConfig.gameVersion.isCompatible();
    }

    @Override
    public void onSettingUpEnvironment(EnvironmentSetupProxy proxy, ModLoaderReporter reporter) {
        // resource & gui
        /*for (BuildConfig.ResourceDir resourceDir : legacyModInstance.buildConfig.resourceDirs) {
            if (resourceDir.gameVersion.isCompatible()) {
                switch (resourceDir.resourceType) {
                    case RESOURCE:
                        //proxy.addResourceDirectory(this, new File(getDirectory(), resourceDir.path));
                        break;
                    case GUI:
                        proxy.addGuiAssetsDirectory(this, new File(getDirectory(), resourceDir.path));
                        //break;
                }
            }
        }*/

        // addon behavior packs
       /* String behaviorPackDirName = legacyModInstance.buildConfig.defaultConfig.behaviorPacksDir;
        if (behaviorPackDirName != null) {
            File[] behaviorPacks = new File(getDirectory(), behaviorPackDirName).listFiles();
            if (behaviorPacks != null) {
                for (File behaviorPack : behaviorPacks) {
                    if (new ResourceGameVersion(new File(behaviorPack, "game_version.json")).isCompatible()) {
                        proxy.addBehaviorPackDirectory(this, behaviorPack);
                    }
                }
            }
        }

        // addon resource packs
        String resourcePackDirName = legacyModInstance.buildConfig.defaultConfig.resourcePacksDir;
        if (resourcePackDirName != null) {
            File[] resourcePacks = new File(getDirectory(), resourcePackDirName).listFiles();
            if (resourcePacks != null) {
                for (File resourcePack : resourcePacks) {
                    if (new ResourceGameVersion(new File(resourcePack, "game_version.json")).isCompatible()) {
                        proxy.addResourcePackDirectory(this, resourcePack);
                    }
                }
            }
        }*/

        // java directories
        for (BuildConfig.DeclaredDirectory directory : legacyModInstance.buildConfig.javaDirectories) {
            if (directory.version.isCompatible()) {
                proxy.addJavaDirectory(this, directory.getFile(getDirectory()));
            }
        }

        // native directories
        for (BuildConfig.DeclaredDirectory directory : legacyModInstance.buildConfig.nativeDirectories) {
            if (directory.version.isCompatible()) {
                proxy.addNativeDirectory(this, directory.getFile(getDirectory()));
            }
        }
    }

    @Override
    public void onPrepareResources(ModLoaderReporter reporter) {
        legacyModInstance.RunPreloaderScripts();
    }

    @Override
    public void onRunningMod(ModLoaderReporter reporter) {
        legacyModInstance.loadModInfo();
        legacyModInstance.RunLauncherScripts();
        // update properties, after multiplayer info is set by launcher scripts
        getInfo().pullLegacyModProperties(legacyModInstance);
    }



    @Override
    public void onShuttingDown(ModLoaderReporter reporter) {

    }
}
