package com.reider745.behavior.loots;

import com.reider745.behavior.BehaviorContent;
import org.json.JSONObject;

public class LootsContent extends BehaviorContent {
    @Override
    public String getTagName() {
        return "pools";
    }

    @Override
    public String getDefaultDirectory() {
        return "loot_tables";
    }

    @Override
    public void load(JSONObject json, String file) {
        MobLoot.register(file, json);
    }
}
