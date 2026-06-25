package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Player;

import com.reider745.api.ZoteOnly;
import com.reider745.entity.EntityMethod;
import com.reider745.entity.PlayerActorMethods;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.NativeScales;

public class NativePlayer {
    private final Player player;

    public NativePlayer(long entity) {
        player = EntityMethod.fetchOnline(entity);
    }

    public Player getPlayer() {
        return player;
    }

    public long getUid() {
        return EntityMethod.getIdForEntity(player);
    }

    public void setScale(String scale, double value) {
        NativeScales.setServerScaleValue(this.getUid(), scale, value);
    }

    public void setScale(NativeScales.CustomScale scale, double value) {
        setScale(scale.getScaleId(), value);
    }

    public double getScale(String scale) {
        return NativeScales.getScaleValue(this.getUid(), scale);
    }

    public double getScale(NativeScales.CustomScale scale) {
        return getScale(scale.getScaleId());
    }

    public void addScale(String scale, double value) {
        setScale(scale, getScale(scale) + value);
    }

    public void addScale(NativeScales.CustomScale scale, double value) {
        setScale(scale, getScale(scale) + value);
    }


    @ZoteOnly
    public String getLevelName() {
        return player.getLevelName();
    }

    public boolean isValid() {
        return PlayerActorMethods.isValid(player, true);
    }

    public void invokeUseItemNoTarget(int id, int count, int data, ItemInstanceExtra extra) {
        PlayerActorMethods.invokeUseItemNoTarget(player, id, count, data, extra);
    }

    public void addItemToInventory(int id, int count, int data, ItemInstanceExtra extra, boolean dropLeft) {
        PlayerActorMethods.addItemToInventory(player, id, count, data, extra, dropLeft);
    }

    public void addItemToInventory(int id, int count, int data, ItemInstanceExtra extra) {
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

    public ItemInstance getInventorySlot(int slot) {
        return ItemInstance.makeFrom(PlayerActorMethods.getInventorySlot(player, slot));
    }

    public ItemInstance getArmor(int slot) {
        return ItemInstance.makeFrom(PlayerActorMethods.getArmor(player, slot));
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

    public void setInventorySlot(int slot, int id, int count, int data, ItemInstanceExtra extra) {
        PlayerActorMethods.setInventorySlot(player, slot, id, count, data, extra);
    }

    public void setArmor(int slot, int id, int count, int data, ItemInstanceExtra extra) {
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

    public boolean isOperator() {
        return PlayerActorMethods.isOperator(player);
    }

    public void setCanFly(boolean canFly) {
        PlayerActorMethods.setCanFly(player, canFly);
    }

    public void setCanFly() {
        setCanFly(true);
    }

    public boolean canFly() {
        return PlayerActorMethods.canFly(player);
    }

    public void setFlying(boolean flying) {
        PlayerActorMethods.setFlying(player, flying);
    }

    public void setFlying() {
        setFlying(true);
    }

    public boolean isFlying() {
        return PlayerActorMethods.isFlying(player);
    }

    @ZoteOnly
    public String getUserName() {
        return PlayerActorMethods.getUserName(player);
    }
}
