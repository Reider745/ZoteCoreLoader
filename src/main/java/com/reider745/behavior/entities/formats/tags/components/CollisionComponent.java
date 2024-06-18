package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class CollisionComponent extends ComponentsTag {
    public int width, height;

    @Override
    public String getNameTag() {
        return "minecraft:collision_box";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            width = object.getInt("width");
            height = object.getInt("height");

            factory.collision = this;
        }else
            Logger.warning("Not support CollisionComponent "+json.getClass());
    }
}
