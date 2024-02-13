package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.ProjectileItem;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.CustomManager;

import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.innercore.api.constants.EnchantType;

public class CustomProjectileItem extends ProjectileItem {
    @Override
    public String getProjectileEntityType() {
        return "Snowball";
    }

    @Override
    public float getThrowForce() {
        return 1f;
    }

    @Override
    protected void correctNBT(CompoundTag nbt) {
        nbt.put("ItemIc", NBTIO.putItemHelper(this));
    }

    private CustomManager parameters;

    public CustomProjectileItem(int id, Integer meta, int count){
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomProjectileItem(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get(PropertiesNames.NAME, "InnerCore item"));

        parameters = manager;
        this.name = parameters.get(PropertiesNames.NAME, "InnerCore item");
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_DIAMOND;
    }

    @Override
    public boolean isSword() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.weapon) != 0;
    }

    @Override
    public boolean isHoe() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.hoe) != 0;
    }

    @Override
    public boolean isShears() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.shears) != 0;
    }

    @Override
    public boolean isAxe() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.axe) != 0;
    }

    @Override
    public boolean isPickaxe() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.pickaxe) != 0;
    }

    @Override
    public boolean isShovel() {
        return (parameters.get(PropertiesNames.ENCHANTABILITY_TYPE, 0) & EnchantType.shovel) != 0;
    }

    @Override
    public int getMaxDurability() {
        return parameters.get(PropertiesNames.MAX_USE_DURATION);
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get(PropertiesNames.MAX_STACK);
    }

    @Override
    public int getEnchantAbility() {
        return parameters.get(PropertiesNames.ENCHANTABILITY_VALUE, 0);
    }

    @Override
    public Item clone() {
        CustomProjectileItem item = (CustomProjectileItem) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
