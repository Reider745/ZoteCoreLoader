package org.innercore.icstd.inventory;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.reider745.api.ZoteOnly;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenSlowest;
import com.reider745.hooks.item.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.ItemExtraDataBindGenBase;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
import org.innercore.icstd.serialization.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * Created by zheka on 14.02.2018.
 */

@ApiBinding("ItemExtraData")
public class ItemInstanceExtra extends ItemExtraDataBindGenBase implements ISerializableObject {
    private static final PointersStorage<Item> ITEMS = new PointersStorage<>("items", new PointerGenSlowest());
    private static final String legacySaverId = ObjectSaverRegistry.registerSaverStr("_item_instance_extra_data", new ObjectSaver() {
        @Override
        public Object read(ScriptableObject input) {
            if (input == null) {
                return null;
            }

            ItemInstanceExtra extra = new ItemInstanceExtra();

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
            if (input != null && input instanceof ItemInstanceExtra) {
                ItemInstanceExtra extra = (ItemInstanceExtra) input;
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

    private static final String saverId = ObjectSerializer.registerType("ic:ii_ext", ItemInstanceExtra.class, new IObjectSerializer<ItemInstanceExtra>() {
        @Override
        public ItemInstanceExtra read(IScopedReader reader, SerializationContext ctx, String typeId) {
            if (reader.isEmpty())
                return null;
            if (!reader.isObject()) {
                ctx.addError("ItemInstanceExtra invalid format");
                return null;
            }
            ItemInstanceExtra extra = new ItemInstanceExtra();
            IObjectReader o = reader.asObject();
            IArrayReader enchants = o.beginScope("enc").asArray();
            if (enchants != null) {
                for (int i = 0; i < enchants.size() - 1; i += 2) {
                    extra.addEnchant(enchants.getInt(i, 0), enchants.getInt(i + 1, 0));
                }
            }
            o.endChildScope();
            String customName = o.getString("nm", null);
            if (customName != null) {
                extra.setCustomName(customName);
            }
            String customDataString = o.getString("ds", null);
            if (customDataString != null) {
                extra.setAllCustomData(customDataString);
            }
            o.endScope();
            return extra;
        }

        @Override
        public void write(IScopedWriter writer, ItemInstanceExtra extra, SerializationContext ctx, String typeId) {
            if (extra == null || extra.isEmpty()) {
                writer.endScope();
                return;
            }
            IObjectWriter o = writer.asObject();
            if (extra.isEnchanted()) {
                IArrayWriter enchants = o.beginScope("enc").asArray();
                int[][] rawEnchants = extra.getRawEnchants();
                for (int i = 0; i < rawEnchants[0].length; i++) {
                    enchants.addInt(rawEnchants[0][i]);
                    enchants.addInt(rawEnchants[1][i]);
                }
                enchants.endScope();
            }
            if (extra.getCustomName() != null) {
                o.putString("nm", extra.getCustomName());
            }
            if (extra.getAllCustomData() != null) {
                o.putString("ds", extra.getAllCustomData());
            }
            o.endScope();
        }
    });

    public static void initSaverId() {
        // forces class to load
    }

    @Override
    public String getSerializationTypeId() {
        return saverId;
    }

    private long ptr;
    private Item item;
    private boolean isFinalizable = true;
    public boolean isFinalizableInstance() {
        return isFinalizable;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (isFinalizable) {
            finalizeNative(ptr); // TODO: !!! make extra ptr for java extra instance unique in any case
        }
    }

    @ApiBinding
    public ItemInstanceExtra(long extra) {
        if (extra != 0) {
            ptr = extra;
        }
        else {
            ptr = constructNew(); 
        }

        this.item = ITEMS.get(ptr);
    }

    @ZoteOnly
    public ItemInstanceExtra(Item item) {
        ptr = ITEMS.addPointer(item);
        this.item = item;
    }

    @ApiBinding
    public ItemInstanceExtra() {
        ptr = constructNew();
        this.item = ITEMS.get(ptr);
    }

    @ApiBinding
    public ItemInstanceExtra(ItemInstanceExtra extra) {
        long val = getValueOrNullPtr(extra);
        if (val != 0) {
            ptr = constructClone(extra.item);
        }
        else {
            ptr = constructNew();
        }

        this.item = ITEMS.get(ptr);
    }

    public static ItemInstanceExtra cloneExtra(ItemInstanceExtra extra) {
        long value = getValueOrNullPtr(extra);
        return value != 0 ? new ItemInstanceExtra(constructClone(extra.item)) : null;
    }

    @ZoteOnly
    public Item getItem() {
        return item;
    }

    @ZoteOnly
    public void bind(Item item) {
        this.item = item;
        this.customData = null;
        this.customDataLoaded = false;
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
        } catch (JSONException ignore) { }
        return json.length() > 0 ? json : null;
    }

    @ApiBinding
    public ItemInstanceExtra copy() {
        return new ItemInstanceExtra(this);
    }

    @ApiBinding
    public boolean isEmpty() {
        return getValue() == 0;
    }

    // ENCHANT

    @ApiBinding
    public boolean isEnchanted() {
        return item != null && item.hasEnchantments();
    }

    @ApiBinding
    public void addEnchant(int type, int level) {
        Enchantment enchantment = Enchantment.get(type);
        if (enchantment.getName().equals("%enchantment.unknown")) {
            Logger.error("ItemInstanceExtra", "Unknown enchantment with id " + type);
            return;
        }
        if (item != null) {
            enchantment = Enchantment.getEnchantment(type);
            enchantment.setLevel(level, false);
            item.addEnchantment(enchantment);
        }
    }

    @ApiBinding
    public int getEnchantLevel(int type) {
        return item != null ? item.getEnchantmentLevel(type) : 0;
    }

    @ApiBinding
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

    @ApiBinding
    public void removeAllEnchants() {
        if (item != null && item.hasEnchantments()) {
            item.getNamedTag().remove("ench");
        }
    }

    @ApiBinding
    public int getEnchantCount() {
        if (item != null && item.hasEnchantments()) {
            Enchantment[] enchantments = item.getEnchantments();
            return enchantments.length;
        }
        return 0;
    }

    @ApiBinding
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

    @ApiBinding
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

    @ApiBinding("getEnchants")
    public ScriptableObject getEnchantsForScript() {
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

    @ApiBinding
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

    // CUSTOM NAME & TAG

    @ApiBinding
    public String getCustomName() {
        return item != null ? item.getCustomName() : "";
    }

    @ApiBinding
    public void setCustomName(String name) {
        if (item != null) {
            item.setCustomName(name);
        }
    }

    @ApiBinding
    public NativeCompoundTag getCompoundTag() {
        CompoundTag tag = item != null ? item.getNamedTag() : null;
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    @ApiBinding
    public void setCompoundTag(NativeCompoundTag tag) {
        if (item != null) {
            item.setNamedTag(tag.tag);
            applyCustomDataJSON();
        }
    }

    // CUSTOM DATA

    @ApiBinding
    public String getAllCustomData() {
        if (item != null) {
            CompoundTag tag = item.getNamedTag();
            return tag != null ? tag.getString(ItemUtils.INNER_CORE_TAG_NAME) : null;
        }
        return null;
    }

    @ApiBinding
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

    @ApiBinding
    public void removeCustomData() {
        customData = null;
        customDataLoaded = false;
        customDataDirty = false;
        setAllCustomData(null);
    }

    private JSONObject customData = null;
    private boolean customDataLoaded = false;
    private boolean customDataDirty = false;

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
        customDataDirty = false;
    }

    private ItemInstanceExtra putObject(String name, Object value) {
        JSONObject data = getOrCreateCustomDataJSON();
        try {
            data.put(name, value);
            customDataDirty = true;
        } catch (JSONException ignore) {}
        return this;
    }

    @ApiBinding
    public String getString(String name, String fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optString(name, fallback);
        }
        return fallback;
    }

    @ApiBinding
    public String getString(String name) {
        return getString(name, null);
    }

    @ApiBinding
    public ItemInstanceExtra putString(String name, String value) {
        return putObject(name, value);
    }

    @ApiBinding
    public int getInt(String name, int fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optInt(name, fallback);
        }
        return fallback;
    }

    @ApiBinding
    public int getInt(String name) {
        return getInt(name, 0);
    }

    @ApiBinding
    public ItemInstanceExtra putInt(String name, int value) {
        return putObject(name, value);
    }

    @ApiBinding
    public long getLong(String name, long fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optLong(name, fallback);
        }
        return fallback;
    }

    @ApiBinding
    public long getLong(String name) {
        return getLong(name, 0);
    }

    @ApiBinding
    public ItemInstanceExtra putLong(String name, long value) {
        return putObject(name, value);
    }

    @ApiBinding
    public double getFloat(String name, double fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optDouble(name, fallback);
        }
        return fallback;
    }

