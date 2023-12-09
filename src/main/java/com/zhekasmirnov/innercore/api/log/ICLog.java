package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.RhinoException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * Created by zheka on 09.08.2017.
 */

public class ICLog {
    private static LogFilter logFilter;
    private static LogWriter logWriter;

    public static LogFilter getLogFilter() {
        if (logFilter == null) {
            logFilter = new LogFilter();
        }
        return logFilter;
    }

    public static LogWriter getLogWriter() {
        if (logWriter == null) {
            logWriter = new LogWriter(new File(FileTools.DIR_WORK + "inner-core.log"));
            logWriter.clear();
        }
        return logWriter;
    }

    private static HashMap<Long, IEventHandler> eventHandlerForThread = new HashMap<>();

    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    public static void setupEventHandlerForCurrentThread(IEventHandler eventHandler) {
        eventHandlerForThread.put(getThreadId(), eventHandler);
    }

    public static IEventHandler getEventHandlerForCurrentThread() {
        return eventHandlerForThread.get(getThreadId());
    }

    private static String removeFaultSymbolsFromString(String s) {
        return s.replaceAll("%", "%%");
    }

    private static void logMsg(LogType type, String prefix, String msg) {
        if (logFilter != null) {
            logFilter.log(type, prefix, msg);
        }
        if (logWriter != null) {
            logWriter.logMsg(type, prefix, msg);
        }
    }

    public static void l(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onLogEvent(prefix, message);
        }
        logMsg(LogType.LOG, prefix, message);
        Logger.debug(prefix, removeFaultSymbolsFromString(message));
    }

    public static void d(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onDebugEvent(prefix, message);
        }
        logMsg(LogType.DEBUG, prefix, message);
        Logger.debug(prefix, removeFaultSymbolsFromString(message));
    }

    public static void i(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onImportantEvent(prefix, message);
        }
        logMsg(LogType.IMPORTANT, prefix, message);
        if (prefix.toUpperCase().equals("ERROR")) {
            Logger.error(prefix, removeFaultSymbolsFromString(message));
        } else {
            Logger.info(prefix, removeFaultSymbolsFromString(message));
        }
    }

    public static void e(String prefix, String message, Throwable err) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onErrorEvent(prefix, message, err);
        }
        logMsg(LogType.ERROR, "ERROR",
                (prefix != null ? "[" + prefix + "] " : "") + message + (err != null ? "\n" + getStackTrace(err) : ""));
        Logger.error(prefix, removeFaultSymbolsFromString(message + (err != null ? "\n" + getStackTrace(err) : "")));
        if (err != null) {
            err.printStackTrace();
        }
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

    public static void flush() {
        logWriter.flush();
    }

    public static void showIfErrorsAreFound() {
        if (LogFilter.isContainingErrorTags()) {
            DialogHelper.reportStartupErrors("Some errors are occured during Inner Core startup and loading.");
        }
    }
}
