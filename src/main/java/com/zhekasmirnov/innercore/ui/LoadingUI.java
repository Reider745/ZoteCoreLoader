package com.zhekasmirnov.innercore.ui;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
//import com.zhekasmirnov.innercore.utils.UIUtils;


/**
 * Created by zheka on 25.06.2017.
 */

public class LoadingUI {

    public static void initializeFor(Object ctx) {
        initViews();
    }
    private static LoadingOverlayDrawable backgroundViewDrawable;

    public static void initViews() {

    }

    private static boolean isShowed = false;

    public static boolean isShowed() {
        return isShowed;
    }

    public static void show() {
        if (!isShowed) {
            try {
                isShowed = true;
            } catch (Exception err) {
                isShowed = false;
            }
        }
    }

    public static void hide() {
        if (isShowed) {
            isShowed = false;
        }
    }

    private static boolean isOpened = false;

    private static void runCustomUiThread() {
        stopCustomUiThread();
    }

    private static void postOnCustomUiThread(Runnable r) {

    }

    private static void stopCustomUiThread() {

    }

    public static void open() {

    }

    public static void close() {
        if (isOpened) {
            isOpened = false;
        }
    }

    public static void setTextAndProgressBar(String text, float progressBar) {
        Logger.debug(text+":"+progressBar);
    }

    public static void setText(String text) {
    }

    public static void setProgress(float progress) {
    }

    public static void setTip(String text) {
    }
}
