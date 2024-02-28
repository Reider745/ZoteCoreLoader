package com.zhekasmirnov.innercore.api;

import com.reider745.api.CustomManager;
import com.reider745.api.ZoteOnly;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;
import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.apparatus.ecs.ECS;
import com.zhekasmirnov.apparatus.ecs.core.ComponentCollection;
import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import com.zhekasmirnov.apparatus.ecs.types.ECSTags;
import com.zhekasmirnov.apparatus.ecs.types.item.ArmorItemComponent;
import com.zhekasmirnov.apparatus.ecs.types.item.ItemComponent;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;

import cn.nukkit.item.Item;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashSet;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeItem {
    public static final int MAX_ITEM_ID = 32768;
    private static NativeItem[] itemById = new NativeItem[MAX_ITEM_ID];

    public int id;
    private CustomManager properties;
    public final String nameId;
    public final String nameToDisplay;

    public final int entity;

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(ItemComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    @Deprecated(since = "Zote")
    protected NativeItem(int id, long ptr, String nameId, String nameToDisplay) {
        throw new UnsupportedOperationException("NativeItem(id, ptr, nameId, nameToDisplay)");
    }

    protected NativeItem(int id, CustomManager itemManager, String nameId, String nameToDisplay) {
        this.id = id;
        this.properties = itemManager;
        this.nameId = nameId;
        this.nameToDisplay = nameToDisplay;
        itemById[id] = this;

        EntityManager em = ECS.getEntityManager();
        this.entity = em.createEntity();
        em.extend(entity, initCC.setValues(new ItemComponent(id, nameId, nameToDisplay)));
    }

    public static NativeItem newNativeItem(int id, CustomManager manager, String nameId, String name) {
        return new NativeItem(id, manager, nameId, name);
    }

    @Deprecated(since = "Zote")
    public void setGlint(boolean val) {
        setGlint(properties, val);
    }

    public void setHandEquipped(boolean val) {
        setHandEquipped(properties, val);
    }

    public void setLiquidClip(boolean val) {
        setLiquidClip(properties, val);
    }

    @Deprecated(since = "Zote")
    public void setUseAnimation(int val) {
        setUseAnimation(properties, val);
    }

    public void setMaxUseDuration(int val) {
        setMaxUseDuration(properties, val);
    }

    public void setMaxDamage(int val) {
        setMaxDamage(properties, val);
    }

    public void setMaxStackSize(int val) {
        setMaxStackSize(properties, val);
    }

    public void setStackedByData(boolean val) {
        setStackedByData(properties, val);
    }

    @Deprecated(since = "Zote")
    public void setAllowedInOffhand(boolean val) {
        setAllowedInOffhand(properties, val);
    }

    public void setCreativeCategory(int val) {
        setCreativeCategory(properties, val);
    }

    @Deprecated(since = "Zote")
    public void setProperties(String val) {
        setProperties(properties, val);
    }

    public void setEnchantType(int type, int value) {
        setEnchantability(type, value);
    }

    // TODO: Nukkit-MOT's getEnchantAbility not used anywhere.
    public void setEnchantability(int type, int value) {
        setEnchantability(properties, type, value);
    }

    public void setEnchantType(int type) {
        setEnchantType(type, 1);
    }

    public void addRepairItem(int id) {
    }

    public void addRepairItems(int... ids) {
        for (int id : ids) {
            addRepairItem(id);
        }
    }

    public void setArmorDamageable(boolean value) {
        setArmorDamageable(properties, value);
    }

    @ZoteOnly
    public void setFireResistant(boolean resist) {
        setFireResistant(properties, resist);
    }

    @JSStaticFunction
    public static NativeItem getItemById(int id) {
        if (id < 0 || id >= MAX_ITEM_ID) {
            return null;
        }
        return itemById[id];
    }

    @JSStaticFunction
    public static NativeItem createItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        return new NativeItem(id, constructItem(id, nameId, name, iconName, iconIndex), nameId, name);
    }

    @JSStaticFunction
    public static NativeItem createFoodItem(int id, String nameId, String name, String iconName, int iconIndex,
            int food) {
        nameId = NativeAPI.convertNameId(nameId);
        return new NativeItem(id, constructFoodItem(id, nameId, name, iconName, iconIndex, food), nameId, name);
    }

    private static final ComponentCollection armorCC = new ComponentCollection()
            .setTypes(ArmorItemComponent.COMPONENT_ID);

    @JSStaticFunction
    public static NativeItem createArmorItem(int id, String nameId, String name, String iconName, int iconIndex,
            String texture, int slot, int defense, int durability, double knockbackResist) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        ArmorItemComponent component = new ArmorItemComponent(slot, defense, (float) knockbackResist);
        NativeItem item = new NativeItem(id, constructArmorItem(id, nameId, name, iconName, iconIndex, texture, slot,
                defense, durability, (float) knockbackResist), nameId, name);
        // TODO: ArmorRegistry.registerArmor(id, component);
        ECS.getEntityManager().extend(item.entity, armorCC.setValues(component));
        return item;
    }

    private static final ComponentCollection throwableCC = new ComponentCollection().setTypes("tag:throwable-item");

    @JSStaticFunction
    public static NativeItem createThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        NativeItem item = new NativeItem(id, constructThrowableItem(id, nameId, name, iconName, iconIndex), nameId,
                name);
        ECS.getEntityManager().extend(item.entity, throwableCC);
        return item;
    }

    @JSStaticFunction
    public static boolean isGlintItemInstance(int id, int data, Object extra) {
        return NativeAPI.isGlintItemInstance(id, data, NativeItemInstanceExtra.unwrapObject(extra));
    }

    /*
     * native part
     */

    public static CustomManager constructItem(int id, String nameId, String name, String iconName, int iconIndex) {
        return CustomItem.registerItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name));
    }

    public static CustomManager constructFoodItem(int id, String nameId, String name, String iconName, int iconIndex,
            int food) {
        return CustomItem.registerItemFood(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name),
                food);
    }

    public static CustomManager constructArmorItem(int id, String nameId, String name, String iconName, int iconIndex,
            String texture, int slot, int defense, int durability, float knockbackResist) {
        return CustomItem.registerArmorItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name),
                slot, defense, durability, (float) knockbackResist);
    }

    public static CustomManager constructThrowableItem(int id, String nameId, String name, String iconName,
            int iconIndex) {
        return CustomItem.registerThrowableItem(nameId, id,
                NameTranslation.fixUnicodeIfRequired("item_" + nameId, name));
    }

    @Deprecated(since = "Zote")
    public static void setGlint(CustomManager manager, boolean val) {
    }

    public static void setHandEquipped(CustomManager manager, boolean val) {
        ItemMethod.setHandEquipped(manager, val);
    }

    public static void setLiquidClip(CustomManager manager, boolean val) {
        ItemMethod.setLiquidClip(manager, val);
    }

    @Deprecated(since = "Zote")
    public static void setUseAnimation(CustomManager manager, int val) {
    }

    public static void setMaxUseDuration(CustomManager manager, int val) {
        ItemMethod.setMaxUseDuration(manager, val);
    }

    public static void setMaxDamage(CustomManager manager, int val) {
        ItemMethod.setMaxDamage(manager, val);
    }

    public static void setMaxStackSize(CustomManager manager, int val) {
        ItemMethod.setMaxStackSize(manager, val);
    }

    public static void setStackedByData(CustomManager manager, boolean val) {
        ItemMethod.setStackedByData(manager, val);
    }

    @Deprecated(since = "Zote")
    public static void setAllowedInOffhand(CustomManager manager, boolean val) {
        // JIC if something changed, check PlayerOffhandInventory.OFFHAND_ITEMS
    }

    public static void setCreativeCategory(CustomManager manager, int val) {
        ItemMethod.setCreativeCategory(manager, val);
    }

    @Deprecated(since = "Zote")
    public static void setProperties(CustomManager manager, String val) {
    }

    public static void setEnchantability(CustomManager manager, int type, int value) {
        ItemMethod.setEnchantability(manager, type, value);
    }

    public static void setArmorDamageable(CustomManager manager, boolean value) {
        ItemMethod.setArmorDamageable(manager, value);
    }

    @ZoteOnly
    public static void setFireResistant(CustomManager manager, boolean resist) {
        if (resist || (manager.get(PropertiesNames.ID, 0) >= Item.NETHERITE_INGOT
                && manager.get(PropertiesNames.ID, 0) <= Item.NETHERITE_SCRAP)) {
            manager.put(PropertiesNames.FIRE_RESISTANT, resist);
        } else {
            manager.remove(PropertiesNames.FIRE_RESISTANT);
        }
    }

    public static void addRepairItemId(CustomManager manager, int id) {
        ItemMethod.addRepairItemId(manager, id);
    }

    public static int getMaxStackForId(int id, int data) {
        return ItemMethod.getMaxStackForId(id, data);
    }

    public static int getMaxDamageForId(int id, int data) {
        return ItemMethod.getMaxDamageForId(id, data);
    }

    public static String getNameForId(int id, int data, Item extra) {
        return ItemMethod.getNameForId(id, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static String getNameForId(int id, int data, NativeItemInstanceExtra extra) {
        return ItemMethod.getNameForId(id, data, extra);
    }

    public static void setCreativeCategoryForId(int id, int category) {
        ItemMethod.setCreativeCategory(CustomManager.getFor(id), category);
    }

    public static String getNameForId(int id, int data) {
        return getNameForId(id, data, (NativeItemInstanceExtra) null);
    }

    @JSStaticFunction
    public static boolean isValid(int id) {
        return NativeAPI.getStringIdAndTypeForIntegerId(id) != null;
    }

    public static void addToCreativeInternal(int id, int count, int data, Item extra) {
        CustomItem.addCreative(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void addToCreativeInternal(int id, int count, int data, NativeItemInstanceExtra extra) {
        CustomItem.addCreative(id, count, data, extra);
    }

    @JSStaticFunction
    public static void addToCreativeGroup(String groupName, String displayName, int id) {
        CustomItem.addToCreativeGroup(groupName, id);
    }

    @JSStaticFunction
    public static void addToCreative(int id, int count, int data, Object extra) {
        addToCreativeInternal(id, count, data, NativeItemInstanceExtra.unwrapObject(extra));
    }

    @JSStaticFunction
    public static void setCategoryForId(int id, int category) {
        setCreativeCategoryForId(id, category);
    }

    @JSStaticFunction
    public static void addCreativeGroup(String groupName, String displayName, NativeArray ids) {
        for (int i = 0; i < ids.getLength(); i++) {
            addToCreativeGroup(groupName, displayName, ((Number) ids.get(i)).intValue());
        }
    }

    public static final Object DYNAMIC_ICON_LOCK = new Object();
    public static final Object DYNAMIC_NAME_LOCK = new Object();

    private static HashSet<Integer> itemIdsWithDynamicIcon = new HashSet<>();

    @Deprecated(since = "Zote")
    public static void setItemRequiresIconOverride(int id, boolean enabled) {
        if (enabled) {
            itemIdsWithDynamicIcon.add(id);
        } else {
            itemIdsWithDynamicIcon.remove(id);
        }
    }

    @Deprecated(since = "Zote")
    public static boolean isDynamicIconItem(int id) {
        return itemIdsWithDynamicIcon.contains(id);
    }

    private static boolean isInnerCoreUIOverride = false;
    private static String lastIconOverridePath = null;

    @Deprecated(since = "Zote")
    public static void overrideItemIcon(String name, int index) {
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

    @Deprecated(since = "Zote")
    public static String getLastIconOverridePath() {
        return lastIconOverridePath;
    }

    @Deprecated(since = "Zote")
    public static synchronized String getDynamicItemIconOverride(int id, int count, int data,
            NativeItemInstanceExtra extra) {
        if (isDynamicIconItem(id)) {
            synchronized (DYNAMIC_ICON_LOCK) {
                isInnerCoreUIOverride = true;
                lastIconOverridePath = null;
                Callback.invokeAPICallback("ItemIconOverride", new ItemInstance(id, count, data, extra), true);
                isInnerCoreUIOverride = false;
                return lastIconOverridePath;
            }
        } else {
            return null;
        }
    }
}
