package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeItemInstance {
    private Item item;
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public boolean isValid = false;

    @Deprecated
    public NativeItemInstance(long ptr) {
        throw new UnsupportedOperationException("NativeItemInstance(ptr)");
    }

    public NativeItemInstance(Item item) {
        if (item == null) {
            this.item = Item.get(0).clone();
            this.id = this.count = this.data = 0;
            this.extra = null;
        } else {
            this.item = item;
            this.id = item.getId();
            this.count = item.getCount();
            this.data = item.getAttackDamage() == 0 ? item.getAttackDamage() : item.getDamage();
            if (this.id == 351 && this.data == 15) { // bone_meal fix
                this.id = 858;
                this.data = 0;
            }

            this.extra = ItemUtils.getItemInstanceExtra(item);
            // this.extra = extra != 0 ? new NativeItemInstanceExtra(extra) : null;
        }

        isValid = true;
    }

    public NativeItemInstance(int id, int count, int data) {
        this.item = Item.get(id, count, data);
        this.id = id;
        this.count = count;
        this.data = data;

        isValid = true;
    }

    public long getPointer() {
        InnerCoreServer.useNotSupport("NativeItemInstance.getPointer()");
        return 0;
    }

    public Item getItem() {
        if (item != null) {
            if (item.getId() != id) {
                InnerCoreServer.useNotCurrentSupport("NativeItemInstance.id");
            }
            if (item.getCount() != count) {
                item.setCount(count);
            }
            if (item.getDamage() != data) {
                item.setDamage(data);
            }
            long extra = getExtra(item);
            if (this.extra != null ? extra != this.extra.getValue() : extra != 0) {
                InnerCoreServer.useNotCurrentSupport("NativeItemInstance.extra");
            }
        }
        return item;
    }

    public void destroy() {
        isValid = false;
    }

    public String toString() {
        return "[item=" + id + "," + count + "," + data + "]";
    }

    public static long createItemInstanceData(int id, int count, int data) {
        InnerCoreServer.useNotSupport("NativeItemInstance.createItemInstanceData(id, count, data)");
        return 0;
    }

    public static int getId(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.getId(ptr)");
        return 0;
    }

    public static int getCount(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.getCount(ptr)");
        return 0;
    }

    public static int getData(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.getData(ptr)");
        return 0;
    }

    public static int setItemInstance(long ptr, int id, int count, int data) {
        InnerCoreServer.useNotSupport("NativeItemInstance.setItemInstance(ptr, id, count, data)");
        return 0;
    }

    public static int destroy(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.destroy(ptr)");
        return 0;
    }

    public static long getExtra(Item item) {
        if (item == null) {
            return 0;
        }
        NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);
        return extra != null ? extra.getValue() : 0;
    }

    public static long getExtra(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.getExtra(ptr)");
        return 0;
    }

    public static void setExtra(long ptr, int ench) {
        InnerCoreServer.useNotSupport("NativeItemInstance.setExtra(ptr, ench)");
    }
}
