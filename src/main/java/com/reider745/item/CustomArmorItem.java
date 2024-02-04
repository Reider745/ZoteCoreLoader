package com.reider745.item;

import com.reider745.api.CustomManager;

import com.reider745.item.ItemMethod.PropertiesNames;

public class CustomArmorItem extends CustomItemClass {
    private int slot;

    public CustomArmorItem(int id, Integer meta, int count) {
        super(id, meta, count);
    }

    public CustomArmorItem(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager);
    }

    @Override
    protected void initItem() {
        super.initItem();
        this.slot = parameters.get(PropertiesNames.Armors.SLOT, -1);
    }

    @Override
    public boolean isArmor() {
        return true;
    }

    @Override
    public boolean isHelmet() {
        return slot == 0;
    }

    @Override
    public boolean canBePutInHelmetSlot() {
        return this.isHelmet();
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
    public boolean isUnbreakable() {
        return !parameters.get(PropertiesNames.ARMOR_DAMAGEABLE, true);
    }

    @Override
    public int getArmorPoints() {
        return parameters.get(PropertiesNames.Armors.DEFENSE, 0);
    }

    @Override
    public int getToughness() {
        return (int) (float) parameters.get(PropertiesNames.Armors.KNOCKBACK_RESIST, 0f);
    }
}
