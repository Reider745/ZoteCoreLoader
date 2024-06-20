package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class FamilyComponent extends ComponentsTag {
    @Override
    public String getNameTag() {
        return "minecraft:type_family";
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            factory.family.clear();

            JSONArray family = object.getJSONArray("family");
            for(int i = 0;i < family.length();i++)
                factory.family.add(family.getString(i));
        }else
            Logger.warning("Not support FamilyComponent "+json.getClass());
    }
}
