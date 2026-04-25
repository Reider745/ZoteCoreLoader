package com.reider745.network;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.network.protocol.ProtocolInfo;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

@Hooks(className = "cn.nukkit.inventory.transaction.CraftingTransaction")
public class CraftingTransactionPacket implements HookClass {

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void sendInventories(CraftingTransaction transaction) {
        for (InventoryAction action : transaction.getActionList()) {
            if (action instanceof SlotChangeAction) {
                SlotChangeAction sca = (SlotChangeAction) action;
                sca.getInventory().sendSlot(sca.getSlot(), transaction.getSource());
            }
        }
    }
}
