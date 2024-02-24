package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

import com.reider745.hooks.ItemUtils;
import com.reider745.item.NukkitIdConvertor;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeItemInstance {
    private Item item;
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public boolean isValid = false;

    @Deprecated(since = "Zote")
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
        this.item = createItemInstanceData(id, count, data);
        this.id = id;
        this.count = count;
        this.data = data;

        isValid = true;
    }

    public Item getPointer() {
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

    public static Item createItemInstanceData(int id, int count, int data) {
        return Item.get(id, data, count);
    }

    public static int getId(Item item) {
        return item != null ? item.getId() : 0;
    }

    public static int getCount(Item item) {
        return item != null ? item.getCount() : 0;
    }

    public static int getData(Item item) {
        return item != null ? item.getDamage() : 0;
    }

    public static int setItemInstance(Item item, int id, int count, int data) {
        if (item != null) {
            if (item.getId() != id) {
                Logger.warning("NativeItemInstance", "Id " + item.getId() + " cannot be changed via pointer method!");
            }
            item.setCount(count);
            item.setDamage(data);
        }
        return 0;
    }

    public static int destroy(Item item) {
        return 0;
    }

    public static Item getExtra(Item item) {
        if (item == null) {
            return null;
        }
        NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);
        return extra != null ? extra.getValue() : null;
    }

    public static void setExtra(Item item, int ench) {
        NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);
        if (extra != null) {
            extra.addEnchant(ench, 1);
        }
    }
}
