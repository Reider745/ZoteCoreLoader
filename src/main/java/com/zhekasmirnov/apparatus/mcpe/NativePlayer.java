package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Player;

import com.reider745.api.ZoteOnly;
import com.reider745.entity.PlayerActorMethods;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class NativePlayer {
    private final Player player;

    public NativePlayer(long entity) {
        player = PlayerActorMethods.fetchOnline(entity);
    }

    public Player getPlayer() {
        return player;
    }

    @ZoteOnly
    public String getLevelName() {
        return player.getLevelName();
    }

    public boolean isValid() {
        return PlayerActorMethods.isValid(player, true);
    }

    public void invokeUseItemNoTarget(int id, int count, int data, NativeItemInstanceExtra extra) {
        PlayerActorMethods.invokeUseItemNoTarget(player, id, count, data, extra);
    }

    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra, boolean dropLeft) {
        PlayerActorMethods.addItemToInventory(player, id, count, data, extra, dropLeft);
    }

    public void addItemToInventory(int id, int count, int data, NativeItemInstanceExtra extra) {
        addItemToInventory(id, count, data, extra, true);
    }

    public void addItemToInventory(int id, int count, int data) {
        addItemToInventory(id, count, data, null, true);
    }

    public void addItemToInventoryPtr(long itemStack, boolean dropLeft) {
        PlayerActorMethods.addItemToInventoryPtr(player, itemStack, dropLeft);
    }

    public void addExperience(int amount) {
        PlayerActorMethods.addExperience(player, amount);
    }

    public int getDimension() {
        return PlayerActorMethods.getDimension(player);
    }

    public int getGameMode() {
        return PlayerActorMethods.getGameMode(player);
    }

    public ItemStack getInventorySlot(int slot) {
        return ItemStack.fromPtr(PlayerActorMethods.getInventorySlot(player, slot));
    }

    public ItemStack getArmor(int slot) {
        return ItemStack.fromPtr(PlayerActorMethods.getArmor(player, slot));
    }

    public float getExhaustion() {
        return PlayerActorMethods.getExhaustion(player);
    }

    public float getExperience() {
        return PlayerActorMethods.getExperience(player);
    }

    public float getHunger() {
        return PlayerActorMethods.getHunger(player);
    }

    public float getLevel() {
        return PlayerActorMethods.getLevel(player);
    }

    public float getSaturation() {
        return PlayerActorMethods.getSaturation(player);
    }

    public int getScore() {
        return PlayerActorMethods.getScore(player);
    }

    public int getSelectedSlot() {
        return PlayerActorMethods.getSelectedSlot(player);
    }

    public void setInventorySlot(int slot, int id, int count, int data, NativeItemInstanceExtra extra) {
        PlayerActorMethods.setInventorySlot(player, slot, id, count, data, extra);
    }

    public void setArmor(int slot, int id, int count, int data, NativeItemInstanceExtra extra) {
        PlayerActorMethods.setArmor(player, slot, id, count, data, extra);
    }

    public void setExhaustion(float value) {
        PlayerActorMethods.setExhaustion(player, value);
    }

    public void setExperience(float value) {
        PlayerActorMethods.setExperience(player, value);
    }

    public void setHunger(float value) {
        PlayerActorMethods.setHunger(player, value);
    }

    public void setLevel(float value) {
        PlayerActorMethods.setLevel(player, value);
    }

    public void setSaturation(float value) {
        PlayerActorMethods.setSaturation(player, value);
    }

    public void setSelectedSlot(int slot) {
        PlayerActorMethods.setSelectedSlot(player, slot);
    }

    public void setRespawnCoords(int x, int y, int z) {
        PlayerActorMethods.setRespawnCoords(player, x, y, z);
    }

    public void spawnExpOrbs(float x, float y, float z, int amount) {
        PlayerActorMethods.spawnExpOrbs(player, x, y, z, amount);
    }

    public boolean isSneaking() {
        return PlayerActorMethods.isSneaking(player);
    }

    public void setSneaking(boolean sneaking) {
        PlayerActorMethods.setSneaking(player, sneaking);
    }

    public int getItemUseDuration() {
        return PlayerActorMethods.getItemUseDuration(player);
    }

    public float getItemUseIntervalProgress() {
        return PlayerActorMethods.getItemUseIntervalProgress(player);
    }

    public float getItemUseStartupProgress() {
        return PlayerActorMethods.getItemUseStartupProgress(player);
    }

    @ZoteOnly
    public boolean isOperator() {
        return PlayerActorMethods.isOperator(player);
    }

    @ZoteOnly
    public void setCanFly(boolean canFly) {
        PlayerActorMethods.setCanFly(player, canFly);
    }

    @ZoteOnly
    public void setCanFly() {
        setCanFly(true);
    }

    @ZoteOnly
    public boolean canFly() {
        return PlayerActorMethods.canFly(player);
    }

    @ZoteOnly
    public void setFlying(boolean flying) {
        PlayerActorMethods.setFlying(player, flying);
    }

    @ZoteOnly
    public void setFlying() {
        setFlying(true);
    }

    @ZoteOnly
    public boolean isFlying() {
        return PlayerActorMethods.isFlying(player);
    }
}
