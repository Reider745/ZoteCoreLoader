package com.zhekasmirnov.innercore.api;

import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * Created by zheka on 14.02.2018.
 */

public class NativeItemInstanceExtra {
    private static final int saverId;

    static {
        saverId = ObjectSaverRegistry.registerSaver("_item_instance_extra_data", new ObjectSaver() {
            @Override
            public Object read(ScriptableObject input) {
                if (input == null) {
                    return null;
                }

                NativeItemInstanceExtra extra = new NativeItemInstanceExtra();

                NativeArray _ids = ScriptableObjectHelper.getNativeArrayProperty(input, "eId", null);
                NativeArray _levels = ScriptableObjectHelper.getNativeArrayProperty(input, "eLvl", null);
                if (_ids != null && _levels != null) {
                    Object[] ids = _ids.toArray();
                    Object[] levels = _levels.toArray();
                    int count = Math.min(ids.length, levels.length);
                    for (int i = 0; i < count; i++) {
                        extra.addEnchant((int) ids[i], (int) levels[i]);
                    }
                }

                String custom = ScriptableObjectHelper.getStringProperty(input, "$", null);
                if (custom != null) {
                    extra.setAllCustomData(custom);
                }

                String name = ScriptableObjectHelper.getStringProperty(input, "N", null);
                if (name != null) {
                    extra.setCustomName(name);
                }

                return extra.isEmpty() ? null : extra;
            }

            @Override
            public ScriptableObject save(Object input) {
                if (input != null && input instanceof NativeItemInstanceExtra) {
                    NativeItemInstanceExtra extra = (NativeItemInstanceExtra) input;
                    if (!extra.isEmpty()) {
                        ScriptableObject saved = ScriptableObjectHelper.createEmpty();

                        if (extra.isEnchanted()) {
                            int[][] enchants = extra.getRawEnchants();
                            Object[] ids = new Object[enchants[0].length];
                            Object[] levels = new Object[enchants[1].length];
                            for (int i = 0; i < ids.length; i++) {
                                ids[i] = enchants[0][i];
                                levels[i] = enchants[1][i];
                            }
                            saved.put("eId", saved, ScriptableObjectHelper.createArray(ids));
                            saved.put("eLvl", saved, ScriptableObjectHelper.createArray(levels));
                        }

                        String custom = extra.getAllCustomData();
                        if (custom != null) {
                            saved.put("$", saved, custom);
                        }

                        String name = extra.getCustomName();
                        if (name != null) {
                            saved.put("N", saved, name);
                        }

                        return saved;
                    }
                }
                return null;
            }
        });
    }

    public static void initSaverId() {
        // forces class to load
    }

    private Item item;

    public boolean isFinalizableInstance() {
        return false;
    }

    @Deprecated
    public NativeItemInstanceExtra(long extra) {
        throw new UnsupportedOperationException();
    }

    public NativeItemInstanceExtra(Item item) {
        ObjectSaverRegistry.registerObject(this, saverId);

        if (item != null) {
            this.item = item;
        } else {
            this.item = constructNew();
        }
    }

    public NativeItemInstanceExtra() {
        ObjectSaverRegistry.registerObject(this, saverId);
        this.item = constructNew();
    }

    public NativeItemInstanceExtra(NativeItemInstanceExtra extra) {
        ObjectSaverRegistry.registerObject(this, saverId);

        Item item = getValueOrNullPtr(extra);
        if (item != null) {
            this.item = constructClone(item);
        } else {
            this.item = constructNew();
        }
    }

    public JSONObject asJson() {
        if (isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            int[][] enchants = getRawEnchants();
            int[] ids = enchants[0];
            int[] levels = enchants[1];
            if (ids.length > 0) {
                JSONArray enchantsJson = new JSONArray();
                for (int i = 0; i < ids.length; i++) {
                    JSONObject enchantJson = new JSONObject();
                    enchantJson.put("id", ids[i]);
                    enchantJson.put("l", levels[i]);
                    enchantsJson.put(enchantJson);
                }
                json.put("enchants", enchantsJson);
            }
            String name = getCustomName();
            if (name != null && name.length() > 0) {
                json.put("name", name);
            }
            String data = getAllCustomData();
            if (data != null && data.length() > 0) {
                json.put("data", data);
            }
        } catch (Exception ignore) {
        }
        return json.length() > 0 ? json : null;
    }

    public NativeItemInstanceExtra copy() {
        return new NativeItemInstanceExtra(this);
    }

    public Item getValue() {
        return item;
    }

    public void bind(Item item) {
        this.item = item;
        this.customData = null;
        this.customDataLoaded = false;
    }

    public boolean isEmpty() {
        return item != null ? !item.hasCompoundTag() : true;
    }

    public void applyTo(Scriptable item) {
        if (item != null) {
            item.put("extra", item, getValue());
        }
    }

