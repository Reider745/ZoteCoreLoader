package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import android.util.Pair;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.*;

/**
 * Created by zheka on 10.09.2017.
 */

public abstract class WorkbenchRecipe {
    private static long recipeNextUid = 1;

    private static synchronized long nextRecipeUid() {
        return recipeNextUid++;
    }


    protected int id, count, data;
    protected NativeItemInstanceExtra extra;
    protected HashMap<Character, RecipeEntry> entries = new HashMap<>();
    boolean isValid = true;
    boolean isVanilla = false;

    private final long recipeUid = nextRecipeUid();

    public WorkbenchRecipe(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    public WorkbenchRecipe(AddonRecipeParser parser, AddonRecipeParser.ParsedRecipe recipe) {
        JSONObject json = recipe.getContents();
        JSONObject result = json.optJSONObject("result");
        if (result != null) {
            Pair<Integer, Integer> idData = parser.getIdAndDataForItemJson(result, -1);
            if(idData != null){
                this.id = idData.first;
                this.data = idData.second;
            } else {
                Logger.debug("cannot find vanilla numeric ID for " + result);
                isValid = false;
            }
            count = result.optInt("count", 1);
            extra = null;
        }
    }

    public long getRecipeUid() {
        return recipeUid;
    }

    public boolean isValid(){
        return isValid && id != 0;
    }

    public WorkbenchRecipe setVanilla(boolean vanilla) {
        isVanilla = vanilla;
        return this;
    }

    public boolean isVanilla() {
        return isVanilla;
    }

    public void setEntries(HashMap<Character, RecipeEntry> entries) {
        this.entries = entries;
    }

    public RecipeEntry getEntry(char c) {
        return entries.containsKey(c) ? entries.get(c) : RecipeEntry.noentry;
    }

    public ItemInstance getResult() {
        return new ItemInstance(id, count, data, extra);
    }

    public boolean isMatchingResult(int id, int count, int data) {
        return this.id == id && this.count == count && this.data == data;
    }

    public abstract String getRecipeMask();

    public abstract boolean isMatchingField(WorkbenchField field);

    public abstract void addVariants(ArrayList<WorkbenchRecipe> list);

    public abstract RecipeEntry[] getSortedEntries();

    public void addToVanillaWorkbench() { }

    public void putIntoTheField(WorkbenchField field, long player) {
        InventoryPool pool = new InventoryPool(player);

        RecipeEntry[] entries = getSortedEntries();
        for (RecipeEntry entry : entries) {
            if (entry.id != 0) {
                pool.addRecipeEntry(entry);
            }
        }

        pool.pullFromInventory();

        HashMap<Long, ArrayList<Pair<Integer, RecipeEntry>>> entryGroups = new HashMap<>();

        for (RecipeEntry entry : entries) {
            if (entry.id == 0) {
                continue;
            }

            long code = entry.getCode();
            if (entryGroups.containsKey(code)) {
                continue;
            }

            ArrayList<Pair<Integer, RecipeEntry>> group = new ArrayList<>();
            for (int i = 0; i < entries.length; i++) {
                RecipeEntry _entry = entries[i];
                if (entry.id == _entry.id && entry.data == _entry.data) {
                    group.add(new Pair<>(i, entry));
                }
            }

            entryGroups.put(code, group);
        }

        for (int i = 0; i < 9; i++) {
            field.getFieldSlot(i).set(0, 0, 0, null);
        }

        Set<Long> groupCodes = entryGroups.keySet();
        for (Long key : groupCodes) {
            ArrayList<Pair<Integer, RecipeEntry>> group = entryGroups.get(key);
            InventoryPool.PoolEntrySet entrySet = pool.getPoolEntrySet(group.get(0).second); // this element always exists or assertion failed

            if (entrySet == null || entrySet.isEmpty()) {
                for (Pair<Integer, RecipeEntry> entryPair : group) {
                    int index = entryPair.first;
                    RecipeEntry entry = entryPair.second;
                    if (index > 8) {
                        continue;
                    }
                    AbstractSlot slot = field.getFieldSlot(index);
                    slot.set(entry.id, 0, entry.data != -1 ? entry.data : 0, null);
                }
            }
            else {
                ArrayList<AbstractSlot> groupSlots = new ArrayList<>();
                for (int i = 0; i < group.size(); i++) {
                    int index = group.get(i).first;
                    if (index >= 0 && index < 9) {
                        groupSlots.add(field.getFieldSlot(index));
                    }
                }
                entrySet.spreadItems(groupSlots);

                /*entrySet = entrySet.getMajorEntrySet();
                InventoryPool.PoolEntry item = entrySet.getFirstEntry();
                int totalCount = entrySet.getTotalCount();
                int countPerSlot = totalCount / group.size();
                int amountOfAdditionalSlots = totalCount - countPerSlot * group.size();

                for (int i = 0; i < group.size(); i++) {
                    int index = group.get(i).first;
                    if (index > 8) {
                        continue;
                    }

                    int count = countPerSlot + (i < amountOfAdditionalSlots ? 1 : 0);
                    if (count > 1 && NativeItem.getMaxStackForId(item.id, item.data) == 1) {
                        count = 1;
                    }

                    Slot slot = field.getFieldSlot(index);
                    int amount = entrySet.getAmountOfItem(count);
                    slot.set(item.id, amount, item.data, amount != 0 ? item.extra : null);
                }*/
            }
        }
    }

    public ItemInstance provideRecipeForPlayer(WorkbenchField field, long player) {
        ItemInstance result = getResult();
        WorkbenchFieldAPI api = new WorkbenchFieldAPI(field);

        Callback.invokeCallback("CraftRecipePreProvided", this, field, player);

        Function callback = getCallback();
        if (callback != null) {
            Scriptable scope = callback.getParentScope();
            callback.call(Compiler.assureContextForCurrentThread(), scope, scope, new Object[]{
                    Context.javaToJS(api, scope),
                    field.asScriptableField(),
                    result,
                    player
            });
        }
        else {
            for (int i = 0; i < 9; i++) {
                api.decreaseFieldSlot(i);
            }
        }

        Callback.invokeCallback("CraftRecipeProvided", this, field, api.isPrevented(), player);

        return api.isPrevented() ? null : result;
    }

    public ItemInstance provideRecipe(WorkbenchField field) {
        return provideRecipeForPlayer(field, NativeAPI.getPlayer());
    }



    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null || prefix.equals("undefined")) {
            prefix = "";
        }
        this.prefix = prefix;
    }

    public boolean isMatchingPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.equals("undefined")) {
            return this.prefix == null || this.prefix.isEmpty();
        }

        return prefix.contains(this.prefix);
    }

    private Function callback;

    public void setCallback(Function callback) {
        this.callback = callback;
    }

    public Function getCallback() {
        return callback;
    }



    public Collection<RecipeEntry> getEntryCollection() {
        return entries.values();
    }

    public ArrayList<Long> getEntryCodes() {
        ArrayList<Long> codes = new ArrayList<>();
        Collection<RecipeEntry> entries = getEntryCollection();
        for (RecipeEntry entry : entries) {
            codes.add(entry.getCode());
        }

        return codes;
    }

    public boolean isPossibleForInventory(HashMap<Long, Integer> inventory) {
        Collection<RecipeEntry> entries = getEntryCollection();

        for (RecipeEntry entry : entries) {
            if (!inventory.containsKey(entry.getCode())) {
                return false;
            }
        }

        return true;
    }
}
