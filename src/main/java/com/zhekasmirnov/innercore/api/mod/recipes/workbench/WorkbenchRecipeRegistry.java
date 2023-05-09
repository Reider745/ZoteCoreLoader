package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mcpe.NativePlayer;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import com.zhekasmirnov.innercore.api.mod.ui.container.Slot;

import java.util.*;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchRecipeRegistry {
    /**
     * for each possible field mask contains array of possible recipes, that fits to this mask
     * allows to quickly find recipes by grid pattern
     */
    private static final HashMap<String, ArrayList<WorkbenchRecipe>> recipes = new HashMap<>();

    /**
     * for each single pair id-data contains array of recipes, that contain this component
     * the key is (data << 16) | id or -id if data is not important
     */
    private static final HashMap<Long, ArrayList<WorkbenchRecipe>> componentQuickAccess = new HashMap<>();

    private static final Map<Long, WorkbenchRecipe> recipeByUid = new HashMap<>();


    private static ArrayList<WorkbenchRecipe> getRecipeArrayByMask(String mask) {
        if (!recipes.containsKey(mask)) {
            recipes.put(mask, new ArrayList<WorkbenchRecipe>());
        }
        return recipes.get(mask);
    }

    private static void addRecipeToQuickAccess(WorkbenchRecipe recipe) {
        ArrayList<Long> codes = recipe.getEntryCodes();
        for (Long code : codes) {
            ArrayList<WorkbenchRecipe> recipeList = componentQuickAccess.get(code);
            if (recipeList == null) {
                recipeList = new ArrayList<>();
                componentQuickAccess.put(code, recipeList);
            }

            recipeList.add(recipe);
        }
    }

    public static void addRecipe(WorkbenchRecipe recipe) {
        if(!recipe.isValid()){
            return;
        }
        
        ArrayList<WorkbenchRecipe> vars = new ArrayList<>();
        recipe.addVariants(vars);

        boolean isFirst = true;
        for (WorkbenchRecipe variant : vars) {
            String mask = variant.getRecipeMask();
            ArrayList<WorkbenchRecipe> recipes = getRecipeArrayByMask(mask);
            if(!recipes.contains(variant)){
                recipes.add(variant);
                recipeByUid.put(variant.getRecipeUid(), variant);
            }

            if (isFirst) {
                addRecipeToQuickAccess(variant);
                isFirst = false;
            }
        }

        if (!recipe.isVanilla()) {
            recipe.addToVanillaWorkbench();
        }
    }

    public static void removeRecipeByResult(int id, int count, int data) {
        Collection<ArrayList<WorkbenchRecipe>> recipes;

        int entriesRemoved = 0;

        recipes = WorkbenchRecipeRegistry.recipes.values();
        for (ArrayList<WorkbenchRecipe> group : recipes) {
            for (int i = 0; i < group.size(); i++) {
                WorkbenchRecipe recipe = group.get(i);
                if (recipe.id == id && (recipe.data == data || data == -1) && (recipe.count == count || count == -1)) {
                    group.remove(i--);
                    entriesRemoved++;
                }
            }
        }

        recipes = componentQuickAccess.values();
        for (ArrayList<WorkbenchRecipe> group : recipes) {
            for (int i = 0; i < group.size(); i++) {
                WorkbenchRecipe recipe = group.get(i);
                if (recipe.id == id && (recipe.data == data || data == -1) && (recipe.count == count || count == -1)) {
                    group.remove(i--);
                    entriesRemoved++;
                }
            }
        }

        NativeWorkbench.removeRecipeByResult(id, count, data);
        ICLog.d("RECIPES", "removing recipe (" + id + ", " + count + ", " + data + ") complete, " + entriesRemoved + " variations and entries were removed.");
    }

    public static Collection<WorkbenchRecipe> getRecipesByResult(int id, int count, int data) {
        Collection<ArrayList<WorkbenchRecipe>> recipes;
        HashSet<WorkbenchRecipe> found = new HashSet<>();

        recipes = componentQuickAccess.values();
        for (ArrayList<WorkbenchRecipe> group : recipes) {
            for (int i = 0; i < group.size(); i++) {
                WorkbenchRecipe recipe = group.get(i);
                if (recipe.id == id && (recipe.data == data || data == -1) && (recipe.count == count || count == -1)) {
                    found.add(recipe);
                }
            }
        }

        return found;
    }

    public static Collection<WorkbenchRecipe> getRecipesByIngredient(int id, int data) {
        ArrayList<WorkbenchRecipe> recipes = new ArrayList<>();
        addRecipesThatContainItem(id, data, recipes);
        return recipes;
    }

    public static WorkbenchRecipe getRecipeByUid(long uid) {
        return recipeByUid.get(uid);
    }

    public static Collection<WorkbenchRecipe> getAllRecipes() {
        return recipeByUid.values();
    }


    public static String[] getFieldMasks(WorkbenchField field) {
        StringBuilder shaped = new StringBuilder();
        ArrayList<Character> chars = new ArrayList<>();

        // TODO: only 2x2 and 3x3 recipes are supported, maybe change it
        for (int i = 0; i < 9; i++) {
            AbstractSlot slot = field.getFieldSlot(i % 3, i / 3);
            char c = (char) (slot != null && slot.getCount() > 0 ? slot.getId() : 0);
            shaped.append(c);
            if (c != 0) {
                chars.add(c);
            }
        }

        StringBuilder shapeless = new StringBuilder("$$");
        Collections.sort(chars);
        for (Character c : chars) {
            shapeless.append(c);
        }
        return new String[] {
                shaped.toString(),
                shapeless.toString()
        };
    }

    public static WorkbenchRecipe getRecipeFromField(WorkbenchField field, String prefix) {
        String[] masks = getFieldMasks(field);

        if (recipes.containsKey(masks[0])) {
            ArrayList<WorkbenchRecipe> _recipes = recipes.get(masks[0]);

            for(WorkbenchRecipe recipe : _recipes) {
                if (recipe.isMatchingField(field) && recipe.isMatchingPrefix(prefix)) {
                    return recipe;
                }
            }
        }

        if (recipes.containsKey(masks[1])) {
            ArrayList<WorkbenchRecipe> _recipes = recipes.get(masks[1]);

            for(WorkbenchRecipe recipe : _recipes) {
                if (recipe.isMatchingField(field)) {
                    return recipe;
                }
            }
        }

        return null;
    }

    public static ItemInstance getRecipeResult(WorkbenchField field, String prefix) {
        WorkbenchRecipe recipe = getRecipeFromField(field, prefix);
        if (recipe != null) {
            return recipe.getResult();
        }
        return null;
    }

    public static ItemInstance provideRecipeForPlayer(WorkbenchField field, String prefix, long player) {
        WorkbenchRecipe recipe = getRecipeFromField(field, prefix);
        if (recipe != null) {
            return recipe.provideRecipeForPlayer(field, player);
        }
        return null;
    }

    public static ItemInstance provideRecipe(WorkbenchField field, String prefix) {
        return provideRecipeForPlayer(field, prefix, NativeAPI.getPlayer());
    }

    public static void cleanupWorkbenchField(WorkbenchField field, long playerUid) {
        float[] pos = new float[3];
        NativeAPI.getPosition(NativeAPI.getPlayer(), pos);

        NativePlayer player = new NativePlayer(playerUid);
        for (int i = 0; i < 9; i++) {
            AbstractSlot slot = field.getFieldSlot(i);
            slot.validate();
            if (slot.getId() != 0) {
                player.addItemToInventory(slot.getId(), slot.getCount(), slot.getData(), slot.getExtra(), true);
                slot.set(0, 0, 0, null);
            }
        }
    }



    public static void addRecipesThatContainItem(int id, int data, Collection<WorkbenchRecipe> list) {
        long code = RecipeEntry.getCodeByItem(id, data);

        ArrayList<WorkbenchRecipe> recipes = componentQuickAccess.get(code);
        if (recipes != null) {
            for (WorkbenchRecipe recipe : recipes) {
                list.add(recipe);
            }
        }

        if (data != -1) {
            code = RecipeEntry.getCodeByItem(id, -1);

            recipes = componentQuickAccess.get(code);
            if (recipes != null) {
                for (WorkbenchRecipe recipe : recipes) {
                    list.add(recipe);
                }
            }
        }
    }

    public static class UIRecipeLists {
        int possibleCount;
        List<WorkbenchRecipe> recipes;
    }

    private static void addItemToInvMap(HashMap<Long, Integer> inventory, int id, int count, int data) {
        long code;
        code = RecipeEntry.getCodeByItem(id, data);
        if (!inventory.containsKey(code)) {
            inventory.put(code, count);
        } else {
            inventory.put(code, inventory.get(code) + count);
        }
        code = RecipeEntry.getCodeByItem(id, -1);
        if (!inventory.containsKey(code)) {
            inventory.put(code, count);
        } else {
            inventory.put(code, inventory.get(code) + count);
        }
    }

    public static UIRecipeLists getAvailableRecipesForPlayerInventory(long playerUid, WorkbenchField field, String prefix) {
        HashSet<WorkbenchRecipe> visibleRecipes = new HashSet<>();
        HashMap<Long, Integer> inventory = new HashMap<>();

        NativePlayer player = new NativePlayer(playerUid);
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventorySlot(i);
            if (item.id != 0 && item.count > 0) {
                addItemToInvMap(inventory, item.id, item.count, item.data);
                addRecipesThatContainItem(item.id, item.data, visibleRecipes);
            }
        }

        if (field != null) {
            for (int i = 0; i < 9; i++) {
                AbstractSlot slot = field.getFieldSlot(i);
                if (slot.getId() != 0 && slot.getCount() > 0) {
                    addItemToInvMap(inventory, slot.getId(), slot.getCount(), slot.getData());
                    addRecipesThatContainItem(slot.getId(), slot.getData(), visibleRecipes);
                }
            }
        }

        int possiblePos = 0;
        ArrayList<WorkbenchRecipe> allRecipes = new ArrayList<>();

        for (WorkbenchRecipe recipe : visibleRecipes) {
            if (recipe.isMatchingPrefix(prefix)) {
                if (recipe.isPossibleForInventory(inventory)) {
                    allRecipes.add(possiblePos++, recipe);
                } else {
                    allRecipes.add(recipe);
                }
            }
        }

        UIRecipeLists uiRecipeLists = new UIRecipeLists();
        uiRecipeLists.possibleCount = possiblePos;
        uiRecipeLists.recipes = allRecipes;
        return uiRecipeLists;
    }

}
