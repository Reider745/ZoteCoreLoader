package com.reider745.item;

import cn.nukkit.item.Item;
import com.reider745.api.CustomManager;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomItem extends Item {


    public static HashMap<Integer, CustomManager> items = new HashMap<>();

    public static void init(){
        items.forEach((key, value) -> Item.list[key] = value.clazz);
    }

    public static CustomManager getItemManager(int id){
        return CustomManager.getFor(id);
    }

    public static HashMap<String, Integer> customItems = new HashMap<>();

    public static ArrayList<int[]> creative = new ArrayList<>();

    public static CustomManager registerItem(String textId, int id, String name, Class item){
        CustomManager manager = new CustomManager(id, item, "item");
        manager.put("name", name);

        items.put(id, manager);
        customItems.put("item_"+textId, id);
        CustomManager.put(id, manager);

        return manager;
    }

    public static CustomManager registerItem(String textId, int id, String name){
        return registerItem(textId, id, name, CustomItem.class);
    }

    private CustomManager parameters;

    public CustomItem(int id, Integer meta, int count, String name) {
        super(id, meta, count, name);

        parameters = getItemManager(id);
        this.name = parameters.get("name", "InnerCore item");
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get("max_stack", 64);
    }
}
