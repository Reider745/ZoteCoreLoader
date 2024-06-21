package com.reider745.behavior.entities.formats.tags.components.navigation;

import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

public class NavigationWalkComponent extends NavigationComponentsTag {

    boolean is_amphibious = false,
            can_path_over_water = true,
            can_pass_doors = true,
            can_open_doors = false,
            avoid_damage_blocks = false;

    @Override
    public String getNameTag() {
        return "minecraft:navigation.walk";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            NavigationWalkComponent walk = new NavigationWalkComponent();

            walk.is_amphibious = object.optBoolean("is_amphibious", is_amphibious);
            walk.can_path_over_water = object.optBoolean("can_path_over_water", can_path_over_water);
            walk.avoid_damage_blocks = object.optBoolean("avoid_damage_blocks", avoid_damage_blocks);
            walk.can_open_doors = object.optBoolean("can_open_doors", can_open_doors);
            walk.can_pass_doors = object.optBoolean("can_pass_doors", can_pass_doors);

            factory.navigationWalkComponent = walk;
        }else
            Logger.warning("Not support NavigationWalkComponent "+json.getClass());
    }

    @Override
    public void onUpdate(CustomEntity entity) {

    }
}
