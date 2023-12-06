package com.zhekasmirnov.innercore.ui;

public class MainMenuBanner {
    private static MainMenuBanner instance;

    public static MainMenuBanner getInstance() {
        if (instance == null) {
            instance = new MainMenuBanner();
        }
        return instance;
    }

    public enum Location {
        BOTTOM,
        LEFT_SIDE
    }

    public final boolean isAvailable;

    private MainMenuBanner() {
        isAvailable = false;
    }

    public boolean runFullScreenAdRandom() {
        return false;
    }

    public boolean isShowed(String name) {
        return false;
    }

    public void close(final String name) {
    }

    public void show(final String name, final Location location) {
    }
}
