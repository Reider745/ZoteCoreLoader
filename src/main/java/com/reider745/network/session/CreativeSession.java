/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.reider745.network.session;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

public class CreativeSession implements Session {

    private final Inventory outputInventory;

    public CreativeSession(Player connection) {
        this.outputInventory = connection.getInventory();
    }

    @Override
    public Inventory getOutput() {
        return this.outputInventory;
    }

    @Override
    public boolean process() {
        return true;
    }

    @Override
    public void addInput(Item item, int slot) {
        this.outputInventory.setItem(slot, item);
    }

    @Override
    public void postProcess() {

    }

}