    @ApiBinding
    public double getFloat(String name) {
        return getFloat(name, 0);
    }

    @ApiBinding
    public ItemInstanceExtra putFloat(String name, double value) {
        return putObject(name, value);
    }

    @ApiBinding
    public boolean getBoolean(String name, boolean fallback) {
        JSONObject data = getCustomDataJSON();
        if (data != null) {
            return data.optBoolean(name, fallback);
        }
        return fallback;
    }

    @ApiBinding
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    @ApiBinding
    public ItemInstanceExtra putBoolean(String name, boolean value) {
        return putObject(name, value);
    }

    @ApiBinding("getSerializable")
    public Object getScriptableSerializable(String name) {
        SerializationContext ctx = new SerializationContext();
        return ScriptableSerializer.scriptableFromJsonString(getString(name), ctx, true);
    }

    @ApiBinding("putSerializable")
    public ItemInstanceExtra putScriptableSerializable(String name, Object obj) {
        SerializationContext ctx = new SerializationContext();
        return putString(name, ScriptableSerializer.scriptableToJsonString(obj, ctx));
    }

    @Override
    public String toString() {
        return "ItemExtra{json=" + asJson() + "}";
    }

    // LEGACY NATIVE BIND

    private static long constructNew() {
        return ITEMS.addPointer(Item.AIR_ITEM.clone());
    }

