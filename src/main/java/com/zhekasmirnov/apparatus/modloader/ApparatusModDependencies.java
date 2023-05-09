package com.zhekasmirnov.apparatus.modloader;

import org.json.JSONObject;

public class ApparatusModDependencies {
    private int modPriority = 0;

    public void loadFrom(JSONObject json) {
        modPriority = json.optInt("priority", 0);
    }

    public int getModPriority() {
        return modPriority;
    }

    public void setModPriority(int modPriority) {
        this.modPriority = modPriority;
    }
}
