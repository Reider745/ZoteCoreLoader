package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.*;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class UIElementCleaner {
    public UIElement element;
    public Rect rect;

    public UIElementCleaner(UIElement element) {
        this.element = element;
    }

    public UIElementCleaner clone() {
        UIElementCleaner cleaner = new UIElementCleaner(element);
        cleaner.set(rect);
        return cleaner;
    }

    public void set(Rect rect) {
        this.rect = rect;
    }

    public void clean(Canvas canvas, float scale) {
    }

    public void debug(Canvas canvas, float scale) {
    }
}
