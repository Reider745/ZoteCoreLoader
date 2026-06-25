package org.innercore.icstd.inventory;

import org.jetbrains.annotations.Nullable;
import com.zhekasmirnov.innercore.api.gen.ItemContainerSlotBindGenBase;
import org.innercore.icstd.annotations.ApiBinding;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.region.BlockSource;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

@ApiBinding
public class ItemContainerSlot extends ItemContainerSlotBindGenBase implements AbstractSlot {
    @ApiBinding public int id;
    @ApiBinding public int count;
    @ApiBinding public int data;
    @ApiBinding public ItemInstanceExtra extra;

    private String name;
    private ItemContainer container;
    private Boolean isSavingEnabled = null;

    @ApiHidden public ItemContainerSlot(int id, int count, int data, ItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    @ApiHidden public ItemContainerSlot(int id, int count, int data) {
        this(id, count, data, null);
    }

    @ApiHidden public ItemContainerSlot() {
        this(0, 0, 0, null);
    }

    @ApiHidden public ItemContainerSlot(ScriptableObject scriptable) {
        this(
                ScriptableObjectHelper.getIntProperty(scriptable, "id", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "count", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "data", 0),
                ItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null))
        );
    }

    @ApiHidden public ItemContainerSlot(JSONObject json, boolean convert) {
        this(convert ? IdConversionMap.serverToLocal(json.optInt("id", 0)) : json.optInt("id", 0), json.optInt("count", 0), json.optInt("data", 0), ItemInstanceExtra.fromJson(json.optJSONObject("extra")));
    }

    @ApiHidden void setContainer(ItemContainer container, String name) {
        this.name = name;
        this.container = container;
    }

    @ApiBinding public String getName() {
        return name;
    }

    @ApiBinding public ItemContainer getContainer() {
        return container;
    }

    @ApiHidden public ScriptableObject asScriptable() {
        ScriptableObject object = ScriptableObjectHelper.createEmpty();
        object.put("id", object, id);
        object.put("count", object, count);
        object.put("data", object, data);
        object.put("extra", object, extra);
        return object;
    }

    @ApiHidden public JSONObject asJson() {
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

    @ApiBinding public boolean isEmpty() {
        return this.id == 0 && this.count == 0 && this.data == 0 && this.extra == null;
    }

    @ApiBinding public void markDirty() {
        if (this.container != null) {
            this.container.markSlotDirty(name);
        }
    }

    @ApiBinding public void clear() {
        this.id = this.count = this.data = 0;
        this.extra = null;
        this.markDirty();
    }

    @ApiBinding public void validate() {
        if (this.id == 0 || this.count <= 0) {
            this.clear();
        }
    }

    @ApiBinding public void dropAt(BlockSource blockSource, float x, float y, float z) {
        blockSource.spawnDroppedItem(x, y, z, id, count, data, extra);
        this.clear();
    }

    @ApiBinding public void setSlot(int id, int count, int data, @Nullable ItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
        this.markDirty();
    }

    @ApiBinding public void setSlot(int id, int count, int data) {
        this.setSlot(id, count, data, null);
    }

    @ApiBinding public void resetSavingEnabled() {
        isSavingEnabled = null;
    }

    @ApiBinding public void setSavingEnabled(boolean savingEnabled) {
        isSavingEnabled = savingEnabled;
    }

    @ApiBinding public boolean isSavingEnabled() {
        return isSavingEnabled != null ? isSavingEnabled : (container != null && container.isGlobalSlotSavingEnabled());
    }

    @ApiBinding @Override public int getId() {
        return id;
    }

    @ApiBinding @Override public int getCount() {
        return count;
    }

    @ApiBinding @Override public int getData() {
        return data;
    }

    @ApiBinding @Override @Nullable public ItemInstanceExtra getExtra() {
        return extra;
    }

    @ApiBinding @Override public void set(int id, int count, int data, @Nullable ItemInstanceExtra extra) {
        setSlot(id, count, data, extra);
    }

    @Override public String toString() {
        return "ItemContainerSlot{" +
                "id=" + id +
                ", count=" + count +
                ", data=" + data +
                ", name='" + name + '\'' +
                '}';
    }
}
