package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Player")
public class PlayerHooks {
    @Inject(type_hook = TypeHook.BEFORE)
    public static void entityBaseTick(HookController controller){
        Player self = controller.getSelf();
        int tick = Server.getInstance().getTick();

        if(tick % 10 == 0){
            self.sendAllInventories();
        }
    }
}
