package com.reider745.workbench;

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
public class Workbench implements HookClass {

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void sendInventories(CraftingTransaction transaction) {
        if (transaction.getSource().protocol >= ProtocolInfo.v1_16_0) {
            for (InventoryAction action : transaction.getActionList()) {
                if (action instanceof SlotChangeAction) {
                    SlotChangeAction sca = (SlotChangeAction) action;
                    sca.getInventory().sendSlot(sca.getSlot(), transaction.getSource());
                }
            }
        } else {
            for (Inventory inventory : transaction.getInventories()) {
                inventory.sendContents(transaction.getSource());
                if (inventory instanceof PlayerInventory) {
                    ((PlayerInventory) inventory).sendArmorContents(transaction.getSource());
                }
            }
        }
    }
}
