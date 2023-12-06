package com.zhekasmirnov.innercore.utils;

import android.graphics.Color;
import org.mozilla.javascript.MembersPatch;

public class ColorsPatch {

    public static void init() {
        MembersPatch.addOverride("android.graphics.Color.rgb", "com.zhekasmirnov.innercore.utils.ColorsPatch.rgb");
        MembersPatch.addOverride("android.graphics.Color.argb", "com.zhekasmirnov.innercore.utils.ColorsPatch.argb");
    }

    public static int rgb(float red, float green, float blue) {
        // Logger.debug("MEMBERS_PATCH", "Called patched Color.rgb() method");
        if (red > 1 || green > 1 || blue > 1) {
            return Color.rgb((int) red, (int) green, (int) blue);
        }
        return Color.rgb(red, green, blue);
    }

    public static int argb(float alpha, float red, float green, float blue) {
        // Logger.debug("MEMBERS_PATCH", "Called patched Color.argb() method");
        if (red > 1 || green > 1 || blue > 1) {
            return Color.argb((int) alpha, (int) red, (int) green, (int) blue);
        }
        return Color.argb(alpha, red, green, blue);
    }
}
