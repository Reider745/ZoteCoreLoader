package com.reider745.commands;

import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;

public class CommandsHelper {
    public static void init() {
        final SimpleCommandMap map = Server.getInstance().getCommandMap();
        map.register("inner_core", new ModsListCommand());
        map.register("inner_core", new CustomBlocksCommands());
        map.register("inner_core", new CustomItemsCommands());
        map.register("inner_core", new StateCommand());
        map.register("inner_core", new InnerCoreNetworkCommand());
        map.register("inner_core", new GenChunksCommands());
    }
}
