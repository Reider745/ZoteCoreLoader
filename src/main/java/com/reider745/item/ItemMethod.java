package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import com.reider745.api.CustomManager;

public class ItemMethod {
    public static int getMaxDamageForId(int id, int data){
        return Item.get(id, data).getMaxDurability();
    }

    public static void setMaxDamage(CustomManager manager, int id){
        manager.put("max_damage", id);
    }

    public static void setMaxStackSize(CustomManager manager, int stack){
        manager.put("max_stack", stack);
    }

    public static int getMaxStackForId(int id, int data){
        return Item.get(id, data).getMaxStackSize();
    }

    public static String getNameForId(int id, int data, long extra){
        return Item.get(id, data).getName();
    }

    public static String getStringIdAndTypeForIntegerId(int id){
        Item item = Item.get(id);
        if(item != null)
            return (item instanceof ItemBlock ? "block" : "item") + " :" + id;
        return null;
    }
}
