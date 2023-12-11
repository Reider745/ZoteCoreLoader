package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;

/**
 * Created by zheka on 12.09.2017.
 */

public class RandomBitmapWrap extends BitmapWrap {
    RandomBitmapWrap(Bitmap bitmap) {
        super();

        this.bitmap = Bitmap.getSingletonInternalProxy();
        this.width = this.height = 16;
    }

    @Override
    public boolean store() {
        return true;
    }

    @Override
    public boolean restore() {
        return true;
    }

    @Override
    public BitmapWrap resize(int w, int h) {
        restoreIfNeeded();
        width = w;
        height = h;
        return this;
    }
}
