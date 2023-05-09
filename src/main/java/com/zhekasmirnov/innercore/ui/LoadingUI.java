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
               Logger.debug("INNERCORE-LOADING-UI", "showing...");
                isShowed = true;
            } catch (Exception err) {
                isShowed = false;
                Logger.error("InnerCore", err);
            }
        }
    }

    public static void hide() {
        if (isShowed) {
           Logger.debug("INNERCORE-LOADING-UI", "hiding...");
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
        if (!isOpened) {
            isOpened = true;
            runCustomUiThread();

            (new Thread(new Runnable() {
                @Override
                public void run() {
                    postOnCustomUiThread(new Runnable() {
                        @Override
                        public void run() {
                            show();
                           Logger.debug("LOADING-UI", "opened");
                        }
                    });

                    while (isOpened) {
                        boolean isOnTop = true;
                        if (isOnTop && !isShowed) {
                           Logger.debug("LOADING-UI", "application open, showing...");
                            postOnCustomUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    show();
                                }
                            });
                        }
                        if (!isOnTop && isShowed) {
                            postOnCustomUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hide();
                                }
                            });
                        }

                        try {
                            Thread.sleep(75);
                        } catch (InterruptedException ignore) {
                        }
                    }

                    postOnCustomUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hide();
                            stopCustomUiThread();
                        }
                    });

                }
            })).start();
        }
    }

    public static void close() {
        if (isOpened) {
            isOpened = false;
        }
    }

    public static void setTextAndProgressBar(String text, float progressBar) {
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
