package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

import java.util.*;

public class NativeWorkbench {

    private static class RecipeRegistry {
        public int resultId;
        public int resultCount;
        public int resultData;
        public NativeItemInstanceExtra resultExtra;
        public String[] pattern;
        public HashMap<Character, ItemStack> ingredients;
        public int[] ingredients_;
        public boolean shapeless = false;

        public RecipeRegistry(int resultId, int resultCount, int resultData, NativeItemInstanceExtra resultExtra, String[] pattern,
                HashMap<Character, ItemStack> ingredients) {
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtra = resultExtra;
            this.pattern = pattern;
            this.ingredients = ingredients;
        }

        public RecipeRegistry(int resultId, int resultCount, int resultData, NativeItemInstanceExtra resultExtra, int[] ingredients) {
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtra = resultExtra;
            this.ingredients_ = ingredients;
            this.shapeless = true;
        }
    }

    private static ArrayList<RecipeRegistry> recipes = new ArrayList<>();

    private static void addShapedRecipe(int resultId, int resultCount, int resultData, NativeItemInstanceExtra resultExtra,
            String[] pattern, int[] ingredients) {
        if (resultId == 0) {
            Logger.error("Recipe result id 0");
            return;
        }

        final HashMap<Character, ItemStack> ingredients_ = new HashMap<>();
        for (int i = 0; i < ingredients.length; i += 3)
            ingredients_.put((char) ingredients[i], new ItemStack(ingredients[i + 1], 1, ingredients[i + 2]));
        recipes.add(new RecipeRegistry(resultId, resultCount, resultData, resultExtra, pattern, ingredients_));
    }

    public static void addShapedRecipe(ItemStack result, String[] pattern, int[] ingredients) {
        addShapedRecipe(result.id, result.count, result.data, result.extra, pattern, ingredients);
    }

    private static void addShapelessRecipe(int resultId, int resultCount, int resultData, NativeItemInstanceExtra resultExtra,
            int[] ingredients) {
        if (resultId == 0) {
            Logger.error("Recipe result id 0");
            return;
        }

        recipes.add(new RecipeRegistry(resultId, resultCount, resultData, resultExtra, ingredients));
    }

    public static void addShapelessRecipe(ItemStack result, int[] ingredients) {
        addShapelessRecipe(result.id, result.count, result.data, result.extra, ingredients);
    }

    private static final ArrayList<int[]> removeds = new ArrayList<>();

    public static void removeRecipeByResult(int resultId, int resultCount, int resultData) {
        removeds.add(new int[] { resultId, resultCount, resultData });
    }

    public static void init() {
        RuntimeItemMapping mapping = RuntimeItems.getMapping(InnerCoreServer.PROTOCOL);
        CraftingManager craftingManager = Server.getInstance().getCraftingManager();
        for (int[] item : removeds) {
            // TODO: data -1, shaped recipes support
            Item itemInstance = ItemUtils.get(item[0], item[1], item[2]);

            craftingManager.getShapelessRecipes(InnerCoreServer.PROTOCOL).forEach((uid, recipes) -> {
                for (UUID uuid : recipes.keySet()) {
                    ShapelessRecipe recipe = recipes.get(uuid);
                    if (recipe.getResult().equals(itemInstance)) {
                        recipes.remove(uuid);
                        break;
                    }
                }
            });
        }
        for (RecipeRegistry recipe : recipes) {
            Item result = ItemUtils.get(recipe.resultId, recipe.resultCount, recipe.resultData, recipe.resultExtra);
            try {
                mapping.toRuntime(result.getId(), result.getDamage());
            } catch (IllegalArgumentException e) {
                Logger.warning("NativeWorkbench",
                        "Unknown legacy2Runtime mapping: id=" + recipe.resultId + ", meta=" + recipe.resultData);
                continue;
            }
            boolean accepted = true;
            Item item;
            if (!recipe.shapeless) {
                final Map<Character, Item> ingredients = new HashMap<>();
                for (Map.Entry<Character, ItemStack> ingredient : recipe.ingredients.entrySet()) {
                    ItemStack stack = ingredient.getValue();
                    item = ItemUtils.get(stack.id, stack.data);
                    try {
                        mapping.toRuntime(item.getId(), item.getDamage());
                    } catch (IllegalArgumentException e) {
                        accepted = false;
                        Logger.warning("NativeWorkbench", "Unknown legacy2Runtime mapping: id=" + stack.id + ", meta="
                                + stack.data + " (recipe: id=" + recipe.resultId + ", meta=" + recipe.resultData + ")");
                        continue;
                    }
                    ingredients.put(ingredient.getKey(), item);
                }
                if (accepted) {
                    craftingManager.registerRecipe(419,
                            new ShapedRecipe(result, recipe.pattern, ingredients, new ArrayList<>()));
                }
            } else {
                final Collection<Item> ingredients = new ArrayList<>();
                for (int id, data, i = 0; i < recipe.ingredients_.length; i += 3) {
                    id = recipe.ingredients_[i + 1];
                    data = recipe.ingredients_[i + 2];
                    item = ItemUtils.get(id, data);
                    try {
                        mapping.toRuntime(item.getId(), item.getDamage());
                    } catch (IllegalArgumentException e) {
                        accepted = false;
                        Logger.warning("NativeWorkbench", "Unknown legacy2Runtime mapping: id=" + id + ", meta=" + data
                                + " (recipe: id=" + recipe.resultId + ", meta=" + recipe.resultData + ")");
                        continue;
                    }
                    ingredients.add(item);
                }
                if (accepted) {
                    craftingManager.registerRecipe(419, new ShapelessRecipe(result, ingredients));
                }
            }
        }
        recipes.clear();
        craftingManager.rebuildPacket();
    }
}
