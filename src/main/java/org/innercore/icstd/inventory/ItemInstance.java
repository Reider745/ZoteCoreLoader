package org.innercore.icstd.inventory;

import cn.nukkit.item.Item;
import com.reider745.hooks.item.ItemUtils;
import com.reider745.item.NukkitIdConverter;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.ItemInstanceBindGenBase;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.modules.ItemModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

@ApiBinding
public class ItemInstance extends ItemInstanceBindGenBase {
    @ApiBinding public int id = 0;
    @ApiBinding public int count = 0;
    @ApiBinding public int data = 0;
    @ApiBinding public ItemInstanceExtra extra = null;

    @ApiBinding
    public ItemInstance(int id, int count, int data, ItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    @ApiBinding
    public ItemInstance(int id, int count, int data) {
        this(id, count, data, null);
    }

    @ApiBinding
    public ItemInstance(ItemInstance rhs) {
        if (rhs == null) return;
        this.id = rhs.id;
        this.count = rhs.count;
        this.data = rhs.data;
        this.extra = rhs.extra;
    }

    @ApiBinding
    public ItemInstance() { this(0, 0, 0, null); }

    @ApiBinding
    public void setItem(int id, int count, int data) {
        this.id = id;
        this.count = count;
        this.data = data;
    }

    @ApiBinding
    public ItemInstance(Scriptable scriptable) {
        this(
                ScriptableObjectHelper.getIntProperty(scriptable, "id", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "count", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "data", 0),
                ItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null))
        );
    }

    public static ItemInstance makeFromPointer(long pointer, boolean nullable) {
        if (pointer != 0) {
            NativeItemInstance instance = new NativeItemInstance(pointer);
            ItemInstance item = new ItemInstance(instance.id, instance.count, instance.data, instance.extra);
            instance.destroy();
            return item;
        }
        return nullable ? null : new ItemInstance();
    }

    public static ItemInstance makeFromPointer(long pointer) { return makeFromPointer(pointer, false); }

    public static ItemInstance makeFrom(Item item) {
        final ItemInstance instance = new ItemInstance(item.getId(), item.getCount(), item.getDamage(), ItemUtils.getItemInstanceExtra(item));
        NukkitIdConverter.convert(instance);
        return instance;
    }

    @ApiBinding
    public static ItemInstance parse(Object obj, boolean nullable) {
        while (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        if (obj == null || obj == Undefined.instance) {
            return nullable ? null : new ItemInstance();
        } else if (obj instanceof ItemInstance) {
            return (ItemInstance) obj;
        } else if (obj instanceof Scriptable) {
            return new ItemInstance((Scriptable) obj);
        } else if (obj instanceof JSONObject || obj instanceof CharSequence) {
            JSONObject json;
            try {
                json = obj instanceof JSONObject ? (JSONObject) obj : new JSONObject(obj.toString());
            } catch (JSONException e) {
                return nullable ? null : new ItemInstance();
            }
            return new ItemInstance(json.optInt("id", 0), json.optInt("count", 0), json.optInt("data", 0), ItemInstanceExtra.fromJson(json.optJSONObject("extra")));
        } else if (obj instanceof Long) {
            return ItemInstance.makeFromPointer((Long) obj, nullable);
        }
        return nullable ? null : new ItemInstance();
    }

    @ApiBinding
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

    @ApiBinding
    public boolean isEmpty() {
        return id == 0 && count == 0 && data == 0 && extra == null;
    }

    @ApiBinding
    public long getExtraPtr() {
        return ItemInstanceExtra.getValueOrNullPtr(extra);
    }

    @ApiBinding
    public int getMaxStackSize() {
        return ItemModule.getMaxStack(id, data);
    }

    @ApiBinding
    public int getMaxDamage() {
        return ItemModule.getMaxDamage(id, data);
    }

    @ApiBinding
    public String getItemName() {
        return ItemModule.getName(id, data, getExtraPtr());
    }

    @ApiBinding
    public boolean isGlint() {
        return ItemModule.isGlint(id, data, extra);
    }

    @ApiBinding
    public NativeItemModel getItemModel() {
        return NativeItemModel.getForWithFallback(id, data);
    }

    @Override
    public String toString() {
        return "ItemInstance{" +
                "id=" + id +
                ", count=" + count +
                ", data=" + data +
                (extra != null ? ", extra=" + extra : "") +
                '}';
    }
}
