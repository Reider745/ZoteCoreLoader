package com.reider745.item;

//import cn.nukkit.blockstate.BlockStorage;
import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.item.food.FoodNormal;
import com.reider745.api.CustomManager;

import java.util.*;

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
    public static HashMap<String, ArrayList<Integer>> categories = new HashMap<>();

    public static boolean isCreativeItemModded(Item item){
        boolean is = isCreativeItem(item);
        if(!is){
            for (int[] _item : creative) {
                if(_item[0] == item.getId() && _item[2] == item.getDamage())
                    return true;
            }
        }
        return false;
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
            if(id == str)
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
        //BlockStorage.forEach((id, block) -> Item.list[id] = block);
        CustomItem.init();

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

        sortAddedToCreative.forEach(item -> Item.addCreativeItem(Item.get(item[0], item[2], item[1])));
    }

    public static CustomManager registerItem(String textId, int id, String name, Class item){
        CustomManager manager = new CustomManager(id, item, "item");
        manager.put("name", name);
        manager.put("max_damage", -1);
        manager.put("max_stack", 64);

        items.put(id, manager);
        customItems.put("item_"+textId, id);
        CustomManager.put(id, manager);

        return manager;
    }

    public static CustomManager registerItem(String textId, int id, String name){
        return registerItem(textId, id, name, CustomItem.class);
    }

    public static CustomManager registerItemFood(String textId, int id, String name, int food){
        CustomManager manager = registerItem(textId, id, name);
       // Food.registerDefaultFood(new FoodNormal(food, 4)).addRelative(id);
        return manager;
    }


    private CustomManager parameters;

    public CustomItem(int id, Integer meta, int count) {
        super(id, meta, count, getItemManager(id).get("name", "InnerCore item"));

        parameters = getItemManager(id);
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
        CustomItem item = (CustomItem) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
