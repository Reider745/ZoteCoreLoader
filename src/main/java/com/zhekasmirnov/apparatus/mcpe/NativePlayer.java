package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import com.reider745.entity.PlayerActorMethods;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class NativePlayer {
    private final Player pointer;
    private Player player;

    public NativePlayer(long entity) {
        pointer = PlayerActorMethods.constructNew(entity);
    }

    public Player getPointer() {
        return pointer;
    }

    public boolean isValid() {
        try {
            return PlayerActorMethods.isValid(pointer);
        } catch (NoSuchMethodError e) {
            return pointer != null && pointer.isValid();
        }
    }

    public void invokeUseItemNoTarget(int id, int count, int data, NativeItemInstanceExtra extra){
        PlayerActorMethods.invokeUseItemNoTarget(pointer, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
    
    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra, boolean dropLeft){
        PlayerActorMethods.addItemToInventory(pointer, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra), dropLeft);
    }

    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra){
        addItemToInventory(id, count, data, extra, true);
    }

    public void addItemToInventory(int id, int count, int data){
        addItemToInventory(id, count, data, null, true);
    }
    
    public void addItemToInventoryPtr(long itemStack, boolean dropLeft){
        PlayerActorMethods.addItemToInventoryPtr(pointer, itemStack, dropLeft);
    }
    
    public void addExperience(int amount){
        PlayerActorMethods.addExperience(pointer, amount);
    }
    
    public int getDimension(){
        return PlayerActorMethods.getDimension(pointer);
    }
    
    public int getGameMode(){
        return PlayerActorMethods.getGameMode(pointer);
    }
    
    public ItemStack getInventorySlot(int slot){
        return ItemStack.fromPtr(PlayerActorMethods.getInventorySlot(pointer, slot));
    }
    
    public ItemStack getArmor(int slot){
        return ItemStack.fromPtr(PlayerActorMethods.getArmor(pointer, slot));
    }
    
    public float getExhaustion(){
        return PlayerActorMethods.getExhaustion(pointer);
    }
    
    public float getExperience(){
        return PlayerActorMethods.getExperience(pointer);
    }
    
    public float getHunger(){
        return PlayerActorMethods.getHunger(pointer);
    }
    
    public float getLevel(){
        return PlayerActorMethods.getLevel(pointer);
    }
    
    public float getSaturation(){
        return PlayerActorMethods.getSaturation(pointer);
    }
    
    public int getScore(){
        return PlayerActorMethods.getScore(pointer);
    }
    
    public int getSelectedSlot(){
        return PlayerActorMethods.getSelectedSlot(pointer);
    }
    
    public void setInventorySlot(int slot, int id, int count, int data, NativeItemInstanceExtra extra){
        PlayerActorMethods.setInventorySlot(pointer, slot, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
    
    public void setArmor(int slot, int id, int count, int data, NativeItemInstanceExtra extra){
        PlayerActorMethods.setArmor(pointer, slot, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
        
    public void setExhaustion(float value){
        PlayerActorMethods.setExhaustion(pointer, value);
    }
    
    public void setExperience(float value){
        PlayerActorMethods.setExperience(pointer, value);
    }
    
    public void setHunger(float value){
        PlayerActorMethods.setHunger(pointer, value);
    }
    
    public void setLevel(float value){
        PlayerActorMethods.setLevel(pointer, value);
    }
    
    public void setSaturation(float value){
        PlayerActorMethods.setSaturation(pointer, value);
    }
    
    public void setSelectedSlot(int slot){
        PlayerActorMethods.setSelectedSlot(pointer, slot);
    }
    
    public void setRespawnCoords(int x, int y, int z){
        PlayerActorMethods.setRespawnCoords(pointer, x, y, z);
    }
    
    public void spawnExpOrbs(float x, float y, float z, int amount){
        PlayerActorMethods.spawnExpOrbs(pointer, x, y, z, amount);
    }
    
    public boolean isSneaking(){
        return PlayerActorMethods.isSneaking(pointer);
    }
    
    public void setSneaking(boolean sneaking){
        PlayerActorMethods.setSneaking(pointer, sneaking);
    }

    public int getItemUseDuration() {
        return PlayerActorMethods.getItemUseDuration(pointer);
    }

    public float getItemUseIntervalProgress() {
        return PlayerActorMethods.getItemUseIntervalProgress(pointer);
    }

    public float getItemUseStartupProgress() {
        return PlayerActorMethods.getItemUseStartupProgress(pointer);
    }
}
