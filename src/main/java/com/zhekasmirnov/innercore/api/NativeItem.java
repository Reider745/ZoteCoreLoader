package com.zhekasmirnov.innercore.api;

import com.reider745.api.CustomManager;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import org.innercore.icstd.modules.ItemModule;
import org.mozilla.javascript.annotations.JSStaticFunction;

import com.zhekasmirnov.apparatus.ecs.ECS;
import com.zhekasmirnov.apparatus.ecs.core.ComponentCollection;
import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import com.zhekasmirnov.apparatus.ecs.types.ECSTags;
import com.zhekasmirnov.apparatus.ecs.types.item.ArmorItemComponent;
import com.zhekasmirnov.apparatus.ecs.types.item.ItemComponent;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;

/**
 * Created by zheka on 26.07.2017.
 */
@Deprecated
public class NativeItem {
    public static final int MAX_ITEM_ID = 32768;
    private static NativeItem[] itemById = new NativeItem[MAX_ITEM_ID];

    public int id;
    public final String nameId;
    public final String nameToDisplay;

    public final int entity;

    protected NativeItem(int id, String nameId, String nameToDisplay) {
        this.id = id;
        this.nameId = nameId;
        this.nameToDisplay = nameToDisplay;
        itemById[id] = this;
        NameTranslation.sendNameToGenerateCache(id, 0, nameToDisplay);

        EntityManager em = ECS.getEntityManager();
        entity = em.createEntity();
        em.extend(entity, initCC.setValues(new ItemComponent(id, nameId, nameToDisplay)));
    }

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(ItemComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    public static NativeItem constructLegacyItemComponent(int id, String nameId, String name) {
        return new NativeItem(id, nameId, name);
    }

    public static NativeItem constructBlock(int id, CustomManager manager, String nameId, String name) {
        return new NativeItem(id, nameId, name);
    }


    private static final ComponentCollection armorCC = new ComponentCollection().setTypes(ArmorItemComponent.COMPONENT_ID);

    public static NativeItem constructLegacyArmorComponent(int id, String nameId, String name, int slot, int defense, float knockbackResist) {
        NativeItem item = new NativeItem(id, nameId, name);
        ArmorItemComponent component = new ArmorItemComponent(slot, defense, knockbackResist);
        ArmorRegistry.registerArmor(id, component);
        ECS.getEntityManager().extend(item.entity, armorCC.setValues(component));
        return item;
    }

    private static final ComponentCollection throwableCC = new ComponentCollection().setTypes("tag:throwable-item");

    public static NativeItem constructLegacyThrowableComponent(int id, String nameId, String name) {
        NativeItem item = new NativeItem(id, nameId, name);
        ECS.getEntityManager().extend(item.entity, throwableCC);
        return item;
    }

    // backwards compatibility methods
    public static NativeItem getItemById(int id) {
        if (id < 0 || id >= MAX_ITEM_ID) {
            return null;
        }
        AdaptedScriptAPI.logDeprecation("NativeItem.getItemById");
        return itemById[id];
    }

    public void setGlint(boolean val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setGlint");
        ItemModule.setGlint(id, val);
    }

    public void setHandEquipped(boolean val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setHandEquipped");
        ItemModule.setHandEquipped(id, val);
    }

    public void setLiquidClip(boolean val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setLiquidClip");
        ItemModule.setLiquidClip(id, val);
    }

    public void setUseAnimation(int val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setUseAnimation");
        ItemModule.setUseAnimation(id, val);
    }

    public void setMaxUseDuration(int val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setMaxUseDuration");
        ItemModule.setMaxUseDuration(id, val);
    }

    public void setMaxDamage(int val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setMaxDamage");
        ItemModule.setMaxDamage(id, val);
    }

    public void setMaxStackSize(int val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setMaxStackSize");
        ItemModule.setMaxStack(id, val);
    }

    public void setStackedByData(boolean val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setStackedByData");
        ItemModule.setStackedByData(id, val);
    }

    public void setAllowedInOffhand(boolean val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setAllowedInOffhand");
        ItemModule.setAllowedInOffhand(id, val);
    }

    public void setCreativeCategory(int val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setCreativeCategory");
        ItemModule.setCreativeCategory(id, val);
    }

    public void setProperties(String val) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setProperties");
        ItemModule.setProperties(id, val);
    }

    public void setEnchantType(int type, int value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setEnchantType");
        ItemModule.setEnchantType(id, type, value);
    }

    public void setEnchantability(int type, int value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setEnchantability");
        ItemModule.setEnchantType(id, type, value);
    }

    public void setEnchantType(int type) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setEnchantType");
        setEnchantType(type, 1);
    }

    public void addRepairItem(int repairId) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.addRepairItem");
        ItemModule.addRepairItem(id, repairId);
    }

    public void addRepairItems(int... repairIds) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.addRepairItems");
        for (int repairId : repairIds) {
            ItemModule.addRepairItem(id, repairId);
        }
    }

    public void setArmorDamageable(boolean value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setArmorDamageable");
        ItemModule.setArmorDamageable(id, value);
    }

    public void setShouldDespawn(boolean value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setShouldDespawn");
        ItemModule.setShouldDespawn(id, value);
    }

    public void setFireResistant(boolean value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setFireResistant");
        ItemModule.setFireResistant(id, value);
    }

    public void setExplodable(boolean value) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setExplodable");
        ItemModule.setExplodable(id, value);
    }


    @JSStaticFunction
    public static NativeItem createItem(int id, String nameId, String name, String iconName, int iconIndex) {
        AdaptedScriptAPI.logDeprecation("NativeItem.createItem");
        return ItemModule.createItem(id, nameId, name, iconName, iconIndex);
    }

    @JSStaticFunction
    public static NativeItem createArmorItem(int id, String nameId, String name, String iconName, int iconIndex, String texture, int slot, int defense, int durability, double knockbackResist) {
        AdaptedScriptAPI.logDeprecation("NativeItem.createArmorItem");
        return ItemModule.createArmorItem(id, nameId, name, iconName, iconIndex, texture, slot, defense, durability, (float) knockbackResist);
    }

    @JSStaticFunction
    public static NativeItem createThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        AdaptedScriptAPI.logDeprecation("NativeItem.createThrowableItem");
        return ItemModule.createThrowableItem(id, nameId, name, iconName, iconIndex);
    }

    @JSStaticFunction
    public static void setCreativeCategoryForId(int id, int category) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setCreativeCategoryForId");
        ItemModule.setCreativeCategory(id, category);
    }

    @JSStaticFunction
    public static void addToCreative(int id, int count, int data, Object extra) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.addToCreative");
        ItemModule.addToCreative(id, count, data, extra);
    }

    @JSStaticFunction
    public static String getDynamicItemIconOverride(int id, int count, int data, ItemInstanceExtra extra) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.getDynamicItemIconOverride");
        return ItemModule.getDynamicIconOverride(id, count, data, extra);
    }

    @JSStaticFunction
    public static void overrideItemIcon(String name, int index) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.overrideItemIcon");
        ItemModule.overrideCurrentIcon(name, index);
    }

    @JSStaticFunction
    public static void setItemRequiresIconOverride(int id, boolean enabled) {
        AdaptedScriptAPI.logIndevDeprecation("NativeItem.setItemRequiresIconOverride");
        ItemModule.setRequiresIconOverride(id, enabled);
    }
}
