package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;

public class NameableComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:nameable";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        factory.isNameable = true;
    }
}
