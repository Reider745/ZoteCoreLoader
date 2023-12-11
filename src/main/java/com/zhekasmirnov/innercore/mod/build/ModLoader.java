package com.zhekasmirnov.innercore.mod.build;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.apparatus.modloader.ModLoaderReporter;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheka on 30.07.2017.
 */

public class ModLoader {
    public static final String LOGGER_TAG = "INNERCORE-MOD";
    public static ModLoader instance;

    public static void initialize() {
        instance = new ModLoader();
    }

    public ArrayList<Mod> modsList = new ArrayList<>();

    public ModLoader() {
    }

    public void loadMods() {
        // THIS IS DEPRECATED !!!
        // THIS IS DEPRECATED !!!
        // THIS IS DEPRECATED !!!

        modsList.clear();

        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);

        List<String> allModLocations = requestHandler.getAllLocations();
        Logger.debug(LOGGER_TAG, "found " + allModLocations.size() + " potential mod dirs.");

        int modIndex = 1;
        for (String locationName : allModLocations) {
            Logger.debug(LOGGER_TAG, "investigating mod location: " + locationName);
            File file = requestHandler.get(locationName);
            if (file.isDirectory()) {
                LoadingUI.setTextAndProgressBar("Loading Mods: " + modIndex + "/" + allModLocations.size(),
                        0.15f + (modIndex++) * .25f / allModLocations.size());

                String modDir = file.getAbsolutePath() + "/";
                if (ModBuilder.analyzeAndSetupModDir(modDir)) {
                    Logger.debug(LOGGER_TAG, "building and importing mod: " + file.getName());
                    Mod mod = ModBuilder.buildModForDir(modDir, modPack, locationName);
                    if (mod != null) {
                        modsList.add(mod);
                        mod.onImport();
                    } else {
                        Logger.debug(LOGGER_TAG, "failed to build mod: build.config file could not be parsed.");
                    }
                }
            }
        }
    }

    public void runPreloaderScripts() {
        LoadingStage.setStage(LoadingStage.STAGE_MODS_PRELOAD);

        Logger.debug(LOGGER_TAG, "imported " + modsList.size() + " mods.");
        for (int i = 0; i < modsList.size(); i++) {
            Mod mod = modsList.get(i);
            LoadingUI.setText("Initializing Mods: " + (i + 1) + "/" + modsList.size() + ": " + mod.getName());
            mod.RunPreloaderScripts();
        }
    }

    public void startMods() {
        for (int i = 0; i < modsList.size(); i++) {
            Mod mod = modsList.get(i);
            LoadingUI.setTextAndProgressBar("Running Mods: " + (i + 1) + "/" + modsList.size() + " ",
                    0.7f + i * .3f / modsList.size());
            mod.RunLauncherScripts();
        }

        for (Mod mod : modsList) {
            if (mod.isEnabled) {
                MultiplayerModList.getSingleton().add(new LegacyInnerCoreMod(mod));
            }
        }
    }

    private List<File> resourcePackDirs = new ArrayList<>();
    private List<File> behaviorPackDirs = new ArrayList<>();

    public static void addMinecraftResourcePack(File directory) {
        if (!instance.resourcePackDirs.contains(directory)) {
            instance.resourcePackDirs.add(directory);
            ICLog.d("ModLoader", "added minecraft pack: " + directory);
        }
    }

    public static void addMinecraftBehaviorPack(File directory) {
        if (!instance.behaviorPackDirs.contains(directory)) {
            instance.behaviorPackDirs.add(directory);
            ICLog.d("ModLoader", "added minecraft pack: " + directory);
        }
    }

    public static void addGlobalMinecraftPacks() {
        new File(FileTools.DIR_WORK, "resource_packs").mkdirs();
        new File(FileTools.DIR_WORK, "behavior_packs").mkdirs();

        DirectorySetRequestHandler globalResourcePacks = ModPackContext.getInstance().getCurrentModPack()
                .getRequestHandler(ModPackDirectory.DirectoryType.RESOURCE_PACKS);
        for (String location : globalResourcePacks.getAllLocations()) {
            File packDir = globalResourcePacks.get(location);
            if (packDir.isDirectory()) {
                addMinecraftResourcePack(packDir);
            }
        }

        DirectorySetRequestHandler globalBehaviorPacks = ModPackContext.getInstance().getCurrentModPack()
                .getRequestHandler(ModPackDirectory.DirectoryType.BEHAVIOR_PACKS);
        for (String location : globalBehaviorPacks.getAllLocations()) {
            File packDir = globalBehaviorPacks.get(location);
            if (packDir.isDirectory()) {
                addMinecraftBehaviorPack(packDir);
            }
        }
    }

    public enum MinecraftPackType {
        RESOURCE,
        BEHAVIOR;

        public static MinecraftPackType fromString(String str) {
            switch (str) {
                case "resource":
                    return RESOURCE;
                case "behavior":
                case "behaviour":
                    return BEHAVIOR;
            }
            throw new IllegalArgumentException("invalid minecraft pack type: " + str);
        }
    }

    public void loadResourceAndBehaviorPacks() {
        // invoke preloader callback
        Callback.invokeAPICallback("AddRuntimePacks");

    }

    public File addRuntimePack(MinecraftPackType type, String name) {
        InnerCoreServer.useNotSupport("ModLoader.addRuntimePack(type, name)");
        return null;
    }

    public void addResourceAndBehaviorPacksInWorld(File worldDir) {
        InnerCoreServer.useNotSupport("ModLoader.addResourceAndBehaviorPacksInWorld(worldDir)");
    }

    private static final ModLoaderReporter defaultLogReporter = (message, error) -> ICLog.e("ERROR", message, error);

    public static void loadModsAndSetupEnvViaNewModLoader() {
        List<ApparatusModLoader.AbstractModSource> modSources = new ArrayList<>();
        modSources.add((modList, reporter) -> {
            ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
            DirectorySetRequestHandler requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
            List<String> allModLocations = requestHandler.getAllLocations();
            Logger.debug(LOGGER_TAG, "found " + allModLocations.size() + " potential mod dirs.");
            for (String locationName : allModLocations) {
                Logger.debug(LOGGER_TAG, "investigating mod location: " + locationName);
                File file = requestHandler.get(locationName);
                if (file.isDirectory()) {
                    String modDir = file.getAbsolutePath() + "/";
                    if (ModBuilder.analyzeAndSetupModDir(modDir)) {
                        modList.add(new LegacyInnerCoreMod(file, modPack, locationName));
                    }
                }
            }
        });
        ApparatusModLoader.getSingleton().reloadModsAndSetupEnvironment(modSources,
                InnerCore.getEnvironmentSetupProxy(), defaultLogReporter);
    }

    public static void prepareResourcesViaNewModLoader() {
        ApparatusModLoader.getSingleton().prepareModResources(defaultLogReporter);
    }

    public static void runModsViaNewModLoader() {
        ApparatusModLoader.getSingleton().runMods(defaultLogReporter);
    }
}
