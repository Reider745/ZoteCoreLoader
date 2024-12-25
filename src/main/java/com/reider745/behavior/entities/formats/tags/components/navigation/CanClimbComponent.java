package com.reider745.behavior.entities.formats.tags.components.navigation;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class CanClimbComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:can_climb";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        factory.canClimb = true;
    }
}
