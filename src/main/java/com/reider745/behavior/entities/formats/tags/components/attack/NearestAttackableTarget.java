package com.reider745.behavior.entities.formats.tags.components.attack;

import cn.nukkit.entity.Entity;
import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class NearestAttackableTarget extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:behavior.nearest_attackable_target";
    }

    private interface Condition {
        boolean test(Entity entity);
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {

    }
}
