package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptStackElement;

/**
 * Created by zheka on 09.08.2017.
 */

public class ICLog {
    public static int INLINE_STACKTRACE_LINES_LIMIT = 5;
    private static LogFilter logFilter = new LogFilter();
    private static LogWriter logWriter;
    private static boolean hasEvenSeenRhinoErrors = false;

    public static LogFilter getLogFilter() {
        return logFilter;
    }

    public static LogWriter getLogWriter() {
        return logWriter;
    }

    static {
        logWriter = new LogWriter(new File(FileTools.DIR_WORK + "inner-core.log"));
        logWriter.clear();
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
        logFilter.log(type, prefix, msg);
        logWriter.logMsg(type, prefix, msg);
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
            String stacktrace = captureRhinoStackTrace(INLINE_STACKTRACE_LINES_LIMIT, true);
            Logger.error(prefix, removeFaultSymbolsFromString(message + (stacktrace != null ? " (" + stacktrace + ")" : "")));
        } else {
            Logger.info(prefix, removeFaultSymbolsFromString(message));
        }
    }

    public static void e(String prefix, String message) {
        e(prefix, message, null);
    }

    public static void e(String prefix, String message, Throwable err) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onErrorEvent(prefix, message, err);
        }

        String stacktrace = err != null ? "\n" + getStackTrace(err) : null;
        if (stacktrace == null) {
            stacktrace = captureRhinoStackTrace(INLINE_STACKTRACE_LINES_LIMIT, true);
            if (stacktrace != null) {
                stacktrace = " (" + stacktrace + ")";
            }
        }
        logMsg(LogType.ERROR, "ERROR", (prefix != null ? "[" + prefix + "] " : "") + message + (stacktrace != null ? stacktrace : ""));
        Logger.error(prefix, removeFaultSymbolsFromString(message + (stacktrace != null ? stacktrace : "")));
    }

    public static String getStackTrace(Throwable err) {
        return exceptionToString(err, false, false, -1, -1);
    }

    public static String exceptionToString(Throwable err, boolean inline, boolean originalCauseOnly, int javaFrameLimit, int jsFrameLimit) {
        String jsStack = captureRhinoStackTrace(err, jsFrameLimit, inline);
        if (err instanceof WrappedException && ((WrappedException) err).getWrappedException() != null) {
            err = ((WrappedException) err).getWrappedException();
        }
        if (originalCauseOnly) {
            while (err.getCause() != null) {
                err = err.getCause();
            }
        }
        if (inline) {
            StackTraceElement[] stack = err.getStackTrace();
            return err.getMessage() + " " + jsStack + " " + (stack.length > 0 ? stack[stack.length - 1] : "");
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try {
                // todo: actually limit stack trace
                if (javaFrameLimit != 0) {
                    err.printStackTrace(pw);
                } else {
                    pw.print(err);
                }
            } catch (Throwable th) {
                pw.write("<internal error occurred while attempting to retrieve stacktrace>");
            }
            if (jsStack != null) {
                return "JS STACK TRACE:\n" + jsStack + "\n\nFULL STACK TRACE:\n" + sw.toString();
            } else {
                return sw.toString();
            }
        }
    }

    public static String captureRhinoStackTrace(Throwable th, int limit, boolean inlined) {
        try {
            if (th instanceof RhinoException) {
                return getRhinoStackTrace((RhinoException) th, limit, inlined);
            }
            try {
                throw Context.throwAsScriptRuntimeEx(th != null ? th : new RuntimeException());
            } catch (RhinoException exc) {
                return getRhinoStackTrace(exc, limit, inlined);
            } catch (RuntimeException ignore) {
            }
        } catch (Throwable th2) {
            ICLog.i("ERROR", "captureRhinoStackTrace failed with error " + th); // note: avoid recursion
        }
        return null;
    }

    public static String captureRhinoStackTrace(int limit, boolean inlined) {
        return captureRhinoStackTrace(null, limit, inlined);
    }

    private static String getRhinoStackTrace(RhinoException exc, int limit, boolean inlined) {
        ScriptStackElement[] stack = null;
        try {
            stack = exc.getScriptStack(limit <= 0 ? -1 : limit, null);
        } catch (Throwable th) {
            if (!hasEvenSeenRhinoErrors) {
                Logger.error("InnerCore-Log", "FATAL ERROR occurred while attempting to retrieve stacktrace; following errors will be ignored: " + th.getMessage());
                hasEvenSeenRhinoErrors = true;
            }
        }

        if (stack == null) {
            return "<failed to get stacktrace>";
        } else if (stack.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (ScriptStackElement element : stack) {
                if (sb.length() != 0) {
                    sb.append(inlined ? ", " : "\n");
                } else if (inlined) {
                    sb.append("at ");
                }

                if (!inlined) {
                    sb.append("\tat ");
                }
                sb.append(element.fileName);
                if (element.lineNumber >= 0) {
                    sb.append(':').append(element.lineNumber);
                }
                if (element.functionName != null) {
                    sb.append(" (").append(element.functionName).append(')');
                }
            }
            return sb.toString();
        }
        return null;
    }

    public static void flush() {
        logWriter.flush();
    }

    public static void showIfErrorsAreFound() {
        if (LogFilter.isContainingErrorTags()) {
            DialogHelper.reportStartupErrors("Some errors are occured during Inner Core startup and loading.");
        }
    }

    private static long elapsedTimeStart = 0;

    public static void initElapsedTime() {
        elapsedTimeStart = System.nanoTime();
    }

    public static double getElapsedTime() {
        return (System.nanoTime() - elapsedTimeStart) / 1e9;
    }

    public static double getElapsedTimeRound() {
        return Math.round((System.nanoTime() - elapsedTimeStart) / 1e7) / 100.0;
    }

    public static String timePrefix() {
        return getElapsedTimeRound() + ": ";
    }
}
