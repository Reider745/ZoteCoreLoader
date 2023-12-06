package com.zhekasmirnov.innercore.ui;

import android.app.Activity;
import java.util.ArrayList;

public class ModLoadingOverlay {
    private LoadingOverlayDrawable drawable;

    private static final ArrayList<ModLoadingOverlay> overlayInstances = new ArrayList<>();

    public ModLoadingOverlay(Activity context) {
    }

    public void openNow() {
        synchronized (overlayInstances) {
            overlayInstances.add(this);
        }
    }

    public void closeNow() {
        synchronized (overlayInstances) {
            overlayInstances.remove(this);
        }
    }

    public boolean await(int maxTime) {
        openNow();
        return false;
    }

    public void close() {
        closeNow();
    }

    public static void sendLoadingProgress(float progress) {
        synchronized (overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setProgress(progress);
                }
            }
        }
    }

    public static void sendLoadingText(String str) {
        synchronized (overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setText(str);
                }
            }
        }
    }

    public static void sendLoadingTip(String str) {
        synchronized (overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setTip(str);
                }
            }
        }
    }
}
