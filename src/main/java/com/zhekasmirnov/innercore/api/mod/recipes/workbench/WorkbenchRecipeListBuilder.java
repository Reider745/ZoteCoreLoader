package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import android.util.Pair;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.api.container.ItemContainer;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WorkbenchRecipeListBuilder {
    public interface RecipeComparator {
        int compare(WorkbenchRecipe recipe1, WorkbenchRecipe recipe2);
    }


    private final long player;
    private final ItemContainer container;
    private String craftPrefix = "";

    public WorkbenchRecipeListBuilder(long player, ItemContainer container) {
        this.player = player;
        this.container = container;
    }

    public void setCraftPrefix(String craftPrefix) {
        this.craftPrefix = craftPrefix;
    }

    public JSONObject buildAvailableRecipesPacket(RecipeComparator comparator) {
        WorkbenchRecipeRegistry.UIRecipeLists list = WorkbenchRecipeRegistry.getAvailableRecipesForPlayerInventory(player, (WorkbenchField) container, craftPrefix);

        int index = 0;
        List<Pair<WorkbenchRecipe, Boolean>> recipeVisibility = new ArrayList<>();
        for (WorkbenchRecipe recipe : list.recipes) {
            recipeVisibility.add(new Pair<>(recipe, index++ < list.possibleCount));
        }

        //noinspection Java8ListSort
        Collections.sort(recipeVisibility, (pair1, pair2) -> {
            int booleanCmp = Boolean.compare(pair1.second, pair2.second);
            if (booleanCmp != 0) {
                return -booleanCmp;
            }
            return comparator.compare(pair1.first, pair2.first);
        });

        JSONObject packet = new JSONObject();
        try {
            JSONArray jsonList = new JSONArray();
            packet.put("recipes", jsonList);

            for (Pair<WorkbenchRecipe, Boolean> pair : recipeVisibility) {
                ItemStack result = ItemStack.parse(pair.first.getResult());
                JSONObject recipeJson = new JSONObject();
                recipeJson.put("id", pair.first.getRecipeUid());
                recipeJson.put("result", result.asJson());
                recipeJson.put("d", !pair.second);
                jsonList.put(recipeJson);
            }
        } catch (JSONException ignore) { }
        return packet;
    }

    public static void selectRecipe(ItemContainer container, WorkbenchRecipe recipe, long player) {
        deselectRecipe(container, player);
        container.runTransaction(container0 -> {
            recipe.putIntoTheField(container, player);
            container.markAllSlotsDirty();
            container.sendChanges();
        });
    }

    public static void deselectRecipe(ItemContainer container, long player) {
        container.runTransaction(container0 -> {
            WorkbenchRecipeRegistry.cleanupWorkbenchField(container, player);
        });
    }

}
