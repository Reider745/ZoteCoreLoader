package com.zhekasmirnov.innercore.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.view.View;

import com.zhekasmirnov.innercore.api.log.ICLog;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIUtils {
    public static int screenWidth = 2028, screenHeight = 1080;
    public static int xOffset = 0, yOffset = 0;

    @Deprecated(since = "Zote")
    public static void initialize(Activity ctx) {
    }

    @Deprecated(since = "Zote")
    public static void getOffsets(int[] offsets) {
    }

    @Deprecated(since = "Zote")
    public static Activity getContext() {
        return Activity.getSingletonInternalProxy();
    }

    @Deprecated(since = "Zote")
    public static View getDecorView() {
        return View.getSingletonInternalProxy();
    }

    @Deprecated(since = "Zote")
    public static void runOnUiThreadUnsafe(Runnable action) {
        getContext().runOnUiThread(action);
    }

    @Deprecated(since = "Zote")
    public static void runOnUiThread(final Runnable action) {
        getContext().runOnUiThread(() -> {
            try {
                action.run();
            } catch (Exception e) {
                processError(e);
            }
        });
    }

    public static void processError(Exception e) {
        ICLog.e("INNERCORE-UI", "exception occured in UI engine:", e);
    }

    public static void log(String msg) {
        ICLog.d("INNERCORE-UI", msg);
    }

    @Deprecated(since = "Zote")
    public static ComponentName getActivityOnTop() {
        return ComponentName.getSingletonInternalProxy();
    }

    @Deprecated(since = "Zote")
    public static boolean isInnerCoreActivityOpened() {
        return true;
    }
}
