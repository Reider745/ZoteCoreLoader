package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.reider745.api.Client;
import com.reider745.api.CustomManager;
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
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
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
    private CustomManager itemManager;
    public final String nameId;
    public final String nameToDisplay;

    public final int entity;

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(ItemComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    @Deprecated
    protected NativeItem(int id, long ptr, String nameId, String nameToDisplay) {
        throw new UnsupportedOperationException("NativeItem(id, ptr, nameIdm nameToDisplay)");
    }

    protected NativeItem(int id, CustomManager itemManager, String nameId, String nameToDisplay) {
        this.id = id;
        this.itemManager = itemManager;
        this.nameId = nameId;
        this.nameToDisplay = nameToDisplay;
        itemById[id] = this;
        NameTranslation.sendNameToGenerateCache(id, 0, nameToDisplay);

        EntityManager em = ECS.getEntityManager();
        this.entity = em.createEntity();
        em.extend(entity, initCC.setValues(new ItemComponent(id, nameId, nameToDisplay)));
    }

    public static NativeItem newNativeItem(int id, CustomManager ptr, String nameId, String name) {
        return new NativeItem(id, ptr, nameId, name);
    }

    @Client
    public void setGlint(boolean val) {
    }

    public void setHandEquipped(boolean val) {
        ItemMethod.setHandEquipped(itemManager, val);
    }

    public void setLiquidClip(boolean val) {
        ItemMethod.setLiquidClip(itemManager, val);
    }

    @Client
    public void setUseAnimation(int val) {
    }

    public void setMaxUseDuration(int val) {
        ItemMethod.setMaxUseDuration(itemManager, val);
    }

    public void setMaxDamage(int val) {
        ItemMethod.setMaxDamage(itemManager, val);
    }

    public void setMaxStackSize(int val) {
        ItemMethod.setMaxStackSize(itemManager, val);
    }

    public void setStackedByData(boolean val) {
        ItemMethod.setStackedByData(itemManager, val);
    }

    @Client
    public void setAllowedInOffhand(boolean val) {
        // JIC if something changed, check PlayerOffhandInventory.OFFHAND_ITEMS
    }

    public void setCreativeCategory(int val) {
        ItemMethod.setCreativeCategory(itemManager, val);
    }

    @Client
    public void setProperties(String val) {
    }

    public void setEnchantType(int type, int value) {
        setEnchantability(type, value);
    }

    // TODO: Nukkit-MOT's getEnchantAbility not used anywhere. 
    public void setEnchantability(int type, int value) {
        ItemMethod.setEnchantability(itemManager, type, value);
    }

    public void setEnchantType(int type) {
        setEnchantType(type, 1);
    }

    public void addRepairItem(int id) {
        ItemMethod.addRepairItemId(itemManager, id);
    }

    public void addRepairItems(int... ids) {
        for (int id : ids) {
            ItemMethod.addRepairItemId(itemManager, id);
        }
    }

    public void setArmorDamageable(boolean value) {
        ItemMethod.setArmorDamageable(itemManager, value);
    }

    public void setFireResistant(boolean resist) {
        if (resist || (id >= Item.NETHERITE_INGOT && id <= Item.NETHERITE_SCRAP)) {
            itemManager.put(PropertiesNames.FIRE_RESISTANT, resist);
        } else {
            itemManager.remove(PropertiesNames.FIRE_RESISTANT);
        }
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
        return new NativeItem(id,
                CustomItem.registerItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name)),
                nameId, name);
    }

    public static NativeItem createFoodItem(int id, String nameId, String name, String iconName, int iconIndex,
            int food) {
        nameId = NativeAPI.convertNameId(nameId);
        return new NativeItem(id, CustomItem.registerItemFood(nameId, id,
                NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), food), nameId, name);
    }

    private static final ComponentCollection armorCC = new ComponentCollection()
            .setTypes(ArmorItemComponent.COMPONENT_ID);

    @JSStaticFunction
    public static NativeItem createArmorItem(int id, String nameId, String name, String iconName, int iconIndex,
            String texture, int slot, int defense, int durability, double knockbackResist) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        ArmorItemComponent component = new ArmorItemComponent(slot, defense, (float) knockbackResist);
        NativeItem item = new NativeItem(id,
                CustomItem.registerArmorItem(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name),
                        slot, defense, durability, (float) knockbackResist),
                nameId, name);
        ArmorRegistry.registerArmor(id, component);
        ECS.getEntityManager().extend(item.entity, armorCC.setValues(component));
        return item;
    }

    private static final ComponentCollection throwableCC = new ComponentCollection().setTypes("tag:throwable-item");

    @JSStaticFunction
    public static NativeItem createThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        NativeItem item = new NativeItem(id, CustomItem.registerThrowableItem(nameId, id,
                NameTranslation.fixUnicodeIfRequired("item_" + nameId, name)), nameId, name);
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

    public static long constructItem(int id, String nameId, String name, String iconName, int iconIndex) {
        InnerCoreServer.useNotSupport("NativeItem.constructItem(id, nameId, name, iconName, iconIndex)");
        return 0;
    }

    public static long constructArmorItem(int id, String nameId, String name, String iconName, int iconIndex,
            String texture, int slot, int defense, int durability, float knockbackResist) {
        InnerCoreServer.useNotSupport(
                "NativeItem.constructArmorItem(id, nameId, name, iconName, iconIndex, texture, slot, defense, durability, knockbackResist)");
        return 0;
    }

    public static long constructThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        InnerCoreServer.useNotSupport("NativeItem.constructThrowableItem(id, nameId, name, iconName, iconIndex)");
        return 0;
    }

    public static void setGlint(long ptr, boolean val) {
        InnerCoreServer.useNotSupport("NativeItem.setGlint(ptr, val)");
    }

    public static void setHandEquipped(long ptr, boolean val) {
        InnerCoreServer.useNotSupport("NativeItem.setHandEquipped(ptr, val)");
    }

    public static void setLiquidClip(long ptr, boolean val) {
        InnerCoreServer.useNotSupport("NativeItem.setLiquidClip(ptr, val)");
    }

    public static void setUseAnimation(long ptr, int val) {
    }

    public static void setMaxUseDuration(long ptr, int val) {
        InnerCoreServer.useNotSupport("NativeItem.setMaxUseDuration(ptr, val)");
    }

    public static void setMaxDamage(long ptr, int val) {
        InnerCoreServer.useNotSupport("NativeItem.setMaxDamage(ptr, val)");
    }

    public static void setMaxStackSize(long ptr, int val) {
        InnerCoreServer.useNotSupport("NativeItem.setMaxStackSize(ptr, val)");
    }

    public static void setStackedByData(long ptr, boolean val) {
        InnerCoreServer.useNotSupport("NativeItem.setStackedByData(ptr, val)");
    }

    public static void setAllowedInOffhand(long ptr, boolean val) {
        InnerCoreServer.useNotSupport("NativeItem.setAllowedInOffhand(ptr, val)");
    }

    public static void setCreativeCategory(long ptr, int val) {
        InnerCoreServer.useNotSupport("NativeItem.setCreativeCategory(ptr, val)");
    }

    public static void setProperties(long ptr, String val) {
        InnerCoreServer.useNotSupport("NativeItem.setProperties(ptr, val)");
    }

    public static void setEnchantability(long ptr, int type, int value) {
        InnerCoreServer.useNotSupport("NativeItem.setEnchantability(ptr, type, value)");
    }

    public static void setArmorDamageable(long ptr, boolean value) {
        InnerCoreServer.useNotSupport("NativeItem.setArmorDamageable(ptr, value)");
    }

    public static void addRepairItemId(long ptr, int id) {
        InnerCoreServer.useNotSupport("NativeItem.addRepairItemId(ptr, id)");
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

    public static void setItemRequiresIconOverride(int id, boolean enabled) {
        if (enabled) {
            itemIdsWithDynamicIcon.add(id);
        } else {
            itemIdsWithDynamicIcon.remove(id);
        }
        NativeAPI.setItemRequiresIconOverride(id, enabled);
    }

    public static boolean isDynamicIconItem(int id) {
        return itemIdsWithDynamicIcon.contains(id);
    }

    private static boolean isInnerCoreUIOverride = false;
    private static String lastIconOverridePath = null;

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

    public static String getLastIconOverridePath() {
        return lastIconOverridePath;
    }

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
