package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.io.File;
import java.io.IOException;

/**
 * Created by zheka on 12.09.2017.
 */

public class RandomBitmapWrap extends BitmapWrap {
    private File cache;
    private static long cacheId = "bitmap-cache".hashCode();

    RandomBitmapWrap(Bitmap bitmap) {
        super();

        this.bitmap = bitmap != null && !bitmap.isRecycled() ? bitmap : MISSING_BITMAP.copy(MISSING_BITMAP.getConfig(), true);
        this.width = this.bitmap.getWidth();
        this.height = this.bitmap.getHeight();
        this.config = this.bitmap.getConfig();

        this.cache = BitmapCache.getCacheFile("" + cacheId++);
    }

    @Override
    public boolean store() {
        if (this.bitmap == null || this.bitmap.isRecycled()) {
            recycle();
            return false;
        }

        try {
            BitmapCache.writeToFile(cache, bitmap);
        } catch (IOException e) {
            ICLog.e("UI", "failed to store bitmap " + bitmap, e);
            recycle();
            return false;
        }

        bitmap.recycle();
        bitmap = null;
        return true;
    }

    @Override
    public boolean restore() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        try {
            bitmap = Bitmap.createBitmap(width, height, config);
            BitmapCache.readFromFile(cache, bitmap);
        } catch (Exception e) {
            ICLog.e("UI", "failed to restore bitmap " + bitmap, e);
            recycle();
            return false;
        }

        return true;
    }

    @Override
    public BitmapWrap resize(int w, int h) {
        restoreIfNeeded();

        Bitmap bmp = bitmap;
        if (bmp != null && !bmp.isRecycled()) {
            bmp = Bitmap.createScaledBitmap(bmp, w, h, false);
        }

        if (bmp != bitmap) {
            return new RandomBitmapWrap(bmp);
        }

        width = w;
        height = h;
        return this;
    }

    @Override
    public void recycle() {
        super.recycle();
        cache.delete();
    }
}
