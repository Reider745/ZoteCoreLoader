package com.reider745.commands;

import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;

public class CommandsHelper {
    public static void init(){
        final SimpleCommandMap map = Server.getInstance().getCommandMap();
        final String fallbackPrefix = "inner_core";

        map.register(fallbackPrefix, new ModsListCommand());
        map.register(fallbackPrefix, new CustomBlocksCommands());
        map.register(fallbackPrefix, new CustomItemsCommands());
        map.register(fallbackPrefix, new StateCommand());
        map.register(fallbackPrefix, new InnerCoreNetworkCommand());
        map.register(fallbackPrefix, new GenChunksCommands());
        map.register(fallbackPrefix, new PlayerInfoCommand());
    }
}
