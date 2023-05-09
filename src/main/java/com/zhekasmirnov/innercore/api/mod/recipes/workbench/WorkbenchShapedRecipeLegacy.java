package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchShapedRecipeLegacy extends WorkbenchRecipeLegacy {
    private RecipeEntry[][] pattern;

    public WorkbenchShapedRecipeLegacy(JSONObject json) {
        super(json);

        workbenchRecipe = new WorkbenchShapedRecipe(id, count, data, null);

        JSONArray _pattern = json.optJSONArray("pattern");
        if (_pattern != null) {
            String[] pattern = new String[_pattern.length()];
            for (int i = 0; i < _pattern.length(); i++) {
                pattern[i] = _pattern.optString(i);
            }
            setPattern(pattern);
        }
        else {
            setPattern(new String[0]);
        }

        ((WorkbenchShapedRecipe) workbenchRecipe).setPattern(pattern);
    }

    void setPattern(String[] pattern) {
        int width = 0;
        int height = pattern.length;
        for (String line : pattern) {
            if (line.length() > width) {
                width = line.length();
            }
        }

        if (height == 0) {
            throw new IllegalArgumentException("invalid recipe pattern: empty array (height=0)");
        }

        if (width == 0) {
            throw new IllegalArgumentException("invalid recipe pattern: all lines are empty (width=0)");
        }

        this.pattern = new RecipeEntry[height][width];
        for (int y = 0; y < height; y++) {
            String line = pattern[y];
            for (int x = 0; x < width; x++) {
                if (x >= line.length()) {
                    this.pattern[y][x] = RecipeEntry.noentry;
                }
                else {
                    this.pattern[y][x] = getEntry(line.charAt(x));
                }
            }
        }
    }

}