    public boolean isEnchanted() {
        return item != null ? item.hasEnchantments() : false;
    }

    public void addEnchant(int type, int level) {
        Enchantment enchantment = Enchantment.get(type);
        if (enchantment.getName().equals("%enchantment.unknown")) {
            Logger.error("NativeItemInstanceExtra", "Unknown enchantment with id " + type);
            return;
        }
        if (item != null) {
            enchantment = Enchantment.getEnchantment(type);
            enchantment.setLevel(level, false);
            item.addEnchantment(enchantment);
        }
    }

    public int getEnchantLevel(int type) {
        return item != null ? item.getEnchantmentLevel(type) : 0;
    }

    public void removeEnchant(int type) {
        if (item != null && item.hasEnchantments()) {
            ListTag<CompoundTag> ench = item.getNamedTag().getList("ench", CompoundTag.class);
            for (int i = 0, l = ench.size(); i < l; i++) {
                if (ench.get(i).getShort("id") == type) {
                    ench.remove(i);
                    return;
                }
            }
        }
    }

    public void removeAllEnchants() {
        if (item != null && item.hasEnchantments()) {
            item.getNamedTag().remove("ench");
        }
    }

    public int getEnchantCount() {
        if (item != null && item.hasEnchantments()) {
            Enchantment[] enchantments = item.getEnchantments();
            return enchantments.length;
        }
        return 0;
    }

    public String getEnchantName(int id, int lvl) {
        Enchantment enchantment = item != null ? item.getEnchantment(id) : null;
        if (enchantment == null) {
            enchantment = Enchantment.get(id);
        }
        if (!enchantment.getName().equals("%enchantment.unknown")) {
            return lvl > 0 ? enchantment.getName() + " " + Enchantment.getLevelString(lvl) : enchantment.getName();
        }
        return null;
    }

    public String getEnchantName(int id) {
        return getEnchantName(id, getEnchantLevel(id));
    }

    public int[][] getRawEnchants() {
        if (item == null) {
            return new int[0][0];
        }
        int count = getEnchantCount();
        int ids[] = new int[count];
        int levels[] = new int[count];
        Enchantment[] enchantments = item.getEnchantments();
        for (int i = 0, l = enchantments.length; i < l; i++) {
            Enchantment enchantment = enchantments[i];
            ids[i] = enchantment.getId();
            levels[i] = enchantment.getLevel();
        }
        return new int[][] {
                ids,
                levels
        };
    }

    public ScriptableObject getEnchants() {
        int[][] enchants = getRawEnchants();
        int[] ids = enchants[0];
        int[] levels = enchants[1];

        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (int i = 0; i < ids.length; i++) {
            if (levels[i] > 0) {
                result.put(ids[i], result, levels[i]);
            }
        }
        return result;
    }

    public String getAllEnchantNames() {
        StringBuilder str = new StringBuilder();

        int[][] enchants = getRawEnchants();
        int[] ids = enchants[0];
        int[] levels = enchants[1];
        for (int i = 0; i < ids.length; i++) {
            if (levels[i] > 0) {
                str.append(getEnchantName(ids[i], levels[i])).append("\n");
            }
        }

        return str.toString();
    }

    public String getAllCustomData() {
        if (item != null) {
            CompoundTag tag = item.getNamedTag();
            return tag != null ? tag.getString(ItemUtils.INNER_CORE_TAG_NAME) : null;
        }
        return null;
    }

    public void setAllCustomData(String extra) {
        customData = new JSONObject(extra);
        if (item != null) {
            CompoundTag tag = item.getOrCreateNamedTag();
            if (!customData.isEmpty()) {
                tag.putString(ItemUtils.INNER_CORE_TAG_NAME, customData.toString());
            } else {
                tag.remove(ItemUtils.INNER_CORE_TAG_NAME);
            }
            item.setNamedTag(tag);
        }
    }

    public String getCustomName() {
        return item != null ? item.getCustomName() : "";
    }

    public void setCustomName(String name) {
        if (item != null) {
            item.setCustomName(name);
        }
    }

    public NativeCompoundTag getCompoundTag() {
        CompoundTag tag = item != null ? item.getNamedTag() : null;
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        if (item != null) {
            item.setNamedTag(tag.tag);
            applyCustomDataJSON();
        }
    }

    private JSONObject customData = null;
    private boolean customDataLoaded = false;

    private JSONObject getCustomDataJSON() {
        if (!customDataLoaded) {
            String raw = getAllCustomData();
            if (raw != null) {
                try {
                    customData = new JSONObject(raw);
                } catch (JSONException e) {
                    customData = null;
                }
            }
            customDataLoaded = true;
        }
        return customData;
    }

    private JSONObject getOrCreateCustomDataJSON() {
        JSONObject data = getCustomDataJSON();
        if (data == null) {
            data = customData = new JSONObject();
        }
        return data;
    }

