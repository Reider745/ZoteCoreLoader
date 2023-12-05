package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.background.IDrawing;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapCache;

import java.util.ArrayList;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindowBackgroundDrawable extends Drawable implements IBackgroundProvider {
    public UIWindow window;
    private int backgroundColor = Color.WHITE;

    public UIWindowBackgroundDrawable(UIWindow window) {
        this.window = window;
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    private ArrayList<IDrawing> backgroundDrawings = new ArrayList<>();

    @Override
    public void addDrawing(IDrawing drawing) {
        backgroundDrawings.add(drawing);
    }

    @Override
    public void clearAll() {
        backgroundDrawings.clear();
    }

    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such
     * as alpha (set via setAlpha) and color filter (set via setColorFilter).
     *
     * @param canvas The canvas to draw into
     */

    private Bitmap cachedBackground = null;
    private Canvas cachedCanvas = null;

    @Override
    public void draw(@NonNull Canvas canvas) {
/*
        if (!isCacheReleased && (window.isBackgroundDirty || cachedBackground == null)) {
            if (drawDirty(canvas)) {
                window.isBackgroundDirty = false;
            }
        }

        if (cachedBackground != null) {
            canvas.drawBitmap(cachedBackground, 0, 0, null);
        }
        else {
            try {
                drawIntoCache(canvas);
            } catch(OutOfMemoryError err) {
                handleLowMemory();
            }
        }
*/
        //drawIntoCache(canvas);


        try {
            drawIntoCache(canvas);
        } catch(OutOfMemoryError err) {
            handleLowMemory();
        }
    }

    private boolean drawDirty(Canvas canvas) {
        try {
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            if (cachedBackground == null) {
                cachedBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                cachedCanvas = new Canvas(cachedBackground);
            } else if (cachedBackground.getWidth() != w || cachedBackground.getHeight() != h) {
                cachedBackground.recycle();
                cachedBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                cachedCanvas = new Canvas(cachedBackground);
            }

            drawIntoCache(cachedCanvas);
            return true;
        } catch (OutOfMemoryError ignored) {
            handleLowMemory();
            return false;
        }
    }

    private void handleLowMemory() {
        releaseCache();
        BitmapCache.immediateGC();
        NativeItemModel.tryReleaseModelBitmapsOnLowMemory(1024 * 1024 * 16); // release some item model icons
    }

    private void drawIntoCache(Canvas canvas) {
        float scale = window.getScale();
        canvas.drawColor(backgroundColor);

        for (IDrawing drawing : backgroundDrawings) {
            drawing.onDraw(canvas, scale);
        }
    }

    private boolean isCacheReleased = false;

    public void prepareCache() {
        isCacheReleased = false;
    }

    public void releaseCache() {
        if (cachedBackground != null) {
            cachedCanvas = null;
            cachedBackground.recycle();
            cachedBackground = null;
            isCacheReleased = true;
        }
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
}
