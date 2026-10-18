package com.reider745.behavior.spawnrules.version;

import com.reider745.behavior.spawnrules.SpawnRulesVersion;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class SpawnRules18 extends SpawnRulesVersion {
    @Override
    public void load(JSONObject json, String file) {
        com.reider745.behavior.spawnrules.MobSpawner.register(new com.reider745.behavior.spawnrules.SpawnRule(json));
    }
}
