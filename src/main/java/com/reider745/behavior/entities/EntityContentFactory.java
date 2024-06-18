package com.reider745.behavior.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.level.Position;
import com.reider745.behavior.entities.formats.tags.DescriptionsFactory;
import com.reider745.behavior.entities.formats.tags.components.CollisionComponent;

import java.util.ArrayList;
import java.util.HashMap;

public class EntityContentFactory {
    public DescriptionsFactory.DescriptionTag description;
    public CollisionComponent collision;
    private ArrayList<ComponentsTag> components = new ArrayList<>();

    public boolean isFireImmune = false;

    public static final HashMap<String, EntityContentFactory> factoryMap = new HashMap<>();

    public EntityDefinition getEntityDefinition() {
        return new EntityDefinition(
                description.identifier,
                null,
                false,
                description.identifier,
                CustomEntity.class
        );
    }

    public void build(){
        factoryMap.put(description.identifier, this);
        Entity.registerEntity(description.identifier, CustomEntity.class);
        EntityManager.get()
                .registerDefinition(this.getEntityDefinition());
    }
}
