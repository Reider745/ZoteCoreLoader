package com.zhekasmirnov.innercore.api.mod.recipes.furnace;

import cn.nukkit.Server;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.RecipeEntry;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by zheka on 15.09.2017.
 */

public class FurnaceRecipeRegistry {
    private static HashMap<Long, FurnaceRecipe> recipes = new HashMap<>();
    private static HashMap<Long, Integer> fuel = new HashMap<>();

    public static void addFurnaceRecipe(FurnaceRecipe recipe){
        if(!recipe.isValid()){
            return;
        }
        
        long key = recipe.getInputKey();

        if (recipe.getPrefix() == null) {
            NativeFurnaceRegistry.nativeAddRecipe(recipe.inId, recipe.inData, recipe.resId, recipe.resData);
        }
        else if (recipes.containsKey(key)) {
            NativeFurnaceRegistry.nativeRemoveRecipe(recipe.inId, recipe.inData);
        }

        recipe.setPrefix(recipe.getPrefix());
        recipes.put(key, recipe);
    }

    public static void addFurnaceRecipe(int inId, int inData, int resId, int resData, String prefix) {
        if (prefix != null && (prefix.isEmpty() || prefix.equals("undefined"))) {
            prefix = null;
        }

        FurnaceRecipe recipe = new FurnaceRecipe(inId, inData, resId, resData);
        recipe.setPrefix(prefix);
        addFurnaceRecipe(recipe);
    }

    public static void removeFurnaceRecipe(int inId, int inData) {
        long key = RecipeEntry.getCodeByItem(inId, inData);

        if (recipes.containsKey(key)) {
            FurnaceRecipe recipe = recipes.get(key);
            if (recipe.getPrefix() == null) {
                NativeFurnaceRegistry.nativeRemoveRecipe(inId, inData);
            }
            recipes.remove(key);
        }
    }

    public static void addFuel(int id, int data, int burnDuration) {
        long key = RecipeEntry.getCodeByItem(id, data);

        fuel.put(key, burnDuration);
        NativeFurnaceRegistry.nativeAddFuel(id, data, burnDuration);
    }

    public static void removeFuel(int id, int data) {
        long key = RecipeEntry.getCodeByItem(id, data);
        if (fuel.containsKey(key)) {
            NativeFurnaceRegistry.nativeRemoveFuel(id, data);
        }

        fuel.remove(key);
    }



    public static FurnaceRecipe getRecipe(int id, int data, String prefix) {
        if (prefix != null && (prefix.isEmpty() || prefix.equals("undefined"))) {
            prefix = null;
        }

        long key = RecipeEntry.getCodeByItem(id, data);
        if (recipes.containsKey(key)) {
            return recipes.get(key);
        }
        key = RecipeEntry.getCodeByItem(id, -1);
        if (recipes.containsKey(key)) {
            return recipes.get(key);
        }

        return null;
    }

    public static int getBurnDuration(int id, int data) {
        long key = RecipeEntry.getCodeByItem(id, data);
        if (fuel.containsKey(key)) {
            return fuel.get(key);
        }
        key = RecipeEntry.getCodeByItem(id, -1);
        if (fuel.containsKey(key)) {
            return fuel.get(key);
        }
        return 0;
    }

    public static Collection<FurnaceRecipe> getFurnaceRecipeByResult(int id, int aux, String prefix) {
        Collection<FurnaceRecipe> allRecipes = recipes.values();
        ArrayList<FurnaceRecipe> found = new ArrayList<>();

        for (FurnaceRecipe recipe : allRecipes) {
            ItemInstance result = recipe.getResult();
            if (result.getId() == id && (aux == -1 || result.getData() == aux) || recipe.isMatchingPrefix(prefix)) {
                found.add(recipe);
            }
        }

        return found;
    }

    public static Collection<FurnaceRecipe> getAllRecipes() {
        return recipes.values();
    }



    private static boolean nativeRecipesLoaded = false;
    public static void loadNativeRecipesIfNeeded() {
        if (nativeRecipesLoaded) {
            return;
        } try {
            FileTools.unpackAsset("innercore/recipes/furnace_fuel.json", FileTools.DIR_WORK + "furnace.json");
        } catch (IOException e) {
            //ICLog.e("RECIPES", "failed to unpack recipes", e);
            return;
        }

        JSONObject fuel;
        try {
            fuel = FileTools.readJSON(FileTools.DIR_WORK + "furnace.json");
        } catch (Exception e) {
            ICLog.e("RECIPES", "failed to load recipes", e);
            return;
        }

        JSONArray keys;

        keys = fuel.names();
        if (keys != null) {
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.optString(i);

                String[] split = key.split(":");
                // Handle negatives                    
                split[0] = split[0].replace("_", "-");
                int id, data = -1;
                if (split.length > 1) {
                    id = Integer.valueOf(split[0]);
                    data = Integer.valueOf(split[1]);
                }
                else {
                    id = Integer.valueOf(split[0]);
                }

                int time = fuel.optInt(key);
                if (time > 0) {
                    addFuel(id, data, time);
                }
            }
        }

        nativeRecipesLoaded = true;
    }
}
