package com.zhekasmirnov.apparatus.adapter.innercore;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class UserDialog {
    public static void dialog(String str, String s, Throwable err, boolean b) {
        Logger.error(str, s, err);
    }

    public static void insignificantError(String s, Throwable th, boolean b) {
        Logger.error(s, th);
    }
}
