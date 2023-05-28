package com.reider745.item;

import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItems;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.util.HashMap;

public class CustomItem extends Item {
    public static class ItemManager {
        private HashMap<String, Object> parameters = new HashMap<>();
        private int id;
        public Class item;

        public ItemManager(int id, Class item){
            this.id = id;
            this.item = item;
        }

        public <T>T get(String key, T def){
            T value = (T) parameters.get(key);
            if(value == null)
                return def;
            return value;
        }

        public <T>void put(String key, T value){
            parameters.put(key, value);
        }
    }

    public static HashMap<Integer, ItemManager> items = new HashMap<>();

    public static void init(){
        items.forEach((key, value) -> Item.list[key] = value.item);
    }

    public static ItemManager getItemManager(int id){
        return items.get(id);
    }

    public static HashMap<String, Integer> customItems = new HashMap<>();

    public static ItemManager registerItem(String textId, int id, String name, Class item){
        ItemManager itemManager = new ItemManager(id, item);
        itemManager.put("name", name);

        items.put(id, itemManager);
        customItems.put("item_"+textId, id);

        return itemManager;
    }

    public static ItemManager registerItem(String textId, int id, String name){
        return registerItem(textId, id, name, CustomItem.class);
    }

    private ItemManager parameters;

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
