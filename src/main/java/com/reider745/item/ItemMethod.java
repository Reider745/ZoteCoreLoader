package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import com.reider745.InnerCoreServer;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;
import com.reider745.hooks.item.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.IDRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.innercore.icstd.inventory.ItemInstanceExtra;

import java.util.ArrayList;
import java.util.HashMap;

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
        public static final String ENCHANTABILITY_TYPE = "enchantability_type";
        public static final String ENCHANTABILITY_VALUE = "enchantability_value";
        public static final String ARMOR_DAMAGEABLE = "armor_damageable";
        public static final String REPAIRS = "repairs";
        public static final String CREATIVE_CATEGORY = "creative_category";
        public static final String FIRE_RESISTANT = "fire_resistant";
        public static final String EXPLODABLE = "explodable";

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
        if (InnerCoreServer.canEndLoadMods())
            return ItemUtils.get(id, data).getMaxDurability();
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

    private static final Int2IntMap CREATIVE_CATEGORY = new Int2IntOpenHashMap();

    public static void setCreativeCategory(int id, int val) {
        CREATIVE_CATEGORY.put(id, val);
    }

    public static int getCreativeCategory(int id) {
        CustomManager manager = getCustomManager(id);
        if (manager != null)
            return manager.get(PropertiesNames.CREATIVE_CATEGORY, 1);
        if(id != 0)//Какова вообще фига?
            Logger.warning("not get CreativeCategory " + id);
        return 1;
    }

    public static int getMaxStackForId(int id, int data) {
        if (InnerCoreServer.canEndLoadMods())
            return ItemUtils.get(id, data).getMaxStackSize();
        CustomManager manager = getCustomManager(id);
        if (manager != null)
            return manager.get(PropertiesNames.MAX_STACK, 64);
        return 64;
    }

    public static String getNameForId(int id, int data, ItemInstanceExtra extra) {
        CustomManager manager = CustomBlock.getBlockManager(id);
        if (manager != null)
            return NameTranslation.translate(CustomBlock.getVariants(manager).get(data));

        manager = getCustomManager(id);
        if (manager != null)
            return NameTranslation.translate(manager.get(PropertiesNames.NAME));
        return ItemUtils.get(id, data).getName();
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
        Item item = ItemUtils.get(id);
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
                    vanillaMapping = vanillaMapping.toLowerCase().replace(" ", "_");
                }
            }
            if (vanillaMapping != null) {
                return (item instanceof ItemBlock ? "block:" : "item:") + vanillaMapping;
            }
        }
        return null;
    }

    public static void setHandEquipped(CustomManager manager, boolean val) {
        manager.put(PropertiesNames.HAND_EQUIPPED, val);
    }

    public static void setLiquidClip(CustomManager manager, boolean val) {
        manager.put(PropertiesNames.LIQUID_CLIP, val);
    }

    public static void setMaxUseDuration(CustomManager manager, int val) {
        manager.put(PropertiesNames.MAX_USE_DURATION, val);
    }

    public static void setStackedByData(CustomManager manager, boolean val) {
        manager.put(PropertiesNames.STACKED_BY_DATA, val);
    }

    public static void setEnchantability(CustomManager manager, int type, int value) {
        int enchantability = manager.get(PropertiesNames.ENCHANTABILITY_TYPE, 0);
        manager.put(PropertiesNames.ENCHANTABILITY_TYPE, type | enchantability);
        manager.put(PropertiesNames.ENCHANTABILITY_VALUE, value);
    }

    public static void setArmorDamageable(CustomManager manager, boolean value) {
        manager.put(PropertiesNames.ARMOR_DAMAGEABLE, value);
    }

    public static Int2BooleanMap despawn = new Int2BooleanOpenHashMap();

    public static void setShouldDespawn(int id, boolean value) {
        despawn.put(id, value);
    }

    public static boolean canShouldDespawn(int id){
        return despawn.getOrDefault(id, true);
    }

    public static void setFireResistant(CustomManager manager, boolean resist) {
        if (resist || (manager.get(PropertiesNames.ID, 0) >= Item.NETHERITE_INGOT
                && manager.get(PropertiesNames.ID, 0) <= Item.NETHERITE_SCRAP)) {
            manager.put(PropertiesNames.FIRE_RESISTANT, resist);
        } else {
            manager.remove(PropertiesNames.FIRE_RESISTANT);
        }
    }

    public static HashMap<Integer, Boolean> explodable = new HashMap<>();

    public static void setExplodable(int id, boolean value) {
        explodable.put(id, value);
    }

    public static boolean canExplodable(int id){
        return explodable.getOrDefault(id, true);
    }

    public static boolean isArmorDamageable(int id) {
        Item item = Item.get(id);
        return !item.isUnbreakable();
    }

    public static int getArmorValue(int id) {
        Item item = Item.get(id);
        if(item != null)
            return item.getArmorPoints();
        return 0;
    }

    public static void addRepairItemId(CustomManager manager, int id) {
        ArrayList<Integer> repairs = manager.get(PropertiesNames.REPAIRS);
        if (repairs == null) {
            manager.put(PropertiesNames.REPAIRS, repairs = new ArrayList<>());
        }
        repairs.add(id);

        Repairs.update(manager.get(PropertiesNames.ID), repairs);
    }
}
