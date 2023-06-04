package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import android.util.Pair;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.api.container.ItemContainerSlot;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchShapedRecipe extends WorkbenchRecipe {
    private RecipeEntry[][] pattern;

    public WorkbenchShapedRecipe(int id, int count, int data, NativeItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public WorkbenchShapedRecipe(AddonRecipeParser parser, AddonRecipeParser.ParsedRecipe recipe) {
        super(parser, recipe);

        JSONObject json = recipe.getContents();
        JSONObject _entries = json.optJSONObject("key");
        if (_entries != null) {
            JSONArray names = _entries.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    try {
                        String name = names.optString(i);
                        JSONObject entry = _entries.getJSONObject(name);
                        if (entry != null) {
                            Pair<Integer, Integer> idData = parser.getIdAndDataForItemJson(entry, -1);
                            if(idData != null){
                                entries.put(name.charAt(0), new RecipeEntry(idData.first, idData.second));
                            } else {
                                Logger.debug("cannot find vanilla numeric ID for " + entry);
                                isValid = false;
                            }
                        } else {
                            isValid = false;
                        }
                    } catch (Exception e) {
                        ICLog.e("RECIPES", "failed to parse json for recipe json=" + json, e);
                        isValid = false;
                    }
                }
            } else {
                isValid = false;
            }
        } else {
            isValid = false;
        }

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
            isValid = false;
        }
    }

    public void setPattern(String[] pattern) {
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

    public void setPattern(RecipeEntry[][] pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getRecipeMask() {
        String mask = "";
        for (RecipeEntry[] entries : pattern) {
            for (RecipeEntry entry : entries) {
                mask += entry.getMask();
            }
        }
        return mask;
    }

    @Override
    public boolean isMatchingField(WorkbenchField field) {
        for (int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length; x++) {
                AbstractSlot slot = field.getFieldSlot(x, y);
                if (!pattern[y][x].isMatching(slot)) {
                    return false;
                }
            }
        }

        return true;
    }

    private WorkbenchShapedRecipe generateVariantWithOffset(int offsetX, int offsetY) {
        RecipeEntry[][] newPattern = new RecipeEntry[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int _x = x - offsetX;
                int _y = y - offsetY;
                if (_x >= 0 && _y >= 0 && _y < pattern.length && _x < pattern[0].length) {
                    newPattern[y][x] = pattern[_y][_x];
                }
                else {
                    newPattern[y][x] = RecipeEntry.noentry;
                }
            }
        }

        WorkbenchShapedRecipe recipe = new WorkbenchShapedRecipe(id, count, data, extra);
        recipe.setEntries(entries);
        recipe.setPattern(newPattern);
        recipe.setPrefix(getPrefix());
        recipe.setCallback(getCallback());
        return recipe;
    }

    @Override
    public void addVariants(ArrayList<WorkbenchRecipe> list) {
        if (pattern.length == 3 && pattern[0].length == 3) {
            list.add(this);
        }
        else {
            for (int y = 0; y < 4 - pattern.length; y++) {
                for (int x = 0; x < 4 - pattern[0].length; x++) {
                    list.add(generateVariantWithOffset(x, y));
                }
            }
        }
    }

    @Override
    public RecipeEntry[] getSortedEntries() {
        RecipeEntry[] entries = new RecipeEntry[9];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (y < pattern.length && x < pattern[0].length) {
                    entries[x + y * 3] = pattern[y][x];
                }
                else {
                    entries[x + y * 3] = RecipeEntry.noentry;
                }
            }
        }

        return entries;
    }

    @Override
    public void addToVanillaWorkbench() {
        int space = (int) ' ';
        int ch = (int) 'A';
        List<Integer> ingredientsList = new ArrayList<>();
        String[] patternStrings = new String[pattern.length];
        for (int y = 0; y < pattern.length; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < pattern[y].length; x++) {
                RecipeEntry entry = pattern[y][x];
                if (entry == RecipeEntry.noentry) {
                    line.append((char) space);
                } else {
                    line.append((char) ch);
                    ingredientsList.add(ch);
                    ingredientsList.add(entry.id);
                    ingredientsList.add(entry.data);
                    ch++;
                }
            }
            patternStrings[y] = line.toString();
        }

        int[] ingredients = new int[ingredientsList.size()];
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = ingredientsList.get(i);
        }
        NativeWorkbench.addShapedRecipe(new ItemStack(getResult()), patternStrings, ingredients);
    }
}
