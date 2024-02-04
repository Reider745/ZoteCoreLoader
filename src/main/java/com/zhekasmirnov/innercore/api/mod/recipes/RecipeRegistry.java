package com.zhekasmirnov.innercore.api.mod.recipes;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.recipes.furnace.FurnaceRecipe;
import com.zhekasmirnov.innercore.api.mod.recipes.furnace.FurnaceRecipeRegistry;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.*;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by zheka on 08.09.2017.
 */

public class RecipeRegistry {
    static final String LOGGER_TAG = "INNERCORE-RECIPES";

    private static String tryGetReadableStringFromResult(ItemInstance result) {
        int id = result.getId();
        String name = IDRegistry.getNameByID(id);
        name = name != null ? name : Integer.toString(id);
        return result.getCount() + "x " + name + "#" + result.getData() + (result.getExtraValue() != null ? "[+extra]" : "");
    }

    private static void reportComponentArrayError(Object[] components, String errorPrefix) {
        String str = "[";
        for (Object obj : components) {
            if (!str.endsWith("[")) {
                str += ", ";
            }
            if (obj instanceof CharSequence) {
                str += "'";
            }
            str += obj;
            if (obj instanceof CharSequence) {
                str += "'";
            }
        }

        throw new IllegalArgumentException(errorPrefix + "recipe component array must be formatted like this: 'letter', id, data, ... repeat, found: " + str + "]");
    }

    private static HashMap<Character, RecipeEntry> extractEntries(NativeArray data, WorkbenchRecipe recipe) {
        String errorPrefix = "In shaped recipe for " + tryGetReadableStringFromResult(recipe.getResult()) + ": ";

        HashMap<Character, RecipeEntry> entries = new HashMap<>();
        Object[] components = data.toArray();
        if (components.length % 3 != 0) {
            reportComponentArrayError(components, errorPrefix);
        }

        for (int i = 0; i < components.length / 3; i++) {
            try {
                Object _letter = components[i * 3];
                Object _id = components[i * 3 + 1];
                Object _data = components[i * 3 + 2];

                if (_letter != null && _letter instanceof CharSequence) {
                    _letter = ((CharSequence) _letter).charAt(0);
                }
                if (_letter instanceof Character) {
                    if (_id == null || !(_id instanceof Number)) {
                        ICLog.i("ERROR", errorPrefix + "recipe entry id is invalid: " + _id);
                        return null;
                    }
                    if (_data == null || !(_data instanceof Number)) {
                        ICLog.i("ERROR", errorPrefix + "recipe entry data is invalid: " + _data);
                        return null;
                    }
                    if (((Number)_id).intValue() == 0) {
                        ICLog.i("ERROR", errorPrefix + "recipe entry id is invalid: " + _id);
                        return null;
                    }

                    entries.put((Character) _letter, new RecipeEntry(((Number)_id).intValue(), ((Number)_data).intValue()));
                }
                else {
                    ICLog.i("ERROR", errorPrefix + "recipe entry character is invalid: " + _letter);
                }
            } catch (Exception e) {
                ICLog.i("ERROR", errorPrefix + "recipe format error: " + e);
                reportComponentArrayError(components, errorPrefix);
            }
        }

        return entries;
    }

    private static HashMap<Character, RecipeEntry> extractShapelessEntries(NativeArray data, WorkbenchRecipe recipe) {
        String errorPrefix = "In shapeless recipe for " + tryGetReadableStringFromResult(recipe.getResult()) + ": ";

        HashMap<Character, RecipeEntry> entries = new HashMap<>();
        Object[] components = data.toArray();

        int index = 0;
        for (Object component : components) {
            try {
                ScriptableObject obj = (ScriptableObject) component;
                int id = ScriptableObjectHelper.getIntProperty(obj, "id", 0);
                int aux = ScriptableObjectHelper.getIntProperty(obj, "data", 0);

                if (id == 0) {
                    ICLog.i("ERROR", errorPrefix + "recipe entry id is invalid: " + id);
                    return null;
                }

                entries.put((char) index++, new RecipeEntry(id, aux));
            } catch (Exception e) {
                ICLog.i("ERROR", errorPrefix + "recipe format error: " + e);
                reportComponentArrayError(components, errorPrefix);
            }
        }

        return entries;
    }

    @JSStaticFunction
    public static WorkbenchShapedRecipe addShapedGeneric(ScriptableObject result, NativeArray mask, NativeArray data, Function func, String prefix, boolean vanilla) {
        int id = ScriptableObjectHelper.getIntProperty(result, "id", 0);
        int count = ScriptableObjectHelper.getIntProperty(result, "count", 1);
        int aux = ScriptableObjectHelper.getIntProperty(result, "data", 0);
        NativeItemInstanceExtra extra = NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(result, "extra", 0));
        if (id == 0) {
            throw new IllegalArgumentException("recipe id is 0");
        }

