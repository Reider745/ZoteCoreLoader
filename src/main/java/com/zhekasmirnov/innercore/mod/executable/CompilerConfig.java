package com.zhekasmirnov.innercore.mod.executable;

import com.zhekasmirnov.innercore.api.mod.API;

/**
 * Created by zheka on 28.07.2017.
 */

public class CompilerConfig {
    private API apiInstance;
    private int optimizationLevel = -1;
    private String name = "Unknown Executable";

    private String modName = null;

    public boolean isLibrary = false;

    public CompilerConfig(API apiInstance) {
        this.apiInstance = apiInstance;
    }

    public API getApiInstance() {
        return apiInstance;
    }

    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int level) {
        optimizationLevel = level;
    }

    public String getName() {
        return name;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public String getFullName() {
        return modName != null ? modName + "$" + name : name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
