package com.reider745.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.item.food.FoodNormal;
import com.reider745.InnerCoreServer;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;
import com.reider745.hooks.ItemUtils;
import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

import java.util.*;
import java.util.function.Consumer;

public class CustomItem {


    public static HashMap<Integer, CustomManager> items = new HashMap<>();
    public static HashMap<Integer, Integer> foods = new HashMap<>();

    public static void init(){
        items.forEach((key, value) -> Item.list[key] = value.getClazz());
        foods.forEach((key, value) -> Food.registerFood(new FoodNormal(value, 4), InnerCoreServer.plugin).addRelative(key));
        CustomBlock.blocks.forEach((id, manager) -> Item.list[id] = manager.getClazz());
    }

    public static CustomManager getItemManager(int id){
        return CustomManager.getFor(id);
    }

    public static HashMap<String, Integer> customItems = new HashMap<>();

    public static int getIdForText(String id) {
        return customItems.get(id);
    }

    public static class ItemCreative {
        public int id, count, data;
        public long extra;
        public ItemCreative(int id, int count, int data, long extra){
            this.id = id;
            this.count = count;
            this.data = data;
            this.extra = extra;
        }
    }

    public static ArrayList<ItemCreative> creative = new ArrayList<>();
    public static HashMap<String, ArrayList<Integer>> groups = new HashMap<>();

    public static boolean isCreativeItem(int id, int damage){
        for(ItemCreative item : creative)
            if(item.id == id && item.data == damage)
                return true;
        return false;
    }

    private static <T>void addFirst(ArrayList<T> list, T value){
        ArrayList<T> newList = new ArrayList<>();
        newList.add(value);

        for(T v : list)
            newList.add(v);

        list.clear();
        list.addAll(newList);
    }

    public static void checkAddedItem(int id, int count, int damage, NativeItemInstanceExtra extra){
        if(!CustomItem.isCreativeItem(id, damage) && id > 2000) {
            CustomItem.addToCreativeGroup(CustomItem.TECHNICAL_GROUP, id);
            addFirst(CustomItem.creative, new ItemCreative(id, count, damage, extra != null ? extra.getValue() : 0));
        }
    }

    public static void addToCreativeGroup(String id, int itemId){
        ArrayList<Integer> items = groups.get(id);
        if(items == null)
            items = new ArrayList<>();

        items.add(itemId);

        groups.put(id, items);
    }

    public static ArrayList<Integer> getGroupForElement(int id){
        for(ArrayList<Integer> items : groups.values())
            for(Integer id_item_group : items)
                if(id_item_group == id)
                    return items;
        return null;
    }

    public static final String TECHNICAL_GROUP = "technical_modded_item";


    public static String getTextIdForNumber(int id){
        for(String texId : customItems.keySet())
            if(customItems.get(texId).equals(id))
                return texId;
        return null;
    }

    public static CustomManager registerItem(String textId, int id, String name, Class<?> item){
        CustomManager manager = new CustomManager(id, item, "item");
        manager.put(PropertiesNames.NAME, name);
        manager.put(PropertiesNames.MAX_DAMAGE, 0);
        manager.put(PropertiesNames.MAX_STACK, 64);
        manager.put(PropertiesNames.ID, id);

        items.put(id, manager);
        customItems.put("item_"+textId, id);
        CustomManager.put(id, manager);

        return manager;
    }

    public static CustomManager registerItem(String textId, int id, String name){
        return registerItem(textId, id, name, CustomItemClass.class);
    }

    public static CustomManager registerItemFood(String textId, int id, String name, int food){
        CustomManager manager = registerItem(textId, id, name);
        foods.put(id, food);
        return manager;
    }

    public static void addCreative(int id, int count, int data, long extra) {
        creative.add(new ItemCreative(id, count, data, extra));
    }

    public static CustomManager registerThrowableItem(String nameId, int id, String name) {
        return registerItem(nameId, id, name, CustomProjectileItem.class);
    }

    public static CustomManager registerArmorItem(String nameId, int id, String name, int slot, int defense, int durability, float knockbackResist) {
        CustomManager manager = registerItem(nameId, id, name, CustomItemArmor.class);
        manager.put(PropertiesNames.Armors.SLOT, slot);
        manager.put(PropertiesNames.Armors.DEFENSE, defense);
        manager.put(PropertiesNames.MAX_DAMAGE, durability);
        manager.put(PropertiesNames.Armors.KNOCKBACK_RESIST, knockbackResist);
        return manager;
    }

    public static boolean hasData(int id) {
        CustomManager manager = getItemManager(id);
        if(manager == null) return false;
        return manager.get(PropertiesNames.MAX_DAMAGE, 0) > 0;
    }

    private static final HashMap<Integer, ArrayList<ItemCreative>> category_all = new HashMap<>();

    public static void sortCategory(){
        for (ItemCreative item : creative){
            int cat_id = ItemMethod.getCreativeCategory(item.id);
            ArrayList<ItemCreative> items = category_all.getOrDefault(cat_id, new ArrayList<>());
            items.add(item);
            category_all.put(cat_id, items);
        }

    }

    private static int get(int id, ArrayList<ItemCreative> items){
        for (int i = 0; i < items.size(); i++) {
            ItemCreative item = items.get(i);
            if(item.id == id)
                return i;
        }
        return -1;
    }

    private static int get(ItemCreative item_check, ArrayList<ItemCreative> items){
        for (int i = 0; i < items.size(); i++) {
            ItemCreative item = items.get(i);
            if(item == item_check)
                return i;
        }
        return -1;
    }

    public static ArrayList<ItemCreative> sortCreativeItems(int category){
        ArrayList<ItemCreative> items = category_all.getOrDefault(category, new ArrayList<>());
        ArrayList<ItemCreative> items_clone = (ArrayList<ItemCreative>) items.clone();
        ArrayList<ItemCreative> result = new ArrayList<>();

        for (ItemCreative item : items){
            if(items_clone.size() == 0) break;

            ArrayList<Integer> ids = getGroupForElement(item.id);
            if(ids != null)
                for(Integer id : ids)
                    while (true){
                        int index = get(id, items_clone);
                        if(index == -1) break;
                        result.add(items_clone.remove(index));
                    }

            int index = get(item, items_clone);
            if(index != -1)
                result.add(items_clone.remove(index));
        }
        if(result.size() != items.size())
            throw new RuntimeException("Error sort items");
        return result;
    }

    private static final Consumer<ItemCreative> func = item -> Item.addCreativeItem(407, ItemUtils.get(item.id, item.count, item.data, item.extra));

    public static void addCreativeItemsBuild() {
        sortCategory();
        sortCreativeItems(1).forEach(func);
    }

    public static void addCreativeItemsNature() {
        sortCreativeItems(2).forEach(func);
    }

    public static void addCreativeItemsWeapons() {
        sortCreativeItems(3).forEach(func);
    }

    public static void addCreativeItems() {
        sortCreativeItems(4).forEach(func);
    }
}
