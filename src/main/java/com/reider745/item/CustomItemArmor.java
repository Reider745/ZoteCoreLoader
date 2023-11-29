package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import com.reider745.api.CustomManager;

public class CustomItemArmor extends ItemArmor {
    private CustomManager parameters;
    private int slot;
    private int defense;
    private float knockbackResist;

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
        super(id, meta, count, manager.get("name", "InnerCore item"));

        parameters = manager;
        this.name = parameters.get("name", "InnerCore item");
        this.slot = parameters.get("slot");
        this.defense = parameters.get("defense");
        this.knockbackResist = parameters.get("knockbackResist");
    }

    @Override
    public int getMaxDurability() {
        return parameters.get("max_damage");
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get("max_stack");
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
