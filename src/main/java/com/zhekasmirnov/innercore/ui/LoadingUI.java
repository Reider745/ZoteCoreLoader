package com.zhekasmirnov.innercore.ui;

import android.app.Activity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

/**
 * Created by zheka on 25.06.2017.
 */

public class LoadingUI {
    public static void initializeFor(Activity ctx) {
    }

    public static void initViews() {
    }

    private static boolean isShowed = false;

    public static boolean isShowed() {
        return isShowed;
    }

    public static void show() {
        if (!isShowed) {
            Logger.debug("INNERCORE-LOADING-UI", "showing...");
            isShowed = true;
        }
    }

    public static void hide() {
        if (isShowed) {
            Logger.debug("INNERCORE-LOADING-UI", "hiding...");
            isShowed = false;
        }
    }

    private static boolean isOpened = false;

    public static void open() {
        if (!isOpened) {
            isOpened = true;
            show();
            Logger.debug("LOADING-UI", "opened");
        }
    }

    public static void close() {
        if (isOpened) {
            isOpened = false;
        }
    }

    public static void setTextAndProgressBar(String text, float progressBar) {
        Logger.debug("INNERCORE", "updated loading ui: " + text + " (" + progressBar + ")");
        ModLoadingOverlay.sendLoadingText(text);
        ModLoadingOverlay.sendLoadingProgress(progressBar);
    }

    public static void setText(String text) {
        Logger.debug("INNERCORE", "updated loading ui text: " + text);
        ModLoadingOverlay.sendLoadingText(text);
        ModLoadingOverlay.sendLoadingTip("");
    }

    public static void setProgress(float progress) {
        Logger.debug("INNERCORE", "updated loading ui progress: " + progress);
        ModLoadingOverlay.sendLoadingProgress(progress);
    }

    public static void setTip(String text) {
        Logger.debug("INNERCORE", "updated loading ui tip: " + text);
        ModLoadingOverlay.sendLoadingTip(text);
    }
}
