package com.zhekasmirnov.apparatus.adapter.innercore.game.item;

import cn.nukkit.item.Item;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.*;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class ItemStack {
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public ItemStack(int id, int count, int data, NativeItemInstanceExtra extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = NativeItemInstanceExtra.cloneExtra(extra);
    }

    public ItemStack(int id, int count, int data) {
        this(id, count, data, null);
    }

    public ItemStack() {
        this(0, 0, 0, null);
    }

    public ItemStack(ItemStack stack) {
        this(stack.id, stack.count, stack.data, null);
    }

    public ItemStack(NativeItemInstance itemInstance) {
        this(itemInstance.id, itemInstance.count, itemInstance.data, itemInstance.extra);
    }

    public ItemStack(ScriptableObject scriptable) {
        this(
                ScriptableObjectHelper.getIntProperty(scriptable, "id", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "count", 0),
                ScriptableObjectHelper.getIntProperty(scriptable, "data", 0),
                NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null)));
    }

    public static ItemStack fromPtr(long ptr) {
        InnerCoreServer.useNotCurrentSupport("ItemStack.fromPtr(ptr)");
        return new ItemStack();
    }

    public static ItemStack fromPtr(Item item) {
        if (item != null) {
            return new ItemStack(new NativeItemInstance(item));
        } else {
            return new ItemStack();
        }
    }

    public static ItemStack parse(Object obj) {
        while (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        if (obj == null || "undefined".equals(obj.toString().toLowerCase())) {
            return null;
        }
        if (obj instanceof ScriptableObject) {
            return new ItemStack((ScriptableObject) obj);
        } else if (obj instanceof ItemStack) {
            return new ItemStack((ItemStack) obj);
        } else if (obj instanceof JSONObject || obj instanceof CharSequence) {
            JSONObject json;
            try {
                json = obj instanceof JSONObject ? (JSONObject) obj : new JSONObject(obj.toString());
            } catch (JSONException e) {
                return null;
            }
            return new ItemStack(json.optInt("id", 0), json.optInt("count", 0), json.optInt("data", 0), NativeItemInstanceExtra.fromJson(json.optJSONObject("extra")));
        } else if (obj instanceof NativeItemInstance) {
            return new ItemStack((NativeItemInstance) obj);
        } else if (obj instanceof Item) {
            return ItemStack.fromPtr((Item) obj);
        }
        return null;
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
        } catch (JSONException ignore) {
        }
        return json;
    }

    public boolean isEmpty() {
        return id == 0 && count == 0 && data == 0 && extra == null;
    }

    public long getExtraPtr() {
        return NativeItemInstanceExtra.getValueOrNullPtr(extra);
    }

    public int getMaxStackSize() {
        return NativeItem.getMaxStackForId(id, data);
    }

    public int getMaxDamage() {
        return NativeItem.getMaxDamageForId(id, data);
    }

    public String getItemName() {
        return NativeItem.getNameForId(id, data, getExtraPtr());
    }

    public boolean isGlint() {
        return NativeItem.isGlintItemInstance(id, data, extra);
    }

    public NativeItemModel getItemModel() {
        return NativeItemModel.getForWithFallback(id, data);
    }

    @Override
    public String toString() {
        return "ItemStack{" + "id=" + id + ", count=" + count + ", data=" + data
                + (extra != null ? ", extra=" + extra : "") + '}';
    }
}
