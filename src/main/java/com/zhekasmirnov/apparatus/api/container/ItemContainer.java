package com.zhekasmirnov.apparatus.api.container;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class ItemContainer {
    public boolean isServer = true;
    public void markSlotDirty(String name){

    }

    public boolean isGlobalSlotSavingEnabled(){
        return true;
    }

    public void setSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra){

    }

    public void setSlot(String name, int id, int count, int data){

    }
}
