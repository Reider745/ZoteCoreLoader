package com.reider745.behavior.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.level.Position;
import com.reider745.behavior.entities.formats.tags.DescriptionsFactory;
import com.reider745.behavior.entities.formats.tags.components.CollisionComponent;
import com.reider745.behavior.entities.formats.tags.components.HealthComponent;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationComponentsTag;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class EntityContentFactory {
    public DescriptionsFactory.DescriptionTag description;
    public CollisionComponent collision = new CollisionComponent();
    public HealthComponent health = new HealthComponent();
    public ArrayList<NavigationComponentsTag> navigations_compononets = new ArrayList<>();

    public boolean isFireImmune = false;
    public boolean isJump = false;
    public boolean isMovement = false;
    public boolean isPhysics = false;

    public float movement = .5f;

    public static final HashMap<String, EntityContentFactory> factoryMap = new HashMap<>();

    public void addNavigationComponent(NavigationComponentsTag tag){
        for (int i = 0; i < navigations_compononets.size(); i++) {
            if (navigations_compononets.get(i).getPriority() < tag.getPriority()) {
                navigations_compononets.add(i, tag);
                return;
            }
        }
        navigations_compononets.add(tag);
    }

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
        Logger.info("Register entity "+description.identifier);

        factoryMap.put(description.identifier, this);
        EntityManager.get()
                .registerDefinition(this.getEntityDefinition());
    }
}
