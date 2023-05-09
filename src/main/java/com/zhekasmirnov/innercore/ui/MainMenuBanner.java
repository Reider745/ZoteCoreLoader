package com.zhekasmirnov.innercore.ui;

public class MainMenuBanner {
    private static final MainMenuBanner instance = new MainMenuBanner();

    public static MainMenuBanner getInstance() {
        return instance;
    }

    public enum Location {
        BOTTOM,
        LEFT_SIDE
    }


    //private final double density = AdsManager.getInstance().getDesiredAdDensity();
    //private final HashMap<String, PopupWindow> windows = new HashMap<>();
    
    private MainMenuBanner() {
        /*if (density == 1) {
            isAvailable = false;
            return;
        }
        if (density < 0.8) {
            isAvailable = false;
        } else {
            isAvailable = Math.random() < (density - 0.8) * 10;
        }*/
    }

    public boolean runFullScreenAdRandom() {
        /*if (density < 0.8) {
            return AdsManager.getInstance().runDesiredDensityRandom();
        } else if (density < 0.9) {
            return !isAvailable;
        } else {
            return Math.random() < (density - 0.9) * 10;
        }*/
        return false;
    }

    public boolean isShowed(String name) {
        return true;
    }

    public void close(final String name) {

    }

    public void show(final String name, final Location location) {

    }
}