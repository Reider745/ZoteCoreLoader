package com.zhekasmirnov.innercore.ui;

import android.app.Activity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

/**
 * Created by zheka on 25.06.2017.
 */

public class LoadingUI {

    @Deprecated(since = "Zote")
    public static void initializeFor(Activity ctx) {
    }

    @Deprecated(since = "Zote")
    public static void initViews() {
    }

    private static boolean isShowed = false;

    @Deprecated(since = "Zote")
    public static boolean isShowed() {
        return isShowed;
    }

    @Deprecated(since = "Zote")
    public static void show() {
        if (!isShowed) {
            isShowed = true;
        }
    }

    @Deprecated(since = "Zote")
    public static void hide() {
        if (isShowed) {
            isShowed = false;
        }
    }

    private static boolean isOpened = false;

    @Deprecated(since = "Zote")
    public static void open() {
        if (!isOpened) {
            isOpened = true;
            show();
        }
    }

    @Deprecated(since = "Zote")
    public static void close() {
        if (isOpened) {
            isOpened = false;
        }
    }

    public static void setTextAndProgressBar(String text, float progressBar) {
        Logger.debug("INNERCORE", "updated loading ui: " + text + " (" + progressBar * 100f + "%)");
    }

    public static void setText(String text) {
        Logger.debug("INNERCORE", "updated loading ui text: " + text);
    }

    public static void setProgress(float progress) {
        Logger.debug("INNERCORE", "updated loading ui progress: " + progress * 100f + "%");
    }

    public static void setTip(String text) {
        Logger.debug("INNERCORE", "updated loading ui tip: " + text);
    }
}
