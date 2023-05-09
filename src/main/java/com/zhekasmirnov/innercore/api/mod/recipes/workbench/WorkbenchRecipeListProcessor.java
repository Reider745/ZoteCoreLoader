package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.apparatus.adapter.innercore.UserDialog;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.api.container.ItemContainer;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class WorkbenchRecipeListProcessor {
    public interface OnRecipeSelectRequestedListener {
        void onSelectRequested(ItemContainer container, long uid);
    }


    private final ScriptableObject target;
    private OnRecipeSelectRequestedListener listener = null;

    private float minX = 60, minY = 40, maxX = 960;

    private static final String PREFIX = "wbRecipeSlot";
    private int slotsPerLine = 6;

    private int maximumRecipesShowed = 250;

    public WorkbenchRecipeListProcessor(ScriptableObject target) {
        this.target = target;
    }

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

    private void assureSlotAt(ItemContainer container, final int index, boolean darken, long uid) {


        Function click = new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                if (listener != null) {
                    listener.onSelectRequested(container, uid);
                }
                /*if (currentIndex == index) {
                    return null;
                }*/

                /*
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
                            recipe.putIntoTheField(self.targetField);
                            if (selectionListener != null) {
                                selectionListener.onRecipeSelected(recipe);
                            }
                        } else {
                            ICLog.i("ERROR", "cannot select recipe: failed to find binding for index " + index);
                        }
                    }
                });*/

                return null;
            }
        };

        String name = getSlotName(index);
        if (target.has(name, target)) {
            Object _element = target.get(name, target);
            if (_element instanceof ScriptableObject) {
                ScriptableObject element = (ScriptableObject) _element;
                element.put("darken", element, darken);
                element.put("onClick", element, click);
                element.put("onLongClick", element, click);
                return;
            }
        }

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
    private void assureSlotCount(ItemContainer container, int count) {
        for (int i = count; i < lastSlotCount; i++) {
            removeSlotAt(i);
        }
        for (int i = 0; i < count; i++) {
            assureSlotAt(container, i, true, 0);
        }
        lastSlotCount = count;
    }

    public void setListener(OnRecipeSelectRequestedListener listener) {
        this.listener = listener;
    }

    public int processRecipeListPacket(ItemContainer container, JSONObject packet) {
        if (container.isServer) {
            throw new IllegalArgumentException("requires client container");
        }

        JSONArray list = packet.optJSONArray("recipes");
        if (list != null) {
            assureSlotCount(container, list.length());
            for (int index = 0; index < list.length(); index++) {
                JSONObject recipe = list.optJSONObject(index);
                if (recipe != null) {
                    long uid = recipe.optLong("id");
                    JSONObject resultJson = recipe.optJSONObject("result");
                    if (resultJson != null && uid != 0) {
                        ItemStack result = ItemStack.parse(resultJson);
                        if (result != null) {
                            result.id = IdConversionMap.serverToLocal(result.id);
                            container.setSlot(PREFIX + index, result.id, result.count, result.data, result.extra);
                            assureSlotAt(container, index, recipe.optBoolean("d"), uid);
                            continue;
                        }
                    }
                }
                container.setSlot(PREFIX + index, 0, 0, 0);
            }
            return list.length();
        } else {
            assureSlotCount(container, 0);
            return 0;
        }
    }

}
