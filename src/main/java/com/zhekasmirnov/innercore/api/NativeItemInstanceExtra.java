package com.zhekasmirnov.innercore.api;

import com.reider745.api.pointers.ClassPointer;
import com.reider745.hooks.ItemUtils;
import com.reider745.item.ItemExtraDataProvider;
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

    private long ptr;
    private ItemExtraDataProvider extraProvider;

    public boolean isFinalizableInstance() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        super.finalize();
        if (isFinalizableInstance()) {
            ItemUtils.removePointer(ptr);
        }
    }

    public NativeItemInstanceExtra(long extra) {
        ObjectSaverRegistry.registerObject(this, saverId);

        if (extra != 0) {
            this.ptr = extra;
        } else {
            this.ptr = constructNew();
        }

        ItemExtraDataProvider extraProvider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        extraProvider.extra = this;
        this.extraProvider = extraProvider;
    }

    public NativeItemInstanceExtra() {
        ObjectSaverRegistry.registerObject(this, saverId);
        this.ptr = constructNew();
        ItemExtraDataProvider extraProvider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        extraProvider.extra = this;
        this.extraProvider = extraProvider;
    }

    public NativeItemInstanceExtra(NativeItemInstanceExtra extra) {
        ObjectSaverRegistry.registerObject(this, saverId);

        long val = getValueOrNullPtr(extra);
        if (val != 0) {
            this.ptr = constructClone(val);
        } else {
            this.ptr = constructNew();
        }

        ItemExtraDataProvider extraProvider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        extraProvider.extra = this;
        this.extraProvider = extraProvider;
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

    public long getValue() {
        return ptr;
    }

    public ItemExtraDataProvider getExtraProvider() {
        return extraProvider;
    }

    public boolean isEmpty() {
        return getValue() == 0;
    }

    public void applyTo(Scriptable item) {
        if (item != null) {
            item.put("extra", item, getValue());
        }
    }

    public boolean isEnchanted() {
        Item item = extraProvider.get();
        return item != null ? item.hasEnchantments() : false;
    }

    public void addEnchant(int type, int level) {
        Enchantment enchantment = Enchantment.get(type);
        if (enchantment.getName().equals("%enchantment.unknown")) {
            Logger.error("NativeItemInstanceExtra", "Unknown enchantment with id " + type);
            return;
        }
        Item item = extraProvider.get();
        if (item != null) {
            enchantment = Enchantment.getEnchantment(type);
            enchantment.setLevel(level, false);
            item.addEnchantment(enchantment);
        }
    }

    public int getEnchantLevel(int type) {
        Item item = extraProvider.get();
        return item != null ? item.getEnchantmentLevel(type) : 0;
    }

    public void removeEnchant(int type) {
        Item item = extraProvider.get();
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
        Item item = extraProvider.get();
        if (item != null && item.hasEnchantments()) {
            item.getNamedTag().remove("ench");
        }
    }

    public int getEnchantCount() {
        Item item = extraProvider.get();
        if (item != null && item.hasEnchantments()) {
            Enchantment[] enchantments = item.getEnchantments();
            return enchantments.length;
        }
        return 0;
    }

    public String getEnchantName(int id, int lvl) {
        Item item = extraProvider.get();
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
        Item item = extraProvider.get();
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
        Item item = extraProvider.get();
        return item != null ? item.getNamedTag().getString(ItemUtils.INNER_CORE_TAG_NAME) : null;
    }

    public void setAllCustomData(String extra) {
        customData = new JSONObject(extra);
    }

    public String getCustomName() {
        Item item = extraProvider.get();
        return item != null ? item.getCustomName() : "";
    }

    public void setCustomName(String name) {
        Item item = extraProvider.get();
        if (item != null) {
            item.setCustomName(name);
        }
    }

    public NativeCompoundTag getCompoundTag() {
        CompoundTag tag = extraProvider.getCompoundTag();
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        extraProvider.setCompoundTag(tag != null ? tag.tag : null);
    }

    private JSONObject customData = null;
    private boolean customDataLoaded = false;

    // Method is private in Inner Core
    public JSONObject getCustomDataJSON() {
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

    private static long constructNew() {
        ItemExtraDataProvider extraProvider = new ItemExtraDataProvider(null);
        return ItemUtils.items_pointers.addPointer(extraProvider);
    }

    public static long constructClone(long ptr) {
        ClassPointer<Item> pointer = ItemUtils.items_pointers.getInstance(ptr);
        if (pointer == null) {
            Logger.error("NativeItemInstanceExtra",
                    "Unavailable pointer " + pointer + ", new extra will be constructed");
            return constructNew();
        }
        ItemExtraDataProvider extraProvider = new ItemExtraDataProvider(pointer.getReference());
        return ItemUtils.items_pointers.addPointer(extraProvider);
    }

    public static NativeItemInstanceExtra unwrapObject(Object extra) {
        if (extra instanceof Wrapper) {
            extra = ((Wrapper) extra).unwrap();
        }
        if (extra instanceof NativeItemInstanceExtra) {
            return (NativeItemInstanceExtra) extra;
        }
        return null;
    }

    public static long unwrapValue(Object extra) {
        if (extra == null) {
            return 0;
        }
        if (extra instanceof Wrapper) {
            extra = ((Wrapper) extra).unwrap();
        }
        if (extra instanceof NativeItemInstanceExtra) {
            return ((NativeItemInstanceExtra) extra).getValue();
        }
        if (extra instanceof Number) {
            return ((Number) extra).intValue();
        }
        return 0;
    }

    public static long getValueOrNullPtr(NativeItemInstanceExtra extra) {
        return extra != null ? extra.getValue() : 0;
    }

    public static NativeItemInstanceExtra getExtraOrNull(long extra) {
        return extra != 0 ? new NativeItemInstanceExtra(extra) : null;
    }

    public static NativeItemInstanceExtra cloneExtra(NativeItemInstanceExtra extra) {
        long value = getValueOrNullPtr(extra);
        return value != 0 ? new NativeItemInstanceExtra(constructClone(value)) : null;
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
