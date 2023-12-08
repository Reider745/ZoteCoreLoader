package com.zhekasmirnov.apparatus.adapter.innercore.game.entity;

import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.innercore.api.NativeAPI;

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

    public ItemStack getCarriedItem() {
        return ItemStack.fromPtr(EntityMethod.getEntityCarriedItem(uid));
    }

    public ItemStack getOffhandItem() {
        return ItemStack.fromPtr(EntityMethod.getEntityOffhandItem(uid));
    }

    public ItemStack getArmorSlot(int slot) {
        return ItemStack.fromPtr(EntityMethod.getEntityArmor(uid, slot));
    }

    public void setArmorSlot(int slot, ItemStack stack) {
        NativeAPI.setEntityArmor(uid, slot, stack.id, stack.count, stack.data, stack.getExtraPtr());
    }
}
