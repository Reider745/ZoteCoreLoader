package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.reider745.api.CustomManager;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;
import com.zhekasmirnov.apparatus.ecs.ECS;
import com.zhekasmirnov.apparatus.ecs.core.ComponentCollection;
import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import com.zhekasmirnov.apparatus.ecs.types.ECSTags;
import com.zhekasmirnov.apparatus.ecs.types.item.ArmorItemComponent;
import com.zhekasmirnov.apparatus.ecs.types.item.ItemComponent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
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
    private CustomManager pointer;
    public final String nameId;
    public final String nameToDisplay;

    public final int entity;

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(ItemComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    protected NativeItem(int id, CustomManager ptr, String nameId, String nameToDisplay) {
        this.id = id;
        this.pointer = ptr;
        this.nameId = nameId;
        this.nameToDisplay = nameToDisplay;
        itemById[id] = this;
        NameTranslation.sendNameToGenerateCache(id, 0, nameToDisplay);

        EntityManager em = ECS.getEntityManager();
        entity = em.createEntity();
        em.extend(entity, initCC.setValues(new ItemComponent(id, nameId, nameToDisplay)));
    }

    public void setGlint(boolean val) {
        setGlint(pointer, val);
    }

    public void setHandEquipped(boolean val) {
        setHandEquipped(pointer, val);
    }

    public void setLiquidClip(boolean val) {
        setLiquidClip(pointer, val);
    }

    public void setUseAnimation(int val) {
        setUseAnimation(pointer, val);
    }

    public void setMaxUseDuration(int val){
        //setMaxUseDuration(pointer, val);
    }

    public void setMaxDamage(int val) {
        setMaxDamage(pointer, val);
    }

    public void setMaxStackSize(int val) {
        setMaxStackSize(pointer, val);
    }

    public void setStackedByData(boolean val) {
        setStackedByData(pointer, val);
    }

    public void setAllowedInOffhand(boolean val) {
        setAllowedInOffhand(pointer, val);
    }

    public void setCreativeCategory(int val){
        setCreativeCategory(pointer, val);
    }

    public void setProperties(String val){
        setProperties(pointer, val);
    }

    public void setEnchantType(int type, int value) {
        setEnchantability(pointer, type, value);
    }

    public void setEnchantability(int type, int value) {
        setEnchantability(pointer, type, value);
    }

    public void setEnchantType(int type) {
        setEnchantType(type, 1);
    }

    public void addRepairItem(int id) {
        addRepairItemId(pointer, id);
    }

    public void addRepairItems(int ... ids) {
        for (int id : ids) {
            addRepairItemId(pointer, id);
        }
    }

    public void setArmorDamageable(boolean value) {
        setArmorDamageable(pointer, value);
    }



    private static void registerIcon(int id, String iconName, int iconIndex) {
        String name = ResourcePackManager.getItemTextureName(iconName, iconIndex);
        if (name != null) {
            NativeAPI.addTextureToLoad(name);
            NativeItemModel.getFor(id, 0).setItemTexturePath(name);
        }
        // ItemIconSource.instance.registerIcon(id, name);
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

        /*if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
            Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        registerIcon(id, iconName, iconIndex);*/
        return new NativeItem(id, constructItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex), nameId, name);
    }

    public static NativeItem createFoodItem(int id, String nameId, String name, int food){
        nameId = NativeAPI.convertNameId(nameId);
        return new NativeItem(id, CustomItem.registerItemFood(nameId, id, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), food), nameId, name);
    }

    private static final ComponentCollection armorCC = new ComponentCollection().setTypes(ArmorItemComponent.COMPONENT_ID);

    @JSStaticFunction
    public static NativeItem createArmorItem(int id, String nameId, String name, String iconName, int iconIndex, String texture, int slot, int defense, int durability, double knockbackResist) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        
        if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
            //Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        ArmorItemComponent component = new ArmorItemComponent(slot, defense, (float) knockbackResist);
        registerIcon(id, iconName, iconIndex);
        NativeItem item = new NativeItem(id, constructArmorItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex, texture, slot, defense, durability, (float) knockbackResist), nameId, name);
        //ArmorRegistry.registerArmor(id, component);
        ECS.getEntityManager().extend(item.entity, armorCC.setValues(component));
        return item;
    }

    private static final ComponentCollection throwableCC = new ComponentCollection().setTypes("tag:throwable-item");

    @JSStaticFunction
    public static NativeItem createThrowableItem(int id, String nameId, String name, String iconName, int iconIndex) {
        nameId = NativeAPI.convertNameId(nameId); // any name id must be lowercase
        
        if (!ResourcePackManager.isValidItemTexture(iconName, iconIndex)) {
           // Logger.debug("WARNING", "invalid item icon: " + iconName + " " + iconIndex);
            iconName = "missing_icon";
            iconIndex = 0;
        }
        registerIcon(id, iconName, iconIndex);
        NativeItem item = new NativeItem(id, constructThrowableItem(id, nameId, NameTranslation.fixUnicodeIfRequired("item_" + nameId, name), iconName, iconIndex), nameId, name);
        ECS.getEntityManager().extend(item.entity, throwableCC);
        return item;
    }

    @JSStaticFunction
    public static boolean isGlintItemInstance(int id, int data, Object extra) {
        return NativeAPI.isGlintItemInstance(id, data, NativeItemInstanceExtra.unwrapValue(extra));
    }



    /*
     * native part
     */

    public static CustomManager constructItem(int id, String nameId, String name, String iconName, int iconIndex){
        return CustomItem.registerItem(nameId, id, name);
    }

    public static CustomManager constructArmorItem(int id, String nameId, String name, String iconName, int iconIndex, String texture, int slot, int defense, int durability, float knockbackResist){
        return CustomItem.registerArmorItem(nameId, id, name, slot, defense, durability, knockbackResist);
    }

    public static CustomManager constructThrowableItem(int id, String nameId, String name, String iconName, int iconIndex){
        return CustomItem.registerThrowableItem(nameId, id, name);
    }



    public static void setGlint(CustomManager ptr, boolean val){

    }

    public static void setHandEquipped(CustomManager ptr, boolean val){
        ItemMethod.setHandEquipped(ptr, val);
    }

    public static void setLiquidClip(CustomManager ptr, boolean val){
        ItemMethod.setLiquidClip(ptr, val);
    }

    public static void setUseAnimation(CustomManager ptr, int val){}

    public static void setMaxUseDuration(CustomManager ptr, int val){
        ItemMethod.setMaxUseDuration(ptr, val);
    }

    public static void setMaxDamage(CustomManager ptr, int val){
        ItemMethod.setMaxDamage(ptr, val);
    }

    public static void setMaxStackSize(CustomManager ptr, int val){
        ItemMethod.setMaxStackSize(ptr, val);
    }
    
    public static void setStackedByData(CustomManager ptr, boolean val){
        ItemMethod.setStackedByData(ptr, val);
    }
    
    public static void setAllowedInOffhand(CustomManager ptr, boolean val){
        ItemMethod.setAllowedInOffhand(ptr, val);
    }

    public static void setCreativeCategory(CustomManager ptr, int val){
        ItemMethod.setCreativeCategory(ptr, val);
    }

    public static void setProperties(CustomManager ptr, String val){
        //InnerCoreServer.useNotSupport("setProperties");
    }

    public static void setEnchantability(CustomManager ptr, int type, int value){
        ItemMethod.setEnchantability(ptr, type, value);
    }

    public static void setArmorDamageable(CustomManager ptr, boolean value){
        ItemMethod.setArmorDamageable(ptr, value);
    }
    
    public static void addRepairItemId(CustomManager ptr, int id){
        ItemMethod.addRepairItemId(ptr, id);
    }



    public static int getMaxStackForId(int id, int data){
        return ItemMethod.getMaxStackForId(id, data);
    }

    public static int getMaxDamageForId(int id, int data){
        return ItemMethod.getMaxDamageForId(id, data);
    }

    public static String getNameForId(int id, int data, long extra){
        return ItemMethod.getNameForId(id, data, extra);
    }

    public static void setCreativeCategoryForId(int id, int category){
    }
    
    public static String getNameForId(int id, int data) {
        return getNameForId(id, data, 0);
    }


    @JSStaticFunction
    public static boolean isValid(int id) {
        return NativeAPI.getStringIdAndTypeForIntegerId(id) != null;
    }


    public static void addToCreativeInternal(int id, int count, int data, long extra){
        CustomItem.addCreative(id, count, data, extra);

        //Item.addCreativeItem(Item.get(id, data, count));
    }

    @JSStaticFunction
    public static void addToCreativeGroup(String groupName, String displayName, int id){
        CustomItem.addToCreativeGroup(groupName, id);
    }

    @JSStaticFunction
    public static void addToCreative(int id, int count, int data, Object extra) {
        addToCreativeInternal(id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
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
        }
        else {
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
        synchronized(DYNAMIC_ICON_LOCK) {
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

    public static synchronized String getDynamicItemIconOverride(int id, int count, int data, NativeItemInstanceExtra extra) {
        if (isDynamicIconItem(id)) {
            synchronized(DYNAMIC_ICON_LOCK) {
                isInnerCoreUIOverride = true;
                lastIconOverridePath = null;
                Callback.invokeAPICallback("ItemIconOverride", new ItemInstance(id, count, data, extra), true);
                isInnerCoreUIOverride = false;
                return lastIconOverridePath;
            }
        }
        else {
            return null;
        }
    }
}
