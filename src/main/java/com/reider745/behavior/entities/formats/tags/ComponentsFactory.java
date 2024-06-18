package com.reider745.behavior.entities.formats.tags;

import com.reider745.behavior.entities.ComponentsTag;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.TagContentFactory;
import com.reider745.behavior.entities.formats.tags.components.CollisionComponent;
import com.reider745.behavior.entities.formats.tags.components.FireImmuneComponent;
import org.json.JSONObject;

import java.util.ArrayList;

public class ComponentsFactory extends TagContentFactory {
    public static ArrayList<ComponentsTag> tags = new ArrayList<>();

    static {
        tags.add(new FireImmuneComponent());
        tags.add(new CollisionComponent());
    }

    @Override
    public String getNameTag() {
        return "components";
    }

    @Override
    public void loadJSON(EntityContentFactory entityFactory, JSONObject json) {
        for(ComponentsTag tag : tags){
            if(json.has(tag.getNameTag())){
                tag.load(entityFactory, json.get(tag.getNameTag()));
            }
        }
    }
}