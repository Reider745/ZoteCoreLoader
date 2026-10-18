package com.reider745.behavior.entities.formats.tags.components.collision;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class CollisionComponent extends ComponentsTag {
    public float width = 1, height = 2;

    @Override
    public String getNameTag() {
        return "minecraft:collision_box";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            CollisionComponent component = new CollisionComponent();

            component.width = object.optFloat("width", 1);
            component.height = object.optFloat("height", 2);
            if (component.width <= 0 || component.height <= 0) throw new IllegalArgumentException("Invalid collision box");

            factory.collision = component;
        }else
            Logger.warning("Not support CollisionComponent "+json.getClass());
    }
}
