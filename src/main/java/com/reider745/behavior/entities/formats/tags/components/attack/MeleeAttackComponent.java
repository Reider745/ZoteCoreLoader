package com.reider745.behavior.entities.formats.tags.components.attack;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class MeleeAttackComponent extends ComponentsTag {
    public int priority;
    public float speed_multiplier = 1;
    public boolean track_target;

    @Override
    public String getNameTag() {
        return "minecraft:behavior.melee_attack";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            MeleeAttackComponent component = new MeleeAttackComponent();

            component.priority = object.optInt("priority", priority);
            component.speed_multiplier = object.optFloat("speed_multiplier", 1);
            component.track_target = object.optBoolean("track_target", track_target);

            factory.meleeAttackComponent = component;
        }else
            Logger.warning("Not support MeleeAttackComponent "+json.getClass());
    }
}
