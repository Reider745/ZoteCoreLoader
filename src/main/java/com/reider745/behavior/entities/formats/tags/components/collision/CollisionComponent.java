package com.reider745.behavior.entities.formats.tags.components.collision;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class CollisionComponent extends ComponentsTag {
    public int width = 1, height = 2;

    @Override
    public String getNameTag() {
        return "minecraft:collision_box";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            CollisionComponent component = new CollisionComponent();

            component.width = object.getInt("width");
            component.height = object.getInt("height");

            factory.collision = component;
        }else
            Logger.warning("Not support CollisionComponent "+json.getClass());
    }
}
