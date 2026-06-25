package org.innercore.icstd.modules;

import org.innercore.icstd.inventory.ItemInstanceExtra;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Undefined;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.gen.ItemBindGenBase;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.Entity;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;

import org.jetbrains.annotations.Nullable;

@ApiBinding("Item")
public class ItemModule extends ItemBindGenBase {
    @ApiBinding
    @Deprecated
    public static NativeItem getItemById(int id) {
        return NativeItem.getItemById(id);
    }

    @ApiBinding
    @Deprecated
    public static NativeItem getItemById(@Nullable String nameId) {
        return getItemById(getNumericId(nameId));
    }

    @ApiBinding
    public static int getNumericId(@Nullable String nameId) {
        if (nameId != null) {
            int numericId = IDRegistry.getCustomItemIdByName(nameId);
            if (numericId != 0) {
                return numericId;
            }
        }
		ICLog.e("InnerCore-ItemRegistry", "Invalid item namedID: " + nameId + ", -1 will be returned", null);
        return -1;
    }

    @ApiBinding
    public static int getNumericId(int id) {
        return id; // for compatibility reasons
    }

    private static void registerIcon(int id, String iconName, int iconIndex) {
        String name = ResourcePackManager.getItemTextureName(iconName, iconIndex);
        if (name != null) {
            NativeAPI.addTextureToLoad(name);
            NativeItemModel.getFor(id, 0).setItemTexturePath(name);
        }
        // ItemIconSource.instance.registerIcon(id, name);
    }

