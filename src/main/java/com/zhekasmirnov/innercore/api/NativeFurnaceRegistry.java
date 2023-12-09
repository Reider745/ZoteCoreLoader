package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zheka on 15.09.2017.
 */

public class NativeFurnaceRegistry {
    private static List<Map<String, Integer>> recipes = new ArrayList<>();

    public static void nativeAddRecipe(int inputId, int inputData, int outputId, int outputData) {
        Map<String, Integer> recipe = new HashMap<>();
        recipe.put("type", 0);

        recipe.put("inputId", inputId);
        recipe.put("inputData", inputData);

        recipe.put("outputId", outputId);
        recipe.put("outputData", outputData);

        recipes.add(recipe);
    }

    public static void nativeRemoveRecipe(int inputId, int inputData) {
        HashMap<String, Integer> recipe = new HashMap<>();
        recipe.put("type", 1);

        recipe.put("inputId", inputId);
        recipe.put("inputData", inputData);

        recipes.add(recipe);
    }

    private static Map<Integer, Short> burningFuels = new HashMap<>();
    private static Set<Integer> unattendedFuels = new HashSet<>();

    public static void nativeAddFuel(int id, int data, int burnDuration) {
        int idData = (data & 0xFFFF) | ((id & 0xFFFF) << 16);
        if (unattendedFuels.contains(idData)) {
            unattendedFuels.remove(idData);
        }
        burningFuels.put(idData, (short) (burnDuration & 0xFFFF));
    }

    public static void nativeRemoveFuel(int id, int data) {
        int idData = (data & 0xFFFF) | ((id & 0xFFFF) << 16);
        if (burningFuels.containsKey(idData)) {
            burningFuels.remove(idData);
        }
        unattendedFuels.add(idData);
    }

    public static boolean isFuelRemoved(Item item) {
        return unattendedFuels.contains(getIdData(item));
    }

    public static short getBurnTime(Item item) {
        int idData = getIdData(item);
        if (unattendedFuels.contains(idData)) {
            return 0;
        }
        if (burningFuels.containsKey(idData)) {
            return burningFuels.get(idData);
        }
        return 0;
    }

    private static int getIdData(Item item) {
        if (item == null) {
            return 0;
        }
        int idData = (item.getId() & 0xFFFF) << 16;
        return item.hasMeta() ? idData | (item.getDamage() & 0xFFFF) : idData;
    }

    @SuppressWarnings("unused")
    private static boolean equalsIdData(int idData, Item item) {
        if (item == null) {
            return ((idData >> 16) & 0xFFFF) == 0;
        }
        return (short) ((idData >> 16) & 0xFFFF) == item.getId()
                && (!item.hasMeta() || (idData & 0xFFFF) == item.getDamage());
    }

    public static void init() {
        CraftingManager manager = Server.getInstance().getCraftingManager();
        for (Map<String, Integer> recipe : recipes) {
            int type = recipe.get("type");
            switch (type) {
                case 0 ->
                    manager.registerFurnaceRecipe(ProtocolInfo.CURRENT_PROTOCOL, new FurnaceRecipe(
                            Item.get(recipe.get("outputId"), recipe.get("outputData")),
                            Item.get(recipe.get("inputId"), recipe.get("inputData"))));
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
