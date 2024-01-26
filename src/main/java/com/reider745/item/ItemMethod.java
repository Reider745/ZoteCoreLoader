package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.IDRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;

import java.util.ArrayList;

public class ItemMethod {
    public static boolean isPostLoad = false;

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

    private static CustomManager getCustomManager(int id) {
        CustomManager manager = CustomItem.getItemManager(id);
        if (manager == null)
            manager = CustomBlock.getBlockManager(id);
        return manager;
    }

    public static int getMaxDamageForId(int id, int data) {
        if (isPostLoad)
            return Item.get(id, data).getMaxDurability();
        CustomManager manager = getCustomManager(id);
        if (manager != null)
            return manager.get(PropertiesNames.MAX_DAMAGE, 0);
        return 0;
    }

    public static void setMaxDamage(CustomManager manager, int damage) {
        manager.put(PropertiesNames.MAX_DAMAGE, damage);
    }

    public static void setMaxStackSize(CustomManager manager, int stack) {
        manager.put(PropertiesNames.MAX_STACK, stack);
    }

    public static void setCreativeCategory(CustomManager ptr, int val) {
        ptr.put(PropertiesNames.CREATIVE_CATEGORY, val);
    }

    public static int getCreativeCategory(int id) {
        CustomManager ptr = getCustomManager(id);
        if (ptr != null)
            return ptr.get(PropertiesNames.CREATIVE_CATEGORY, 1);
        Logger.warning("not get CreativeCategory " + id);
        return 0;
    }

    public static int getMaxStackForId(int id, int data) {
        if (isPostLoad)
            return Item.get(id, data).getMaxStackSize();
        CustomManager manager = getCustomManager(id);
        if (manager != null)
            return manager.get(PropertiesNames.MAX_STACK, 64);
        return 64;
    }

    public static String getNameForId(int id, int data, long extra) {
        CustomManager manager = CustomBlock.getBlockManager(id);
        if (manager != null)
            return NameTranslation.translate(CustomBlock.getVariants(manager).get(data));

        manager = getCustomManager(id);
        if (manager != null)
            return NameTranslation.translate(manager.get(PropertiesNames.NAME));
        return Item.get(id, data).getName();
    }

    public static String getStringIdAndTypeForIntegerId(int id) {
        String vanillaMapping;
        if (id >= IDRegistry.BLOCK_ID_OFFSET) {
            vanillaMapping = CustomBlock.getTextIdForNumber(id);
            if (vanillaMapping != null) {
                return "block:" + vanillaMapping;
            }
        } else if (id >= IDRegistry.ITEM_ID_OFFSET) {
            vanillaMapping = CustomItem.getTextIdForNumber(id);
            if (vanillaMapping != null) {
                return "item:" + vanillaMapping;
            }
        }
        vanillaMapping = IDRegistry.getStringIdAndTypeForVanillaId(id);
        if (vanillaMapping != null) {
            return vanillaMapping;
        }
        Item item = Item.get(id);
        if (item != null) {
            vanillaMapping = item.getNamespaceId();
            if (vanillaMapping != null) {
                int delimiter = vanillaMapping.indexOf(':');
                if (delimiter != -1) {
                    vanillaMapping = vanillaMapping.substring(delimiter + 1);
                }
            }
            if (vanillaMapping == null) {
                vanillaMapping = item.getName();
                if (vanillaMapping != null) {
                    vanillaMapping = vanillaMapping.toLowerCase().replace(" ", "");
                }
            }
            if (vanillaMapping != null) {
                return (item instanceof ItemBlock ? "block:" : "item:") + vanillaMapping;
            }
        }
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
        ptr.put(PropertiesNames.ARMOR_DAMAGEABLE, !value);
    }

    public static void addRepairItemId(CustomManager ptr, int id) {
        ArrayList<Integer> repairs = ptr.get(PropertiesNames.REPAIRS);
        if (repairs == null)
            repairs = new ArrayList<>();

        repairs.add(id);

        ptr.put(PropertiesNames.REPAIRS, repairs);
        Repairs.update(ptr.get(PropertiesNames.ID), repairs);
    }
}
