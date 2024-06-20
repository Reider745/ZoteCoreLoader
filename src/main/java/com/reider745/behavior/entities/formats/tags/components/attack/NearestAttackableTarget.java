package com.reider745.behavior.entities.formats.tags.components.attack;

import cn.nukkit.entity.Entity;
import com.reider745.behavior.api.VanillaMobsFamily;
import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearestAttackableTarget extends ComponentsTag {
    public interface Condition {
        boolean test(Entity entity);
    }

    private static class IsFamilyCondition implements Condition {
        private final String other, value;

        public IsFamilyCondition(JSONObject obj){
            other = obj.getString("subject");
            value = obj.getString("value");
        }

        @Override
        public boolean test(Entity entity) {
            switch (other){
                case "other" -> {
                    return VanillaMobsFamily.getFor(entity.getNetworkId()).contains(this.value);
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
            max_dist = json.getInt("max_dist");
            attack_interval = json.getInt("attack_interval");

            final JSONArray any_of = json.getJSONObject("filters").getJSONArray("any_of");
            for(int i = 0;i < any_of.length();i++){
                JSONObject condition = any_of.getJSONObject(i);

                switch (condition.getString("test")){
                    case "is_family" ->
                        filters.add(new IsFamilyCondition(condition));
                    default ->
                        throw new RuntimeException("Not support NearestAttackableTarget.Filters");
                }
            }
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

            self.priority = object.getInt("priority");
            final JSONArray entity_types = object.getJSONArray("entity_types");
            for(int i = 0;i < entity_types.length();i++)
                self.list.add(new Filters(entity_types.getJSONObject(i)));

            factory.nearestAttackableTarget = self;
        }else
            Logger.warning("Not support NearestAttackableTarget "+json.getClass());
    }
}
