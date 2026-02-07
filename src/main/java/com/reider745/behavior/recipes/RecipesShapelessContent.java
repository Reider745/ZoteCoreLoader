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

import java.util.ArrayList;
import java.util.Collection;

public class RecipesShapelessContent extends RecipesShapedContent {
    @Override
    public String getTagName() {
        return "minecraft:recipe_shapeless";
    }

    @Override
    public void load(JSONObject content, String file) {
        final JSONObject json = content.getJSONObject(this.getTagName());
        final JSONArray ingredientsJson = json.getJSONArray("ingredients");

        final Item result = parse(json.get("result"));
        final Collection<ItemDescriptor> ingredients = new ArrayList<>();

        for (int i = 0; i < ingredientsJson.length(); i++) {
            Item item = parse(ingredientsJson.get(i));
            if(item.isNull()) {
                Logger.warning("Invalid ingredient " + ingredientsJson.get(i));
                return;
            }
            ingredients.add(new DefaultDescriptor(item));
        }

        if(result.isNull()){
            Logger.warning("Invalid result " + json.get("result"));
            return;
        }

        final JSONArray tags = json.getJSONArray("tags");
        for(int i = 0; i < tags.length(); i++) {
            final String tag = tags.getString(i);
            final String id = String.valueOf(RECIPE_ID--);
            final int priority = json.optInt("priority", 1);

            switch(tag) {
                case "crafting_table" -> RecipeRegistry.registerShapelessRecipe(new ShapelessRecipe(id, priority, result, ingredients));
                case "stonecutter" -> RecipeRegistry.addStonecutterRecipe(new StonecutterRecipe(id, priority, result, ingredients));
                default -> Logger.warning("Not implemented yet: " + tag + ", " + file);
            }
        }
    }
}
