package com.reider745.behavior.entities.formats.tags.components.navigation;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class MovementBasicComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:movement.basic";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        factory.isMovement = true;
    }
}
