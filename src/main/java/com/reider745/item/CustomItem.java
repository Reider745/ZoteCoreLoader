package com.reider745.item;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.item.food.FoodNormal;
import com.reider745.InnerCoreServer;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;
import com.reider745.item.ItemMethod.PropertiesNames;

import java.util.*;

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

    public static ArrayList<int[]> creative = new ArrayList<>();
    public static HashMap<String, ArrayList<Integer>> categories = new HashMap<>();

    public static boolean isCreativeItem(int id, int damage){
        for(int[] item : creative)
            if(item[0] == id && item[2] == damage)
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

    public static void checkAddedItem(int id, int damage){

        if(!CustomItem.isCreativeItem(id, damage) && id > 2000) {
            System.out.println(creative);
            Server.getInstance().getLogger().info("clone item   "+id+":"+damage);

            CustomItem.addToCreativeGroup(CustomItem.TECHNICAL_GROUP, id);
            addFirst(CustomItem.creative, new int[] {id, 1, damage});
        }
    }

    public static void addToCreativeGroup(String id, int itemId){
        ArrayList<Integer> items = categories.get(id);
        if(items == null)
            items = new ArrayList<>();

        items.add(itemId);

        categories.put(id, items);
    }

    private static boolean hasForSet(Set<String> sets, String str){
        for(String id : sets)
            if(id.equals(str))
                return true;
        return false;
    }

    private static Object[] getFullGroupForItem(int itemId, Set<String> skip){
        Set<String> sets = categories.keySet();

        for (String id : sets){
            ArrayList<Integer> items = categories.get(id);

            if(items.indexOf(itemId) != -1 && hasForSet(skip, id))
                return new Object[] {id, items};
        }

        return null;
    }

    private static Object[] getCreativeItemForId(int itemId, ArrayList<Integer> black){
        for(int i = 0;i < creative.size();i++) {
            int[] item = creative.get(i);
            if (item[0] == itemId && black.indexOf(i) == -1)
                return new Object[]{i, item};
        }
        return null;
    }

    public static final String TECHNICAL_GROUP = "technical_modded_item";

    public static void initCreativeItems(){
        ArrayList<int[]> sortAddedToCreative = new ArrayList<>();

        ArrayList<Integer> blackListItemIndex = new ArrayList<>();
        HashMap<String, Boolean> hasAddedGroup = new HashMap<>();

        ArrayList<Integer> items_technical = categories.get(TECHNICAL_GROUP);
        if(items_technical != null){
            for(Integer id : items_technical) {
                Object[] itemForCreative = getCreativeItemForId(id, blackListItemIndex);

                if(itemForCreative != null) {
                    blackListItemIndex.add((int) itemForCreative[0]);
                    sortAddedToCreative.add((int[]) itemForCreative[1]);
                }
            }
            hasAddedGroup.put(TECHNICAL_GROUP, true);
        }


        for(int i = 0;i < creative.size();i++){
            int[] item = creative.get(i);
            blackListItemIndex.add(i);

            Object[] group = getFullGroupForItem(item[0], hasAddedGroup.keySet());
            if(group != null  && !hasAddedGroup.containsKey(group[0])){
                ArrayList<Integer> items = (ArrayList<Integer>) group[1];

                for(Integer id : items) {
                    Object[] itemForCreative = getCreativeItemForId(id, blackListItemIndex);

                    if(itemForCreative != null) {
                        blackListItemIndex.add((int) itemForCreative[0]);
                        sortAddedToCreative.add((int[]) itemForCreative[1]);
                    }
                }

                hasAddedGroup.put((String) group[0], true);
                return;
            }

            sortAddedToCreative.add(item);
        }

        sortAddedToCreative.forEach(item -> {
            Item add = Item.get(item[0], item[2], item[1]);
            //System.out.println(add);
            Item.addCreativeItem(Item.v1_16_0, add);
        });
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
        creative.add(new int[] {id, count, data});
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
}
