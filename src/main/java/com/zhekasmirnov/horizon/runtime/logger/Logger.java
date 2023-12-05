package com.zhekasmirnov.horizon.runtime.logger;

import cn.nukkit.Server;

public class Logger {
    public static Server server;

    public static void debug(String message) {
        server.getLogger().info(message);
    }

    public static void debug(String tag, String message) {
        server.getLogger().info("[" + tag + "] " + message);
    }

    public static void error(String message) {
        server.getLogger().error(message);
    }

    public static void error(String tag, String message) {
        server.getLogger().error("[" + tag + "] " + message);
    }

    public static void error(String tag, Exception exc) {
        server.getLogger().error("[" + tag + "] ", exc);
    }

    public static void info(String tag, String message) {
        server.getLogger().info("[" + tag + "] " + message);
    }
}
