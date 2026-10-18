package com.reider745.behavior.entities.formats.tags.components.attack;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import com.reider745.behavior.api.VanillaMobsFamily;
import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.formats.tags.components.IUpdateEntity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearestAttackableTarget extends ComponentsTag implements IUpdateEntity {
    public interface Condition {
        boolean test(Entity entity);
    }

    private static class IsFamilyCondition implements Condition {
        private final String other, value;

        public IsFamilyCondition(JSONObject obj){
            other = obj.optString("subject", "other");
            value = obj.getString("value");
        }

        @Override
        public boolean test(Entity entity) {
            switch (other){
                case "other" -> {
                    return VanillaMobsFamily.getFor(entity).contains(this.value);
                }
            }
            throw new RuntimeException("not support NearestAttackableTarget.IsFamilyCondition");
        }
    }

    public static class Filters {
        public int max_dist;
        public int attack_interval ;
        public ArrayList<Condition> filters = new ArrayList<>();

        public Filters(JSONObject json){
            max_dist = json.optInt("max_dist", 16);
            attack_interval = json.optInt("attack_interval", 40);

            final JSONObject filterObject = json.optJSONObject("filters");
            final JSONArray any_of = filterObject == null ? null : filterObject.optJSONArray("any_of");
            if(any_of != null){
                for(int i = 0;i < any_of.length();i++){
                    JSONObject condition = any_of.getJSONObject(i);

                    switch (condition.getString("test")){
                        case "is_family" ->
                                filters.add(new IsFamilyCondition(condition));
                        default ->
                                { /* Evaluated by MobFilters in the entity runtime. */ }
                    }
                }
            }
        }

        public boolean test(CustomEntity customEntity, Entity entity){
            for(Condition condition : filters){
                if(entity.distance(customEntity) < max_dist && condition.test(entity))
                    return true;
            }
            return false;
        }
    }

    public int priority = 1;
    public ArrayList<Filters> list = new ArrayList<>();

    @Override
    public String getNameTag() {
        return "minecraft:behavior.nearest_attackable_target";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            NearestAttackableTarget self = new NearestAttackableTarget();

            self.priority = object.optInt("priority", 0);
            final JSONArray entity_types = object.optJSONArray("entity_types");
            for(int i = 0; entity_types != null && i < entity_types.length(); i++)
                self.list.add(new Filters(entity_types.getJSONObject(i)));

            factory.nearestAttackableTarget = self;
        }else
            Logger.warning("Not support NearestAttackableTarget "+json.getClass());
    }

    @Override
    public void onUpdate(CustomEntity entity) {
        if(entity.getServer().getDifficulty() == 0)
            return;

        boolean isTarget = false;

        for(Filters filters : list){
            for(Entity check : entity.level.getEntities()){
                if(filters.test(entity, check) && entity.raycast(check) == null && (!check.isPlayer || !((Player) check).isCreative())){
                    entity.setTarget(check);
                    isTarget = true;
                    break;
                }
            }
            if(isTarget) break;
        }

        if(!isTarget && entity.getTarget() != null){
            entity.setTarget(null);
            return;
        }

        final Entity target = entity.getTarget();
        final EntityContentFactory factory = entity.getFactory();

        if(factory.meleeAttackComponent != null && target != null && entity.distance(target) < 1.5){
            if(entity.tickDamage >= entity.timeDamage){
                entity.tickDamage = 0;
                target.attack(
                        new EntityDamageByEntityEvent(
                                entity, target,
                                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                                factory.damage
                        )
                );
            }else
                entity.tickDamage++;
        }else
            entity.tickDamage = 0;
    }
}
