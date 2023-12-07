package com.reider745.commands;

import cn.nukkit.command.SimpleCommandMap;
import com.reider745.InnerCoreServer;

public class CommandsHelper {
    public static void init(){
        final SimpleCommandMap map = InnerCoreServer.server.getCommandMap();
        final String fallbackPrefix = "inner_core";

        map.register(fallbackPrefix, new ModsListCommand());
        map.register(fallbackPrefix, new CustomBlocksCommands());
        map.register(fallbackPrefix, new CustomItemsCommands());
        map.register(fallbackPrefix, new StateCommand());
        map.register(fallbackPrefix, new InnerCoreNetworkCommand());
        map.register(fallbackPrefix, new GenChunksCommands());
    }
}
