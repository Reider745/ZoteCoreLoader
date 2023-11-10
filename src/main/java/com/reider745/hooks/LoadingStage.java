package com.reider745.hooks;

import com.reider745.Main;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Server")
public class LoadingStage {
    @Inject()
    public static void start(HookController controller) throws Exception {
        Main.LoadingStages.start(controller.getSelf());
    }
}
