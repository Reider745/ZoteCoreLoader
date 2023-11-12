package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.lang.TextContainer;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Player")
public class PlayerHooks {
    @Inject
    public static void entityBaseTick(Player self, int tickDiff){

    }

    @Inject
    public static void completeLoginSequence(Player self){
        System.out.println("Full connection");
    }

    @Inject
    public static void close(Player self, TextContainer message, String reason, boolean notify){
        System.out.println("disconnection");
    }
}
