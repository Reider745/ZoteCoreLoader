package com.reider745.network.transaction;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface Transaction<I, S, T>{

    /**
     * Called when the transaction has been a success
     */
    void commit();

    /**
     * Called when a transaction failed
     */
    void revert();

    /**
     * Get inventory window id
     *
     * @return window id
     */
    byte getInventoryWindowId();
    int slot();
    Inventory inventory();
    Item sourceItem();
    boolean hasInventory();
    Item targetItem();
}