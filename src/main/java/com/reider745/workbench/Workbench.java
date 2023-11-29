package com.reider745.workbench;

import cn.nukkit.Player;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import com.reider745.api.hooks.HookController;

import java.util.concurrent.ConcurrentHashMap;

public class Workbench {
    private static ConcurrentHashMap<Player, CraftItemEvent> playerCrafts = new ConcurrentHashMap<>();

    public static void addCraft(CraftItemEvent event){
        playerCrafts.put(event.getPlayer(), event);
    }

    public static void slotChange(HookController controller, SlotChangeAction self, Player player){
        if(self.getInventory() instanceof PlayerUIInventory playerUIInventory){
            CraftItemEvent event = playerCrafts.get(player);
            if(event == null){
                controller.setReplace(false);
                return;
            }
            playerCrafts.remove(player);
            CraftingGrid grid = player.getCraftingGrid();

            System.out.println(grid.getClass());
            System.out.println(grid.getSize());
            System.out.println(self.getSlot());

            Item item = playerUIInventory.getItem(self.getSlot());
            System.out.println(item);
            item.setCount(item.getCount()*2);
            playerUIInventory.setItem(self.getSlot(), item);

            controller.setResult(true);
            controller.setReplace(true);
        }
        controller.setReplace(false);
    }
}
