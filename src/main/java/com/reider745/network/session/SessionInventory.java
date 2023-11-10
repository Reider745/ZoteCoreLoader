package com.reider745.network.session;

import cn.nukkit.Player;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;

/**
 * @author geNAZt
 *
 * This inventory is used to hold items which are "consumed" by the crafting action issued
 */
public class SessionInventory extends BaseInventory {

    public SessionInventory(InventoryHolder owner, int size) {
        super(owner, InventoryType.CRAFTING);
        this.setSize(size);
    }

    @Override
    public void sendContents(Player playerConnection) {

    }

    @Override
    public void sendSlot(int index, Player player) {

    }
}
