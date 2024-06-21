package com.reider745.behavior.entities.formats.tags.components.attack;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class HurtByTargetComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:behavior.hurt_by_target";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject){
            //TODO: Not support priority
            factory.isHurtByTarget = true;
        }else
            Logger.warning("Not support FireImmuneComponent "+json.getClass());
    }
}
