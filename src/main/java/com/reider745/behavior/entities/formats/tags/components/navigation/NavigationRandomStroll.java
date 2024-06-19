package com.reider745.behavior.entities.formats.tags.components.navigation;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class NavigationRandomStroll extends NavigationComponentsTag {
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
            priority = object.getInt("priority");
            speed_multiplier = object.getInt("speed_multiplier");
        }else
            Logger.warning("Not support NavigationRandomStroll "+json.getClass());
    }

    @Override
    public void update(CustomEntity entity) {
        //entity.set
    }
}
