package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeTileEntity {
    public static final boolean isContainer = false;

    private long pointer;

    protected int type;
    protected int size;
    protected int x, y, z;

    public NativeTileEntity(long ptr) {
        this.pointer = ptr;
        this.type = getType(ptr);
        this.size = getSize(ptr);
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public NativeItemInstance getSlot(int slot) {
        if (slot < 0 || slot >= size)
            return null;
        Item itemPtr = getSlot(pointer, slot);
        if (itemPtr == null)
            return null;
        return new NativeItemInstance(itemPtr);
    }

    public void setSlot(int slot, int id, int count, int data, Object extra) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(pointer, slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
    }

    public void setSlot(int slot, int id, int count, int data) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(pointer, slot, id, count, data, 0);
    }

    public void setSlot(int slot, NativeItemInstance item) {
        if (slot < 0 || slot >= size)
            return;
        setSlot2(pointer, slot, item.item);
    }

    public NativeCompoundTag getCompoundTag() {
        return new NativeCompoundTag(getCompoundTag(pointer));
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        if (tag != null) {
            setCompoundTag(pointer, tag.pointer);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        nativeFinalize(pointer);
    }

    public static NativeTileEntity getTileEntity(int x, int y, int z) {
        long ptr = getInWorld(x, y, z);
        if (ptr == 0) {
            return null;
        }
        else {
            return new NativeTileEntity(ptr);
        }
    }

    /*
     * native part
     */

    public static native long getInWorld(int x, int y, int z);

    public static native void nativeFinalize(long pointer);

    public static native int getType(long pointer);

    public static native int getSize(long pointer);

    public static native Item getSlot(long pointer, int slot);

    public static native void setSlot(long pointer, int slot, int id, int count, int data, long extra);

    public static native void setSlot2(long pointer, int slot, Item itemInstance);

    public static native long getCompoundTag(long pointer);

    public static native void setCompoundTag(long pointer, long tag);
}
