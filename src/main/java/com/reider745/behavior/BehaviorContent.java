package com.reider745.behavior;

import org.json.JSONObject;

public abstract class BehaviorContent {
    public abstract String getTagName();
    public abstract String getDefaultDirectory();
    public abstract void load(JSONObject json);
}
