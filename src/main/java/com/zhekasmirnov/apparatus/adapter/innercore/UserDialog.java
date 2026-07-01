package com.zhekasmirnov.apparatus.adapter.innercore;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;

public class UserDialog {
    public static void toast(String message) {
        PrintStacking.print(message);
        if (message.length() < 8192)
            ICLog.d("TOAST", message);
    }

    public static void message(String message) {
        NativeAPI.clientMessage(message);
    }

    public static void tip(String message) {
        NativeAPI.tipMessage(message);
    }

    public static void dialog(String title, String message, Throwable error, boolean showLog, boolean isForced) {
        if (isForced) {
            DialogHelper.openFormattedDialog(message + (error != null ? "\n\nSTACKTRACE:\n" + DialogHelper.getFormattedStackTrace(error) : "") + (showLog ? "\n\nLOG:\n" + DialogHelper.getFormattedLog() : ""), title);
        } else {
            DialogHelper.openFormattedDialog(message +
                            (error != null ? "\n\nSTACKTRACE:\n" + DialogHelper.getFormattedStackTrace(error) : "") +
                            (showLog ? "\n\nLOG:\n" + DialogHelper.getFormattedLog() : ""),
                    title, 3, () -> insignificantError(title + ": " + message, error, true, false));
        }
    }

    public static void dialog(String title, String message, Throwable error, boolean showLog) {
        dialog(title, message, error, showLog, false);
    }

    public static void dialog(String title, String message) {
        dialog(title, message, null, false);
    }

    public static void dialog(String message) {
        dialog("message", message);
    }


    private static class DecisionStatus {
        private boolean result = false;
    }

    public static boolean awaitDecision(String title, String text, String positiveLabel, String negativeLabel) {
        return false;
    }

    public static void insignificantError(String message, Throwable err, boolean showToast, boolean showTrace) {
        if (message.length() < 8192)
            ICLog.e("ERROR", message, err);
        if (showToast) {
            if (err != null) {
                toast(message + ": " + err.getMessage() + (showTrace ? ", stacktrace: \n" + ICLog.getStackTrace(err) : ", see log for details "));
            } else {
                toast(message);
            }
        }
    }

    public static void insignificantError(String message, Throwable err, boolean showToast) {
        insignificantError(message, err, showToast, false);
    }

    public static void insignificantError(String message, Throwable err) {
        insignificantError(message, err, true);
    }
}
