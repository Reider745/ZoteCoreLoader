package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class JumpComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:jump.static";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        factory.isJump = true;
    }
}
