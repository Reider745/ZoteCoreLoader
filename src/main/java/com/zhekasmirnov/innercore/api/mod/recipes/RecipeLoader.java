package com.zhekasmirnov.innercore.api.mod.recipes;

import com.zhekasmirnov.apparatus.minecraft.addon.AddonContext;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.IDRegistry;
import com.zhekasmirnov.innercore.api.mod.recipes.furnace.FurnaceRecipe;
import com.zhekasmirnov.innercore.api.mod.recipes.furnace.FurnaceRecipeRegistry;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.*;
import com.zhekasmirnov.innercore.api.unlimited.IDDataPair;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class RecipeLoader {

    public List<String> listRecipeDirectories() {
        List<String> result = new ArrayList<>();
        result.add("definitions/recipe/");
        for (String behaviorPackDir : MinecraftVersions.getCurrent().getVanillaBehaviorPacksDirs()) {
            result.add(behaviorPackDir + "recipes/");
        }
        return result;
    }

    public List<String> listRecipeDefinitions() {
        List<String> result = new ArrayList<>();
        List<String> recipeDirectories = listRecipeDirectories();
        Collections.reverse(recipeDirectories); // start from highest version
        for (String recipeDir : recipeDirectories) {
            String[] filesInDir = FileTools.listAssets(recipeDir);
            if (filesInDir != null) {
                for (String fileInDir : filesInDir) {
                    result.add(recipeDir + fileInDir);
                }
            }
        }
        return result;
    }

    public void load() {
        // loadLegacy();

        List<String> files = listRecipeDefinitions();
        AddonRecipeParser parser = AddonContext.getInstance().getRecipeParser();

        // get all recipes
        parser.prepare();

        List<AddonRecipeParser.ParsedRecipe> recipesWithoutIdentifier = new ArrayList<>();
        Map<String, AddonRecipeParser.ParsedRecipe> recipeMap = new HashMap<>();
        for (String file : files) {
            try {
                JSONObject fileContent = FileTools.getAssetAsJSON(file);
                for (AddonRecipeParser.ParsedRecipe recipe : parser.parse(fileContent)) {
                    String identifier = recipe.getIdentifier();
                    if (identifier != null) {
                        recipeMap.put(identifier, recipe);
                    } else {
                        recipesWithoutIdentifier.add(recipe);
                    }
                }
            } catch (JSONException | IllegalArgumentException | NullPointerException e) {
                ICLog.e(RecipeRegistry.LOGGER_TAG, "unable to read recipe definition " + file, e);
            }
        }

        List<AddonRecipeParser.ParsedRecipe> allRecipes = new ArrayList<>();
        allRecipes.addAll(recipesWithoutIdentifier);
        allRecipes.addAll(recipeMap.values());

        for (AddonRecipeParser.ParsedRecipe recipe : allRecipes) {
            try {
                List<String> tags = recipe.getTags();
                switch (recipe.getType()) {
                    case "minecraft:recipe_furnace":
                        if (tags.contains("furnace")) {
                            FurnaceRecipeRegistry.addFurnaceRecipe(new FurnaceRecipe(parser, recipe));
                        }
                        break;
                    case "minecraft:recipe_shaped":
                        if (tags.contains("crafting_table")) {
                            WorkbenchRecipeRegistry
                                    .addRecipe(new WorkbenchShapedRecipe(parser, recipe).setVanilla(true));
                        }
                        break;
                    case "minecraft:recipe_shapeless":
                        if (tags.contains("crafting_table")) {
                            WorkbenchRecipeRegistry
                                    .addRecipe(new WorkbenchShapelessRecipe(parser, recipe).setVanilla(true));
                        }
                        break;
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                ICLog.e(RecipeRegistry.LOGGER_TAG, "unable to read recipe definition " + recipe.getIdentifier(), e);
            }
        }
    }

    public static IDDataPair getIdData(String stringId) {
        return getIdData(stringId, -1);
    }

    public static IDDataPair getIdData(String stringId, int defaultData) {
        String[] result = stringId.split(":");
        String name = result.length == 1 ? result[0] : result[1];
        int id = IDRegistry.getIDByName(name.toLowerCase());
        if (id == 0) {
            return null;
        }

        int data = defaultData;
        if (result.length == 3) {
            data = Integer.parseInt(result[2]);
        }
        return new IDDataPair(id, data);
    }
}
