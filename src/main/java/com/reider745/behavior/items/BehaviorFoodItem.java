package com.reider745.behavior.items;

import cn.nukkit.item.ItemEdible;
import com.reider745.api.CustomManager;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod.PropertiesNames;

/** Uses Nukkit's normal consumption events, hunger and inventory handling. */
public class BehaviorFoodItem extends ItemEdible {
    private final CustomManager manager;

    public BehaviorFoodItem(int id, Integer meta, int count) {
        super(id, meta, count, CustomItem.getItemManager(id).get(PropertiesNames.NAME, "Behavior pack food"));
        manager = CustomItem.getItemManager(id);
    }

    @Override
    public int getMaxStackSize() {
        return manager.get(PropertiesNames.MAX_STACK, 64);
    }

    @Override
    public int getMaxDurability() {
        return manager.get(PropertiesNames.MAX_DAMAGE, 0);
    }
}
