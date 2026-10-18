package com.reider745.behavior.entities.formats.tags;

import com.reider745.behavior.entities.EntityContentFactory;
import com.reider745.behavior.entities.TagContentEntities;
import com.reider745.behavior.entities.TagContentFactory;
import org.json.JSONObject;

public class DescriptionsFactory extends TagContentFactory {
    public static class DescriptionTag extends TagContentEntities{
        public String identifier;
        public String runtimeIdentifier;
        public boolean isSpawnable, isSummonable, isExperimental;

        private DescriptionTag(JSONObject json){
            identifier = json.getString("identifier");

            runtimeIdentifier = json.optString("runtime_identifier", null);
            isSpawnable = json.optBoolean("is_spawnable", false);
            isSummonable = json.optBoolean("is_summonable", false);
            isExperimental = json.optBoolean("is_experimental", false);
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
