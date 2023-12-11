package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public interface AbstractSlot {
    int getId();

    int getCount();

    int getData();

    NativeItemInstanceExtra getExtra();

    void set(int id, int count, int data, NativeItemInstanceExtra extra);

    void validate();
}
