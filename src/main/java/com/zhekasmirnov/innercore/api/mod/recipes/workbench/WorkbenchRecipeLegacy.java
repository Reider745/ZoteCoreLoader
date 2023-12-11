package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.innercore.api.log.ICLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by zheka on 10.09.2017.
 */

public abstract class WorkbenchRecipeLegacy {
    protected WorkbenchRecipe workbenchRecipe;
    protected int id, count, data;

    protected HashMap<Character, RecipeEntry> entries = new HashMap<>();

    WorkbenchRecipeLegacy(JSONObject json) {
        JSONArray result = json.optJSONArray("result");
        if (result != null) {
            id = result.optInt(0);
            count = result.optInt(1);
            data = result.optInt(2);
        }

        JSONObject _entries = json.optJSONObject("components");
        if (_entries != null) {
            JSONArray names = _entries.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    try {
                        String name = names.optString(i);
                        JSONArray entry = _entries.getJSONArray(name);
                        if (entry != null) {
                            entries.put(name.charAt(0), new RecipeEntry(entry.optInt(0), entry.optInt(1)));
                        }
                    } catch (Exception e) {
                        ICLog.e("RECIPES", "failed to parse json for recipe json=" + json, e);
                    }
                }
            }
        }

    }

    RecipeEntry getEntry(char c) {
        return entries.containsKey(c) ? entries.get(c) : RecipeEntry.noentry;
    }

    public WorkbenchRecipe getRecipe(){
        workbenchRecipe.setEntries(entries);
        return workbenchRecipe;
    }
}
