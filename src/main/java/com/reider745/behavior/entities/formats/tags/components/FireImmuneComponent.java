package com.reider745.behavior.entities.formats.tags.components;


import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class FireImmuneComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:fire_immune";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof Boolean value)
            factory.isFireImmune = value;
        else if(json instanceof JSONObject object){
            factory.isFireImmune = true;
        }else
            Logger.warning("Not support FireImmuneComponent "+json.getClass());
    }
}
