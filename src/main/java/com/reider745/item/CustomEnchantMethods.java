package com.reider745.item;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.*;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentType;
import com.reider745.api.CallbackHelper;
import com.reider745.api.Client;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;
import com.zhekasmirnov.innercore.api.NativeCallback;

// TODO: onEnchantPostAttack, onEnchantPostHurt,

public class CustomEnchantMethods {
    private static final PointersStorage<Enchant> pointers = new PointersStorage<>("enchants", new PointerGenFastest(), false);
    private static class Enchant extends Enchantment {
        private int minLevel, maxLevel, frequency;
        private final int[] types = new int[2];
        private Item item_book = null;
        private boolean isLootable, isDiscoverable, isMeleeDamage, isProtection, isTreasure_;

        protected Enchant(int id, String name, Rarity rarity, EnchantmentType type) {
            super(id, name, rarity, type);
            Enchantment.register(this, false);
        }

        @Override
        public int getMinLevel() {
            return minLevel;
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public boolean isTreasure() {
            return isTreasure_;
        }

        public void postInit(){
            item_book = Item.get(Item.ENCHANTED_BOOK);

            item_book.addEnchantment(this);
        }

        public boolean canEnchantItem(int id, Item item) {
            if (id == 16383 || id == -1) {
                return true;
            } else if (item instanceof ItemArmor) {
                if (item.isArmor()) {
                    return true;
                } else {
                    switch (id) {
                        case 1:
                            return item.isHelmet();
                        case 2:
                            return item.isChestplate();
                        case 3:
                            return item.isLeggings();
                        case 4:
                            return item.isBoots();
                        default:
                            return false;
                    }
                }
            } else {
                if((item.isSword() || item.isAxe() || item instanceof ItemBow) && id == 16)
                    return true;
                switch (id) {
                    case 16:
                        return item.isSword();
                    case 64:
                        return item.isHoe();
                    case 32:
                        return item instanceof ItemBow;
                    case 4096:
                        return item instanceof ItemFishingRod;
                   /* case WEARABLE:
                        return item instanceof ItemSkull;*/
                    /*case TRIDENT:
                        return item instanceof ItemTrident;*/
                    default:
                        return false;
                }
            }
        }

        @Override
        public boolean canEnchant(Item item) {
            for(int type : types)
                if(type != 0 && !canEnchantItem(id, item))
                    return false;
            return true;
        }

        @Override
        public double getDamageBonus(Entity entity) {
            if(!isMeleeDamage)
                return 0;

            Item item = entity instanceof EntityHuman human ? human.getInventory().getItemInHand() : Item.AIR_ITEM.clone();

            return super.getDamageBonus(entity) + CallbackHelper.applyCustomValue("onEnchantGetDamageBonus", () ->
                    NativeCallback.onEnchantGetDamageBonus(this.id, item.getAttackDamage(), entity.getId()), 0f).getValue();
        }

        @Override
        public float getProtectionFactor(EntityDamageEvent event) {
            if(!isProtection)
                return 0;

            return super.getProtectionFactor(event) + CallbackHelper.applyCustomValue("onEnchantGetProtectionBonus", () ->
                            NativeCallback.onEnchantGetProtectionBonus(this.id, (int) event.getDamage() * 2, event.getCause().ordinal(), event.getEntity().getId()),
                    0f).getValue();
        }
    }

    public static long constructNew(int id, String nameId) {
        return pointers.addPointer(new Enchant(id, nameId, Enchantment.Rarity.COMMON, EnchantmentType.ALL));
    }

    @Client //Наверно клиент
    public static void setDescription(long pointer, String description) {
    }

    public static void setFrequency(long pointer, int frequency) {
        pointers.get(pointer).frequency = frequency;
    }

    public static void setIsLootable(long pointer, boolean isLootable) {
        pointers.get(pointer).isLootable = isLootable;
    }

    public static void setIsDiscoverable(long pointer, boolean isDiscoverable) {
        pointers.get(pointer).isDiscoverable = isDiscoverable;
    }

    public static void setIsTreasure(long pointer, boolean isTreasure) {
        pointers.get(pointer).isTreasure_ = isTreasure;
    }

    public static void setIsMeleeDamageEnchant(long pointer, boolean value) {
        pointers.get(pointer).isMeleeDamage = value;
    }

    public static void setIsProtectionEnchant(long pointer, boolean value) {
        pointers.get(pointer).isProtection = value;
    }

    public static void setMasks(long pointer, int mask1, int mask2) {
        Enchant enchant = pointers.get(pointer);

        enchant.types[0] = mask1;
        enchant.types[1] = mask2;
    }

    public static void setMinMaxLevel(long pointer, int minLevel, int maxLevel) {
        Enchant enchant = pointers.get(pointer);
        enchant.minLevel = minLevel;
        enchant.maxLevel = maxLevel;
    }

    public static void setMinMaxCostPoly(long pointer, float aMin, float bMin, float cMin, float aMax, float bMax, float cMax) {
    }

    public static void passCurrentDamageBonus(float bonus) {
        CallbackHelper.setValueForCurrent(bonus);
    }

    public static void passCurrentProtectionBonus(float bonus) {
        CallbackHelper.setValueForCurrent(bonus);
    }

    public static void postInit(){
        pointers.getPointers().values().forEach(v -> v.get().postInit());
    }
}