package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntityFurnace;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.item.Item;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zheka on 15.09.2017.
 */

public class NativeFurnaceRegistry {
    private static ArrayList<HashMap<String, Integer>> recipes = new ArrayList<>();

    public static void nativeAddRecipe(int inputId, int inputData, int outputId, int outputData){
        HashMap<String, Integer> recipe = new HashMap<>();
        recipe.put("type", 0);

        recipe.put("inputId", inputId);
        recipe.put("inputData", inputData);

        recipe.put("outputId", outputId);
        recipe.put("outputData", outputData);

        recipes.add(recipe);
    }
    public static void nativeRemoveRecipe(int inputId, int inputData){
        HashMap<String, Integer> recipe = new HashMap<>();
        recipe.put("type", 0);

        recipe.put("inputId", inputId);
        recipe.put("inputData", inputData);

        recipes.add(recipe);
    }
    public static void nativeAddFuel(int id, int data, int burnDuration){
        HashMap<String, Integer> recipe = new HashMap<>();
        recipe.put("type", 2);
        recipe.put("id", id);
        recipe.put("data", data);
        recipes.add(recipe);
       // BlockEntityFurnace.fuels.put(id+":"+data, (short) burnDuration);
    }
    public static void nativeRemoveFuel(int id, int data){
        //BlockEntityFurnace.fuels.put(id+":"+data, (short) 0);
    }

    public static void init(){
        CraftingManager manager = Server.getInstance().getCraftingManager();
        for (HashMap<String, Integer> recipe : recipes){
            int type = recipe.get("type");

            switch (type){
                case 0:
                    manager.registerFurnaceRecipe(new FurnaceRecipe(
                            NativeWorkbench.checkItem(Item.get(recipe.get("outputId"), recipe.get("outputData"))),
                            Item.get(recipe.get("inputId"), recipe.get("inputData"))
                    ));
                case 1:
                    AtomicReference<Integer> key = null;
                    manager.getFurnaceRecipes().forEach((k, v) -> {
                        Item item = v.getInput();
                        int data = recipe.get("inputData");
                        if(item.getId() == recipe.get("inputId") || (data == -1 || data == item.getDamage()))
                            key.set(k);
                    });

                    manager.getFurnaceRecipes().remove(key);
                    break;
                case 2:
                    NativeWorkbench.checkItem(Item.get(recipe.get("id"), recipe.get("data")));
                    break;
            }
        }
    }
}
