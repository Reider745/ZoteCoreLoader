package com.reider745.hooks;

import cn.nukkit.Server;
import com.reider745.Main;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.annotation.AutoInject;
import com.reider745.api.hooks.annotation.Hooks;
import com.zhekasmirnov.apparatus.Apparatus;

@Hooks(class_name = "cn.nukkit.Server")
public class LoadingStage {
    @AutoInject(signature = "()V", static_method = false)
    public static void start(HookController controller) throws Exception {
        Main.LoadingStages.start(controller.getSelf());
    }
}
