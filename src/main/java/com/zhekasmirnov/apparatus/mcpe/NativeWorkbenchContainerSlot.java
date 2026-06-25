package com.zhekasmirnov.apparatus.mcpe;

import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import org.innercore.icstd.inventory.ItemInstance;

public class NativeWorkbenchContainerSlot implements AbstractSlot {
    public int id, count, data;
    public ItemInstanceExtra extra;

    public NativeWorkbenchContainerSlot(int id, int count, int data, ItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    NativeWorkbenchContainerSlot() {
        this(0, 0, 0, null);
    }

    public NativeWorkbenchContainerSlot(ItemInstance item) {
        this(item.id, item.count, item.data, item.extra);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getData() {
        return data;
    }

    @Override
    public ItemInstanceExtra getExtra() {
        return extra;
    }

    public long getExtraPtr() {
        return ItemInstanceExtra.getValueOrNullPtr(extra);
    }

    @Override
    public void set(int id, int count, int data, ItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    public void set(ItemInstance item) {
        set(item.id, item.count, item.data, item.extra);
    }

    @Override
    public void validate() {
        if (this.id == 0 || this.count <= 0) {
            this.id = this.count = this.data = 0;
            this.extra = null;
        }
    }
}
