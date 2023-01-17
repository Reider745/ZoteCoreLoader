package com.zhekasmirnov.innercore.api.log;

import cn.nukkit.Server;

public class ICLog {
    public static Server server;
    public static String buildMessage(String title, String text){
        return "["+title+"] "+text;
    }
    public static void e(String title, String text, Throwable e){
        server.getLogger().emergency(buildMessage(title, text), e);
    }
    public static void i(String title, String text){
        server.getLogger().info(buildMessage(title, text));
    }
    public static void d(String title, String text){
        server.getLogger().info(buildMessage(title, text));
    }
}
