package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.apparatus.api.container.ItemContainerSlot;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;

/**
 * Created by zheka on 10.09.2017.
 */

public class RecipeEntry {
    public static final RecipeEntry noentry = new RecipeEntry(0, 0);

    public final int id, data;

    public RecipeEntry(int id, int data) {
        this.id = id;
        this.data = data;
    }

    public RecipeEntry(ItemContainerSlot slot) {
        this(slot.getId(), slot.getData());
    }

    public Character getMask() {
        return (char) id;
    }

    public static long getCodeByItem(int id, int data) {
        return (data << 16) | id;
    }

    public long getCode() {
        return getCodeByItem(id, data);
    }

    public boolean isMatching(AbstractSlot slot) {
        if (slot == null) {
            return id == 0;
        }
        return slot.getId() == id && (data == -1 || slot.getData() == -1 || slot.getData() == data);
    }

    public boolean isMatching(RecipeEntry entry) {
        return entry.id == id && (data == -1 || entry.data == -1 || entry.data == data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecipeEntry) {
            return isMatching((RecipeEntry) obj);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (int) getCode();
    }
}
