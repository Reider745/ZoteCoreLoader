package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.pointers.ClassPointer;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

import java.lang.ref.WeakReference;

public class ItemExtraDataProvider extends ClassPointer<Item> {
    private boolean hasClear = true;
    public NativeItemInstanceExtra extra;

    public ItemExtraDataProvider(WeakReference<Item> reference) {
        super(reference);
    }

    public ItemExtraDataProvider() {
        super(null);
        hasClear = false;
    }

    public NativeItemInstanceExtra getExtra() {
        return extra;
    }

    public void apply(Item item){
        hasClear = true;
    }

    @Override
    public boolean hasClear() {
        return hasClear && super.hasClear();
    }

    public CompoundTag getCompoundTag(){
        Item item = get();
        return item == null ? null : item.getNamedTag();
    }
}
