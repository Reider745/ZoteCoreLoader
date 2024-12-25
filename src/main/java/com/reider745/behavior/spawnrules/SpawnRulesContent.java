package com.reider745.behavior.spawnrules;

import com.reider745.behavior.BehaviorContent;
import com.reider745.behavior.spawnrules.version.SpawnRules18;
import org.json.JSONObject;

import java.util.HashMap;

public class SpawnRulesContent extends BehaviorContent {
    private static final HashMap<String, SpawnRulesVersion> versions = new HashMap<>();

    static {
        versions.put("1.8.0", new SpawnRules18());
    }

    @Override
    public String getTagName() {
        return "minecraft:spawn_rules";
    }

    @Override
    public String getDefaultDirectory() {
        return "spawn_rules";
    }

    @Override
    public void load(JSONObject json, String file) {
        final String version_name = json.getString("format_version");
        final SpawnRulesVersion version = versions.get(version_name);
        if(version == null)
            throw new RuntimeException("Not support spawn rules version "+version_name);
        version.load(json.getJSONObject(getTagName()), file);
    }
}
