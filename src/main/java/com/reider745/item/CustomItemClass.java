package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import com.reider745.api.CustomManager;

public class CustomItemClass extends Item implements ItemDurable {
    private CustomManager parameters;

    public CustomItemClass(int id, Integer meta, int count){
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomItemClass(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get("name", "InnerCore item"));

        parameters = manager;
        this.name = parameters.get("name", "InnerCore item");
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
        CustomItemClass item = (CustomItemClass) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
