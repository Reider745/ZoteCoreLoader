package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.reider745.item.ItemExtraDataProvider;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;
import com.zhekasmirnov.innercore.api.runtime.saver.JsonHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
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
    private boolean isFinalizable = false;
    private ItemExtraDataProvider provider;

    public boolean isFinalizableInstance() {
        return isFinalizable;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        ItemUtils.removePointer(ptr);
        /*if (isFinalizable) {
            finalizeNative(ptr); // TODO: !!! make extra ptr for java extra instance unique in any case
        }*/
    }

    public NativeItemInstanceExtra(long extra) {
        ObjectSaverRegistry.registerObject(this, saverId);

        if (extra != 0) {
            ptr = extra;
        }
        else {
            ptr = constructNew(); 
        }
        
        isFinalizable = nativeIsFinalizable(ptr);
        provider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        provider.extra = this;
    }

    public NativeItemInstanceExtra() {
        ObjectSaverRegistry.registerObject(this, saverId);
        ptr = constructNew();
        provider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        provider.extra = this;
        isFinalizable = nativeIsFinalizable(ptr);
    }

    public NativeItemInstanceExtra(NativeItemInstanceExtra extra) {
        ObjectSaverRegistry.registerObject(this, saverId);

        long val = getValueOrNullPtr(extra);
        if (val != 0) {
            ptr = constructClone(val);
        }
        else {
            ptr = constructNew();
        }
        provider = (ItemExtraDataProvider) ItemUtils.items_pointers.getInstance(ptr);
        provider.extra = this;
        isFinalizable = nativeIsFinalizable(ptr);
    }

    public ItemExtraDataProvider getProvider() {
        return provider;
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

    public NativeItemInstanceExtra copy() {
        return new NativeItemInstanceExtra(this);
    }

    public long getValue() {
        return getExtraPtr(ptr);
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
        return nativeIsEnchanted(ptr);
    }

    public void addEnchant(int type, int level) {
        nativeAddEnchant(ptr, type, level);
    }

    public int getEnchantLevel(int type) {
        return nativeGetEnchantLevel(ptr, type);
    }

    public void removeEnchant(int type) {
        nativeRemoveEnchant(ptr, type);
    }

    public void removeAllEnchants() {
        nativeRemoveAllEnchants(ptr);
    }

    public int getEnchantCount() {
        return nativeGetEnchantCount(ptr);
    }

    public String getEnchantName(int id, int lvl) {
        return nativeEnchantToString(id, lvl);
    }

    public String getEnchantName(int id) {
        return getEnchantName(id, getEnchantLevel(id));
    }

    public int[][] getRawEnchants() {
        ScriptableObject result = ScriptableObjectHelper.createEmpty();

        int count = getEnchantCount();
        int ids[] = new int[count];
        int levels[] = new int[count];
        nativeGetAllEnchants(ptr, ids, levels);
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
        return nativeGetModExtra(ptr);
    }

    public void setAllCustomData(String extra) {
        nativeSetModExtra(ptr, extra);
    }

    public String getCustomName() {
        return nativeGetCustomName(ptr);
    }

    public void setCustomName(String name) {
        nativeSetCustomName(ptr, name);
    }

    public NativeCompoundTag getCompoundTag() {
        CompoundTag tag = nativeGetCompoundTag(ptr);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        nativeSetCompoundTag(ptr, tag != null ? tag.pointer : null);
    }



    private JSONObject customData = null;
    private boolean customDataLoaded = false;

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
        }
        else {
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
        /*if (obj == null) {
            putString(name, null);
            return this;
        }
        ScriptableObject saved = ObjectSaverRegistry.saveObject(obj);
        if (saved != null) {
            String str = JsonHelper.scriptableToJsonString(saved, false);
            putString(name, str);
        } else {
            putString(name, null);
        }
        return this;*/
    }

    public Object getSerializable(String name) {
        try {
            return ScriptableSerializer.scriptableFromJson(ScriptableSerializer.stringToJson(getString(name)));
        } catch (JSONException e) {
            return null;
        }

        /*try {
            String str = getString(name);
            if (str != null) {
                return JsonHelper.parseJsonString(str);
            } else {
                return null;
            }
        } catch(JSONException e) {
            return null;
        }*/
    }

    public void removeCustomData() {
        customData = null;
        customDataLoaded = false;
        setAllCustomData(null);
    }

    @Override
    public String toString() {
        /* JSONObject object = new JSONObject();
        try {
            object.put("customData", customData);
		} catch (JSONException e) {
            e.printStackTrace();
            return "error";
		}
        return object.toString(); */
        return "ItemExtra{json=" + asJson() + "}";
    }

    public long getPtr() {
        return ptr;
    }

    private static long constructNew(){
        ItemExtraDataProvider instance = new ItemExtraDataProvider(null);
        return ItemUtils.items_pointers.addPointer(instance);
    }
    public static long constructClone(long ptr){
        ItemExtraDataProvider instance = new ItemExtraDataProvider(ItemUtils.items_pointers.getInstance(ptr).getReference());
        return ItemUtils.items_pointers.addPointer(instance);
    }
    private static long getExtraPtr(long ptr){
        return ptr;
    }
    private static void finalizeNative(long ptr){

    }
    private static boolean nativeIsFinalizable(long ptr){
        return true;
    }

    private boolean nativeIsEnchanted(long ptr){
        Item item = provider.get();
        if(item == null) return false;
        return item.hasEnchantments();
    }
    private void nativeAddEnchant(long ptr, int id, int level){
        Item item = provider.get();
        if(item != null) {
            Enchantment enchantment = Enchantment.getEnchantment(id);
            enchantment.setLevel(level);
            item.addEnchantment(enchantment);
        }
    }
    private void nativeRemoveEnchant(long ptr, int id){
        Item item = provider.get();
        if(item != null && item.hasEnchantments()) {

            ListTag<CompoundTag> ench = item.getNamedTag().getList("ench", CompoundTag.class);
            for(int i = 0;i < ench.size();i++)
                if (ench.get(i).getShort("id") == id) {
                    ench.remove(i);
                    return;
                }
        }
    }
    private void nativeRemoveAllEnchants(long ptr){
        Item item = provider.get();
        if(item != null && item.hasEnchantments())
            item.getNamedTag().remove("ench");
    }
    private int nativeGetEnchantLevel(long ptr, int id){
        Item item = provider.get();
        if(item != null && item.hasEnchantments())
            return item.getEnchantmentLevel(id);
        return 0;
    }
    private int nativeGetEnchantCount(long ptr){
        Item item = provider.get();
        if(item != null && item.hasEnchantments())
            return item.getEnchantments().length;
        return 0;
    }
    private void nativeGetAllEnchants(long ptr, int[] ids, int[] levels){
        Item item = provider.get();
        if(item != null){
            Enchantment[] enchantments = item.getEnchantments();
            for (int i = 0; i < ids.length; i++){
                Enchantment enchantment = enchantments[i];

                ids[i] = enchantment.id;
                levels[i] = enchantment.getLevel();
            }
        }
    }
    private String nativeGetModExtra(long ptr){
        Item item = provider.get();
        if(item != null)
            return item.getNamedTag().getString(ItemUtils.INNER_CORE_TAG_NAME);
        return null;
    }
    private void nativeSetModExtra(long ptr, String extra){
        customData = new JSONObject(extra);
        /*Item item = provider.get();
        if(item != null)
            item.getNamedTag().putString(ItemUtils.INNER_CORE_TAG_NAME, extra);*/
    }
    private String nativeGetCustomName(long ptr){
        Item item = provider.get();
        if(item != null)
            return item.getCustomName();
        return "";
    }
    private void nativeSetCustomName(long ptr, String extra){
        Item item = provider.get();
        if(item != null)
            item.setCustomName(extra);
    }
    private String nativeEnchantToString(int id, int level){
        Item item = provider.get();
        if(item != null)
            return item.getEnchantment(id).getName();
        return "";
    }
    private CompoundTag nativeGetCompoundTag(long ptr){
        return provider.getCompoundTag();
    }
    private void nativeSetCompoundTag(long ptr, CompoundTag tag){
        provider.setCompoundTag(tag);
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
