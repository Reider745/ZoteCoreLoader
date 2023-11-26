package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntityFurnace;
import cn.nukkit.event.inventory.FurnaceBurnEvent;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        recipe.put("type", 1);

        recipe.put("inputId", inputId);
        recipe.put("inputData", inputData);

        recipes.add(recipe);
    }

    public static class ItemStorage {
        private int id, data;
        public ItemStorage(int id, int data){
            this.id = id;
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ItemStorage item)
                return this.id == item.id && this.data == item.data;
            if(obj instanceof Item item)
                return this.id == item.getId() && (item.hasMeta() ? item.getDamage() == data : 0 == data);
            return false;
        }
    }

    private static HashMap<ItemStorage, Integer> fuels = new HashMap<>();
    private static HashMap<ItemStorage, Boolean> remove_fuels = new HashMap<>();

    public static void nativeAddFuel(int id, int data, int burnDuration){
        ItemStorage storage = new ItemStorage(id, data);

        Set<Map.Entry<ItemStorage, Boolean>> keys = remove_fuels.entrySet();
        for(Map.Entry<ItemStorage, Boolean> key : keys)
            if(key.getKey().equals(storage) && key.getValue()){
                remove_fuels.put(key.getKey(), false);
                break;
            }

        fuels.put(storage, burnDuration);
    }
    public static void nativeRemoveFuel(int id, int data){
        remove_fuels.put(new ItemStorage(id, data), true);
    }

    public static boolean canRemove(Item item){
         Set<Map.Entry<ItemStorage, Boolean>> keys = remove_fuels.entrySet();
         for(Map.Entry<ItemStorage, Boolean> key : keys){
             if(key.getKey().equals(item))
                 return key.getValue();
         }
         return false;
    }

    public static Integer get(Item item){
        Set<Map.Entry<ItemStorage, Integer>> keys = fuels.entrySet();
        for(Map.Entry<ItemStorage, Integer> key : keys)
            if(key.getKey().equals(item))
                return key.getValue();
        return null;
    }

    public static short getBurnTime(Item item){
        if(canRemove(item))
            return 0;
        Integer burn = get(item);
        if(burn != null)
            return burn.shortValue();
        return 0;
    }

    public static void init(){
        CraftingManager manager = Server.getInstance().getCraftingManager();
        for (HashMap<String, Integer> recipe : recipes){
            int type = recipe.get("type");
            switch (type){
                case 0 ->
                    manager.registerFurnaceRecipe(ProtocolInfo.CURRENT_PROTOCOL, new FurnaceRecipe(
                            Item.get(recipe.get("outputId"), recipe.get("outputData")),
                            Item.get(recipe.get("inputId"), recipe.get("inputData"))
                    ));
                case 1 -> {
                    AtomicReference<Integer> key = new AtomicReference<>();
                    manager.getFurnaceRecipes(ProtocolInfo.CURRENT_PROTOCOL).forEach((k, v) -> {
                        Item item = v.getInput();
                        int data = recipe.get("inputData");
                        if (item.getId() == recipe.get("inputId") || (data == -1 || data == item.getDamage()))
                            key.set(k);
                    });

                    manager.getFurnaceRecipes(ProtocolInfo.CURRENT_PROTOCOL).remove(key.get());
                }
            }
        }
    }
}
