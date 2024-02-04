package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.reider745.item.NukkitIdConvertor;

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
            this.item = Item.AIR_ITEM.clone();
            this.id = this.count = this.data = 0;
            this.extra = null;
        } else {
            this.item = item;
            this.id = item.getId();
            this.count = item.getCount();
            this.data = item.hasMeta() ? item.getDamage() : 0;

            NukkitIdConvertor.convert(this);

            this.extra = ItemUtils.getItemInstanceExtra(item);
        }

        isValid = true;
    }

    public NativeItemInstance(int id, int count, int data) {
        this.item = ItemUtils.get(id, count, data);
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
                return (item = ItemUtils.get(id, count, data, extra));
            }
            if (item.getCount() != count) {
                item.setCount(count);
            }
            if (item.getDamage() != data) {
                item.setDamage(data);
            }
            CompoundTag tag = item.getOrCreateNamedTag();
            if (extra != null && !extra.isEmpty()) {
                tag = extra.getValue().getNamedTag();
                extra.bind(item);
            } else {
                tag.remove(ItemUtils.INNER_CORE_TAG_NAME);
            }
            item.setNamedTag(tag);
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

    public static Item getExtra(Item item) {
        if (item == null) {
            return null;
        }
        NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);
        return extra != null ? extra.getValue() : null;
    }

    public static long getExtra(long ptr) {
        InnerCoreServer.useNotSupport("NativeItemInstance.getExtra(ptr)");
        return 0;
    }

    public static void setExtra(long ptr, int ench) {
        InnerCoreServer.useNotSupport("NativeItemInstance.setExtra(ptr, ench)");
    }
}
