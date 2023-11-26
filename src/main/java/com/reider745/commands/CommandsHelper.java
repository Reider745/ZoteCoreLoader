package com.reider745.commands;

import com.reider745.InnerCoreServer;

public class CommandsHelper {
    public static void init(){
        InnerCoreServer.server.getCommandMap().register("innercore", new ModsListCommand());
    }
}
