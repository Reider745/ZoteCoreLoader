package com.reider745.behavior.recipes;

import cn.nukkit.item.Item;
import cn.nukkit.recipe.RecipeRegistry;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.impl.ShapelessRecipe;
import cn.nukkit.recipe.impl.StonecutterRecipe;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class RecipesShapelessContent extends RecipesShapedContent {
    @Override
    public String getTagName() {
        return "minecraft:recipe_shapeless";
    }

    @Override
    public void load(JSONObject content, String file) {
        JSONObject json = content.getJSONObject(getTagName());
        Item result = RecipeItems.result(json.get("result"));
        Collection<ItemDescriptor> ingredients = new ArrayList<>();
        JSONArray input = json.getJSONArray("ingredients");
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            Item item = parse(input.get(i));
            count += item.getCount();
            int amount = item.getCount();
            if (count > 9) throw new IllegalArgumentException("Shapeless recipes require 1 to 9 items");
            item.setCount(1);
            for (int n = 0; n < amount; n++) ingredients.add(new DefaultDescriptor(item.clone()));
        }
        if (count < 1 || count > 9) throw new IllegalArgumentException("Shapeless recipes require 1 to 9 items");
        String id = recipeId(json);
        int priority = json.optInt("priority", 0);
        Set<String> seen = new HashSet<>();
        JSONArray tags = json.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            if (!seen.add(tag)) continue;
            switch (tag) {
                case "crafting_table" -> RecipeRegistry.registerShapelessRecipe(new ShapelessRecipe(id, priority, result, ingredients));
                case "stonecutter" -> {
                    if (count != 1) throw new IllegalArgumentException("Stonecutter recipes require one item");
                    RecipeRegistry.addStonecutterRecipe(new StonecutterRecipe(id, priority, result, ingredients));
                }
                default -> Logger.warning("Unsupported recipe station " + tag + ", " + file);
            }
        }
    }
}
