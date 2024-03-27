package com.reider745.commands;

import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;

public class CommandsHelper {
    public static void init() {
        final SimpleCommandMap map = Server.getInstance().getCommandMap();
        final String tag = "inner_core";

        map.register(tag, new ModsListCommand());
        map.register(tag, new CustomBlocksCommands());
        map.register(tag, new CustomItemsCommands());
        map.register(tag, new StateCommand());
        map.register(tag, new InnerCoreNetworkCommand());
        map.register(tag, new GenChunksCommands());
        map.register(tag, new CallbackProfilingCommand());
        map.register(tag, new GetAllPlayersIdCommand());
    }
}
