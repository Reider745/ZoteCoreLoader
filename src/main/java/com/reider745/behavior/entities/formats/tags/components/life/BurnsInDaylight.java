package com.reider745.behavior.entities.formats.tags.components.life;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.formats.tags.components.IUpdateEntity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class BurnsInDaylight extends ComponentsTag implements IUpdateEntity {
    @Override
    public String getNameTag() {
        return "minecraft:burns_in_daylight";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject){
            factory.addUpdate(new BurnsInDaylight());
        }else
            Logger.warning("Not support BurnsInDaylight "+json.getClass());
    }

    @Override
    public void onUpdate(CustomEntity entity) {
        if (entity.level.shouldMobBurn(entity)) {
            if (entity.armor == null || entity.armor[0] == null) {
                entity.setOnFire(100);
            } else if (entity.armor[0].getId() == 0) {
                entity.setOnFire(100);
            }
        }
    }
}
