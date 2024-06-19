package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class HealthComponent extends ComponentsTag {
    public int defaultValue = 20;
    public int max = 20;

    @Override
    public String getNameTag() {
        return "minecraft:health";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            defaultValue = object.getInt("value");
            max = object.getInt("max");

            factory.health = this;
        }else
            Logger.warning("Not support HealthComponent " + json.getClass());
    }
}
