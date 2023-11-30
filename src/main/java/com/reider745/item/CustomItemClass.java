package com.reider745.item;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.math.Vector3;
import com.reider745.api.CustomManager;

import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.runtime.Callback;

public class CustomItemClass extends Item implements ItemDurable {
    private CustomManager parameters;
    private int max_damage, max_stack;
    private boolean use_no_target;

    public CustomItemClass(int id, Integer meta, int count){
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomItemClass(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get(PropertiesNames.NAME, "InnerCore item"));

        parameters = manager;
        this.name = parameters.get(PropertiesNames.NAME, "InnerCore item");
        this.max_damage = parameters.get(PropertiesNames.MAX_DAMAGE);
        this.max_stack = parameters.get(PropertiesNames.MAX_STACK);
        this.use_no_target = Callback.count("ItemUseNoTarget") > 0;
    }

    @Override
    public int getMaxDurability() {
        return max_damage;
    }

    @Override
    public int getMaxStackSize() {
        return max_stack;
    }

    @Override
    public boolean onRelease(Player player, int ticksUsed) {
        NativeCallback.onItemUseReleased(ticksUsed, player.getId());
        if(ticksUsed > getMaxDurability())
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
        return use_no_target;
    }

    @Override
    public Item clone() {
        CustomItemClass item = (CustomItemClass) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
