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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected Item parse(Object json) {
        if (json instanceof JSONObject jsonObject) {
            Item item = Item.fromString(jsonObject.getString("item"));
            item.setCount(jsonObject.optInt("count", 1));
            item.setDamage(jsonObject.optInt("data", 0));
            return item;
        } else if (json instanceof JSONArray jsonArray) {
            if(jsonArray.length() == 1){
                return parse(jsonArray.get(0));
            }
        }

        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void load(JSONObject content, String file) {
        final JSONObject json = content.getJSONObject(getTagName());
        final Item result = parse(json.get("result"));
        final Map<Character, ItemDescriptor> map = new HashMap<>();

        final JSONObject key = json.getJSONObject("key");
        for(String character : key.keySet()) {
            map.put(character.charAt(0), new DefaultDescriptor(parse(key.getJSONObject(character))));
        }

        final JSONArray pattern =  json.getJSONArray("pattern");
        final String[] patters = new String[pattern.length()];

        for(int i = 0; i < pattern.length(); i++) {
            patters[i] = pattern.getString(i);
            for(int a = patters[i].length(); a < 3; a++) {
                patters[i] = patters[i] + " ";
            }
        }

        final JSONArray tags = json.getJSONArray("tags");
        for(int i = 0; i < tags.length(); i++) {
            final String tag = tags.getString(i);

            switch(tag) {
                case "crafting_table" -> RecipeRegistry.registerShapedRecipe(new ShapedRecipe(String.valueOf(RECIPE_ID--), json.optInt("priority", 1), result, patters, map, List.of()));
                default -> Logger.warning("Not implemented yet: " + tag + ", " + file);
            }
        }
    }
}
