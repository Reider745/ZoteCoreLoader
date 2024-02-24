package com.zhekasmirnov.innercore.ui;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by zheka on 05.01.2018.
 */

@Deprecated(since = "Zote")
public class LoadingOverlayDrawable extends Drawable {

    public LoadingOverlayDrawable(View parent) {
    }

    public void setText(String text) {
    }

    public void setTip(String tip) {
    }

    public void setProgress(float progress) {
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
