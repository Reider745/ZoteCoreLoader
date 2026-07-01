package com.zhekasmirnov.innercore.api.mod.ui.container;

import org.innercore.icstd.inventory.ItemInstanceExtra;

public interface AbstractSlot {
    int getId();

    int getCount();

    int getData();

    ItemInstanceExtra getExtra();

    void set(int id, int count, int data, ItemInstanceExtra extra);

    void validate();
}
