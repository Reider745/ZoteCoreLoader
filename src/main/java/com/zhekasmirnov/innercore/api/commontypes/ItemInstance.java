package com.zhekasmirnov.innercore.api.commontypes;

import cn.nukkit.item.Item;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;


/**
 * Created by zheka on 09.08.2017.
 */

public class ItemInstance extends ScriptableObject {

    @Override
    public String getClassName() {
        return "Item";
    }

    public ItemInstance(int id, int count, int data) {
        put("id", this, id);
        put("count", this, count);
        put("data", this, data);
    }

    public ItemInstance(int id, int count, int data, NativeItemInstanceExtra extra) {
        this(id, count, data);
        put("extra", this, extra);
    }

    public ItemInstance(NativeItemInstance nativeItemInstance) {
        this(nativeItemInstance.id, nativeItemInstance.count, nativeItemInstance.data, nativeItemInstance.extra);
    }

    public ItemInstance(Item ptr) {
        this(new NativeItemInstance(ptr));
    }

    public int getId() {
        return ((Number) get("id")).intValue();
    }

    public int getCount() {
        return ((Number) get("count")).intValue();
    }

    public int getData() {
        return ((Number) get("data")).intValue();
    }

    public NativeItemInstanceExtra getExtra() {
        return NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(this, "extra", null));
    }

    public long getExtraValue() {
        return NativeItemInstanceExtra.unwrapValue(ScriptableObjectHelper.getProperty(this, "extra", null));
    }
}
