package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.item.Item;
import com.reider745.item.CustomItem;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class NativeWorkbench {

    private static class RecipeRegistry {
        public int resultId;
        public int resultCount;
        public int resultData;
        public long resultExtraPtr;
        public String[] pattern;
        public HashMap<Character, Item> ingredients;
        public int[] ingredients_;
        public boolean shapeless = false;

        public RecipeRegistry(int resultId, int resultCount, int resultData, long resultExtraPtr, String[] pattern, HashMap<Character, Item> ingredients){
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtraPtr = resultExtraPtr;
            this.pattern = pattern;
            this.ingredients = ingredients;
        }

        public RecipeRegistry(int resultId, int resultCount, int resultData, long resultExtraPtr, int[] ingredients){
            this.resultId = resultId;
            this.resultCount = resultCount;
            this.resultData = resultData;
            this.resultExtraPtr = resultExtraPtr;
            this.ingredients_ = ingredients;
            this.shapeless = true;
        }
    }
    private static ArrayList<RecipeRegistry> recipes = new ArrayList<>();
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
        recipes.add(new RecipeRegistry(resultId, resultCount, resultData, resultExtraPtr, pattern, ingredients_));
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

        recipes.add(new RecipeRegistry(resultId, resultCount, resultData, resultExtraPtr, ingredients));
    }

    public static void addShapelessRecipe(ItemStack result, int[] ingredients) {
        addShapelessRecipe(result.id, result.count, result.data, result.getExtraPtr(), ingredients);
    }

    public static native void removeRecipeByResult(int resultId, int resultCount, int resultData);

    private static <T>void addFirst(ArrayList<T> list, T value){
        ArrayList<T> newList = new ArrayList<>();
        newList.add(value);

        for(T v : list)
            newList.add(v);

        list.clear();
        list.addAll(newList);
    }

    public static Item checkItem(Item item){
        Server.getInstance().getLogger().info(item+"  isCreativeItem:"+Item.isCreativeItem(item));
        if(!CustomItem.isCreativeItemModded(item) && item.getId() > 2000) {
            Item added = item.clone();
            Server.getInstance().getLogger().info("clone item   "+added.toString());
            added.setCount(1);

            CustomItem.addToCreativeGroup(CustomItem.TECHNICAL_GROUP, added.getId());
            addFirst(CustomItem.creative, new int[] {added.getId(), added.getCount(), added.getDamage()});
            //Item.addCreativeItem(added);
        }
        return item;
    }

    public static void init(){
        CraftingManager craftingManager = Server.getInstance().getCraftingManager();
        for(RecipeRegistry recipe : recipes)
            if(!recipe.shapeless)
                craftingManager.registerRecipe(new ShapedRecipe(checkItem(Item.get(recipe.resultId, recipe.resultData, recipe.resultCount)), recipe.pattern, recipe.ingredients, new ArrayList<>()));
            else {
                final Collection<Item> ingredients = new ArrayList<>();
                for(int i = 0;i < recipe.ingredients_.length;i+=3)
                    ingredients.add(Item.get(recipe.ingredients_[i+1], recipe.ingredients_[i+2]));
                craftingManager.registerRecipe(new ShapelessRecipe(checkItem(Item.get(recipe.resultId, recipe.resultData, recipe.resultCount)), ingredients));
            }
    }
}
