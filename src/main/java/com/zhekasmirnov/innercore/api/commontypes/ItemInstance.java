package com.zhekasmirnov.innercore.api.commontypes;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import org.mozilla.javascript.Scriptable;

// this is a stub class for DungeonUtility, it MUST NOT be used in code
@Deprecated
public class ItemInstance extends org.innercore.icstd.inventory.ItemInstance {
    public ItemInstance(int id, int count, int data, NativeItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public ItemInstance(int id, int count, int data, ItemInstanceExtra extra) {
        super(id, count, data, extra);
    }

    public ItemInstance(int id, int count, int data) {
        super(id, count, data);
    }

    public ItemInstance(org.innercore.icstd.inventory.ItemInstance rhs) {
        super(rhs);
    }

    public ItemInstance() {
        super();
    }

    public ItemInstance(Scriptable scriptable) {
        super(scriptable);
    }
}
