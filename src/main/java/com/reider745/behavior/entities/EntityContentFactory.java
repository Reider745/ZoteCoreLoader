package com.reider745.behavior.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.level.Position;
import com.reider745.behavior.entities.formats.tags.DescriptionsFactory;
import com.reider745.behavior.entities.formats.tags.components.BossComponent;
import com.reider745.behavior.entities.formats.tags.components.CollisionComponent;
import com.reider745.behavior.entities.formats.tags.components.HealthComponent;
import com.reider745.behavior.entities.formats.tags.components.attack.MeleeAttackComponent;
import com.reider745.behavior.entities.formats.tags.components.attack.NearestAttackableTarget;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationComponentsTag;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationRandomStroll;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationWalkComponent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityContentFactory {
    public DescriptionsFactory.DescriptionTag description;
    public CollisionComponent collision = new CollisionComponent();
    public HealthComponent health = new HealthComponent();
    public BossComponent boss = null;

    public NavigationRandomStroll navigationRandomStroll = new NavigationRandomStroll();
    public ArrayList<NavigationComponentsTag> navigations_compononets = new ArrayList<>();
    public NavigationWalkComponent navigationWalkComponent = new NavigationWalkComponent();

    public NearestAttackableTarget nearestAttackableTarget = null;
    public MeleeAttackComponent meleeAttackComponent = null;

    public List<String> family = new ArrayList<>();

    public boolean isFireImmune = false;
    public boolean isJump = false;
    public boolean isMovement = false;
    public boolean isPhysics = false;
    public boolean isNameable;

    public float movement = .5f;
    public int damage = 0;

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
        Logger.warning("Register entity "+description.identifier);

        factoryMap.put(description.identifier, this);
        EntityManager.get()
                .registerDefinition(this.getEntityDefinition());
    }
}
