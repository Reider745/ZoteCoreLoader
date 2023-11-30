package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import com.reider745.api.CustomManager;

import com.reider745.item.ItemMethod.PropertiesNames;

public class CustomItemArmor extends ItemArmor {
    private CustomManager parameters;
    private int slot;
    private int defense;
    private float knockbackResist;
    private boolean ARMOR_DAMAGEABLE;

    @Override
    public int getTier() {
        return 2;
    }

    @Override
    public boolean isHelmet() {
        return slot == 0;
    }

    @Override
    public boolean isChestplate() {
        return slot == 1;
    }

    @Override
    public boolean isLeggings() {
        return slot == 2;
    }

    @Override
    public boolean isBoots() {
        return slot == 3;
    }

    @Override
    public int getArmorPoints() {
        return defense;
    }

    @Override
    public int getToughness() {
        return (int) knockbackResist;
    }

    public CustomItemArmor(int id, Integer meta, int count){
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomItemArmor(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get(PropertiesNames.NAME, "InnerCore item"));

        parameters = manager;
        this.name = parameters.get(PropertiesNames.NAME, "InnerCore item");
        this.slot = parameters.get(PropertiesNames.Armors.SLOT);
        this.defense = parameters.get(PropertiesNames.Armors.DEFENSE);
        this.knockbackResist = parameters.get(PropertiesNames.Armors.KNOCKBACK_RESIST);
        this.ARMOR_DAMAGEABLE = parameters.get(PropertiesNames.ARMOR_DAMAGEABLE, false);
    }

    @Override
    public int getEnchantAbility() {
        return parameters.get(PropertiesNames.ENCHANTABILITY_VALUE, 0);
    }

    @Override
    public int getMaxDurability() {
        return parameters.get(PropertiesNames.MAX_DAMAGE);
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get(PropertiesNames.MAX_STACK);
    }

    @Override
    public boolean isUnbreakable() {
        return ARMOR_DAMAGEABLE;
    }

    @Override
    public Item clone() {
        CustomItemArmor item = (CustomItemArmor) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
