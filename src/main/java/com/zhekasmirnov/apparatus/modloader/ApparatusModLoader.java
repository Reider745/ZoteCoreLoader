package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.ui.LoadingUI;

import java.util.ArrayList;
import java.util.List;

public class ApparatusModLoader {
    private static final ApparatusModLoader singleton = new ApparatusModLoader();

    public static ApparatusModLoader getSingleton() {
        return singleton;
    }

    public interface AbstractModSource {
        void addMods(List<ApparatusMod> mods, ModLoaderReporter reporter);
    }

    private final List<ApparatusMod> allMods = new ArrayList<>();

    private ApparatusModLoader() {

    }


    public List<ApparatusMod> getAllMods() {
        return allMods;
    }

    public void shutdownAndClear(ModLoaderReporter reporter) {
        synchronized (allMods) {
            for (ApparatusMod mod : allMods) {
                mod.onShuttingDown(reporter);
            }
            allMods.clear();
            MultiplayerModList.getSingleton().clear();
        }
    }

    private void resolveDependenciesAndSort(List<ApparatusMod> modList) {
        modList.sort((a, b) -> b.getDependencies().getModPriority() - a.getDependencies().getModPriority());
    }

    public void reloadModsAndSetupEnvironment(List<AbstractModSource> sources, EnvironmentSetupProxy proxy, ModLoaderReporter reporter) {
        shutdownAndClear(reporter);

        List<ApparatusMod> modsFromSources = new ArrayList<>();
        for (AbstractModSource source : sources) {
            source.addMods(modsFromSources, reporter);
        }

        // init mod dependencies and sort
        for (ApparatusMod mod : modsFromSources) {
            mod.initDependencies();
        }
        resolveDependenciesAndSort(modsFromSources);

        // init mods
        for (ApparatusMod mod : modsFromSources) {
            mod.init();
            mod.setModState(ApparatusMod.ModState.INITIALIZED);
        }

        // filter only enabled mods
        for (ApparatusMod mod : modsFromSources) {
            if (mod.isEnabledAndAbleToRun()) {
                allMods.add(mod);
            }
        }

        // setup environment
        MultiplayerModList.getSingleton().clear();
        for (ApparatusMod mod : allMods) {
            mod.onSettingUpEnvironment(proxy, reporter);
            mod.setModState(ApparatusMod.ModState.ENVIRONMENT_SETUP);
            MultiplayerModList.getSingleton().add(mod);
        }
    }

    public void prepareModResources(ModLoaderReporter reporter) {
        for (ApparatusMod mod : allMods) {
            mod.onPrepareResources(reporter);
            mod.setModState(ApparatusMod.ModState.PREPARED);
        }
    }

    public void runMods(ModLoaderReporter reporter) {
        int progress = 1;
        int total = allMods.size();
        for (ApparatusMod mod : allMods) {
            LoadingUI.setTextAndProgressBar("Running Mods " + " " + progress + "/" + total + "...", 0.5f + progress / (float) total * 0.3f);
            LoadingUI.setTip(mod.getInfo().getString("displayed_name", ""));
            progress++;
            mod.onRunningMod(reporter);
            mod.setModState(ApparatusMod.ModState.RUNNING);
        }
    }

}
