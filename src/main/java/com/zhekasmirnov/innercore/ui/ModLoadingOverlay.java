package com.zhekasmirnov.innercore.ui;

import android.app.Activity;

@Deprecated(since = "Zote")
public class ModLoadingOverlay {

    public ModLoadingOverlay(Activity context) {
    }

    public void openNow() {
    }

    public void closeNow() {
    }

    public boolean await(int maxTime) {
        return false;
    }

    public void close() {
    }

    public static void sendLoadingProgress(float progress) {
    }

    public static void sendLoadingText(String str) {
    }

    public static void sendLoadingTip(String str) {
    }
}
