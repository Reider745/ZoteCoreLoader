package com.reider745.network.transaction;


import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;

/**
 * @author geNAZt
 * @version 1.0
 */
public class DropItemTransaction<T> implements Transaction<Void, Void, T> {

    private final Location location;
    private final Vector3 velocity;
    private final Item targetItem;

    public DropItemTransaction(Location location, Vector3 velocity, Item targetItem) {
        this.location = location;
        this.velocity = velocity;
        this.targetItem = targetItem;
    }

    public Location getLocation() {
        return this.location;
    }

    public Vector3 getVelocity() {
        return this.velocity;
    }

    @Override
    public Item targetItem() {
        return this.targetItem;
    }

    @Override
    public boolean hasInventory() {
        return false;
    }

    @Override
    public Item sourceItem() {
        return null;
    }

    @Override
    public Inventory inventory() {
        return null;
    }

    @Override
    public int slot() {
        return -1;
    }

    @Override
    public void commit() {
       this.location.getLevel().dropItem( this.location, this.targetItem, this.velocity);
    }

    @Override
    public void revert() {

    }

    @Override
    public byte getInventoryWindowId() {
        return 0;
    }

    @Override
    public String toString() {
        return "{\"_class\":\"DropItemTransaction\", " +
                "\"location\":" + (this.location == null ? "null" : this.location) + ", " +
                "\"velocity\":" + (this.velocity == null ? "null" : this.velocity) + ", " +
                "\"targetItem\":" + (this.targetItem == null ? "null" : this.targetItem) +
                "}";
    }

}