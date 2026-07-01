package com.zhekasmirnov.apparatus.adapter.innercore.game.item;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import org.mozilla.javascript.Scriptable;

// this is a stub class for DungeonUtility, it MUST NOT be used in code
@Deprecated
public class ItemStack extends ItemInstance {
    public ItemStack(int id, int count, int data, ItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public ItemStack(int id, int count, int data, NativeItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public ItemStack(int id, int count, int data) {
        super(id, count, data);
    }

    public ItemStack(ItemInstance rhs) {
        super(rhs);
    }

    public ItemStack(ItemStack rhs) {
        super(rhs);
    }

    public ItemStack() {
    }

    public ItemStack(Scriptable scriptable) {
        super(scriptable);
    }
}