package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 12.09.2017.
 */

public class WorkbenchUIHandler {
    private final ScriptableObject target;
    private final Container targetCon;
    private final WorkbenchField targetField;

    public WorkbenchUIHandler(ScriptableObject target, Container targetCon, WorkbenchField targetField) {
        this.target = target;
        this.targetCon = targetCon;
        this.targetField = targetField;
    }

    private float minX = 60, minY = 40, maxX = 960;

    private static final String PREFIX = "wbRecipeSlot";
    private int slotsPerLine = 6;

    private int maximumRecipesShowed = 250;

    private void applySlotPosition(ScriptableObject slot, int index) {
        int ix = index % slotsPerLine;
        int iy = index / slotsPerLine;

        float size = (maxX - minX) / slotsPerLine;
        float x = ix * size + minX;
        float y = iy * size + minY;

        slot.put("x", slot, x);
        slot.put("y", slot, y);
        slot.put("size", slot, size);
    }

    private String getSlotName(int index) {
        return PREFIX + index;
    }

    private int currentIndex = -1;

    private void assureSlotAt(final int index, boolean darken) {
        String name = getSlotName(index);
        if (target.has(name, target)) {
            Object _element = target.get(name, target);
            if (_element instanceof ScriptableObject) {
                ScriptableObject element = (ScriptableObject) _element;
                element.put("darken", element, darken);
                return;
            }
        }

        final WorkbenchUIHandler self = this;
        Function click = new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                if (self.currentIndex == index) {
                    return null;
                }

                MainThreadQueue.serverThread.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        self._deselectCurrentRecipe();
                    }
                });

                MainThreadQueue.serverThread.enqueueDelayed(1, new Runnable(){
                    @Override
                    public void run() {
                        self.currentIndex = index;
                        WorkbenchRecipe recipe = self.getRecipeByIndex(index);
                        if (recipe != null) {
                            recipe.putIntoTheField(self.targetField, NativeAPI.getPlayer());
                            if (selectionListener != null) {
                                selectionListener.onRecipeSelected(recipe);
                            }
                        } else {
                            ICLog.i("ERROR", "cannot select recipe: failed to find binding for index " + index);
                        }
                    }
                });

                return null;
            }
        };

        ScriptableObject slot = ScriptableObjectHelper.createEmpty();
        slot.put("type", slot, "slot");
        slot.put("bitmap", slot, "style:slot");
        slot.put("onClick", slot, click);
        slot.put("onLongClick", slot, click);
        slot.put("_index", slot, index);
        slot.put("visual", slot, true);
        applySlotPosition(slot, index);
        target.put(name, target, slot);
    }

    private void refreshSlotAt(final int index, boolean darken) {
        String name = getSlotName(index);
        if (target.has(name, target)) {
            Object _element = target.get(name, target);
            if (_element instanceof ScriptableObject) {
                ScriptableObject element = (ScriptableObject) _element;
                element.put("darken", element, darken);
                applySlotPosition(element, index);
            }
        }
    }

    private void removeSlotAt(int index) {
        String name = PREFIX + index;
        if (target.has(name, target)) {
            target.put(name, target, null);
        }
    }

    private int lastSlotCount = 0;
    private void assureSlotCount(int count) {
        for (int i = count; i < lastSlotCount; i++) {
            removeSlotAt(i);
        }
        for (int i = 0; i < count; i++) {
            assureSlotAt(i, true);
        }
        lastSlotCount = count;
    }



    private HashMap<Integer, WorkbenchRecipe> recipeByIndex = new HashMap<>();

    private void putRecipeForIndex(int index, WorkbenchRecipe recipe) {
        recipeByIndex.put(index, recipe);
    }

    private WorkbenchRecipe getRecipeByIndex(int index) {
        return recipeByIndex.get(index);
    }

    private String prefix = "";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int refresh() {
        // legacy
        WorkbenchRecipeRegistry.UIRecipeLists list = WorkbenchRecipeRegistry.getAvailableRecipesForPlayerInventory(NativeAPI.getPlayer(), targetField, prefix);

        recipeByIndex.clear();
        assureSlotCount(Math.min(maximumRecipesShowed, list.recipes.size()));
        this.currentIndex = -1;

        int index = 0;
        for (WorkbenchRecipe recipe : list.recipes) {
            putRecipeForIndex(index, recipe);

            boolean darken = index >= list.possibleCount;
            refreshSlotAt(index, darken);
            targetCon.setSlot(getSlotName(index), recipe.id, darken ? 1 : recipe.count, recipe.data);
            index++;

            if (index > maximumRecipesShowed) {
                break;
            }
        }

        return index;
    }

    public void refreshAsync(){
        MainThreadQueue.serverThread.enqueueDelayed(1, new Runnable(){
            @Override
            public void run() {
                if(refreshListener != null){
                    refreshListener.onRefreshStarted();
                }

                int index = refresh();
                
                if(refreshListener != null){
                    refreshListener.onRefreshCompleted(index);
                }
            }
        });
    }



    private ISelectionListener selectionListener;
    private IRefreshListener refreshListener;

    public interface ISelectionListener {
        void onRecipeSelected(WorkbenchRecipe recipe);
    }

    public interface IRefreshListener {
        void onRefreshCompleted(int count);
        void onRefreshStarted();
    }

    public void setOnSelectionListener(ISelectionListener listener) {
        selectionListener = listener;
    }

    public void setOnRefreshListener(IRefreshListener listener){
        refreshListener = listener;
    }

    public void deselectCurrentRecipe() {
        MainThreadQueue.serverThread.enqueue(new Runnable() {
            @Override
            public void run() {
                _deselectCurrentRecipe();
            }
        });
    }

    private void _deselectCurrentRecipe() {
        currentIndex = -1;
        if (targetField != null) {
            WorkbenchRecipeRegistry.cleanupWorkbenchField(targetField, NativeAPI.getPlayer());
        }
    }

    public void setMaximumRecipesToShow(int maximumRecipesShowed) {
        this.maximumRecipesShowed = maximumRecipesShowed;
    }
}
