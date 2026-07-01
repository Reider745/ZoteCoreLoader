package com.zhekasmirnov.apparatus.adapter.innercore.game.entity;

import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.innercore.api.NativeAPI;
import org.innercore.icstd.inventory.ItemInstance;

public class EntityActor {
    private final long uid;

    public EntityActor(long uid) {
        this.uid = uid;
    }

    public long getUid() {
        return uid;
    }

    // basics

    public int getDimension() {
        return NativeAPI.getEntityDimension(uid);
    }

    public int getHealth() {
        return NativeAPI.getHealth(uid);
    }

    public int getMaxHealth() {
        return NativeAPI.getMaxHealth(uid);
    }

    // inventory

    public ItemInstance getCarriedItem() {
        return ItemInstance.makeFrom(EntityMethod.getEntityCarriedItem(uid));
    }

    public ItemInstance getOffhandItem() {
        return ItemInstance.makeFrom(EntityMethod.getEntityOffhandItem(uid));
    }

    public ItemInstance getArmorSlot(int slot) {
        return ItemInstance.makeFrom(EntityMethod.getEntityArmor(uid, slot));
    }

    public void setArmorSlot(int slot, ItemInstance stack) {
        NativeAPI.setEntityArmor(uid, slot, stack.id, stack.count, stack.data, stack.extra);
    }
}
