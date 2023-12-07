package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;

import java.util.ArrayList;

public class ItemMethod {
    public static class PropertiesNames {
        public static final String ID = "id";
        public static final String MAX_DAMAGE = "max_damage";
        public static final String MAX_STACK = "max_stack";
        public static final String NAME = "name";
        public static final String HAND_EQUIPPED = "hand_equipped";
        public static final String LIQUID_CLIP = "liquid_clip";
        public static final String MAX_USE_DURATION = "max_use_duration";
        public static final String STACKED_BY_DATA = "stacked_by_data";
        public static final String ALLOWED_IN_OFFHAND = "allowed_in_offhand";
        public static final String ENCHANTABILITY_TYPE = "enchantability_type";
        public static final String ENCHANTABILITY_VALUE = "enchantability_value";
        public static final String ARMOR_DAMAGEABLE = "armor_damageable";
        public static final String REPAIRS = "repairs";
        public static final String CREATIVE_CATEGORY = "creative_category";

        public static class Armors {
            public static final String SLOT = "slot";
            public static final String DEFENSE = "defense";
            public static final String KNOCKBACK_RESIST = "knockbackResist";
        }
    }
    private static CustomManager getCustomManager(int id){
        return CustomItem.getItemManager(id);
    }

    public static int getMaxDamageForId(int id, int data){
        CustomManager manager = getCustomManager(id);
        if(manager != null)
            return manager.get(PropertiesNames.MAX_DAMAGE);
        return Item.get(id, data).getMaxDurability();
    }

    public static void setMaxDamage(CustomManager manager, int id){
        manager.put(PropertiesNames.MAX_DAMAGE, id);
    }

    public static void setMaxStackSize(CustomManager manager, int stack){
        manager.put(PropertiesNames.MAX_STACK, stack);
    }

    public static void setCreativeCategory(CustomManager ptr, int val) {
        ptr.put(PropertiesNames.CREATIVE_CATEGORY, val);
    }

    public static int getCreativeCategory(int id) {
        if(id > 8000)
            return 1;
        CustomManager ptr = getCustomManager(id);
        if(ptr != null)
            return ptr.get(PropertiesNames.CREATIVE_CATEGORY, 3);
        throw new RuntimeException("not get CreativeCategory "+id);
    }

    public static int getMaxStackForId(int id, int data){
        if(id >= 8000)
            return 64;
        CustomManager manager = getCustomManager(id);
        if(manager != null)
            return manager.get(PropertiesNames.MAX_STACK);
        return Item.get(id, data).getMaxStackSize();
    }

    public static String getNameForId(int id, int data, long extra){
        CustomManager manager = CustomBlock.getBlockManager(id);
        if(manager != null)
            return CustomBlock.getVariants(manager).get(data);

        manager = getCustomManager(id);
        if(manager != null)
            return manager.get(PropertiesNames.NAME);
        return Item.get(id, data).getName();
    }

    public static String getStringIdAndTypeForIntegerId(int id){
        Item item = Item.get(id);
        if(item != null)
            return (item instanceof ItemBlock ? "block" : "item") + " :" + id;
        return null;
    }

    public static void setHandEquipped(CustomManager ptr, boolean val) {
        ptr.put(PropertiesNames.HAND_EQUIPPED, val);
    }

    public static void setLiquidClip(CustomManager ptr, boolean val) {
        ptr.put(PropertiesNames.LIQUID_CLIP, val);
    }

    public static void setMaxUseDuration(CustomManager ptr, int val) {
        ptr.put(PropertiesNames.MAX_USE_DURATION, val);
    }

    public static void setStackedByData(CustomManager ptr, boolean val) {
        ptr.put(PropertiesNames.STACKED_BY_DATA, val);
    }

    public static void setAllowedInOffhand(CustomManager ptr, boolean val) {
        ptr.put(PropertiesNames.ALLOWED_IN_OFFHAND, val);
    }

    public static void setEnchantability(CustomManager ptr, int type, int value) {
        ptr.put(PropertiesNames.ENCHANTABILITY_TYPE, type);
        ptr.put(PropertiesNames.ENCHANTABILITY_VALUE, value);
    }

    public static void setArmorDamageable(CustomManager ptr, boolean value) {
        ptr.put(PropertiesNames.ARMOR_DAMAGEABLE, value);
    }

    public static void addRepairItemId(CustomManager ptr, int id) {
        ArrayList<Integer> repairs = ptr.get(PropertiesNames.REPAIRS);
        if(repairs == null) repairs = new ArrayList<>();

        repairs.add(id);

        ptr.put(PropertiesNames.REPAIRS, repairs);
        Repairs.update(ptr.get(PropertiesNames.ID), repairs);
    }
}
