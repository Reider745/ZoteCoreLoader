package com.reider745.behavior.entities.formats.tags.components.collision;

import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class BalloonComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:balloon";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject jsonObject) {
            final JSONArray leftForce = jsonObject.getJSONArray("lift_force");
            factory.liftForce = new Vector3(leftForce.getDouble(0), leftForce.getDouble(1), leftForce.getDouble(2));
        } else
            throw new RuntimeException("Error loaded entity");

    }
}
