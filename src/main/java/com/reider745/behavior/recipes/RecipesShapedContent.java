package com.reider745.behavior.recipes;

import cn.nukkit.item.Item;
import cn.nukkit.recipe.RecipeRegistry;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.impl.ShapedRecipe;
import com.reider745.behavior.BehaviorContent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class RecipesShapedContent extends BehaviorContent {
    protected static long RECIPE_ID = Long.MAX_VALUE;

    @Override
    public String getTagName() {
        return "minecraft:recipe_shaped";
    }

    @Override
    public String getDefaultDirectory() {
        return "recipes";
    }

    @Override
    public boolean isRecipe() {
        return true;
    }

    protected Item parse(Object json) {
        return RecipeItems.parse(json);
    }

    protected String recipeId(JSONObject json) {
        JSONObject description = json.optJSONObject("description");
        return description == null ? String.valueOf(RECIPE_ID--) : description.getString("identifier");
    }

    public ShapedRecipe parseRecipe(JSONObject json) {
        Object resultsJson = json.get("result");
        List<Item> results = new ArrayList<>();
        if (resultsJson instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) results.add(RecipeItems.result(array.get(i)));
        } else {
            results.add(RecipeItems.result(resultsJson));
        }
        if (results.isEmpty()) throw new IllegalArgumentException("Empty recipe result");

        Map<Character, ItemDescriptor> ingredients = new HashMap<>();
        JSONObject key = json.getJSONObject("key");
        for (String symbol : key.keySet()) {
            if (symbol.length() != 1 || symbol.charAt(0) == ' ') {
                throw new IllegalArgumentException("Invalid recipe key: " + symbol);
            }
            ingredients.put(symbol.charAt(0), new DefaultDescriptor(parse(key.get(symbol))));
        }

        JSONArray pattern = json.getJSONArray("pattern");
        if (pattern.isEmpty() || pattern.length() > 3) {
            throw new IllegalArgumentException("Recipe pattern must have 1 to 3 rows");
        }
        String[] shape = new String[pattern.length()];
        int width = pattern.getString(0).length();
        if (width < 1 || width > 3) throw new IllegalArgumentException("Recipe pattern must have 1 to 3 columns");
        boolean hasIngredient = false;
        for (int row = 0; row < shape.length; row++) {
            shape[row] = pattern.getString(row);
            if (shape[row].length() != width) throw new IllegalArgumentException("Unequal recipe row lengths");
            for (char symbol : shape[row].toCharArray()) {
                if (symbol == ' ') continue;
                hasIngredient = true;
                if (!ingredients.containsKey(symbol)) throw new IllegalArgumentException("Undefined recipe key: " + symbol);
            }
        }
        if (!hasIngredient) throw new IllegalArgumentException("Empty recipe pattern");
        return new ShapedRecipe(recipeId(json), json.optInt("priority", 0), results.get(0), shape,
                ingredients, results.subList(1, results.size()));
    }

    @Override
    public void load(JSONObject content, String file) {
        JSONObject json = content.getJSONObject(getTagName());
        ShapedRecipe recipe = parseRecipe(json);
        Set<String> seen = new HashSet<>();
        JSONArray tags = json.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            if (!seen.add(tag)) continue;
            if (tag.equals("crafting_table")) {
                RecipeRegistry.registerShapedRecipe(recipe);
            } else {
                Logger.warning("Unsupported recipe station " + tag + ", " + file);
            }
        }
    }
}
