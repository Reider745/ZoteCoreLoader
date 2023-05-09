package com.zhekasmirnov.apparatus.minecraft.addon.recipe;

import android.util.Pair;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import org.json.JSONObject;

import java.util.List;

public abstract class AddonRecipeParser {
    public static class ParsedRecipe {
        private final String identifier;
        private final String type;
        private final List<String> tags;
        private final JSONObject contents;

        public ParsedRecipe(String identifier, String type, List<String> tags, JSONObject contents) {
            this.identifier = identifier;
            this.type = type;
            this.tags = tags;
            this.contents = contents;
        }

        public String getIdentifier() {
            return identifier;
        }

        public JSONObject getContents() {
            return contents;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getType() {
            return type;
        }
    }

    public void prepare() {

    }

    public abstract List<ParsedRecipe> parse(JSONObject recipeJson);

    public Pair<Integer, Integer> getIdAndDataFromItemString(String stringId, int defaultData) {
        String[] result = stringId.split(":");
        String name = result.length == 1 ? result[0] : result[1];
        int id = AdaptedScriptAPI.IDRegistry.getIDByName(name.toLowerCase());
        int data = defaultData;
        if (result.length == 3) {
            data = Integer.parseInt(result[2]);
        }
        return id != 0 ? new Pair<>(id, data) : null;
    }

    public Pair<Integer, Integer> getIdAndDataForItemJson(JSONObject json, int defaultData) {
        String stringId = json.optString("item");
        int data = defaultData;
        int id = 0;

        int dataFromJson = json.optInt("data", -1);
        if (dataFromJson != -1) {
            data = dataFromJson;
        }

        if (stringId != null) {
            Pair<Integer, Integer> fromStringId = getIdAndDataFromItemString(stringId, data);
            if (fromStringId != null) {
                id = fromStringId.first;
                data = fromStringId.second;
            }
        }

        if (id == 0) {
            return null;
        }

        return new Pair<>(id, data);
    }
}
