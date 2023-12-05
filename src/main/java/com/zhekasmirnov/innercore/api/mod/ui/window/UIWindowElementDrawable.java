package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IElementProvider;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapCache;
import com.zhekasmirnov.innercore.api.mod.ui.types.ITouchEventListener;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;

import java.util.ArrayList;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindowElementDrawable extends Drawable implements IElementProvider, ITouchEventListener {
    public UIWindow window;

    public UIWindowElementDrawable(UIWindow window) {
        this.window = window;
    }

    public ArrayList<UIElement> windowElements = new ArrayList<>();
    private UIStyle windowStyle = UIStyle.DEFAULT;

    private IBackgroundProvider backgroundProvider;

    @Override
    public void setBackgroundProvider(IBackgroundProvider provider) {
        backgroundProvider = provider;
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

    private static Bitmap preparationBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
    private static Canvas preparationCanvas = new Canvas(preparationBitmap);

    @Override
    public void runCachePreparation() {
        long timeStart = System.currentTimeMillis();
        drawDirty(preparationCanvas, window.getScale());
        long timeEnd = System.currentTimeMillis();
        //ICLog.i("UI", "cache preparation took " + (timeEnd - timeStart) + " ms, rendered " + windowElements.size() + " elements.");
    }

    @Override
    public UIStyle getStyleFor(UIElement element) {
        return windowStyle;
    }

    @Override
    public void setWindowStyle(UIStyle style) {

    }


    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such
     * as alpha (set via setAlpha) and color filter (set via setColorFilter).
     *
     * @param canvas The canvas to draw into
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        try {
            if (backgroundProvider != null) {
                ((Drawable) backgroundProvider).draw(canvas);
            }

            if (window.isForegroundDirty) {
                drawDirty(canvas, window.getScale());
                window.isForegroundDirty = false;
            }
        } catch (Exception e) {
            ICLog.e("UI", "uncaught exception occurred", e);
        } catch (OutOfMemoryError e) {
            BitmapCache.immediateGC();
            NativeItemModel.tryReleaseModelBitmapsOnLowMemory(1024 * 1024 * 16); // release some item model icons
        }
    }

    boolean isDebugEnabled = false;

    public synchronized void drawDirty(Canvas canvas, float scale) {
        for (int i = 0; i < windowElements.size(); i++) {
            UIElement element = windowElements.get(i);
            if (!element.isReleased()) {
                element.onDraw(canvas, scale);
                if (isDebugEnabled) {
                    element.debug(canvas, scale);
                }
            }
        }
    }


    /**
     * Specify an alpha value for the drawable. 0 means fully transparent, and
     * 255 means fully opaque.
     *
     * @param alpha
     */
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

    @Override
    public void onTouchEvent(Object event) {

    }
}