    private void applyCustomDataJSON() {
        if (customData != null) {
            setAllCustomData(customData.toString());
        } else {
            setAllCustomData(null);
        }
    }

    private NativeItemInstanceExtra putObject(String name, Object value) {
        JSONObject data = getOrCreateCustomDataJSON();
        try {
            data.put(name, value);
            applyCustomDataJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public NativeItemInstanceExtra putString(String name, String value) {
        return putObject(name, value);
    }

    public NativeItemInstanceExtra putInt(String name, int value) {
        return putObject(name, value);
    }

    public NativeItemInstanceExtra putLong(String name, long value) {
        return putObject(name, value);
    }

    public NativeItemInstanceExtra putFloat(String name, double value) {
        return putObject(name, value);
    }

    public NativeItemInstanceExtra putBoolean(String name, boolean value) {
        return putObject(name, value);
    }

    public String getString(String name, String fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optString(name, fallback);
        }
        return fallback;
    }

    public boolean contains(String name) {
        JSONObject data = getCustomDataJSON();
        return data.opt(name) != null;
    }

    public String getString(String name) {
        return getString(name, null);
    }

    public int getInt(String name, int fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optInt(name, fallback);
        }
        return fallback;
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public long getLong(String name, long fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optLong(name, fallback);
        }
        return fallback;
    }

    public long getLong(String name) {
        return getLong(name, 0);
    }

    public double getFloat(String name, double fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optDouble(name, fallback);
        }
        return fallback;
    }

    public double getFloat(String name) {
        return getFloat(name, 0);
    }

    public boolean getBoolean(String name, boolean fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optBoolean(name, fallback);
        }
        return fallback;
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public NativeItemInstanceExtra putSerializable(String name, Object obj) {
        return putString(name, ScriptableSerializer.jsonToString(ScriptableSerializer.scriptableToJson(obj, null)));
    }

    public Object getSerializable(String name) {
        try {
            return ScriptableSerializer.scriptableFromJson(ScriptableSerializer.stringToJson(getString(name)));
        } catch (JSONException e) {
            return null;
        }
    }

    public void removeCustomData() {
        customData = null;
        customDataLoaded = false;
        setAllCustomData(null);
    }

    @Override
    public String toString() {
        return "ItemExtra{json=" + asJson() + "}";
    }

    private static Item constructNew() {
        return Item.AIR_ITEM.clone();
    }

    @Deprecated
    public static long constructClone(long ptr) {
        throw new UnsupportedOperationException();
    }

    public static Item constructClone(Item item) {
        if (item == null) {
            Logger.error("NativeItemInstanceExtra",
                    "Received null instead of item, new extra will be constructed");
            return Item.AIR_ITEM.clone();
        }
        return item.clone();
    }

    public static NativeItemInstanceExtra unwrapObject(Object extra) {
        if (extra instanceof Wrapper) {
            extra = ((Wrapper) extra).unwrap();
        }
        if (extra instanceof NativeItemInstanceExtra) {
            return (NativeItemInstanceExtra) extra;
        }
        if (extra instanceof Item) {
            return new NativeItemInstanceExtra((Item) extra);
        }
        return null;
    }

    public static Item unwrapValue(Object extra) {
        if (extra == null) {
            return null;
        }
        if (extra instanceof Wrapper) {
            extra = ((Wrapper) extra).unwrap();
        }
        if (extra instanceof NativeItemInstanceExtra) {
            return ((NativeItemInstanceExtra) extra).getValue();
        }
        if (extra instanceof Item) {
            return (Item) extra;
        }
        return null;
    }

    public static Item getValueOrNullPtr(NativeItemInstanceExtra extra) {
        return extra != null ? extra.getValue() : null;
    }

    @Deprecated
    public static NativeItemInstanceExtra getExtraOrNull(long extra) {
        throw new UnsupportedOperationException();
    }

    public static NativeItemInstanceExtra getExtraOrNull(Item extra) {
        return extra != null ? new NativeItemInstanceExtra(extra) : null;
    }

    public static NativeItemInstanceExtra cloneExtra(NativeItemInstanceExtra extra) {
        Item item = getValueOrNullPtr(extra);
        return item != null ? new NativeItemInstanceExtra(constructClone(item)) : null;
    }

    public static NativeItemInstanceExtra fromJson(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        NativeItemInstanceExtra extra = new NativeItemInstanceExtra();
        JSONArray enchants = json.optJSONArray("enchants");
        if (enchants != null) {
            for (int i = 0; i < enchants.length(); i++) {
                JSONObject enchant = enchants.optJSONObject(i);
                extra.addEnchant(enchant.optInt("id"), enchant.optInt("l"));
            }
        }
        String name = json.optString("name");
        if (name != null) {
            extra.setCustomName(name);
        }
        String data = json.optString("data");
        if (data != null) {
            extra.setAllCustomData(data);
        }
        return extra;
    }
}
