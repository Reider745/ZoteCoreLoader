package com.zhekasmirnov.innercore.api;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeItemInstance {
    private long pointer;
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public boolean isValid = false;

    public NativeItemInstance(long ptr) {
        this.pointer = ptr;
        if (this.pointer == 0) {
            this.pointer = createItemInstanceData(0, 0, 0);
            this.id = this.count = this.data = 0;
            this.extra = null;
        }
        else {
            this.id = getId(ptr);
            this.count = getCount(ptr);
            this.data = getData(ptr);
            long extra = getExtra(ptr);
            this.extra = extra != 0 ? new NativeItemInstanceExtra(extra) : null;
        }

        isValid = true;
    }

    public NativeItemInstance(int id, int count, int data) {
        this.pointer = createItemInstanceData(id, count, data);
        this.id = id;
        this.count = count;
        this.data = data;

        isValid = true;
    }

    public long getPointer() {
        setItemInstance(pointer, id, count, data);
        return pointer;
    }

    public void destroy() {
        isValid = false;
        // destroy(pointer);
    }

    public String toString() {
        return "[item=" + id + "," + count + "," + data + "]";
    }


    public static native long createItemInstanceData(int id, int count, int data);
    public static native int getId(long ptr);
    public static native int getCount(long ptr);
    public static native int getData(long ptr);
    public static native int setItemInstance(long ptr, int id, int count, int data);
    public static native int destroy(long ptr);
    public static native long getExtra(long ptr);
    public static native void setExtra(long ptr, int ench);

}
