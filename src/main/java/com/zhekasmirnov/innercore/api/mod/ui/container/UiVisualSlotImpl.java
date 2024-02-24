package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

@Deprecated(since = "Zote")
public interface UiVisualSlotImpl {
    int getId();

    int getCount();

    int getData();

    NativeItemInstanceExtra getExtra();
}
