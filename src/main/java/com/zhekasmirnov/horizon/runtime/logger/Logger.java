package com.zhekasmirnov.horizon.runtime.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

import cn.nukkit.Server;
import org.mozilla.javascript.RhinoException;


public class Logger {
    public static Server server;

    public static void message(String message) {
        server.getLogger().debug(message);
    }

    public static void message(String tag, String message) {
        server.getLogger().debug("[" + tag + "] " + message);
    }

    public static void message(String message, Throwable exc) {
        server.getLogger().debug(message + ": ", exc);
    }

    public static void message(String tag, String message, Throwable exc) {
        server.getLogger().debug("[" + tag + "] " + message, exc);
    }

    public static void debug(String message) {
        server.getLogger().info(message);
    }

    public static void debug(String tag, String message) {
        server.getLogger().info("[" + tag + "] " + message);
    }

    public static void debug(String message, Throwable exc) {
        server.getLogger().info(message + ": ", exc);
    }

    public static void debug(String tag, String message, Throwable exc) {
        server.getLogger().info("[" + tag + "] " + message, exc);
    }

    public static void error(String message) {
        server.getLogger().error(message);
    }

    public static void error(String tag, String message) {
        server.getLogger().error("[" + tag + "] " + message);
    }

    public static void error(String message, Throwable exc) {
        server.getLogger().error(message + ": ", exc);
    }

    public static void error(String tag, String message, Throwable exc) {
        server.getLogger().error("[" + tag + "] " + message, exc);
    }

    public static void info(String message) {
        server.getLogger().info(message);
    }

    public static void info(String tag, String message) {
        server.getLogger().info("[" + tag + "] " + message);
    }

    public static void info(String message, Throwable exc) {
        server.getLogger().info(message + ": ", exc);
    }

    public static void info(String tag, String message, Throwable exc) {
        server.getLogger().info("[" + tag + "] " + message, exc);
    }

    public static void warning(String message) {
        server.getLogger().warning(message);
    }

    public static void warning(String tag, String message) {
        server.getLogger().warning("[" + tag + "] " + message);
    }

    public static void warning(String message, Throwable exc) {
        server.getLogger().warning(message + ": ", exc);
    }

    public static void warning(String tag, String message, Throwable exc) {
        server.getLogger().warning("[" + tag + "] " + message, exc);
    }

    public static void critical(String message) {
        server.getLogger().critical(message);
    }

    public static void critical(String tag, String message) {
        server.getLogger().warning("[" + tag + "] " + message);
    }

    public static void critical(String message, Throwable exc) {
        server.getLogger().critical(message + ": ", exc);
    }

    public static void critical(String tag, String message, Throwable exc) {
        server.getLogger().critical("[" + tag + "] " + message, exc);
    }

    public static String getStackTrace(Throwable err) {
        String jsStack = null;
        if (err instanceof RhinoException) {
            jsStack = ((RhinoException) err).getScriptStackTrace();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        err.printStackTrace(pw);

        if (jsStack != null) {
            return "JS STACK TRACE:\n" + jsStack + "\n\nFULL STACK TRACE:\n" + sw.toString();
        } else {
            return sw.toString();
        }
    }

    public static void setOutputFile(String path) {
    }

    public static void setCrashFile(String path) {
    }
}
