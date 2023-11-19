package com.reider745.item;

import cn.nukkit.item.Item;
import com.reider745.api.CustomManager;

public class CustomItemClass extends Item {
    private CustomManager parameters;

    public CustomItemClass(int id, int count){
        this(id, 0, count, "");
    }

    public CustomItemClass(int id, Integer meta, int count, String name) {
        super(id, meta, count, CustomItem.getItemManager(id).get("name", "InnerCore item"));

        parameters = CustomItem.getItemManager(id);
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
