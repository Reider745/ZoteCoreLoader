package com.zhekasmirnov.innercore.api.mod.util;

import android.graphics.Bitmap;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

/**
 * Created by zheka on 31.07.2017.
 */

public class DebugAPI extends ScriptableObject {
    @Override
    public String getClassName() {
        return "DebugAPI";
    }

    @JSStaticFunction
    public static void dialog(String msg, final String title) {
        Logger.info("DebugAPI/dialog", title + ": " + msg);
    }

    @JSStaticFunction
    public static void dialog(String msg) {
        dialog(msg, "");
    }

    @JSStaticFunction
    public static void img(Bitmap bmp, final String prefix) {
    }

    @JSStaticFunction
    public static void img(Bitmap bmp) {
    }
}
