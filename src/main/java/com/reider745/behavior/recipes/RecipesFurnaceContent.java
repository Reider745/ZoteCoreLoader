package com.reider745.behavior.recipes;

import cn.nukkit.item.Item;
import cn.nukkit.recipe.RecipeRegistry;
import cn.nukkit.recipe.impl.BlastFurnaceRecipe;
import cn.nukkit.recipe.impl.CampfireRecipe;
import cn.nukkit.recipe.impl.FurnaceRecipe;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class RecipesFurnaceContent extends RecipesShapedContent {
    @Override
    public String getTagName() {
        return "minecraft:recipe_furnace";
    }

    @Override
    public void load(JSONObject content, String file) {
        JSONObject json = content.getJSONObject(getTagName());
        Item input = parse(json.get("input"));
        Item output = RecipeItems.result(json.get("output"));
        if (input.getCount() != 1) throw new IllegalArgumentException("Furnace recipes require one input item");
        Set<String> seen = new HashSet<>();
        JSONArray tags = json.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            if (!seen.add(tag)) continue;
            switch (tag) {
                case "furnace" -> RecipeRegistry.registerFurnaceRecipe(new FurnaceRecipe(output, input), 0);
                case "blast_furnace" -> RecipeRegistry.registerBlastFurnaceRecipe(new BlastFurnaceRecipe(output, input), 0);
                case "campfire", "soul_campfire" -> {
                    if (seen.add("campfire_registered")) {
                        RecipeRegistry.registerCampfireRecipe(new CampfireRecipe(output, input), 0);
                    }
                }
                // Nukkit-MOT shares furnace recipes with smokers and restricts inputs to vanilla foods.
                // Registering a smoker-only recipe there would also change ordinary furnaces.
                default -> Logger.warning("Unsupported recipe station " + tag + ", " + file);
            }
        }
    }
}
