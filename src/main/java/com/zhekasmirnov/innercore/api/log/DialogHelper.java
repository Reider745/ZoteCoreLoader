package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

/**
 * Created by zheka on 16.09.2017.
 */

public class DialogHelper {
    public static String getFormattedLog() {
        return ICLog.getLogFilter().buildFilteredLog(true);
    }

    public static String getFormattedStackTrace(Throwable error) {
        return ICLog.getStackTrace(error);
    }

    public static final int DEFAULT_MAX_COUNT = 5;
    private static int totalDialogCount = 0;

    public static void openFormattedDialog(final String text, final String title, int maxCount,
            Runnable dialogOverflowCallback) {
        Logger.info(title, text);
    }

    public static void openFormattedDialog(String text, String title) {
        Logger.info(title, text);
    }

    public static int getTotalDialogCount() {
        return totalDialogCount;
    }

    public interface ICustomErrorCallback {
        boolean show(String message, Throwable error, String log, String stacktrace);
    }

    private static ICustomErrorCallback customFatalErrorCallback = null;

    public static void setCustomFatalErrorCallback(ICustomErrorCallback customFatalErrorCallback) {
        DialogHelper.customFatalErrorCallback = customFatalErrorCallback;
    }

    public static void reportFatalError(String message, Throwable error) {
        String stacktrace = getFormattedStackTrace(error);
        String log = getFormattedLog();
        if (customFatalErrorCallback != null && customFatalErrorCallback.show(message, error, log, stacktrace)) {
            return;
        }
        String text = message + "\n\n\nSTACKTRACE:\n</font>" + stacktrace + "\n\n\nLOG:\n" + log;
        openFormattedDialog(text, "FATAL ERROR");
    }

    private static ICustomErrorCallback customNonFatalErrorCallback = null;

    public static void setCustomNonFatalErrorCallback(ICustomErrorCallback customNonFatalErrorCallback) {
        DialogHelper.customNonFatalErrorCallback = customNonFatalErrorCallback;
    }

    public static void reportNonFatalError(String message, Throwable error) {
        String stacktrace = getFormattedStackTrace(error);
        if (customNonFatalErrorCallback != null
                && customNonFatalErrorCallback.show(message, error, getFormattedLog(), stacktrace)) {
            return;
        }

        String text = message + "\n\n\nSTACKTRACE:\n" + stacktrace;
        openFormattedDialog(text, "NON-FATAL ERROR");
    }

    private static ICustomErrorCallback customStartupErrorCallback = null;

    public static void setCustomStartupErrorCallback(ICustomErrorCallback customStartupErrorCallback) {
        DialogHelper.customStartupErrorCallback = customStartupErrorCallback;
    }

    public static void reportStartupErrors(String message) {
        String log = getFormattedLog();
        if (customStartupErrorCallback != null && customStartupErrorCallback.show(message, null, log, null)) {
            return;
        }

        String text = message + "\n\n\nLOG:\n" + log;
        openFormattedDialog(text, "NON-FATAL ERROR");
    }
}
