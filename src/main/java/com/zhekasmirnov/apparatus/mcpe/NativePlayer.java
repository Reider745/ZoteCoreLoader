package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.item.Item;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class NativePlayer {
    private final long pointer;

    public NativePlayer(long entity) {
        pointer = constructNew(entity);
    }

    public long getPointer() {
        return pointer;
    }

    public boolean isValid() {
        try {
            return isValid(pointer);
        } catch (NoSuchMethodError e) {
            return pointer != 0;
        }
    }

    public void invokeUseItemNoTarget(int id, int count, int data, NativeItemInstanceExtra extra){
        invokeUseItemNoTarget(pointer, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
    
    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra, boolean dropLeft){
        addItemToInventory(pointer, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra), dropLeft);
    }

    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra){
        addItemToInventory(id, count, data, extra, true);
    }

    public void addItemToInventory(int id, int count, int data){
        addItemToInventory(id, count, data, null, true);
    }
    
    public void addItemToInventoryPtr(long itemStack, boolean dropLeft){
        addItemToInventoryPtr(pointer, itemStack, dropLeft);
    }
    
    public void addExperience(int amount){
        addExperience(pointer, amount);
    }
    
    public int getDimension(){
        return getDimension(pointer);
    }
    
    public int getGameMode(){
        return getGameMode(pointer);
    }
    
    public ItemStack getInventorySlot(int slot){
        return ItemStack.fromPtr(getInventorySlot(pointer, slot));
    }
    
    public ItemStack getArmor(int slot){
        return ItemStack.fromPtr(getArmor(pointer, slot));
    }
    
    public float getExhaustion(){
        return getExhaustion(pointer);
    }
    
    public float getExperience(){
        return getExperience(pointer);
    }
    
    public float getHunger(){
        return getHunger(pointer);
    }
    
    public float getLevel(){
        return getLevel(pointer);
    }
    
    public float getSaturation(){
        return getSaturation(pointer);
    }
    
    public int getScore(){
        return getScore(pointer);
    }
    
    public int getSelectedSlot(){
        return getSelectedSlot(pointer);
    }
    
    public void setInventorySlot(int slot, int id, int count, int data, NativeItemInstanceExtra extra){
        setInventorySlot(pointer, slot, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
    
    public void setArmor(int slot, int id, int count, int data, NativeItemInstanceExtra extra){
        setArmor(pointer, slot, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }
        
    public void setExhaustion(float value){
        setExhaustion(pointer, value);
    }
    
    public void setExperience(float value){
        setExperience(pointer, value);
    }
    
    public void setHunger(float value){
        setHunger(pointer, value);
    }
    
    public void setLevel(float value){
        setLevel(pointer, value);
    }
    
    public void setSaturation(float value){
        setSaturation(pointer, value);
    }
    
    public void setSelectedSlot(int slot){
        setSelectedSlot(pointer, slot);
    }
    
    public void setRespawnCoords(int x, int y, int z){
        setRespawnCoords(pointer, x, y, z);
    }
    
    public void spawnExpOrbs(float x, float y, float z, int amount){
        spawnExpOrbs(pointer, x, y, z, amount);
    }
    
    public boolean isSneaking(){
        return isSneaking(pointer);
    }
    
    public void setSneaking(boolean sneaking){
        setSneaking(pointer, sneaking);
    }

    public int getItemUseDuration() {
        return getItemUseDuration(pointer);
    }

    public float getItemUseIntervalProgress() {
        return getItemUseIntervalProgress(pointer);
    }

    public float getItemUseStartupProgress() {
        return getItemUseStartupProgress(pointer);
    }


    private static native long constructNew(long entity);
    private static native boolean isValid(long entity);
    private static native void invokeUseItemNoTarget(long pointer, int id, int count, int data, long extra);
    private static native void addItemToInventory(long pointer, int id, int count, int data, long extra, boolean dropLeft);
    private static native void addItemToInventoryPtr(long pointer, long itemStack, boolean dropLeft);
    private static native void addExperience(long pointer, int amount);
    private static native int getDimension(long pointer);
    private static native int getGameMode(long pointer);
    private static native Item getInventorySlot(long pointer, int slot);
    private static native Item getArmor(long pointer, int slot);
    private static native float getExhaustion(long pointer);
    private static native float getExperience(long pointer);
    private static native float getHunger(long pointer);
    private static native float getLevel(long pointer);
    private static native float getSaturation(long pointer);
    private static native int getScore(long pointer);
    private static native int getSelectedSlot(long pointer);
    private static native void setInventorySlot(long pointer, int slot, int id, int count, int data, long extra); 
    private static native void setArmor(long pointer, int slot, int id, int count, int data, long extra); 
    private static native void setExhaustion(long pointer, float value); 
    private static native void setExperience(long pointer, float value);
    private static native void setHunger(long pointer, float value); 
    private static native void setLevel(long pointer, float value); 
    private static native void setSaturation(long pointer, float value); 
    private static native void setSelectedSlot(long pointer, int slot); 
    private static native void setRespawnCoords(long pointer, int x, int y, int z); 
    private static native void spawnExpOrbs(long pointer, float x, float y, float z, int amount);
    private static native boolean isSneaking(long pointer); 
    private static native void setSneaking(long pointer, boolean sneaking);
    private static native int getItemUseDuration(long pointer);
    private static native float getItemUseIntervalProgress(long pointer);
    private static native float getItemUseStartupProgress(long pointer);

}
