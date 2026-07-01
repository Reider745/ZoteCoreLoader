package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.apparatus.adapter.innercore.UserDialog;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.utils.UIUtils;

import java.util.List;

/**
 * Created by zheka on 16.09.2017.
 */

public class DialogHelper {
    public static String getFormattedLog(int limit) {
        return ICLog.getLogFilter().buildFilteredLog(true, limit);
    }

    public static String getFormattedLog() {
        return getFormattedLog(InnerCoreConfig.getInt("logging.dialog_entries_limit", 32));
    }

    public static String getFormattedStackTrace(Throwable error) {
        return "<font color='#FF0000'>" + ICLog.getStackTrace(error) + "</font>";
    }

    public static void openFormattedDialog(final String text, final String title, int maxCount, Runnable dialogOverflowCallback, Runnable dialogCloseCallback) {

    }

    public static void openFormattedDialog(final String text, final String title, int maxCount, Runnable dialogOverflowCallback) {

    }

    public static void openFormattedDialog(String text, String title) {

    }

    public static int getTotalDialogCount() {
        return 0;
    }

    public interface ICustomErrorCallback {
        boolean show(String message, Throwable error, String log, String stacktrace);
    };

    public static void setCustomFatalErrorCallback(ICustomErrorCallback customFatalErrorCallback) {
    }

    public static void reportFatalError(String message, Throwable error) {

    }

    public static void setCustomNonFatalErrorCallback(ICustomErrorCallback customNonFatalErrorCallback) {

    }

    public static void reportNonFatalError(String message, Throwable error) {

    }

    public static void reportNonFatalErrorList(String message, List<Throwable> errors) {

    }

    public static void setCustomStartupErrorCallback(ICustomErrorCallback customStartupErrorCallback) {

    }

    public static void reportStartupErrors(String message) {

    }
}
