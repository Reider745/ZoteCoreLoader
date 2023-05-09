package com.zhekasmirnov.apparatus.mcpe;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.api.container.ItemContainerSlot;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchField;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipe;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeRegistry;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NativeWorkbenchContainer implements WorkbenchField {
    public final long pointer;
    public final int size;
    public final long owningPlayer;

    private final NativeWorkbenchContainerSlot[] slots;
    private final Map<Integer, NativeWorkbenchContainerSlot> slotPlaceholders = new HashMap<>();

    public NativeWorkbenchContainer(long pointer, int size, long owningPlayer) {
        this.pointer = pointer;
        this.size = size;
        this.slots = new NativeWorkbenchContainerSlot[size * size];
        this.owningPlayer = owningPlayer;
        this.updateSlots();
    }

    // basic slot logic

    ItemStack getSlot(int i) {
        NativeWorkbenchContainerSlot slot = i >= 0 && i < slots.length ? this.slots[i] : null;
        return slot != null ? new ItemStack(slot.id, slot.count, slot.data, slot.extra) : new ItemStack();
    }

    void setSlot(int i, ItemStack item) {
        NativeWorkbenchContainerSlot slot = i >= 0 && i < slots.length ? this.slots[i] : null;
        if (slot != null) {
            slot.set(item);
        }
    }

    public void updateSlots() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new NativeWorkbenchContainerSlot(new NativeItemInstance(getSlot(pointer, i)));
        }
    }

    public void apply() {
        for (int i = 0; i < slots.length; i++) {
            NativeWorkbenchContainerSlot slot = slots[i];
            setSlot(pointer, i, slot.id, slot.count, slot.data, slot.getExtraPtr());
        }
        apply(pointer);
    }

    @Override
    public ItemContainerSlot getFieldSlot(int slot) {
        return slot >= 0 && slot < slots.length ? this.slots[slot] : Java8BackComp.getOrDefault(slotPlaceholders, slot, new NativeWorkbenchContainerSlot());
    }

    @Override
    public ItemContainerSlot getFieldSlot(int x, int y) {
        if (x >= 0 && y >= 0 && x < size && y < size) {
            return getFieldSlot(y * size + x);
        } else {
            return null;
        }
    }

    @Override
    public Scriptable asScriptableField() {
        Object[] slots = new Object[this.slots.length];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = this.slots[i];
        }
        return ScriptableObjectHelper.createArray(slots);
    }

    @Override
    public int getWorkbenchFieldSize() {
        return size;
    }


    // container-like methods for using as a container in JS

    private static final String containerSlotPrefix = "slot";

    public NativeWorkbenchContainerSlot getSlot(String name) {
        if (name.startsWith(containerSlotPrefix)) {
            int index = Integer.parseInt(name.substring(containerSlotPrefix.length()));
            if (index >= 0 && index < size * size) {
                return (NativeWorkbenchContainerSlot) getFieldSlot(index);
            }
        }
        return null;
    }

    public void setSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra) {
        NativeWorkbenchContainerSlot slot = getSlot(name);
        if (slot != null) {
            slot.set(id, count, data, extra);
        }
    }

    public void setSlot(String name, int id, int count, int data) {
        setSlot(name, id, count, data, null);
    }


    // helper methods

    public long getPlayer() {
        return owningPlayer;
    }

    public WorkbenchRecipe getRecipe(String prefix) {
        return WorkbenchRecipeRegistry.getRecipeFromField(this, prefix);
    }

    public WorkbenchRecipe getRecipe() {
        return getRecipe("");
    }

    public ItemStack getPreviewResult(String prefix) {
        WorkbenchRecipe recipe = getRecipe(prefix);
        if (recipe != null) {
            ItemInstance result = recipe.getResult();
            if (result != null) {
                return new ItemStack(result);
            }
        }
        return null;
    }

    public ItemStack getPreviewResult() {
        return getPreviewResult("");
    }

    public ItemStack provideRecipe(String prefix) {
        ItemInstance result = WorkbenchRecipeRegistry.provideRecipeForPlayer(this, prefix, owningPlayer);
        return result != null ? new ItemStack(result) : null;
    }

    public ItemStack provideRecipe() {
        return provideRecipe("");
    }


    // native impl

    private static native long getSlot(long pointer, int slot);
    private static native void setSlot(long pointer, int slot, int id, int count, int data, long extra);
    private static native void apply(long pointer);
}
