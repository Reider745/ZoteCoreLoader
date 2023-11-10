package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.AutoInject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Player")
public class PlayerHooks {
    @AutoInject(arguments = {"tickDiff"}, static_method = false, type_hook = TypeHook.BEFORE)
    public static void entityBaseTick(HookController controller){
        Player self = controller.getSelf();

        self.sendAllInventories();
        int tick = Server.getInstance().getTick();

        if(tick % 10 == 0){
            self.sendMessage("Test message");
        }
    }
}