    @ApiBinding
    public static NativeItem createItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
            Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        registerIcon(id, iconName, iconIndex);
        constructItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex);
        return NativeItem.constructLegacyItemComponent(id, nameId, name);
    }

    @ApiBinding
    public static NativeItem createArmorItem(int id, String nameId, String name, String iconName, int iconIndex, String texture, int slot, int defense, int durability, float knockbackResist) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
            Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        registerIcon(id, iconName, iconIndex);
        constructArmorItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex, texture, slot, defense, durability, (float) knockbackResist);
        return NativeItem.constructLegacyArmorComponent(id, nameId, iconName, slot, defense, knockbackResist);
    }

    @ApiBinding
    public static NativeItem createThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
            Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        registerIcon(id, iconName, iconIndex);
        constructThrowableItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex);
        return NativeItem.constructLegacyThrowableComponent(id, nameId, iconName);
    }

    @ApiBinding
    public static NativeItem createFoodItem(int id, String nameId, String name, String iconName, int iconIndex, int food) {
        NativeItem item = createItem(id, nameId, name, iconName, iconIndex);
        String props = "{\"use_animation\":\"eat\",\"use_duration\": 32,\"food\":{\"nutrition\":" + food 
                + ",\"saturation_modifier\": \"normal\",\"is_meat\": false}, \"components\": {\"minecraft:food\": {\"nutrition\": " 
                + food + ", \"saturation_modifier\": \"normal\"}}}";
        setProperties(id, props);
        setUseAnimation(id, 1);
        setMaxUseDuration(id, 32);
        return item;
    }

    @ApiBinding
    public static boolean isGlint(int id, int data, Object extra) {
        return isGlint(id, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static boolean isGlint(@Nullable String nameId, int data, Object extra) {
        return isGlint(nameId, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static int getMaxUseDuration(int id, int data, Object extra) {
        return getMaxUseDuration(id, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static int getMaxUseDuration(@Nullable String nameId, int data, Object extra) {
        return getMaxUseDuration(nameId, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static void setCategory(int id, int category) {
        setCreativeCategory(id, category);
    }

    @ApiBinding
    public static void setCategory(@Nullable String nameId, int category) {
        setCreativeCategory(nameId, category);
    }

    @ApiBinding
    public static void addToCreative(int id, int count, int data, Object extra) {
        addToCreative(id, count, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static void addToCreative(@Nullable String nameId, int count, int data, Object extra) {
        addToCreative(nameId, count, data, ItemInstanceExtra.unwrapValue(extra));
    }

    @ApiBinding
    public static void addCreativeGroup(String groupName, String displayName, NativeArray ids) {
        for (int offset = 0; offset < ids.getLength(); offset++) {
            Object id = ids.get(offset);
            if (id instanceof Number) {
                addToCreativeGroup(groupName, displayName, ((Number) id).intValue());
            } else if (id instanceof CharSequence || id == null || id == Undefined.instance) {
                addToCreativeGroup(groupName, displayName, id == null || id == Undefined.instance ? null : id.toString());
            } else {
                throw new IllegalArgumentException("Item.addCreativeGroup received malformed id property at index " + offset);
            }
        }
    }

    @ApiBinding
    public static void addRepairItems(int id, NativeArray repairIds) {
        for (int offset = 0; offset < repairIds.getLength(); offset++) {
            Object repairId = repairIds.get(offset);
            if (repairId instanceof Number) {
                addRepairItemId(id, ((Number) repairId).intValue());
            } else if (repairId instanceof CharSequence || repairId == null || repairId == Undefined.instance) {
                addRepairItemId(id, repairId == null || repairId == Undefined.instance ? null : repairId.toString());
            } else {
                throw new IllegalArgumentException("Item.addRepairItems received malformed id property at index " + offset);
            }
        }
    }

    @ApiBinding
    public static void addRepairItems(@Nullable String id, NativeArray repairIds) {
        for (int offset = 0; offset < repairIds.getLength(); offset++) {
            Object repairId = repairIds.get(offset);
            if (repairId instanceof Number) {
                addRepairItemId(id, ((Number) repairId).intValue());
            } else if (repairId instanceof CharSequence || repairId == null || repairId == Undefined.instance) {
                addRepairItemId(id, repairId == null || repairId == Undefined.instance ? null : repairId.toString());
            } else {
                throw new IllegalArgumentException("Item.addRepairItems received malformed id property at index " + offset);
            }
        }
    }

    @ApiBinding
    public static void addRepairItemIds(int id, NativeArray repairIds) {
        addRepairItems(id, repairIds);
    }

    @ApiBinding
    public static void addRepairItemIds(@Nullable String id, NativeArray repairIds) {
        addRepairItems(id, repairIds);
    }

    @ApiBinding
    public static boolean isValid(int id) {
        return NativeAPI.getStringIdAndTypeForIntegerId(id) != null;
    }

    @ApiBinding
    public static boolean isValid(@Nullable String nameId) {
        return getIntegerIdByNameId(nameId) != -1;
    }

    @ApiBinding
    public static void invokeUseItemOn(int id, int count, int data, Object extra, int x, int y, int z, int side, double vx, double vy, double vz, Object entity) {
        NativeAPI.invokeUseItemOn(id, count, data, ItemInstanceExtra.unwrapValue(extra), x, y, z, side, (float) vx, (float) vy, (float) vz, Entity.unwrapEntity(entity));
    } 

    @ApiBinding
    public static void invokeUseItemNoTarget(int id, int count, int data, Object extra) {
        NativeAPI.invokeUseItemNoTarget(id, count, data, ItemInstanceExtra.unwrapValue(extra));
    }


    @ApiBinding
    public static void overrideCurrentName(String name) {
        NativeAPI.overrideItemName(name);
    }

    public static final Object DYNAMIC_ICON_LOCK = new Object();
    public static final Object DYNAMIC_NAME_LOCK = new Object();

    private static boolean isInnerCoreUIOverride = false;
    private static String lastIconOverridePath = null;

    @ApiBinding
    public static void overrideCurrentIcon(String name, int index) {
        synchronized (DYNAMIC_ICON_LOCK) {
            if (!isInnerCoreUIOverride) {
                NativeAPI.overrideItemIcon(name, index);
            }
            lastIconOverridePath = ResourcePackManager.getItemTextureName(name, index);
            if (lastIconOverridePath != null && !lastIconOverridePath.endsWith(".png")) {
                lastIconOverridePath += ".png";
            }
        }
    }

    @ApiBinding
    public static String getLastIconOverridePath() {
        return lastIconOverridePath;
    }

    @ApiBinding
    public static synchronized String getDynamicIconOverride(int id, int count, int data, ItemInstanceExtra extra) {
        if (isRequiresIconOverride(id)) {
            synchronized (DYNAMIC_ICON_LOCK) {
                isInnerCoreUIOverride = true;
                lastIconOverridePath = null;
                Callback.invokeAPICallback("ItemIconOverride", new ItemInstance(id, count, data, extra), true);
                isInnerCoreUIOverride = false;
                return lastIconOverridePath;
            }
        }
        return null;
    }
}
