package com.reider745.item;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.math.Vector3;
import com.reider745.api.CustomManager;

import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.constants.EnchantType;
import com.zhekasmirnov.innercore.api.runtime.Callback;

public class CustomItemClass extends Item {
    protected CustomManager parameters;

    public CustomItemClass(int id, Integer meta, int count) {
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomItemClass(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get(PropertiesNames.NAME, "InnerCore item"));

        parameters = manager;
        initItem();
    }

    protected void initItem() {
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
        return parameters.get(PropertiesNames.MAX_DAMAGE);
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get(PropertiesNames.MAX_STACK);
    }

    @Override
    public boolean onRelease(Player player, int ticksUsed) {
        NativeCallback.onItemUseReleased(ticksUsed, player.getId());
        if (ticksUsed > getMaxDurability())
            NativeCallback.onItemUseComplete(player.getId());
        return super.onRelease(player, ticksUsed);
    }

    @Override
    public int getEnchantAbility() {
        return parameters.get(PropertiesNames.ENCHANTABILITY_VALUE, 0);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        NativeCallback.onItemUsedNoTarget(player.getId());
        return Callback.count("ItemUseNoTarget") > 0;
    }

    @Override
    public Item clone() {
        CustomItemClass item = (CustomItemClass) super.clone();
        item.initItem();
        return item;
    }
}
