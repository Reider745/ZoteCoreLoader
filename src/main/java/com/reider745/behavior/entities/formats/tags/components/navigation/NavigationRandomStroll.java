package com.reider745.behavior.entities.formats.tags.components.navigation;

import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

import java.util.Random;

public class NavigationRandomStroll extends NavigationComponentsTag {
    // TODO: Not support
    public int priority;
    public double speed_multiplier;

    @Override
    public String getNameTag() {
        return "minecraft:behavior.random_stroll";
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            NavigationRandomStroll stroll = new NavigationRandomStroll();

            stroll.priority = object.getInt("priority");
            stroll.speed_multiplier = object.optInt("speed_multiplier", 1);

            factory.navigationRandomStroll = stroll;
        }else
            Logger.warning("Not support NavigationRandomStroll "+json.getClass());
    }

    @Override
    public void update(CustomEntity entity) {
        final Random random = new Random();
        final Vector3 pos = entity.getPosition();
        final Vector3 target = pos.clone();

        target.x += random.nextInt(5) - random.nextInt(10);
        target.z += random.nextInt(5) - random.nextInt(10);

        entity.setTargetBlock(target);
        entity.tickMove = 0;
    }
}
