package com.zhekasmirnov.innercore.ui;

import java.util.ArrayList;

public class ModLoadingOverlay {
    private LoadingOverlayDrawable drawable;

    private static final ArrayList<ModLoadingOverlay> overlayInstances = new ArrayList<>();
    
    public ModLoadingOverlay(Object context) {
    }


    private boolean isOpened = false;

    public void openNow() {
        // init variables

        // close old view

        isOpened = true;

        synchronized(overlayInstances) {
            overlayInstances.add(this);
        }
        
        MainMenuBanner.getInstance().show("loading-" + hashCode(), MainMenuBanner.Location.LEFT_SIDE);
    }

    public void closeNow() {
        isOpened = false;


        synchronized(overlayInstances) {
            overlayInstances.remove(this);
        }
        
        MainMenuBanner.getInstance().close("loading-" + hashCode());
    }

    public boolean await(int maxTime) {
        openNow();

        long startTime = System.currentTimeMillis();
        while (!isOpened && startTime + maxTime > System.currentTimeMillis()) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                break;
            }
        }
        return false;
    }

    public void close() {
        closeNow();
    }


    public static void sendLoadingProgress(float progress) {
        synchronized(overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setProgress(progress);
                }
            }
        }
    }

    public static void sendLoadingText(String str) {
        synchronized(overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setText(str);
                }
            }
        }
    }

    public static void sendLoadingTip(String str) {
        synchronized(overlayInstances) {
            for (ModLoadingOverlay overlay : overlayInstances) {
                if (overlay.drawable != null) {
                    overlay.drawable.setTip(str);
                }
            }
        }
    }
    
};