package com.reider745.behavior.entities;

import com.reider745.behavior.BehaviorContent;
import com.reider745.behavior.entities.formats.EntitiesVersion113;
import org.json.JSONObject;

import java.util.HashMap;

public class EntitiesContent extends BehaviorContent {
    private static final HashMap<String, EntitiesVersion> versions = new HashMap<>();

    static {
        versions.put("1.8.0", new EntitiesVersion113());
        versions.put("1.13.0", new EntitiesVersion113());
        versions.put("1.16.0", new EntitiesVersion113());
    }

    @Override
    public String getTagName() {
        return "minecraft:entity";
    }

    @Override
    public String getDefaultDirectory() {
        return "entities";
    }

    @Override
    public void load(JSONObject json) {
        final String version_name = json.getString("format_version");
        final EntitiesVersion version = versions.get(version_name);
        if(version == null)
            throw new RuntimeException("not support version "+version_name);

        version.load(json.getJSONObject(getTagName())).build();
    }
}
