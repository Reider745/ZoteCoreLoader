package com.zhekasmirnov.innercore.api.mod.util;

import android.graphics.Bitmap;
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
    @Deprecated(since = "Zote")
    public static void dialog(String msg, final String title) {
    }

    @JSStaticFunction
    @Deprecated(since = "Zote")
    public static void dialog(String msg) {
    }

    @JSStaticFunction
    @Deprecated(since = "Zote")
    public static void img(Bitmap bmp, final String prefix) {
    }

    @JSStaticFunction
    @Deprecated(since = "Zote")
    public static void img(Bitmap bmp) {
    }
}
