package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.Rect;
import android.view.WindowManager;
import android.widget.PopupWindow;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.utils.UIUtils;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindowLocation {
    public int x, y, width, height;
    public int scrollX, scrollY;
    public boolean forceScrollX, forceScrollY;
    public boolean globalScale = false;
    public float scale;

    public float zIndex = 0;

    public UIWindowLocation() {
        set(0, 0, 1000, (int) (UIUtils.screenHeight * 1000.0f / UIUtils.screenWidth));
    }

    public UIWindowLocation(ScriptableObject obj) {
        if (obj == null) {
            set(0, 0, 1000, (int) (UIUtils.screenHeight * 1000.0f / UIUtils.screenWidth));
        } else {
            x = ScriptableObjectHelper.getIntProperty(obj, "x", 0);
            y = ScriptableObjectHelper.getIntProperty(obj, "y", 0);
            width = ScriptableObjectHelper.getIntProperty(obj, "width", 1000 - x);
            height = ScriptableObjectHelper.getIntProperty(obj, "height",
                    (int) (UIUtils.screenHeight * 1000.0f / UIUtils.screenWidth) - y);
            forceScrollX = ScriptableObjectHelper.getBooleanProperty(obj, "forceScrollX", false);
            forceScrollY = ScriptableObjectHelper.getBooleanProperty(obj, "forceScrollY", false);
            globalScale = ScriptableObjectHelper.getBooleanProperty(obj, "globalScale", false);

            ScriptableObject padding = ScriptableObjectHelper.getScriptableObjectProperty(obj, "padding", null);
            if (padding != null) {
                int top = ScriptableObjectHelper.getIntProperty(padding, "top", -1);
                if (top >= 0) {
                    setPadding(PADDING_TOP, top);
                }
                int bottom = ScriptableObjectHelper.getIntProperty(padding, "bottom", -1);
                if (bottom >= 0) {
                    setPadding(PADDING_BOTTOM, bottom);
                }
                int left = ScriptableObjectHelper.getIntProperty(padding, "left", -1);
                if (left >= 0) {
                    setPadding(PADDING_LEFT, left);
                }
                int right = ScriptableObjectHelper.getIntProperty(padding, "right", -1);
                if (right >= 0) {
                    setPadding(PADDING_RIGHT, right);
                }
            }

            scrollX = Math.max(width, ScriptableObjectHelper.getIntProperty(obj, "scrollX", width));
            scrollY = Math.max(height, ScriptableObjectHelper.getIntProperty(obj, "scrollY",
                    ScriptableObjectHelper.getIntProperty(obj, "scrollHeight", height)));
        }
    }

    public void setScroll(int x, int y) {
        scrollX = Math.max(width, x);
        scrollY = Math.max(height, y);
    }

    public void setSize(int x, int y) {
        if (scrollX == width) {
            scrollX = x;
        }
        if (scrollY == height) {
            scrollY = y;
        }
        width = x;
        height = y;
        setScroll(scrollX, scrollY);
    }

    public ScriptableObject asScriptable() {
        ScriptableObject loc = new ScriptableObject() {
            @Override
            public String getClassName() {
                return "Window Location";
            }
        };

        loc.put("x", loc, x);
        loc.put("y", loc, y);
        loc.put("width", loc, width);
        loc.put("height", loc, height);
        loc.put("scrollX", loc, scrollX);
        loc.put("scrollY", loc, scrollY);

        return loc;
    }

    public UIWindowLocation copy() {
        UIWindowLocation loc = new UIWindowLocation();
        loc.x = this.x;
        loc.y = this.y;
        loc.width = this.width;
        loc.height = this.height;
        loc.scrollX = this.scrollX;
        loc.scrollY = this.scrollY;
        loc.globalScale = this.globalScale;
        return loc;
    }

    public void set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scrollX = width;
        this.scrollY = height;
    }

    public void set(UIWindowLocation loc) {
        this.x = loc.x;
        this.y = loc.y;
        this.width = loc.width;
        this.height = loc.height;
        this.scrollX = loc.scrollX;
        this.scrollY = loc.scrollY;
        this.globalScale = loc.globalScale;
    }

    public void removeScroll() {
        scrollX = width;
        scrollY = height;
    }

    public static final int PADDING_TOP = 0;
    public static final int PADDING_BOTTOM = 1;
    public static final int PADDING_LEFT = 2;
    public static final int PADDING_RIGHT = 3;

    public void setPadding(int padding, int value) {
        int x1 = this.x;
        int y1 = this.y;
        int x2 = this.x + this.width;
        int y2 = this.y + this.height;

        if (value < 0)
            value = 0;

        switch (padding) {
            case PADDING_TOP:
                y1 = value;
                break;
            case PADDING_BOTTOM:
                y2 = (int) (UIUtils.screenHeight * 1000.0f / UIUtils.screenWidth - value) + 1;
                break;
            case PADDING_LEFT:
                x1 = value;
                break;
            case PADDING_RIGHT:
                x2 = 1000 - value + 1;
                break;
        }

        if (scrollX == this.width) {
            scrollX = Math.max(1, x2 - x1);
        }

        if (scrollY == this.height) {
            scrollY = Math.max(1, y2 - y1);
        }

        this.x = x1;
        this.y = y1;
        this.width = Math.max(1, x2 - x1);
        this.height = Math.max(1, y2 - y1);
    }

    public void setPadding(float top, float bottom, float left, float right) {
        setPadding(PADDING_TOP, (int) top);
        setPadding(PADDING_BOTTOM, (int) bottom);
        setPadding(PADDING_LEFT, (int) left);
        setPadding(PADDING_RIGHT, (int) right);
    }

    public float getScale() {
        return scale = UIUtils.screenWidth / 1000.0f;
    }

    public float getDrawingScale() {
        return (globalScale ? 1.0f : Math.max(scrollX, width) / 1000.0f) * getScale();
    }

    public Rect getRect() {
        return Rect.getSingletonInternalProxy();
    }

    public void showPopupWindow(PopupWindow win) {
    }

    public void updatePopupWindow(PopupWindow win) {
    }

    public WindowManager.LayoutParams getLayoutParams(int a1, int a2, int a3) {
        return WindowManager.LayoutParams.getSingletonInternalProxy();
    }

    public void setupAndShowPopupWindow(PopupWindow win) {
    }

    public void setZ(float z) {
        zIndex = z;
    }

    public int getWindowWidth() {
        return 1000;
    }

    public int getWindowHeight() {
        return (int) globalToWindow(Math.max(scrollY, height));
    }

    public float globalToWindow(float val) {
        return val / getDrawingScale() * getScale();
    }

    public float windowToGlobal(float val) {
        return val * getDrawingScale() / getScale();
    }
}
