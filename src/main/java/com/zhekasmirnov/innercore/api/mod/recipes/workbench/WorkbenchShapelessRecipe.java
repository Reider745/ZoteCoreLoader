package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import android.util.Pair;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchShapelessRecipe extends WorkbenchRecipe {
    public WorkbenchShapelessRecipe(int id, int count, int data, NativeItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public WorkbenchShapelessRecipe(AddonRecipeParser parser, AddonRecipeParser.ParsedRecipe recipe) {
        super(parser, recipe);

        JSONObject json = recipe.getContents();
        JSONArray _entries = json.optJSONArray("ingredients");
        if (_entries != null) {
            for(int i = 0; i < _entries.length(); i++){
                JSONObject entry = _entries.optJSONObject(i);
                if (entry != null) {
                    Pair<Integer, Integer> idData = parser.getIdAndDataForItemJson(entry, -1);
                    if(idData != null){
                        entries.put(Integer.toString(i).charAt(0), new RecipeEntry(idData.first, idData.second));
                    } else {
                        Logger.debug("cannot find vanilla numeric ID for " + entry);
                        isValid = false;
                    }
                } else {
                    isValid = false;
                }
            }
        } else {
            isValid = false;
        }
    }

    @Override
    public String getRecipeMask() {
        ArrayList<Character> masks = new ArrayList<>();
        Collection<RecipeEntry> entries = this.entries.values();

        for (RecipeEntry entry : entries) {
            masks.add(entry.getMask());
        }

        Collections.sort(masks);

        String mask = "$$";
        for (Character c : masks) {
            mask += c;
        }

        return mask;
    }

    @Override
    public boolean isMatchingField(WorkbenchField field) {
        int slotCount = field.getWorkbenchFieldSize(); slotCount *= slotCount;
        boolean[] map = new boolean[slotCount];

        Collection<RecipeEntry> entries = getEntryCollection();
        for (RecipeEntry entry : entries) {
            boolean matchFound = false;
            for (int i = 0; i < slotCount; i++) {
                if (!map[i] && entry.isMatching(field.getFieldSlot(i))) {
                    map[i] = true;
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                return false;
            }
        }

        for (int i = 0; i < slotCount; i++) {
            if (!map[i] && field.getFieldSlot(i).getId() != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void addVariants(ArrayList<WorkbenchRecipe> list) {
        list.add(this);
    }

    @Override
    public RecipeEntry[] getSortedEntries() {
        Collection<RecipeEntry> entries = this.entries.values();
        RecipeEntry[] _entries = new RecipeEntry[entries.size()];
        entries.toArray(_entries);
        return _entries;
    }


    @Override
    public void addToVanillaWorkbench() {
        List<Integer> ingredientsList = new ArrayList<>();
        for (RecipeEntry entry : entries.values()) {
            ingredientsList.add(0);
            ingredientsList.add(entry.id);
            ingredientsList.add(entry.data);
        }

        int[] ingredients = new int[ingredientsList.size()];
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = ingredientsList.get(i);
        }
        NativeWorkbench.addShapelessRecipe(new ItemStack(getResult()), ingredients);
    }
}
