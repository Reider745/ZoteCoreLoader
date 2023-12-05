package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.util.HashMap;

/**
 * Created by zheka on 04.08.2017.
 */

public class WindowParent {
    private static HashMap<Integer, PopupWindow> attachedPopups = new HashMap<>();

    private static int getWindowFlags(final UIWindow window) {
        int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (window.isTouchable()) {
            if (!window.isBlockingBackground()) {
                flags = flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            }
        } else {
            flags = flags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        if (window.isBlockingBackground()) {
            flags = flags | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
        return flags;
    }

    public static void openWindow(final UIWindow window) {

    }

    public static void closeWindow(final UIWindow window) {

    }

    public static void updateWindowPositionAndSize(UIWindow window) {

    }

    public static void applyWindowInsets(final UIWindow window, final WindowInsets insets) {
        UIWindowLocation loc = window.getLocation();
        if (window.isNotFocusable()) {
            PopupWindow popup = attachedPopups.get(window.hashCode());
            if (popup != null) {
                loc.updatePopupWindow(popup);
            }
        }
    }

    public static void releaseWindowLayout(final View layout) {

    }
}