        WorkbenchShapedRecipe recipe = new WorkbenchShapedRecipe(id, count, aux, extra);
        recipe.setCallback(func);
        recipe.setPrefix(prefix);

        HashMap<Character, RecipeEntry> entries = extractEntries(data, recipe);
        if (entries == null) {
            return null;
        }
        recipe.setEntries(entries);

        Object[] _pattern = mask.toArray();
        String[] pattern = new String[(int) mask.getLength()];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = "" + _pattern[i];
        }
        recipe.setPattern(pattern);

        recipe.setVanilla(vanilla);
        WorkbenchRecipeRegistry.addRecipe(recipe);

        return recipe;
    }

    @JSStaticFunction
    public static WorkbenchShapedRecipe addShaped(ScriptableObject result, NativeArray mask, NativeArray data, Function func, String prefix) {
        return addShapedGeneric(result, mask, data, func, prefix, false);
    }

    @JSStaticFunction
    public static WorkbenchShapedRecipe addShapedVanilla(ScriptableObject result, NativeArray mask, NativeArray data, Function func, String prefix) {
        return addShapedGeneric(result, mask, data, func, prefix, true);
    }

    @JSStaticFunction
    public static WorkbenchShapedRecipe addShaped2(int id, int count, int aux, NativeArray mask, NativeArray data, Function func, String prefix) {
        return addShapedGeneric(new ItemInstance(id, count, aux), mask, data, func, prefix, false);
    }

    @JSStaticFunction
    public static WorkbenchShapelessRecipe addShapelessGeneric(ScriptableObject result, NativeArray data, Function func, String prefix, boolean vanilla) {
        int id = ScriptableObjectHelper.getIntProperty(result, "id", 0);
        int count = ScriptableObjectHelper.getIntProperty(result, "count", 1);
        int aux = ScriptableObjectHelper.getIntProperty(result, "data", 0);
        NativeItemInstanceExtra extra = NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(result, "extra", 0));
        if (id == 0) {
            throw new IllegalArgumentException("recipe id is 0");
        }

        WorkbenchShapelessRecipe recipe = new WorkbenchShapelessRecipe(id, count, aux, extra);
        recipe.setCallback(func);
        recipe.setPrefix(prefix);

        HashMap<Character, RecipeEntry> entries = extractShapelessEntries(data, recipe);
        if (entries == null) {
            return null;
        }
        recipe.setEntries(entries);

        recipe.setVanilla(vanilla);
        WorkbenchRecipeRegistry.addRecipe(recipe);

        return recipe;
    }

    @JSStaticFunction
    public static WorkbenchShapelessRecipe addShapeless(ScriptableObject result, NativeArray data, Function func, String prefix) {
        return addShapelessGeneric(result, data, func, prefix, false);
    }

    @JSStaticFunction
    public static WorkbenchShapelessRecipe addShapelessVanilla(ScriptableObject result, NativeArray data, Function func, String prefix) {
        return addShapelessGeneric(result, data, func, prefix, true);
    }

    @JSStaticFunction
    public static WorkbenchShapelessRecipe addShapeless2(int id, int count, int aux, NativeArray data, Function func, String prefix) {
        return addShapelessGeneric(new ItemInstance(id, count, aux), data, func, prefix, false);
    }

    @JSStaticFunction
    public static void deleteRecipe(ScriptableObject item) {
        int id = ScriptableObjectHelper.getIntProperty(item, "id", 0);
        int count = ScriptableObjectHelper.getIntProperty(item, "count", 1);
        int aux = ScriptableObjectHelper.getIntProperty(item, "data", 0);
        if (id == 0) {
            throw new IllegalArgumentException("recipe id is 0");
        }

        removeWorkbenchRecipe(id, count, aux);
    }

    @JSStaticFunction
    public static void removeWorkbenchRecipe(int id, int count, int aux) {
        WorkbenchRecipeRegistry.removeRecipeByResult(id, count, aux);
    }

    @JSStaticFunction
    public static Collection<WorkbenchRecipe> getAllWorkbenchRecipes() {
        return WorkbenchRecipeRegistry.getAllRecipes();
    }

    @JSStaticFunction
    public static Collection<WorkbenchRecipe> getWorkbenchRecipesByResult(int id, int count, int aux) {
        return WorkbenchRecipeRegistry.getRecipesByResult(id, count, aux);
    }

    @JSStaticFunction
    public static Collection<WorkbenchRecipe> getWorkbenchRecipesByIngredient(int id, int aux) {
        return WorkbenchRecipeRegistry.getRecipesByIngredient(id, aux);
    }

    @JSStaticFunction
    public static WorkbenchRecipe getRecipeByUid(Object uid) {
        return WorkbenchRecipeRegistry.getRecipeByUid((Long) Context.jsToJava(uid, Long.class));
    }

    @JSStaticFunction
    public static WorkbenchRecipe getRecipeByField(Object field, String prefix) {
        return WorkbenchRecipeRegistry.getRecipeFromField((WorkbenchField) Context.jsToJava(field, WorkbenchField.class), prefix);
    }

    @JSStaticFunction
    public static ItemInstance getRecipeResult(Object field, String prefix) {
        return WorkbenchRecipeRegistry.getRecipeResult((WorkbenchField) Context.jsToJava(field, WorkbenchField.class), prefix);
    }

    @JSStaticFunction
    public static ItemInstance provideRecipe(Object field, String prefix) {
        return WorkbenchRecipeRegistry.provideRecipe((WorkbenchField) Context.jsToJava(field, WorkbenchField.class), prefix);
    }

    @JSStaticFunction
    public static ItemInstance provideRecipeForPlayer(Object field, String prefix, Object player) {
        return WorkbenchRecipeRegistry.provideRecipeForPlayer((WorkbenchField) Context.jsToJava(field, WorkbenchField.class), prefix, (Long) Context.jsToJava(player, Long.class));
    }



    private static int convertToInt(Object o) {
        try {
            return (int) Context.jsToJava(o, int.class);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String convertToString(Object o) {
        try {
            return (String) Context.jsToJava(o, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    @JSStaticFunction
    public static void addFurnace(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        int iId, iData, rId, rData;
        String prefix = null;

        if (arg4 instanceof Number) {
            iId = convertToInt(arg1);
            iData = convertToInt(arg2);
            rId = convertToInt(arg3);
            rData = convertToInt(arg4);
            prefix = convertToString(arg5);
        }
        else if (arg4 instanceof String || arg4 instanceof Undefined || arg4 == null) {
            iId = convertToInt(arg1);
            iData = -1;
            rId = convertToInt(arg2);
            rData = convertToInt(arg3);
            prefix = convertToString(arg4);
        }
        else {
            throw new IllegalArgumentException("illegal parameters of Recipes.addFurnace: " + arg1 + " " + arg2 + " " + arg3 + " " + arg4 + " " + arg5);
        }

        if (iId != 0 && rId != 0) {
            FurnaceRecipeRegistry.addFurnaceRecipe(iId, iData, rId, rData, prefix);
        }
        else {
            throw new IllegalArgumentException("illegal parameters of Recipes.addFurnace: " + arg1 + " " + arg2 + " " + arg3 + " " + arg4 + " " + arg5);
        }
    }

    @JSStaticFunction
    public static void removeFurnaceRecipe(int id, int data) {
        FurnaceRecipeRegistry.removeFurnaceRecipe(id, data);
    }

    @JSStaticFunction
    public static void addFurnaceFuel(int id, int data, int time) {
        FurnaceRecipeRegistry.addFuel(id, data, time);
    }

    @JSStaticFunction
    public static void removeFurnaceFuel(int id, int data) {
        FurnaceRecipeRegistry.removeFuel(id, data);
    }

    @JSStaticFunction
    public static ScriptableObject getFurnaceRecipeResult(int id, int data, String prefix) {
        FurnaceRecipe recipe = FurnaceRecipeRegistry.getRecipe(id, data, prefix);
        return recipe != null ? recipe.getResult() : null;
    }

    @JSStaticFunction
    public static int getFuelBurnDuration(int id, int data) {
        return FurnaceRecipeRegistry.getBurnDuration(id, data);
    }

    @JSStaticFunction
    public static Collection<FurnaceRecipe> getFurnaceRecipesByResult(int id, int aux, String prefix) {
        return FurnaceRecipeRegistry.getFurnaceRecipeByResult(id, aux, prefix);
    }

    @JSStaticFunction
    public static Collection<FurnaceRecipe> getAllFurnaceRecipes() {
        return FurnaceRecipeRegistry.getAllRecipes();
    }



    public static class WorkbenchUIHandler extends com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchUIHandler {
        public WorkbenchUIHandler(ScriptableObject target, Container targetCon, WorkbenchField field) {
            super(target, targetCon, field);
        }

        @JSStaticFunction
        @Deprecated
        public static void __placeholder() {

        }
    }
}