    public static long constructClone(long ptr) {
        return ITEMS.addPointer(ITEMS.get(ptr));
    }

    public static long constructClone(Item ptr) {
        return ITEMS.addPointer(ptr.clone());
    }

    private static void finalizeNative(long ptr) {
        ITEMS.removePointer(ptr);
    }

    // INTERNAL HELPERS

    public long getValue() {
        if (customDataDirty) applyCustomDataJSON();
        return item.getOrCreateNamedTag().isEmpty() ? 0 : ptr;
    }

    public static ItemInstanceExtra unwrapObject(Object extra) {
        if (extra instanceof Wrapper) {
            extra = ((Wrapper) extra).unwrap();
        }
        if (extra instanceof ItemInstanceExtra) {
            return (ItemInstanceExtra) extra;
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
        if (extra instanceof ItemInstanceExtra) {
            return ((ItemInstanceExtra) extra).getValue();
        }
        if (extra instanceof Number) {
            return ((Number) extra).intValue();
        }
        return 0;
    }

    public static long getValueOrNullPtr(ItemInstanceExtra extra) {
        return extra != null ? extra.getValue() : 0;
    }

    public static ItemInstanceExtra getExtraOrNull(long extra) {
        return extra != 0 ? new ItemInstanceExtra(extra) : null;
    }

    public static ItemInstanceExtra getExtraOrNull(Item extra) {
        return extra != null ? new ItemInstanceExtra(extra) : null;
    }

    public static ItemInstanceExtra fromJson(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        ItemInstanceExtra extra = new ItemInstanceExtra();
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
