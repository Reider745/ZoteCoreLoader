package com.zhekasmirnov.innercore.api.mod.recipes.furnace;

import android.util.Pair;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.RecipeEntry;
import org.json.JSONObject;

/**
 * Created by zheka on 15.09.2017.
 */

public class FurnaceRecipe {
    public final int inId, inData, resId, resData;
    private final boolean isValid;

    public long getInputKey() {
        return RecipeEntry.getCodeByItem(inId, inData);
    }

    public FurnaceRecipe(int inId, int inData, int resId, int resData) {
        this.inId = inId;
        this.inData = inData;
        this.resId = resId;
        this.resData = resData;
        isValid = true;
    }

    public FurnaceRecipe(AddonRecipeParser parser, AddonRecipeParser.ParsedRecipe recipe) {
        JSONObject object = recipe.getContents();
        String inputName = object.optString("input");
        Pair<Integer, Integer> input = parser.getIdAndDataFromItemString(inputName, -1);
        String outputName = object.optString("output");
        Pair<Integer, Integer> output = parser.getIdAndDataFromItemString(outputName, 0);

        if (input == null) {
            Logger.debug("cannot find vanilla numeric ID for " + inputName);
            inId = inData = resId = resData = 0;
            isValid = false;
        } else if (output == null) {
            Logger.debug("cannot find vanilla numeric ID for " + outputName);
            inId = inData = resId = resData = 0;
            isValid = false;
        } else {
            this.inId = input.first;
            this.inData = input.second;
            this.resId = output.first;
            this.resData = output.second;
            isValid = true;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    private String prefix;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isMatchingPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.equals("undefined")) {
            return this.prefix == null || this.prefix.isEmpty();
        }
        return prefix.contains(this.prefix);
    }

    public ItemInstance getResult() {
        return new ItemInstance(resId, 1, resData);
    }
}
