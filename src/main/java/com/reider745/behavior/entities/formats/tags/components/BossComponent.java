package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class BossComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:boss";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            factory.boss = new BossComponent();
        }else
            Logger.warning("Not support BossComponent "+json.getClass());
    }
}
