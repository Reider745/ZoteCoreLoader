package org.innercore.icstd.modules;

import cn.nukkit.item.Item;
import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;
import com.reider745.api.CustomManager;
import com.reider745.hooks.item.ItemUtils;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;
import com.reider745.item.NukkitIdConverter;
import dalvik.annotation.optimization.FastNative;
import org.checkerframework.checker.units.qual.A;
import org.innercore.icstd.annotations.ArgOverload;
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
    public static int getIntegerIdByNameId(String nameId) {
        if(nameId == null) return -1;

        int id = IDRegistry.getCustomItemIdByName(nameId);
        if(id != 0) return id;

        Item item = Item.fromString(nameId);
        return NukkitIdConverter.getInnerCoreForNukkit(item.getId(), item.getDamage()).id;
    }

    @ApiBinding
    public static long constructItem(int id, String nameId, String name, String icon, int iconMeta) {
        CustomItem.registerItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name));

        return 0;// NOT USE
    }

    @ApiBinding
    public static long constructArmorItem(int id, String nameId, String name, String iconName, int iconMeta, String texture, int slot, int defense, int durability, float knockbackResist) {
        CustomItem.registerArmorItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name),
                slot, defense, durability, knockbackResist);

        return 0;// NOT USE
    }

    @ApiBinding
    public static long constructThrowableItem(int id, String nameId, String name, String iconName, int iconMeta) {
        CustomItem.registerThrowableItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name));

        return 0;// NOT USE
    }

    // TODO: The server might need to know whether the item has the glint effect.

    @ApiBinding
    @ClientOnly
    public static boolean isGlint(int id, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static boolean isGlint(String nameId, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return false;
    }


    @ApiBinding
    @ClientOnly
    public static int getMaxUseDuration(int id, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return ItemUtils.get(id, 1, data, ItemInstanceExtra.getExtraOrNull(extra)).getMaxDurability();
    }

    @ApiBinding
    @ClientOnly
    public static int getMaxUseDuration(String nameId, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return getMaxUseDuration(getIntegerIdByNameId(nameId), data, extra);
    }

    @ApiBinding
    public static void addToCreative(int id, @ArgOverload("1") int count, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        CustomItem.addCreative(id, count, data, ItemInstanceExtra.getExtraOrNull(extra));
    }

    @ApiBinding
    public static void addToCreative(String nameId, @ArgOverload("1") int count, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        addToCreative(getIntegerIdByNameId(nameId), count, data, extra);
    }

    @ApiBinding
    public static boolean isFood(int id) {
        return CustomItem.isFood(id);
    }

    @ApiBinding
    public static boolean isFood(String nameId) {
        return isFood(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isArmor(int id) {
        return CustomItem.isArmor(id);
    }

    @ApiBinding
    public static boolean isArmor(String nameId) {
        return isArmor(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isThrowable(int id) {
        return CustomItem.isThrowable(id);
    }

    @ApiBinding
    public static boolean isThrowable(String nameId) {
        return isThrowable(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static int getNutrition(int id) {
        return CustomItem.getNutrition(id);
    }

    @ApiBinding
    public static int getNutrition(String nameId) {
        return getNutrition(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static int getArmorValue(int id) {
        return ItemMethod.getArmorValue(id);
    }

    @ApiBinding
    public static int getArmorValue(String nameId) {
        return getArmorValue(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isArmorDamageable(int id) {
        return ItemMethod.isArmorDamageable(id);
    }

    @ApiBinding
    public static boolean isArmorDamageable(String nameId) {
        return isArmorDamageable(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    @ClientOnly
    public static boolean isRequiresIconOverride(int id) {
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static boolean isRequiresIconOverride(String nameId) {
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static boolean isToolRender(int id) {
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static boolean isToolRender(String nameId) {
        return false;
    }

    @ApiBinding
    public static boolean isHandEquipped(int id) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.isHandEquipped");
        return false;
    }

    @ApiBinding
    public static boolean isHandEquipped(String nameId) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.isHandEquipped");
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static int getUseAnimation(int id) {
        return 0;
    }

    @ApiBinding
    @ClientOnly
    public static int getUseAnimation(String nameId) {
        return 0;
    }

    @ApiBinding
    public static boolean isStackedByData(int id) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.isStackedByData");
        return false;// TODO: nukkit not support
    }

    @ApiBinding
    public static boolean isStackedByData(String nameId) {
        return isStackedByData(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isAllowedInOffhand(int id) {
        return ItemMethod.isAllowedInOffhand(id);
    }

    @ApiBinding
    public static boolean isAllowedInOffhand(String nameId) {
        return isAllowedInOffhand(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isShouldDespawn(int id) {
        return ItemMethod.canShouldDespawn(id);
    }

    @ApiBinding
    public static boolean isShouldDespawn(String nameId) {
        return isShouldDespawn(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isFireResistant(int id) {
        return ItemMethod.isFireResistant(id);
    }

    @ApiBinding
    public static boolean isFireResistant(String nameId) {
        return isFireResistant(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static boolean isExplodable(int id) {
        return ItemMethod.canExplodable(id);
    }

    @ApiBinding
    public static boolean isExplodable(String nameId) {
        return isExplodable(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    @ClientOnly
    public static boolean isMirroredSprite(int id) {
        return false;
    }

    @ApiBinding
    @ClientOnly
    public static boolean isMirroredSprite(String nameId) {
        return false;
    }

    @ApiBinding
    public static int getEnchantSlot(int id) {
        return ItemMethod.getEnchantSlot(id);
    }

    @ApiBinding
    public static int getEnchantSlot(String nameId) {
        return getEnchantSlot(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static int getEnchantValue(int id) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.getEnchantValue");
        return 0;
    }

    @ApiBinding
    public static int getEnchantValue(String nameId) {
        return getEnchantValue(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static int getMaxDamage(int id, @ArgOverload("0") int data) {
        return ItemMethod.getMaxDamageForId(id, data);
    }

    @ApiBinding
    public static int getMaxDamage(String nameId, @ArgOverload("0") int data) {
        return ItemMethod.getMaxDamageForId(getIntegerIdByNameId(nameId), data);
    }

    @ApiBinding
    public static int getCategory(int id) {
        return ItemMethod.getCategory(id);
    }

    @ApiBinding
    public static int getCategory(String nameId) {
        return getCategory(getIntegerIdByNameId(nameId));
    }

    @ApiBinding
    public static int getCreativeCategory(int id) {
        return getCategory(id);
    }

    @ApiBinding
    public static int getCreativeCategory(String nameId) {
        return getCategory(nameId);
    }

    @ApiBinding
    public static boolean isLiquidClip(int id, @ArgOverload("0") int data) {
        return ItemMethod.isLiquidClip(id);
    }

    @ApiBinding
    public static boolean isLiquidClip(String nameId, @ArgOverload("0") int data) {
        return isLiquidClip(getIntegerIdByNameId(nameId), data);
    }

    @ApiBinding
    public static int getMaxStackSize(int id, @ArgOverload("0") int data) {
        return ItemMethod.getMaxStackForId(id, data);
    }

    @ApiBinding
    public static int getMaxStackSize(String nameId, @ArgOverload("0") int data) {
        return getMaxStackSize(getIntegerIdByNameId(nameId), data);
    }

    @ApiBinding
    public static int getMaxStack(int id, @ArgOverload("0") int data) {
        return ItemMethod.getMaxStackForId(id, data);
    }

    @ApiBinding
    public static int getMaxStack(String nameId, @ArgOverload("0") int data) {
        return getMaxStackSize(getIntegerIdByNameId(nameId), data);
    }

    @ApiBinding
    public static String getName(int id, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return ItemMethod.getNameForId(id, data, ItemInstanceExtra.getExtraOrNull(extra));
    }

    @ApiBinding
    public static String getName(String nameId, @ArgOverload("0") int data, @ArgOverload("0") long extra) {
        return getName(getIntegerIdByNameId(nameId), data, extra);
    }

    @ApiBinding
    @ClientOnly
    public static void setGlint(int id, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setGlint(String nameId, boolean enabled) {}

    @ApiBinding
    public static void setToolRender(int id, boolean enabled) {}

    @ApiBinding
    public static void setToolRender(String nameId, boolean enabled) {}

    @ApiBinding
    public static void setHandEquipped(int id, boolean enabled) {
        ItemMethod.setHandEquipped(CustomItem.getItemManager(id), enabled);
    }

    @ApiBinding
    public static void setHandEquipped(String nameId, boolean enabled) {
        setHandEquipped(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    public static void setLiquidClip(int id, boolean enabled) {
        ItemMethod.setLiquidClip(CustomManager.getFor(id), enabled);
    }

    @ApiBinding
    public static void setLiquidClip(String nameId, boolean enabled) {
        setLiquidClip(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    @ClientOnly
    public static void setUseAnimation(int id, int animation) {}

    @ApiBinding
    @ClientOnly
    public static void setUseAnimation(String nameId, int animation) {}

    @ApiBinding
    public static void setMaxUseDuration(int id, int duration) {
        ItemMethod.setMaxUseDuration(CustomManager.getFor(id), duration);
    }

    @ApiBinding
    public static void setMaxUseDuration(String nameId, int duration) {
        setMaxUseDuration(getIntegerIdByNameId(nameId), duration);
    }

    @ApiBinding
    public static void setMaxDamage(int id, int damage) {
        ItemMethod.setMaxDamage(CustomManager.getFor(id), damage);
    }

    @ApiBinding
    public static void setMaxDamage(String nameId, int damage) {
        setMaxDamage(getIntegerIdByNameId(nameId), damage);
    }

    @ApiBinding
    public static void setMaxStack(int id, int size) {
        ItemMethod.setMaxStackSize(CustomManager.getFor(id), size);
    }

    @ApiBinding
    public static void setMaxStack(String nameId, int size) {
        setMaxStack(getIntegerIdByNameId(nameId), size);
    }

    @ApiBinding
    public static void setMaxStackSize(int id, int size) {
        setMaxStack(id, size);
    }

    @ApiBinding
    public static void setMaxStackSize(String nameId, int size) {
        setMaxStack(nameId, size);
    }

    @ApiBinding
    public static void setStackedByData(int id, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.setStackedByData");
    }

    @ApiBinding
    public static void setStackedByData(String nameId, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.setStackedByData");
    }

    @ApiBinding
    public static void setAllowedInOffhand(int id, boolean enabled) {
        ItemMethod.setAllowedInOffhand(id, enabled);
    }

    @ApiBinding
    public static void setAllowedInOffhand(String nameId, boolean enabled) {
        setAllowedInOffhand(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    public static void setShouldDespawn(int id, boolean enabled) {
        ItemMethod.setShouldDespawn(id, enabled);
    }

    @ApiBinding
    public static void setShouldDespawn(String nameId, boolean enabled) {
        setShouldDespawn(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    public static void setFireResistant(int id, boolean enabled) {
        ItemMethod.setFireResistant(CustomManager.getFor(id), enabled);
    }

    @ApiBinding
    public static void setFireResistant(String nameId, boolean enabled) {
        setFireResistant(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    public static void setExplodable(int id, boolean enabled) {
        ItemMethod.setExplodable(id, enabled);
    }

    @ApiBinding
    public static void setExplodable(String nameId, boolean enabled) {
        setExplodable(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    @ClientOnly
    public static void setMirroredSprite(int id, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setMirroredSprite(String nameId, boolean enabled) {}

    @ApiBinding
    public static void setProperties(int id, String properties) {
        InnerCoreServer.useNotCurrentSupport("ItemModule.setProperties");
    }

    @ApiBinding
    public static void setProperties(String nameId, String properties) {
        setProperties(getIntegerIdByNameId(nameId), properties);
    }

    @ApiBinding
    @ClientOnly
    public static void setRequiresIconOverride(int id, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setRequiresIconOverride(String nameId, boolean enabled) {}

    @ApiBinding
    public static void setArmorDamageable(int id, boolean enabled) {
        ItemMethod.setArmorDamageable(CustomManager.getFor(id), enabled);
    }

    @ApiBinding
    public static void setArmorDamageable(String nameId, boolean enabled) {
        setArmorDamageable(getIntegerIdByNameId(nameId), enabled);
    }

    @ApiBinding
    public static void setEnchantability(int id, int type, int value) {
        ItemMethod.setEnchantability(CustomManager.getFor(id), type, value);
    }

    @ApiBinding
    public static void setEnchantability(String nameId, int type, int value) {
        setEnchantability(getIntegerIdByNameId(nameId), type, value);
    }

    //TODO: Nukkit-MOT's getEnchantAbility not used anywhere.
    @ApiBinding
    public static void setEnchantType(int id, int type, int value) {
        setEnchantability(id, type, value);
    }

    @ApiBinding
    public static void setEnchantType(String nameId, int type, int value) {
        setEnchantability(nameId, type, value);
    }

    @ApiBinding
    public static void addRepairItemId(int id, int repairId) {
        ItemMethod.addRepairItemId(CustomManager.getFor(id), repairId);
    }

    @ApiBinding
    public static void addRepairItemId(int id, String repairId) {
        addRepairItemId(id, getIntegerIdByNameId(repairId));
    }

    @ApiBinding
    public static void addRepairItemId(String nameId, String repairId) {
        addRepairItemId(getIntegerIdByNameId(nameId), getIntegerIdByNameId(repairId));
    }

    @ApiBinding
    public static void addRepairItemId(String nameId, int repairId) {
        addRepairItemId(getIntegerIdByNameId(nameId), repairId);
    }

    @ApiBinding
    public static void addRepairItem(int id, int repairId) {
        addRepairItemId(id, repairId);
    }

    @ApiBinding
    public static void addRepairItem(int id, String repairId) {
        addRepairItemId(id, getIntegerIdByNameId(repairId));
    }

    @ApiBinding
    public static void addRepairItem(String nameId, String repairId) {
        addRepairItemId(getIntegerIdByNameId(nameId), getIntegerIdByNameId(repairId));
    }

    @ApiBinding
    public static void addRepairItem(String nameId, int repairId) {
        addRepairItemId(getIntegerIdByNameId(nameId), repairId);
    }

    @ApiBinding
    public static void setCreativeCategory(int id, int category) {
        ItemMethod.setCreativeCategory(id, category);
    }

    @ApiBinding
    public static void setCreativeCategory(String nameId, int category) {
        ItemMethod.setCreativeCategory(getIntegerIdByNameId(nameId), category);
    }

    @ApiBinding
    @ClientOnly
    public static void setNameOverrideCallbackForced(int id, boolean forced) {}

    @ApiBinding
    @ClientOnly
    public static void setNameOverrideCallbackForced(String nameId, boolean forced) {}

    @ApiBinding
    @ClientOnly
    public static void sendCachedNameOverride(int id, int data, String name) {}

    @ApiBinding
    @ClientOnly
    public static void sendCachedNameOverride(String nameId, int data, String name) {}

    @ApiBinding
    public static void addToCreativeGroup(String groupName, String displayName, int id) {
        CustomItem.addToCreativeGroup(groupName, id);
    }

    @ApiBinding
    public static void addToCreativeGroup(String groupName, String displayName, String nameId) {
        CustomItem.addToCreativeGroup(groupName, getIntegerIdByNameId(nameId));
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
        NativeAPI.invokeUseItemOn(id, count, data, ItemInstanceExtra.unwrapValueItem(extra), x, y, z, side, (float) vx, (float) vy, (float) vz, Entity.unwrapEntity(entity));
    } 

    @ApiBinding
    public static void invokeUseItemNoTarget(int id, int count, int data, Object extra) {
        NativeAPI.invokeUseItemNoTarget(id, count, data, ItemInstanceExtra.unwrapValueItem(extra));
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
