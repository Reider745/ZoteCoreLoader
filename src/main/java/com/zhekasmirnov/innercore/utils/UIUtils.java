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

    public static void getOffsets(int[] offsets) {
        offsets[0] = 0;
        offsets[1] = 0;
    }

    public static void initialize(Activity ctx) {
    }

    public static Activity getContext() {
        return Activity.getSingletonInternalProxy();
    }

    public static View getDecorView() {
        return View.getSingletonInternalProxy();
    }

    public static void runOnUiThreadUnsafe(Runnable action) {
        getContext().runOnUiThread(action);
    }

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

    public static ComponentName getActivityOnTop() {
        return ComponentName.getSingletonInternalProxy();
    }

    public static boolean isInnerCoreActivityOpened() {
        return true;
    }
}
