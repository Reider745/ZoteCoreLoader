package com.zhekasmirnov.innercore.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.mcpe161.InnerCore;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIUtils {
    public static int screenWidth, screenHeight;
    public static int xOffset = 0, yOffset = 0;

    public static void getOffsets(int[] offsets) {
        /*Activity activity = InnerCore.getInstance().getCurrentActivity();
        if (activity != null) {
            View view = activity.getWindow().getDecorView();
            if (view != null) {
                int[] loc = new int[2];
                view.getLocationOnScreen(offsets);
                loc[1] = 0;
            }
        }*/
    }

    private static void refreshScreenParams(Activity ctx) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ctx.getWindowManager().getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        Point size = new Point();
        display.getSize(size);

        /*Activity activity = InnerCore.getInstance().getCurrentActivity();
        if (activity != null) {
            View view = activity.getWindow().getDecorView();
            if (view != null) {
                int[] loc = new int[2];
                view.getLocationOnScreen(loc);
                System.out.println("decor location on screen: " + loc[0] + " " + loc[1]);

                screenWidth = Math.max(realSize.x, realSize.y) - loc[0];
                screenHeight = Math.min(realSize.x, realSize.y);// view.getHeight();
                xOffset = 0; //Math.max(realSize.x, realSize.y) - screenWidth;
                yOffset = 0; //Math.min(realSize.x, realSize.y) - screenHeight;
                return;
            }
        }*/

        screenWidth = Math.max(size.x, size.y);
        screenHeight = Math.min(size.x, size.y);
        xOffset = 0; //Math.max(realSize.x, realSize.y) - screenWidth;
        yOffset = 0; //Math.min(realSize.x, realSize.y) - screenHeight;
    }

    public static void initialize(Activity ctx) {
        refreshScreenParams(ctx);
    }

    public static Activity getContext() {
        return null;
        //return EnvironmentSetup.getCurrentActivity();
        /*
        Activity ctx = null;
        if (MinecraftActivity.current != null) {
            ctx = EnvironmentSetup.getCurrentActivity();
        }
        if (ctx == null && ExtractModActivity.current != null) {
            ctx = ExtractModActivity.current.get();
        }
        return ctx;*/
    }

    public static View getDecorView() {
        return getContext().getWindow().getDecorView();
    }

    public static void runOnUiThreadUnsafe(Runnable action) {
        getContext().runOnUiThread(action);
    }

    public static void runOnUiThread(final Runnable action) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    action.run();
                } catch (Exception e) {
                    processError(e);
                }
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
        try {
            ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            return activityManager.getRunningTasks(1).get(0).topActivity;
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
    }

    public static boolean isInnerCoreActivityOpened() {
        ComponentName name = getActivityOnTop();
        return name != null && "com.zhekasmirnov.innercore".equals(name.getPackageName());
    }
}
