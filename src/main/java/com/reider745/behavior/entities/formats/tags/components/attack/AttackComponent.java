package com.reider745.behavior.entities.formats.tags.components.attack;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class AttackComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:attack";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            factory.damage = object.optInt("damage", 1);
        }else
            Logger.warning("Not support MovementComponent "+json.getClass());
    }
}
