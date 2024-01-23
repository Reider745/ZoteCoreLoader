package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

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
        RuntimeItemMapping mapping = RuntimeItems.getMapping(InnerCoreServer.PROTOCOL);
        CraftingManager manager = Server.getInstance().getCraftingManager();
        int outputId, outputData, inputId, inputData;
        for (Map<String, Integer> recipe : recipes) {
            inputId = recipe.get("inputId");
            inputData = recipe.get("inputData");
            final Item input = ItemUtils.get(inputId, inputData);
            try {
                mapping.toRuntime(input.getId(), input.getDamage());
            } catch (IllegalArgumentException e) {
                Logger.warning("NativeFurnace",
                        "Unknown legacy2Runtime mapping: id=" + inputId + ", meta=" + inputData);
                continue;
            }
            switch (recipe.get("type")) {
                case 0 -> {
                    outputId = recipe.get("outputId");
                    outputData = recipe.get("outputData");
                    Item output = ItemUtils.get(outputId, outputData);
                    try {
                        mapping.toRuntime(output.getId(), output.getDamage());
                    } catch (IllegalArgumentException e) {
                        Logger.warning("NativeFurnace",
                                "Unknown legacy2Runtime mapping: id=" + outputId + ", meta=" + outputData
                                        + " (recipe: id=" + inputId + ", meta=" + inputData + ")");
                        continue;
                    }
                    manager.registerFurnaceRecipe(ProtocolInfo.CURRENT_PROTOCOL, new FurnaceRecipe(output, input));
                }
                case 1 -> {
                    AtomicReference<Integer> key = new AtomicReference<>();
                    manager.getFurnaceRecipes(ProtocolInfo.CURRENT_PROTOCOL).forEach((k, v) -> {
                        Item item = v.getInput();
                        if (item.getId() == input.getId()
                                || (input.getDamage() == -1 || input.getDamage() == item.getDamage()))
                            key.set(k);
                    });

                    manager.getFurnaceRecipes(ProtocolInfo.CURRENT_PROTOCOL).remove(key.get());
                }
            }
        }
        recipes.clear();
        manager.rebuildPacket();
    }
}
