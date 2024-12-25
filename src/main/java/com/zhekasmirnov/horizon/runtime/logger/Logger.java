package com.zhekasmirnov.horizon.runtime.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

import cn.nukkit.Server;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.mozilla.javascript.RhinoException;

public class Logger {
    interface Func {
        void message(String text);
    }

    private static Func debugMessage = (message) -> {};
    private static Func infoMessage = (message) -> {};

    public static void initConfig() {
        if(InnerCoreServer.isDebugLogger())
            debugMessage = (message) -> Server.getInstance().getLogger().info(message);
        if(InnerCoreServer.isInfoLogger())
            debugMessage = (message) -> Server.getInstance().getLogger().info(message);
    }

    public static void message(String message) {
        Server.getInstance().getLogger().debug(message);
    }

    public static void message(String tag, String message) {
        Server.getInstance().getLogger().debug("[" + tag + "] " + message);
    }

    public static void message(String message, Throwable exc) {
        Server.getInstance().getLogger().debug(message + ": ", exc);
    }

    public static void message(String tag, String message, Throwable exc) {
        Server.getInstance().getLogger().debug("[" + tag + "] " + message, exc);
    }

    public static void debug(String message) {
        debugMessage.message(message);
    }

    public static void debug(String tag, String message) {
        debugMessage.message("["+tag+"]"+message);
    }

    public static void debug(String message, Throwable exc) {
        debugMessage.message(message + ": " + ICLog.getStackTrace(exc));
    }

    public static void debug(String tag, String message, Throwable exc) {
        debugMessage.message(message + ": " + ICLog.getStackTrace(exc));
    }

    public static void error(String message) {
        Server.getInstance().getLogger().error(message);
    }

    public static void error(String tag, String message) {
        Server.getInstance().getLogger().error("[" + tag + "] " + message);
    }

    public static void error(String message, Throwable exc) {
        Server.getInstance().getLogger().error(message + ": ", exc);
    }

    public static void error(String tag, String message, Throwable exc) {
        Server.getInstance().getLogger().error("[" + tag + "] " + message, exc);
    }

    public static void info(String message) {
        infoMessage.message(message);
    }

    public static void info(String tag, String message) {
        infoMessage.message("["+tag+"]"+ message);
    }

    public static void info(String message, Throwable exc) {
        infoMessage.message(message +": " + ICLog.getStackTrace(exc));
    }

    public static void info(String tag, String message, Throwable exc) {
        infoMessage.message("[" + tag + "] " + message + "\n" + ICLog.getStackTrace(exc));
    }

    public static void warning(String message) {
        Server.getInstance().getLogger().warning(message);
    }

    public static void warning(String tag, String message) {
        Server.getInstance().getLogger().warning("[" + tag + "] " + message);
    }

    public static void warning(String message, Throwable exc) {
        Server.getInstance().getLogger().warning(message + ": ", exc);
    }

    public static void warning(String tag, String message, Throwable exc) {
        Server.getInstance().getLogger().warning("[" + tag + "] " + message, exc);
    }

    public static void critical(String message) {
        Server.getInstance().getLogger().critical(message);
    }

    public static void critical(String tag, String message) {
        Server.getInstance().getLogger().warning("[" + tag + "] " + message);
    }

    public static void critical(String message, Throwable exc) {
        Server.getInstance().getLogger().critical(message + ": ", exc);
    }

    public static void critical(String tag, String message, Throwable exc) {
        Server.getInstance().getLogger().critical("[" + tag + "] " + message, exc);
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
