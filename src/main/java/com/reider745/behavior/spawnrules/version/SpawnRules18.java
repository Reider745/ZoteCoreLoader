package com.reider745.behavior.spawnrules.version;

import com.reider745.behavior.spawnrules.SpawnRulesVersion;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class SpawnRules18 extends SpawnRulesVersion {
    @Override
    public void load(JSONObject json, String file) {
        final JSONObject description = json.getJSONObject("description");

        final String identifier = description.getString("identifier");
        final String populationControl = description.getString("population_control");

        for(String tag : json.keySet()) {
            if(tag.contains("description")) continue;

            Logger.warning("Not support spawn rules tag: " + tag + ", file: ");
        }
    }
}
