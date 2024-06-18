package com.reider745.behavior.entities.formats;

import com.reider745.behavior.entities.EntitiesVersion;
import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.TagContentFactory;
import com.reider745.behavior.entities.formats.tags.ComponentsFactory;
import com.reider745.behavior.entities.formats.tags.DescriptionsFactory;
import org.json.JSONObject;

import java.util.ArrayList;

public class EntitiesVersion113 extends EntitiesVersion {
    private static final ArrayList<TagContentFactory> tags = new ArrayList<>();

    static {
        tags.add(new DescriptionsFactory());
        tags.add(new ComponentsFactory());
    }

    @Override
    public EntityContentFactory load(JSONObject json) {
        final EntityContentFactory entityFactory = new EntityContentFactory();

        for(TagContentFactory factory : tags){
            if(json.has(factory.getNameTag())){
                factory.loadJSON(entityFactory, json.getJSONObject(factory.getNameTag()));
            }
        }

        return entityFactory;
    }
}
