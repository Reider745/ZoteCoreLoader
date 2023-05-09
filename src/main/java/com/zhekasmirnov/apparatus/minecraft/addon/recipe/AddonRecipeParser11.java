package com.zhekasmirnov.apparatus.minecraft.addon.recipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddonRecipeParser11 extends AddonRecipeParser {
    private static final Map<String, String> legacyRecipeType = new HashMap<>();

    static {
        legacyRecipeType.put("crafting_shaped", "minecraft:recipe_shaped");
        legacyRecipeType.put("crafting_shapeless", "minecraft:recipe_shaped");
        legacyRecipeType.put("furnace_recipe", "minecraft:recipe_shaped");
    }

    @Override
    public List<ParsedRecipe> parse(JSONObject recipeJson) {
        List<String> tags = new ArrayList<>();
        JSONArray tagsJson = recipeJson.optJSONArray("tags");
        if (tagsJson != null) {
            for (int i = 0; i < tagsJson.length(); i++) {
                tags.add(tagsJson.optString(i));
            }
        }

        String type = recipeJson.optString("type");
        if (legacyRecipeType.containsKey(type)) {
            type = legacyRecipeType.get(type);
        }

        List<ParsedRecipe> result = new ArrayList<>();
        result.add(new ParsedRecipe(null, type, tags, recipeJson));
        return result;
    }
}
