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
        return "loots_tables";
    }

    @Override
    public void load(JSONObject json, String file) {

    }
}
