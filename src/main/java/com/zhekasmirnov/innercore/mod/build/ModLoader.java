package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.apparatus.modloader.ModLoaderReporter;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    //public File modsDir;

    public ModLoader() {
        //modsDir = new File(FileTools.DIR_WORK + "mods/");
        //FileTools.assureDir(modsDir.getAbsolutePath());
    }

    public void loadMods() {
        // THIS IS DEPRECATED !!!
        // THIS IS DEPRECATED !!!
        // THIS IS DEPRECATED !!!

        modsList.clear();
        /*File[] fileList = modsDir.listFiles();
        if (fileList == null) {
            fileList = new File[0];
        }*/

        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);

        List<String> allModLocations = requestHandler.getAllLocations();
        Logger.debug(LOGGER_TAG, "found " + allModLocations.size() + " potential mod dirs.");

        int modIndex = 1;
        for (String locationName : allModLocations) {
            Logger.debug(LOGGER_TAG, "investigating mod location: " + locationName);
            File file = requestHandler.get(locationName);
            if (file.isDirectory()) {
                LoadingUI.setTextAndProgressBar("Loading Mods: " + modIndex + "/" + allModLocations.size(), 0.15f + (modIndex++) * .25f / allModLocations.size());

                String modDir = file.getAbsolutePath() + "/";
                if (ModBuilder.analyzeAndSetupModDir(modDir)) {
                    Logger.debug(LOGGER_TAG, "building and importing mod: " + file.getName());
                    Mod mod = ModBuilder.buildModForDir(modDir, modPack, locationName);
                    if (mod != null) {
                        modsList.add(mod);
                        mod.onImport();
                    }
                    else {
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
            LoadingUI.setTextAndProgressBar("Running Mods: " + (i + 1) + "/" + modsList.size() + " ", 0.7f + i * .3f / modsList.size());
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

        DirectorySetRequestHandler globalResourcePacks = ModPackContext.getInstance().getCurrentModPack().getRequestHandler(ModPackDirectory.DirectoryType.RESOURCE_PACKS);
        for (String location : globalResourcePacks.getAllLocations()) {
            File packDir = globalResourcePacks.get(location);
            if (packDir.isDirectory()) {
                addMinecraftResourcePack(packDir);
            }
        }

        DirectorySetRequestHandler globalBehaviorPacks = ModPackContext.getInstance().getCurrentModPack().getRequestHandler(ModPackDirectory.DirectoryType.BEHAVIOR_PACKS);
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
            switch(str) {
                case "resource":
                    return RESOURCE;
                case "behavior":
                case "behaviour":
                    return BEHAVIOR;
            }
            throw new IllegalArgumentException("invalid minecraft pack type: " + str);
        }
    }

    private static class MinecraftPack {
        private final MinecraftPackType type;
        private final File directory;
        private final String uuid;
        private final JSONArray version;

        public MinecraftPack(File directory, MinecraftPackType type, String uuid, JSONArray version) {
            this.directory = directory;
            this.type = type;
            this.uuid = uuid;
            this.version = version;
        }

        public JSONObject getJsonForWorldPacks() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("version", version);
                obj.put("pack_id", uuid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public static MinecraftPack fromDirectory(File directory, MinecraftPackType type) {
            File manifest = new File(directory, "manifest.json");
            if (manifest.isFile()) {
                try {
                    JSONObject manifestJson = FileUtils.readJSON(manifest);
                    JSONObject header = manifestJson.optJSONObject("header");
                    if (header != null) {
                        String uuid = header.optString("uuid", null);
                        JSONArray array = header.optJSONArray("version");
                        if (uuid != null && array != null) {
                            return new MinecraftPack(directory, type, uuid, array);
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            ICLog.i("ERROR", "failed to read minecraft pack uuid or version: " + directory);
            return null;
        }
    }


    private List<MinecraftPack> minecraftPacks = new ArrayList<>();

    private void loadMinecraftPacksIntoDirectory(MinecraftPackType type, File targetDir, List<File> files) {
        /*if (targetDir.isDirectory()) {
            FileUtils.clearFileTree(targetDir, false);
        } else {
            targetDir.delete();
            targetDir.mkdirs();
        }

        for (File file : files) {
            File target = new File(targetDir, file.getName());
            while (target.exists()) {
                target = new File(targetDir, target.getName() + "-");
            }
            target.mkdirs();
            try {
                FileUtils.copyFileTree(file, target, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            MinecraftPack pack = MinecraftPack.fromDirectory(target, type);
            if (pack != null) {
                minecraftPacks.add(pack);
            }
        }*/
    }

    private void deleteTempPacksInDirectory(File directory) {
        /*if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                if (new File(packDir, ".tmp_resources").exists()) {
                    FileUtils.clearFileTree(packDir, true);
                }
            }
        }*/
    }

    private List<File> findPackInDirectory(File directory, MinecraftPack pack) {
        List<File> result = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                MinecraftPack packFromDir = MinecraftPack.fromDirectory(packDir, pack.type);
                if (packFromDir != null && packFromDir.uuid != null && packFromDir.uuid.equals(pack.uuid)) {
                    result.add(packDir);
                }
            }
        }
        return result;
    }

    private void injectPacksInDirectory(File directory, MinecraftPackType type) {
        /*deleteTempPacksInDirectory(directory);
        try {
            for (MinecraftPack pack : minecraftPacks) {
                if (pack.type == type) {
                    for (File packDir : findPackInDirectory(directory, pack)) {
                        FileUtils.clearFileTree(packDir, true);
                    }
                    
                    File target = new File(directory, pack.directory.getName());
                    while (target.exists()) {
                        target = new File(directory, target.getName() + "-");
                    }
                    target.mkdirs();

                    try {
                        FileUtils.copyFileTree(pack.directory, target, null, null);
                        new File(target, ".tmp_resources").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }*/
    }

    private void deleteDuplicatePacksInDir(File directory, MinecraftPackType type) {
        /*if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                MinecraftPack pack = MinecraftPack.fromDirectory(packDir, type);
                if (pack != null) {
                    for (MinecraftPack addedPack : minecraftPacks) {
                        if (addedPack.type == type && addedPack.uuid.equals(pack.uuid)) {
                            FileUtils.clearFileTree(packDir, true);
                        }
                    }
                }
            }
        }*/
    }

    public void loadResourceAndBehaviorPacks() {
        minecraftPacks.clear();
        loadMinecraftPacksIntoDirectory(MinecraftPackType.RESOURCE, new File(FileTools.DIR_HORIZON, "resource_packs"), resourcePackDirs);
        loadMinecraftPacksIntoDirectory(MinecraftPackType.BEHAVIOR, new File(FileTools.DIR_HORIZON, "behavior_packs"), behaviorPackDirs);
        
        // delete all duplicate packs (with same uuid and version) in world directories
        // duplicate packs can be created by mcpe for some reason
        
        // temporary comment this out, and replace with per world resources injection
        /*File worldsDir = new File(FileTools.DIR_PACK, "worlds");
        if (worldsDir.isDirectory()) {
            for (File worldDir : worldsDir.listFiles()) {
                deleteDuplicatePacksInDir(new File(worldDir, "behavior_packs"), MinecraftPackType.BEHAVIOR);
                deleteDuplicatePacksInDir(new File(worldDir, "resource_packs"), MinecraftPackType.RESOURCE);
            }
        }*/

        // invoke preloader callback
        Callback.invokeAPICallback("AddRuntimePacks");

    }

    private final HashMap<String, File> runtimePackDirs = new HashMap<>();

    public File addRuntimePack(MinecraftPackType type, String name) {
        if (runtimePackDirs.containsKey(name)) {
            return runtimePackDirs.get(name);
        }

        File packsDirectory = null;
        String moduleType = null;
        String headerUuid = UUID.randomUUID().toString();
        switch (type) {
            case RESOURCE:
                packsDirectory = new File(FileTools.DIR_HORIZON, "resource_packs");
                moduleType = "resources";
            break;
            case BEHAVIOR:
                packsDirectory = new File(FileTools.DIR_HORIZON, "behavior_packs");
                moduleType = "data";
            break;
        }

        File directory = new File(packsDirectory, "runtime_" + name);
        directory.mkdirs();

        JSONArray version001 = new JSONArray().put(0).put(0).put(1);
        try {
            FileTools.writeJSON(
                    new File(directory, "manifest.json").getAbsolutePath(),
                    MinecraftVersions.getCurrent().createRuntimePackManifest(name, headerUuid, moduleType, version001)
            );
        } catch(IOException e) {
            e.printStackTrace();
        }

        minecraftPacks.add(new MinecraftPack(directory, type, headerUuid, version001));
        return directory;
    }

    public void addResourceAndBehaviorPacksInWorld(File worldDir) {
        File worldResourcePacks = new File(worldDir, "world_resource_packs.json");
        File worldResourcePacksDir = new File(worldDir, "resource_packs");
        File worldBehaviorPacks = new File(worldDir, "world_behavior_packs.json");
        File worldBehaviorPacksDir = new File(worldDir, "behavior_packs");
        JSONArray resourcePacksArray = new JSONArray();
        JSONArray behaviorPacksArray = new JSONArray();

        injectPacksInDirectory(worldResourcePacksDir, MinecraftPackType.RESOURCE);
        injectPacksInDirectory(worldBehaviorPacksDir, MinecraftPackType.BEHAVIOR);

        List<MinecraftPack> worldPacks = new ArrayList<>(minecraftPacks);
        File[] worldResourcePackDirs = worldResourcePacksDir.listFiles();
        if (worldResourcePackDirs != null) {
            for (File packDir : worldResourcePackDirs) {
                MinecraftPack pack = MinecraftPack.fromDirectory(packDir, MinecraftPackType.RESOURCE);
                if (pack != null) {
                    worldPacks.add(pack);
                }
            }
        }
        File[] worldBehaviorPackDirs = worldBehaviorPacksDir.listFiles();
        if (worldBehaviorPackDirs != null) {
            for (File packDir : worldBehaviorPackDirs) {
                MinecraftPack pack = MinecraftPack.fromDirectory(packDir, MinecraftPackType.BEHAVIOR);
                if (pack != null) {
                    worldPacks.add(pack);
                }
            }
        }

        for (MinecraftPack pack : worldPacks) {
            switch (pack.type) {
                case RESOURCE:
                    resourcePacksArray.put(pack.getJsonForWorldPacks());
                break;
                case BEHAVIOR:
                    behaviorPacksArray.put(pack.getJsonForWorldPacks());
                break;
            }
        }
        /*try {
            FileUtils.writeJSON(worldResourcePacks, resourcePacksArray);
            FileUtils.writeJSON(worldBehaviorPacks, behaviorPacksArray);
        } catch (IOException e) {
            e.printStackTrace();
            ICLog.e("ERROR", "failed to write world packs json", e);
        }*/
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
        ApparatusModLoader.getSingleton().reloadModsAndSetupEnvironment(modSources,null, defaultLogReporter);
    }

    public static void prepareResourcesViaNewModLoader() {
        ApparatusModLoader.getSingleton().prepareModResources(defaultLogReporter);
    }

    public static void runModsViaNewModLoader() {
        ApparatusModLoader.getSingleton().runMods(defaultLogReporter);
    }
}
