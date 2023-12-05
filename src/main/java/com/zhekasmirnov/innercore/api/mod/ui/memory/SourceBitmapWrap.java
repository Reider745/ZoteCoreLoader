package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;

/**
 * Created by zheka on 12.09.2017.
 */

public class SourceBitmapWrap extends BitmapWrap {
    private String name;
    private boolean isDirectlyFromSource = false;

    SourceBitmapWrap(String name, int w, int h) {
        super();
        this.name = name;
        this.width = w;
        this.height = h;
        this.isStored = true;
    }

    @Override
    public boolean store() {
        if (bitmap != null && !isDirectlyFromSource) {
            bitmap.recycle();
            bitmap = null;
        }

        return true;
    }

    @Override
    public boolean restore() {
        if (width < 1 || height < 1) {
            recycle();
            return false;
        }

        Bitmap bmp = TextureSource.instance.getSafe(name);
        bitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
        isDirectlyFromSource = (bmp == bitmap);
        return true;
    }

    @Override
    public BitmapWrap resize(int w, int h) {
        BitmapWrap wrap = BitmapWrap.wrap(name, w, h);
        storeIfNeeded();
        return wrap;
    }

    @Override
    public void recycle() {
        if (isDirectlyFromSource) {
            bitmap = null;
            isRecycled = true;
            removeCache();
        }
        else {
            super.recycle();
        }
    }
}
