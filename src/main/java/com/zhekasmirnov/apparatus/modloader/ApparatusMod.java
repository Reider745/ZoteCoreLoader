package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;

public abstract class ApparatusMod {
    public enum ModState {
        NONE,
        INITIALIZED,
        ENVIRONMENT_SETUP,
        PREPARED,
        RUNNING,
        SHUTDOWN
    }

    private ModState modState = ModState.NONE;

    private final ApparatusModInfo info = new ApparatusModInfo();
    private final ApparatusModDependencies dependencies = new ApparatusModDependencies();

    public ApparatusModInfo getInfo() {
        return info;
    }

    public ApparatusModDependencies getDependencies() {
        return dependencies;
    }

    public ModState getModState() {
        return modState;
    }

    void setModState(ModState modState) {
        this.modState = modState;
    }


    public abstract void initDependencies();

    public abstract void init();

    public abstract boolean isEnabledAndAbleToRun();

    public abstract void onSettingUpEnvironment(EnvironmentSetupProxy proxy, ModLoaderReporter reporter);

    public abstract void onPrepareResources(ModLoaderReporter reporter);

    public abstract void onRunningMod(ModLoaderReporter reporter);

    public abstract void onShuttingDown(ModLoaderReporter reporter);
}
