package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;

import cn.nukkit.recipe.CraftingRecipe;
import cn.nukkit.recipe.Recipe;
import cn.nukkit.recipe.RecipeRegistry;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.impl.ShapedRecipe;
import cn.nukkit.recipe.impl.ShapelessRecipe;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.item.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.inventory.ItemInstanceExtra;

import java.util.*;

public class NativeWorkbench {
    interface IApply {
        IApply apply(RuntimeItemMapping mapping);
        default void rebuildPacket() {
            if(isFinalLoad) {
                RecipeRegistry.buildPackets();
            }
        }
    }

    private static boolean isFinalLoad = false;

    private static class RecipeRemove implements IApply {
        public int resultId, resultCount, resultData;

        public RecipeRemove(int resultId, int resultCount, int resultData){
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
        }

        @Override
        public IApply apply(RuntimeItemMapping mapping) {
            final Item itemInstance = ItemUtils.get(this.resultId, this.resultCount, this.resultData);

            UUID recipeUuid = null;
            for(Recipe recipe : RecipeRegistry.getRecipes()){
                if(recipe instanceof CraftingRecipe craftingRecipe && recipe.getResult().equals(itemInstance)){
                    recipeUuid = craftingRecipe.getId();
                }
            }

            if(recipeUuid == null || !RecipeRegistry.removeRecipe(recipeUuid)){
                Logger.warning("Not remove recipe " + itemInstance);
            }

            return this;
        }
    }
    private static long RECIPE_ID = Long.MIN_VALUE;

    private static class RecipeRegistryEntry implements IApply {
        public int resultId;
        public int resultCount;
        public int resultData;
        public ItemInstanceExtra resultExtra;
        public String[] pattern;
        public HashMap<Character, ItemInstance> ingredients;
        public int[] ingredients_;
        public boolean shapeless = false;

        public RecipeRegistryEntry(int resultId, int resultCount, int resultData, ItemInstanceExtra resultExtra, String[] pattern,
                                   HashMap<Character, ItemInstance> ingredients) {
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtra = resultExtra;
            this.pattern = pattern;
            this.ingredients = ingredients;
        }

        public RecipeRegistryEntry(int resultId, int resultCount, int resultData, ItemInstanceExtra resultExtra, int[] ingredients) {
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtra = resultExtra;
            this.ingredients_ = ingredients;
            this.shapeless = true;
        }

        @Override
        public IApply apply(RuntimeItemMapping mapping) {
            Item result = ItemUtils.get(this.resultId, this.resultCount, this.resultData, this.resultExtra);
            try {
                mapping.toRuntime(result.getId(), result.getDamage());
            } catch (IllegalArgumentException e) {
                Logger.warning("NativeWorkbench",
                        "Unknown legacy2Runtime mapping: id=" + this.resultId + ", meta=" + this.resultData);
                return this;
            }

            boolean accepted = true;
            Item item;
            if (!this.shapeless) {
                final Map<Character, ItemDescriptor> ingredients = new HashMap<>();
                for (Map.Entry<Character, ItemInstance> ingredient : this.ingredients.entrySet()) {
                    ItemInstance stack = ingredient.getValue();
                    item = ItemUtils.get(stack.id, stack.data);
                    try {
                        mapping.toRuntime(item.getId(), item.getDamage());
                    } catch (IllegalArgumentException e) {
                        accepted = false;
                        Logger.warning("NativeWorkbench", "Unknown legacy2Runtime mapping: id=" + stack.id + ", meta="
                                + stack.data + " (recipe: id=" + this.resultId + ", meta=" + this.resultData + ")");
                        continue;
                    }
                    ingredients.put(ingredient.getKey(), new DefaultDescriptor(item));
                }

                for (String s : this.pattern) {
                    if (s.length() > 3) {
                        Logger.warning("NativeWorkbench", "Skip recipe, error pattern: " + Arrays.toString(this.pattern) + ", item: " + result);
                        accepted = false;
                        break;
                    }
                }

                if (accepted) {
                    RecipeRegistry.registerShapedRecipe(new ShapedRecipe(String.valueOf(RECIPE_ID++), 1, result, this.pattern, ingredients, new ArrayList<>()));
                }
            } else {
                final Collection<ItemDescriptor> ingredients = new ArrayList<>();
                for (int id, data, i = 0; i < this.ingredients_.length; i += 3) {
                    id = this.ingredients_[i + 1];
                    data = this.ingredients_[i + 2];
                    item = ItemUtils.get(id, data);
                    try {
                        mapping.toRuntime(item.getId(), item.getDamage());
                    } catch (IllegalArgumentException e) {
                        accepted = false;
                        Logger.warning("NativeWorkbench", "Unknown legacy2Runtime mapping: id=" + id + ", meta=" + data
                                + " (recipe: id=" + this.resultId + ", meta=" + this.resultData + ")");
                        continue;
                    }
                    ingredients.add(new DefaultDescriptor(item));
                }
                if (accepted) {
                    RecipeRegistry.registerShapelessRecipe(new ShapelessRecipe(String.valueOf(RECIPE_ID++), 10, result, ingredients));
                }
            }

            return this;
        }
    }

    private static final ArrayList<IApply> recipes = new ArrayList<>();
    private static RuntimeItemMapping runtimeItemMapping = null;

    private static void addShapedRecipe(int resultId, int resultCount, int resultData, ItemInstanceExtra resultExtra,
            String[] pattern, int[] ingredients) {
        if (resultId == 0) {
            Logger.error("Recipe result id 0");
            return;
        }
        final HashMap<Character, ItemInstance> ingredients_ = new HashMap<>();
        for (int i = 0; i < ingredients.length; i += 3) {
            int id = ingredients[i + 1];
            if (id == 0) {
                Logger.error("Recipe result id 0");
                return;
            }
            ingredients_.put((char) ingredients[i], new ItemInstance(id, 1, ingredients[i + 2]));
        }

        final RecipeRegistryEntry recipe = new RecipeRegistryEntry(resultId, resultCount, resultData, resultExtra, pattern, ingredients_);

        if(runtimeItemMapping != null) {
            recipe.apply(runtimeItemMapping)
                    .rebuildPacket();
        } else {
            recipes.add(recipe);
        }
    }

    public static void addShapedRecipe(ItemInstance result, String[] pattern, int[] ingredients) {
        addShapedRecipe(result.id, result.count, result.data, result.extra, pattern, ingredients);
    }

    private static void addShapelessRecipe(int resultId, int resultCount, int resultData, ItemInstanceExtra resultExtra,
            int[] ingredients) {
        if (resultId == 0) {
            Logger.error("Recipe result id 0");
            return;
        }

        final RecipeRegistryEntry recipe = new RecipeRegistryEntry(resultId, resultCount, resultData, resultExtra, ingredients);
        recipes.add(recipe);
        recipe.rebuildPacket();
    }

    public static void addShapelessRecipe(ItemInstance result, int[] ingredients) {
        addShapelessRecipe(result.id, result.count, result.data, result.extra, ingredients);
    }


    public static void removeRecipeByResult(int resultId, int resultCount, int resultData) {
        final RecipeRemove recipe = new RecipeRemove(resultId, resultCount, resultData);
        recipes.add(recipe);
        recipe.rebuildPacket();
    }
    public static void init() {
        runtimeItemMapping = RuntimeItems.getMapping(InnerCoreServer.PROTOCOL);

        for (IApply recipe : recipes)
            recipe.apply(runtimeItemMapping);

        recipes.clear();
        isFinalLoad = true;
        RecipeRegistry.buildPackets();
    }
}
