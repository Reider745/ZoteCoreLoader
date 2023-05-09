package com.zhekasmirnov.apparatus.mcpe;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.api.container.ItemContainerSlot;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class NativeWorkbenchContainerSlot extends ItemContainerSlot {
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public NativeWorkbenchContainerSlot(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    NativeWorkbenchContainerSlot() {
        this(0, 0, 0, null);
    }

    public NativeWorkbenchContainerSlot(ItemStack item) {
        this(item.id, item.count, item.data, item.extra);
    }

    public NativeWorkbenchContainerSlot(NativeItemInstance item) {
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
    public NativeItemInstanceExtra getExtra() {
        return extra;
    }

    public long getExtraPtr() {
        return NativeItemInstanceExtra.getValueOrNullPtr(extra);
    }

    @Override
    public void set(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    public void set(ItemStack item) {
        set(item.id, item.count, item.data, item.extra);
    }

    public void set(NativeItemInstance item) {
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
