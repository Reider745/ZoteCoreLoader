package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 02.08.2017.
 */

public class Slot extends ScriptableObject implements AbstractSlot {
    private ScriptableObject target;

    @Override
    public String getClassName() {
        return "slot";
    }

    public Slot(int id, int count, int data) {
        target = this;
        set(id, count, data);
    }

    public Slot(int id, int count, int data, NativeItemInstanceExtra extra) {
        target = this;
        set(id, count, data, extra);
    }

    public Slot() {
        target = this;
        set(0, 0, 0);
    }

    public Slot(ScriptableObject parent) {
        target = parent;
    }

    public void set(int id, int count, int data) {
        put("id", id);
        put("count", count);
        put("data", data);
        put("extra", null);
    }

    public void set(int id, int count, int data, NativeItemInstanceExtra extra) {
        set(id, count, data);
        put("extra", extra);
    }

    public void put(String name, Object prop) {
        target.put(name, target, prop);
    }

    public int getInt(String name) {
        return ScriptableObjectHelper.getIntProperty(target, name, -1);
    }

    public void validate() {
        if (getInt("data") <= 0) {
            put("data", 0);
        }
        if (getInt("id") == 0 || getInt("count") <= 0) {
            set(0, 0, 0, null);
        }
    }

    public void drop(float x, float y, float z) {
        int id = getInt("id");
        int count = getInt("count");
        if (id != 0 && count > 0) {
            NativeAPI.spawnDroppedItem(x, y, z, id, count, getData(), getExtraValue());
        }
        set(0, 0, 0, null);
    }

    public ScriptableObject getTarget() {
        return target;
    }

    public int getId() {
        return getInt("id");
    }

    public int getCount() {
        return getInt("count");
    }

    public int getData() {
        return getInt("data");
    }

    public long getExtraValue() {
        return NativeItemInstanceExtra.unwrapValue(ScriptableObjectHelper.getProperty(target, "extra", null));
    }

    public NativeItemInstanceExtra getExtra() {
        return NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(target, "extra", null));
    }

    public ScriptableObject save() {
        return new Slot(getId(), getCount(), getData(), getExtra());
    }
}
