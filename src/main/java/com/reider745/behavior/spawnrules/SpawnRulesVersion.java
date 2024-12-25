package com.reider745.behavior.spawnrules;

import org.json.JSONObject;

public abstract class SpawnRulesVersion {
    abstract public void load(JSONObject jsonObject, String file);
}
