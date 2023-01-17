package com.zhekasmirnov.horizon.runtime.logger;

import cn.nukkit.Server;

public class Logger {
    public static Server server;
    public static void debug(String debug){
        server.getLogger().info(debug);
    }
    public static void debug(String title, String debug){
        server.getLogger().info("["+title+"]"+debug);
    }
    public static void error(String debug){
        server.getLogger().info(debug);
    }
    public static void error(String title, String debug){
        server.getLogger().error("["+title+"]"+debug);
    }
    public static void error(String title, Exception e){
        server.getLogger().error("["+title+"]", e);
    }
    public static void info(String title, String info){
        server.getLogger().info("["+title+"]"+info);
    }
}
