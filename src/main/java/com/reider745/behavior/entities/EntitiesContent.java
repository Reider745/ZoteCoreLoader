package com.reider745.behavior.entities;

import com.reider745.behavior.BehaviorContent;
import com.reider745.behavior.BehaviorVersion;
import com.reider745.behavior.entities.formats.*;
import org.json.JSONObject;

import java.util.NavigableMap;
import java.util.TreeMap;

public class EntitiesContent extends BehaviorContent {
    private static final NavigableMap<BehaviorVersion, EntitiesVersion> versions = new TreeMap<>();

    static {
        versions.put(BehaviorVersion.parse("1.8.0"), new EntitiesVersion18());
        versions.put(BehaviorVersion.parse("1.10.0"), new EntitiesVersion110());
        versions.put(BehaviorVersion.parse("1.12.0"), new EntitiesVersion112());
        versions.put(BehaviorVersion.parse("1.13.0"), new EntitiesVersion113());
        versions.put(BehaviorVersion.parse("1.14.0"), new EntitiesVersion114());
        versions.put(BehaviorVersion.parse("1.15.0"), new EntitiesVersion115());
        versions.put(BehaviorVersion.parse("1.16.0"), new EntitiesVersion116());
        versions.put(BehaviorVersion.parse("1.16.100"), new EntitiesVersion116100());
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
    public void load(JSONObject json, String file) {
        EntityContentFactory factory = parse(json);
        factory.definition = new com.reider745.behavior.entities.runtime.MobDefinition(
                json.getJSONObject(getTagName()), file);
        factory.build();
    }

    public EntityContentFactory parse(JSONObject json) {
        String format = json.getString("format_version");
        BehaviorVersion.requireSupported(format, "1.8");
        EntitiesVersion version = versions.floorEntry(BehaviorVersion.parse(format)).getValue();
        return version.load(json.getJSONObject(getTagName()));
    }
}
