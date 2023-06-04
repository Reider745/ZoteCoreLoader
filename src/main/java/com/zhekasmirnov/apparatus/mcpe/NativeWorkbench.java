package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.item.Item;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class NativeWorkbench {
    private static ArrayList<Recipe> recipes = new ArrayList<>();
    private static void addShapedRecipe(int resultId, int resultCount, int resultData, long resultExtraPtr, String[] pattern, int[] ingredients){
        if(resultId == 0){
            Logger.error("Recipe result id 0");
            return;
        }
        if(ingredients.length % 3 != 0){
            Logger.error("error ingredients");
            return;
        }

        final HashMap<Character, Item> ingredients_ = new HashMap<>();
        for(int i = 0;i < ingredients.length;i+=3)
            ingredients_.put((char) ingredients[i], Item.get(ingredients[i+1], ingredients[i+2]));

        recipes.add(new ShapedRecipe(Item.get(resultId, resultData, resultCount), pattern, ingredients_, new ArrayList<>()));
    }

    public static void addShapedRecipe(ItemStack result, String[] pattern, int[] ingredients) {
        addShapedRecipe(result.id, result.count, result.data, result.getExtraPtr(), pattern, ingredients);
    }

    private static void addShapelessRecipe(int resultId, int resultCount, int resultData, long resultExtraPtr, int[] ingredients){
        if(resultId == 0){
            Logger.error("Recipe result id 0");
            return;
        }
        if(ingredients.length % 3 != 0){
            Logger.error("error ingredients");
            return;
        }

        final Collection<Item> ingredients_ = new ArrayList<>();
        for(int i = 0;i < ingredients.length;i+=3)
            ingredients_.add(Item.get(ingredients[i+1], ingredients[i+2]));
        recipes.add(new ShapelessRecipe(Item.get(resultId, resultData, resultCount), ingredients_));
    }

    public static void addShapelessRecipe(ItemStack result, int[] ingredients) {
        addShapelessRecipe(result.id, result.count, result.data, result.getExtraPtr(), ingredients);
    }

    public static native void removeRecipeByResult(int resultId, int resultCount, int resultData);

    public static void init(){
        CraftingManager craftingManager = Server.getInstance().getCraftingManager();
        for(Recipe recipe : recipes)
            craftingManager.registerRecipe(recipe);
    }
}
