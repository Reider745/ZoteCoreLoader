package com.reider745.entity;

import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;

import java.util.Collection;

public class PlayerActorMethods {
    public static Player constructNew(long entity){
        Collection<Player> collection = InnerCoreServer.server.getOnlinePlayers().values();
        for(Player player : collection)
            if(player.getId() == entity)
                return player;
        return null;
    }
    public static boolean isValid(Player entity){
        return constructNew(entity.getId()) != null;
    }
    public static void invokeUseItemNoTarget(Player pointer, int id, int count, int data, long extra){
        //pointer.getLevel()
    }
    public static void addItemToInventory(Player pointer, int id, int count, int data, long extra, boolean dropLeft){
        Item[] items = pointer.getInventory().addItem(ItemUtils.get(id, count, data, extra));
        if(dropLeft)
            for(Item item : items)
                pointer.dropItem(item);
    }
    public static void addItemToInventoryPtr(Player pointer, long itemStack, boolean dropLeft){

    }
    public static void addExperience(Player pointer, int amount){
        pointer.addExperience(amount);
    }
    public static int getDimension(Player pointer){
        return pointer.getLevel().getDimension();
    }
    public static int getGameMode(Player pointer){
        return pointer.getGamemode();
    }
    public static Item getInventorySlot(Player pointer, int slot){
        return pointer.getInventory().getItem(slot);
    }
    public static Item getArmor(Player pointer, int slot){
        return pointer.getInventory().getArmorItem(slot);
    }
    public static float getExhaustion(Player pointer){
        return 0;
        //return pointer.get
    }
    public static float getExperience(Player pointer){
        return pointer.getExperience();
    }
    public static float getHunger(Player pointer){
        PlayerFood food = pointer.getFoodData();
        if(food != null)
            return food.getLevel();
        return 0;
    }
    public static float getLevel(Player pointer){
        return pointer.getExperienceLevel();
    }
    public static float getSaturation(Player pointer){
        PlayerFood food = pointer.getFoodData();
        if(food != null)
            return food.getFoodSaturationLevel();
        return 0;
    }
    public static int getScore(Player pointer){
        return 0;
    }
    public static int getSelectedSlot(Player pointer){
        return pointer.getInventory().getHeldItemSlot();
    }
    public static void setInventorySlot(Player pointer, int slot, int id, int count, int data, long extra){
        pointer.getInventory().setItem(slot, ItemUtils.get(id, count, data, extra));
    }
    public static void setArmor(Player pointer, int slot, int id, int count, int data, long extra){
        pointer.getInventory().setArmorItem(slot, ItemUtils.get(id, count, data, extra));
    }
    public static void setExhaustion(Player pointer, float value){

    }
    public static void setExperience(Player pointer, float value){
        pointer.setExperience((int) value);
    }
    public static void setHunger(Player pointer, float value){
        PlayerFood food = pointer.getFoodData();
        if(food != null)
            food.setLevel((int) value);
    }
    public static void setLevel(Player pointer, float value){
        pointer.setExperience(0, (int) value);
    }
    public static void setSaturation(Player pointer, float value){
        PlayerFood food = pointer.getFoodData();
        if(food != null)
            food.setFoodSaturationLevel(value);
    }
    public static void setSelectedSlot(Player pointer, int slot){
        pointer.getInventory().setHeldItemSlot(slot);
    }
    public static void setRespawnCoords(Player pointer, int x, int y, int z){
        pointer.setSpawn(new Vector3(x, y, z));
    }
    public static void spawnExpOrbs(Player pointer, float x, float y, float z, int amount){
        pointer.getLevel().dropExpOrb(new Vector3(x, y, z), amount);
    }
    public static boolean isSneaking(Player pointer){
        return pointer.isSneaking();
    }
    public static void setSneaking(Player pointer, boolean sneaking){
        pointer.setSneaking(sneaking);
    }
    public static int getItemUseDuration(Player pointer){
        return pointer.getInventory().getItemInHand().getMaxDurability();
    }
    public static float getItemUseIntervalProgress(Player pointer){
        return pointer.getInventory().getItemInHand().getMaxDurability();
    }
    public static float getItemUseStartupProgress(Player pointer){
        return pointer.getInventory().getItemInHand().getMaxDurability();
    }
}
