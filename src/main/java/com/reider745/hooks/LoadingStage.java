package com.reider745.hooks;

import cn.nukkit.Server;
import com.reider745.Main;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Server")
public class LoadingStage {
    @Inject
    public static void start(Server self) throws Exception {
        Main.LoadingStages.start(self);
    }
}
