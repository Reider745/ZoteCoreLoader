package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class MovementComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:movement";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            factory.movement = object.optFloat("value", .75f);
        }else
            Logger.warning("Not support MovementComponent "+json.getClass());
    }
}
