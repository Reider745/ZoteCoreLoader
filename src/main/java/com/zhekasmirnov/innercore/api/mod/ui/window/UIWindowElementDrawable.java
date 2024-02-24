package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IElementProvider;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.types.ITouchEventListener;
import com.zhekasmirnov.innercore.api.mod.ui.types.TouchEvent;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import java.util.ArrayList;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class UIWindowElementDrawable extends Drawable implements IElementProvider, ITouchEventListener {
    public UIWindow window;

    public UIWindowElementDrawable(UIWindow window) {
        this.window = window;
    }

    public ArrayList<UIElement> windowElements = new ArrayList<>();
    private UIStyle windowStyle = UIStyle.DEFAULT;

    @Override
    public void setBackgroundProvider(IBackgroundProvider provider) {
    }

    @Override
    public synchronized void addOrRefreshElement(UIElement element) {
        if (windowElements.contains(element)) {
            windowElements.remove(element);
        }

        element.onSetup();

        boolean added = false;
        for (int i = 0; i < windowElements.size(); i++) {
            UIElement existingElement = windowElements.get(i);
            if (existingElement.z > element.z) {
                windowElements.add(i, element);
                added = true;
                break;
            }
        }
        if (!added) {
            windowElements.add(element);
        }

    }

    @Override
    public synchronized void removeElement(UIElement element) {
        if (windowElements.contains(element)) {
            windowElements.remove(element);
            element.onRelease();
        }
    }

    @Override
    public void releaseAll() {
        for (UIElement element : windowElements) {
            element.onReset();
            element.onRelease();
        }
        windowElements.clear();
    }

    @Override
    public void resetAll() {
        for (UIElement element : windowElements) {
            element.onReset();
        }
    }

    @Override
    public void invalidateAll() {
        for (UIElement element : windowElements) {
            element.invalidate();
        }
    }

    @Override
    public void runCachePreparation() {
    }

    @Override
    public UIStyle getStyleFor(UIElement element) {
        return windowStyle;
    }

    @Override
    public void setWindowStyle(UIStyle style) {
        windowStyle = style;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
    }

    boolean isDebugEnabled = false;

    public synchronized void drawDirty(Canvas canvas, float scale) {
    }

    @Override
    public synchronized void onTouchEvent(TouchEvent event) {
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public String toString() {
        return "[ElementDrawable elements=" + windowElements.size() + "]";
    }
}
