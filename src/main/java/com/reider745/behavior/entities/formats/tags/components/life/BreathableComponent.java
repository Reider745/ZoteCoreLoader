package com.reider745.behavior.entities.formats.tags.components.life;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.formats.tags.components.IUpdateEntity;
import org.json.JSONObject;

public class BreathableComponent extends ComponentsTag implements IUpdateEntity {
    public int totalSupply, suffocateTime;
    public double inhaleTime;
    public boolean generatesBubbles;

    @Override
    public String getNameTag() {
        return "minecraft:breathable";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject jsonObject) {
            final BreathableComponent component = new BreathableComponent();

            component.totalSupply = jsonObject.optInt("total_supply", jsonObject.optInt("totalSupply", 20));
            component.suffocateTime = jsonObject.optInt("suffocate_time", 0);
            component.inhaleTime = jsonObject.optDouble("inhale_time", 2.0);
            component.generatesBubbles = jsonObject.optBoolean("generates_bubbles", false);

            factory.addUpdate(component);
        } else
            throw new RuntimeException("Not support");
    }

    @Override
    public int getPriority() {
        return IUpdateEntity.super.getPriority();
    }

    @Override
    public void onUpdate(CustomEntity entity) {
        // TODO: BEHAVIOR
    }
}
