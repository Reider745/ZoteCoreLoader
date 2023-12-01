package com.reider745.commands;

import cn.nukkit.command.SimpleCommandMap;
import com.reider745.InnerCoreServer;

public class CommandsHelper {
    public static void init(){
        SimpleCommandMap map = InnerCoreServer.server.getCommandMap();
        map.register("innercore", new ModsListCommand());
        map.register("innercore", new CustomBlocksCommands());
        map.register("innercore", new CustomItemsCommands());
    }
}
