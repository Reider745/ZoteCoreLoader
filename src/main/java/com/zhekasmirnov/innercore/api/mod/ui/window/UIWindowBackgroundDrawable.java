package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.background.IDrawing;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class UIWindowBackgroundDrawable extends Drawable implements IBackgroundProvider {
    public UIWindow window;

    public UIWindowBackgroundDrawable(UIWindow window) {
        this.window = window;
    }

    @Override
    public void setBackgroundColor(int color) {
    }

    @Override
    public void addDrawing(IDrawing drawing) {
    }

    @Override
    public void clearAll() {
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
    }

    public void prepareCache() {
    }

    public void releaseCache() {
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
