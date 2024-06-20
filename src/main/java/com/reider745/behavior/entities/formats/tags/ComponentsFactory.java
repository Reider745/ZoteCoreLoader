package com.reider745.behavior.entities.formats.tags;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.TagContentFactory;
import com.reider745.behavior.entities.formats.tags.components.*;
import com.reider745.behavior.entities.formats.tags.components.attack.MeleeAttackComponent;
import com.reider745.behavior.entities.formats.tags.components.attack.NearestAttackableTarget;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationRandomStroll;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationWalkComponent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ComponentsFactory extends TagContentFactory {
    public static HashMap<String, ComponentsTag> tags = new HashMap<>();

    private static void add(ComponentsTag tag){
        tags.put(tag.getNameTag(), tag);
    }

    static {
        add(new FireImmuneComponent());
        add(new CollisionComponent());
        add(new HealthComponent());
        add(new FamilyComponent());
        add(new NameableComponent());

        add(new GravityComponent());
        add(new JumpComponent());
        add(new MovementBasicComponent());
        add(new MovementComponent());
        add(new NavigationWalkComponent());
        add(new NavigationRandomStroll());

        add(new AttackComponent());
        add(new NearestAttackableTarget());
        add(new MeleeAttackComponent());
    }

    @Override
    public String getNameTag() {
        return "components";
    }

    @Override
    public void loadJSON(EntityContentFactory entityFactory, JSONObject json) {
        final Iterator<String> it = json.keys();
        while (it.hasNext()){
            final String key = it.next();
            final ComponentsTag tag = tags.get(key);
            if(tag == null){
                Logger.warning("Not support ComponentsFactory "+key);
                continue;
            }

            tag.load(entityFactory, json.get(key));
        }
    }
}