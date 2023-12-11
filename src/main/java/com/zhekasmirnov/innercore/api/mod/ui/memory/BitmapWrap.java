package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;
import java.util.HashMap;

/**
 * Created by zheka on 31.08.2017.
 */

public abstract class BitmapWrap {
    public static final Bitmap MISSING_BITMAP = Bitmap.getSingletonInternalProxy();

    protected Bitmap bitmap;
    protected int width, height;
    protected Bitmap.Config config;

    protected boolean isStored, isRecycled;

    BitmapWrap() {
        isStored = isRecycled = false;
        config = Bitmap.Config.ARGB_8888;
        BitmapCache.registerWrap(this);
    }

    public abstract boolean store();

    public abstract boolean restore();

    public abstract BitmapWrap resize(int w, int h);

    public void storeIfNeeded() {
        isStored = true;
    }

    public void restoreIfNeeded() {
        isStored = false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap.Config getConfig() {
        return config;
    }

    private int useId = -1;

    public int getStackPos() {
        return BitmapCache.getStackPos(useId);
    }

    public Bitmap get() {
        synchronized (this) {
            useId = BitmapCache.getUseId();

            restoreIfNeeded();
            return bitmap != null && !bitmap.isRecycled() ? bitmap : MISSING_BITMAP;
        }
    }

    public boolean isRecycled() {
        return isRecycled;
    }

    public void recycle() {
        isRecycled = true;
        BitmapCache.unregisterWrap(this);
    }

    public void removeCache() {
    }

    public Bitmap getResizedCache(int w, int h) {
        return Bitmap.getSingletonInternalProxy();
    }

    public static BitmapWrap wrap(Bitmap bmp) {
        return new RandomBitmapWrap(bmp);
    }

    private static HashMap<String, BitmapWrap> namedWrapCache = new HashMap<>();

    public static BitmapWrap wrap(String name, int w, int h) {
        String key = name + w + "." + h;
        BitmapWrap wrap = namedWrapCache.get(key);
        if (wrap != null) {
            return wrap;
        } else {
            wrap = new SourceBitmapWrap(name, w, h);
            namedWrapCache.put(key, wrap);
            return wrap;
        }
    }

    public static BitmapWrap wrap(String name) {
        return wrap(name, 16, 16);
    }
}
