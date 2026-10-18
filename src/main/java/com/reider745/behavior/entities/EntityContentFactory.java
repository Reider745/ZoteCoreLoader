package com.reider745.behavior.entities;

import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.formats.tags.DescriptionsFactory;
import com.reider745.behavior.entities.formats.tags.components.BossComponent;
import com.reider745.behavior.entities.formats.tags.components.collision.CollisionComponent;
import com.reider745.behavior.entities.formats.tags.components.life.HealthComponent;
import com.reider745.behavior.entities.formats.tags.components.IUpdateEntity;
import com.reider745.behavior.entities.formats.tags.components.attack.MeleeAttackComponent;
import com.reider745.behavior.entities.formats.tags.components.attack.NearestAttackableTarget;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationRandomStroll;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationWalkComponent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.reider745.behavior.entities.runtime.MobDefinition;
import com.reider745.behavior.entities.formats.tags.ComponentsFactory;
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
    public ArrayList<IUpdateEntity> updates = new ArrayList<>();
    public NavigationWalkComponent navigationWalkComponent = new NavigationWalkComponent();

    public NearestAttackableTarget nearestAttackableTarget = null;
    public MeleeAttackComponent meleeAttackComponent = null;

    public List<String> family = new ArrayList<>();

    public boolean isFireImmune = false;
    public boolean isJump = false;
    public boolean isMovement = false;
    public boolean isPhysics = false;
    public boolean isHurtByTarget = false;
    public boolean isNameable = false;
    public boolean isPushable = false;
    public boolean canClimb = false;

    public float movement = .5f;
    public int damage = 0;

    public static final HashMap<String, EntityContentFactory> factoryMap = new HashMap<>();
    public Vector3 liftForce = new Vector3(0, 0, 0);
    public MobDefinition definition;
    private EntityDefinition networkDefinition;

    public EntityContentFactory instance(JSONObject components) {
        EntityContentFactory instance = new EntityContentFactory();
        instance.description = description;
        instance.definition = definition;
        instance.networkDefinition = getEntityDefinition();
        new ComponentsFactory().loadJSON(instance, components);
        return instance;
    }


    public void addUpdate(IUpdateEntity tag){
        for (int i = 0; i < updates.size(); i++) {
            if (updates.get(i).getPriority() < tag.getPriority()) {
                updates.add(i, tag);
                return;
            }
        }
        updates.add(tag);
    }

    public EntityDefinition getEntityDefinition() {
        if (networkDefinition == null) networkDefinition = new EntityDefinition(
                description.identifier,
                description.runtimeIdentifier,
                description.isSpawnable,
                description.identifier,
                CustomEntity.class
        );
        return networkDefinition;
    }

    public void build(){
        Logger.info("Register entity "+description.identifier);
        EntityContentFactory previous = factoryMap.get(description.identifier);
        if (previous != null) {
            // Pack overrides replace behavior while keeping the network identifier stable.
            networkDefinition = previous.getEntityDefinition();
        } else {
            cn.nukkit.Server.getInstance().enableExperimentMode = true;
            EntityManager.get().registerDefinition(this.getEntityDefinition());
        }
        factoryMap.put(description.identifier, this);
    }
}
