package com.zhekasmirnov.apparatus.api.container;

import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

public class ItemContainerSlot implements AbstractSlot {
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    private String name;
    private ItemContainer container;
    private Boolean isSavingEnabled = null;

    public ItemContainerSlot(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    public ItemContainerSlot(int id, int count, int data) {
        this(id, count, data, null);
    }

    public ItemContainerSlot() {
        this(0, 0, 0, null);
    }

    public ItemContainerSlot(ScriptableObject scriptable) {
        this(
                ScriptableObjectHelper.getIntProperty(scriptable, "id", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "count", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "data", 0),
                NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null))
        );
    }

    public ItemContainerSlot(JSONObject json, boolean convert) {
        this(convert ? IdConversionMap.serverToLocal(json.optInt("id", 0)) : json.optInt("id", 0), json.optInt("count", 0), json.optInt("data", 0), NativeItemInstanceExtra.fromJson(json.optJSONObject("extra")));
    }

    void setContainer(ItemContainer container, String name) {
        this.name = name;
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public ItemContainer getContainer() {
        return container;
    }

    public ScriptableObject asScriptable() {
        ScriptableObject object = ScriptableObjectHelper.createEmpty();
        object.put("id", object, id);
        object.put("count", object, count);
        object.put("data", object, data);
        object.put("extra", object, extra);
        return object;
    }

    public JSONObject asJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("count", count);
            json.put("data", data);
            if (extra != null) {
                JSONObject extraJson = extra.asJson();
                if (extraJson != null) {
                    json.put("extra", extraJson);
                }
            }
        } catch (JSONException ignore) { }
        return json;
    }

    public boolean isEmpty() {
        return this.id == 0 && this.count == 0 && this.data == 0 && this.extra == null;
    }

    public void markDirty() {
        if (this.container != null) {
            this.container.markSlotDirty(name);
        }
    }

    public void clear() {
        this.id = this.count = this.data = 0;
        this.extra = null;
        this.markDirty();
    }

    public void validate() {
        if (this.id == 0 || this.count <= 0) {
            this.clear();
        }
    }

    public void dropAt(NativeBlockSource blockSource, float x, float y, float z) {
        blockSource.spawnDroppedItem(x, y, z, id, count, data, extra);
        this.clear();
    }

    public void setSlot(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
        this.markDirty();
    }

    public void setSlot(int id, int count, int data) {
        this.setSlot(id, count, data, null);
    }

    public void resetSavingEnabled() {
        isSavingEnabled = null;
    }

    public void setSavingEnabled(boolean savingEnabled) {
        isSavingEnabled = savingEnabled;
    }

    public boolean isSavingEnabled() {
        return isSavingEnabled != null ? isSavingEnabled : (container != null && container.isGlobalSlotSavingEnabled());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getData() {
        return data;
    }

    @Override
    public NativeItemInstanceExtra getExtra() {
        return extra;
    }

    @Override
    public void set(int id, int count, int data, NativeItemInstanceExtra extra) {
        setSlot(id, count, data, extra);
    }

    @Override
    public String toString() {
        return "ItemContainerSlot{" +
                "id=" + id +
                ", count=" + count +
                ", data=" + data +
                ", name='" + name + '\'' +
                '}';
    }
}
