package com.zhekasmirnov.apparatus.minecraft.addon.recipe;

import android.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AddonRecipeParser16 extends AddonRecipeParser {
    private final Map<Integer, String> dyeConversion = new HashMap<>();

    {
        dyeConversion.put(0, "black_dye");
        dyeConversion.put(1, "red_dye");
        dyeConversion.put(2, "green_dye");
        dyeConversion.put(3, "cocoa_beans");
        dyeConversion.put(4, "lapis_lazuli");
        dyeConversion.put(5, "purple_dye");
        dyeConversion.put(6, "cyan_dye");
        dyeConversion.put(7, "light_gray_dye");
        dyeConversion.put(8, "gray_dye");
        dyeConversion.put(9, "pink_dye");
        dyeConversion.put(10, "lime_dye");
        dyeConversion.put(11, "yellow_dye");
        dyeConversion.put(12, "light_blue_dye");
        dyeConversion.put(13, "magenta_dye");
        dyeConversion.put(14, "orange_dye");
        dyeConversion.put(15, "bone_meal");
        dyeConversion.put(16, "black_dye");
        dyeConversion.put(17, "brown_dye");
        dyeConversion.put(18, "blue_dye");
        dyeConversion.put(19, "white_dye");
    }

    @Override
    public List<ParsedRecipe> parse(JSONObject recipeJson) {
        List<ParsedRecipe> result = new ArrayList<>();
        for (Iterator<String> it = recipeJson.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject contents = recipeJson.optJSONObject(key);
            if (contents != null) {
                List<String> tags = new ArrayList<>();

                String identifier = null;
                JSONObject description = contents.optJSONObject("description");
                if (description != null) {
                    identifier = description.optString("identifier");
                }

                JSONArray tagsJson = contents.optJSONArray("tags");
                if (tagsJson != null) {
                    for (int i = 0; i < tagsJson.length(); i++) {
                        tags.add(tagsJson.optString(i));
                    }
                }

                result.add(new ParsedRecipe(identifier, key, tags, contents));
            }
        }
        return result;
    }

    @Override
    public Pair<Integer, Integer> getIdAndDataFromItemString(String stringId, int defaultData) {
        if ("dye".equalsIgnoreCase(stringId) || "minecraft:dye".equalsIgnoreCase(stringId)) {
            stringId = dyeConversion.get(defaultData);
            if (stringId == null) {
                return new Pair<>(0, 0);
            }
            defaultData = 0;
        }
        return super.getIdAndDataFromItemString(stringId, defaultData);
    }
}