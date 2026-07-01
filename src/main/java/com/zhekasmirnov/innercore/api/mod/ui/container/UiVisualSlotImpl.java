package com.zhekasmirnov.innercore.api.mod.ui.container;

import org.innercore.icstd.inventory.ItemInstanceExtra;

@Deprecated(since = "Zote")
public interface UiVisualSlotImpl {
    int getId();

    int getCount();

    int getData();

    ItemInstanceExtra getExtra();
}
