package com.reider745.network.transaction;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

/**
 * @author geNAZt
 * @version 1.0
 */
public class InventoryTransaction<I, S, T> implements Transaction<I, S, T> {

    private final Player owner;
    private final Inventory inventory;
    private final int slot;
    private final Item sourceItem;
    private final Item targetItem;
    private final byte inventoryWindowId;

    public InventoryTransaction(Player owner, Inventory inventory, int slot,
                                Item sourceItem, Item targetItem, byte inventoryWindowId) {
        this.owner = owner;
        this.inventory = inventory;
        this.slot = slot;
        this.sourceItem = sourceItem;
        this.targetItem = targetItem;
        this.inventoryWindowId = inventoryWindowId;
    }

    public Player getOwner() {
        return this.owner;
    }

    @Override
    public Inventory inventory() {
        return this.inventory;
    }

    @Override
    public int slot() {
        return this.slot;
    }

    @Override
    public Item sourceItem() {
        return this.sourceItem;
    }

    @Override
    public Item targetItem() {
        return this.targetItem;
    }

    @Override
    public boolean hasInventory() {
        return true;
    }

    @Override
    public void commit() {
        //this.inventory.getViewers().add(this.owner)
        //this.inventory.removeViewerWithoutAction( this.owner );
        this.inventory.setItem( this.slot, this.targetItem );
        //this.inventory.addViewerWithoutAction( this.owner );
    }

    @Override
    public void revert() {
        this.inventory.sendContents( this.owner );
    }

    @Override
    public byte getInventoryWindowId() {
        return this.inventoryWindowId;
    }

    @Override
    public String toString() {
        return "{\"_class\":\"InventoryTransaction\", " +
                "\"owner\":" + (this.owner == null ? "null" : this.owner) + ", " +
                "\"inventory\":" + (this.inventory == null ? "null" : this.inventory) + ", " +
                "\"slot\":\"" + this.slot + "\"" + ", " +
                "\"sourceItem\":" + (this.sourceItem == null ? "null" : this.sourceItem) + ", " +
                "\"targetItem\":" + (this.targetItem == null ? "null" : this.targetItem) + ", " +
                "\"inventoryWindowId\":\"" + this.inventoryWindowId + "\"" +
                "}";
    }

}