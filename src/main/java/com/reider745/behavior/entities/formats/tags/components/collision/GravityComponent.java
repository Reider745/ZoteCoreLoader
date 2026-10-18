package com.reider745.behavior.entities.formats.tags.components.collision;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class GravityComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:physics";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        factory.isPhysics = !(json instanceof org.json.JSONObject object) || object.optBoolean("has_gravity", true);
    }
}
