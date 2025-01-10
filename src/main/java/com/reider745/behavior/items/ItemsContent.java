package com.reider745.behavior.items;

import com.reider745.behavior.BehaviorContent;
import org.json.JSONObject;

public class ItemsContent extends BehaviorContent {
    @Override
    public String getTagName() {
        return "minecraft:item";
    }

    @Override
    public String getDefaultDirectory() {
        return "items";
    }

    @Override
    public void load(JSONObject json, String file) {
        System.out.println(file + " " + json);
    }
}
