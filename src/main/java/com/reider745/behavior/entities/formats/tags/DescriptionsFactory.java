package com.reider745.behavior.entities.formats.tags;

import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.TagContentEntities;
import com.reider745.behavior.entities.TagContentFactory;
import org.json.JSONObject;

public class DescriptionsFactory extends TagContentFactory {
    public static class DescriptionTag extends TagContentEntities{
        public String identifier;
        public boolean isSpawnable, isSummonable, isExperimental;

        private DescriptionTag(JSONObject json){
            identifier = json.getString("identifier");
            isSpawnable = json.getBoolean("is_spawnable");
            isSummonable = json.getBoolean("is_summonable");
            isExperimental = json.getBoolean("is_experimental");

            identifier = Character.toUpperCase(identifier.charAt(0)) + identifier.substring(1);
        }
    }

    @Override
    public String getNameTag() {
        return "description";
    }

    @Override
    public void loadJSON(EntityContentFactory entityFactory, JSONObject json) {
        entityFactory.description = new DescriptionTag(json);
    }
}
