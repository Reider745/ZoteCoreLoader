package com.zhekasmirnov.innercore.api.mod.ui.types;

import android.graphics.*;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapWrap;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class Texture {
    public BitmapWrap bitmap;
    public BitmapWrap[] animation = new BitmapWrap[1];

    public boolean isAnimation = false;

    public float delay = 1; // in ticks
    private float width = -1;
    private float height = -1;

    public Texture(Bitmap bitmap) {
        this.bitmap = BitmapWrap.wrap(bitmap);
        this.isAnimation = false;
    }

    public Texture(Bitmap[] bitmaps) {
        this.animation = new BitmapWrap[bitmaps.length];
        for (int i = 0; i < bitmaps.length; i++) {
            this.animation[i] = BitmapWrap.wrap(bitmaps[i]);
        }

        this.isAnimation = true;
    }

    public Texture(Object obj) {
        this(obj, null);
    }

    public Texture(Object obj, UIStyle style) {
        this.bitmap = BitmapWrap.wrap("missing_bitmap");
        this.isAnimation = false;
    }

    public boolean isAnimated() {
        return animation != null && animation.length > 1;
    }

    public void readOffset(ScriptableObject obj) {
    }

    public int getFrame() {
        if (isAnimation) {
            double num = (System.currentTimeMillis() % 1000000000L) / (delay * 50);
            return ((int) num % animation.length);
        } else {
            return 0;
        }
    }

    public Bitmap getBitmap(int frame) {
        if (isAnimation) {
            return animation[frame].get();
        } else {
            return bitmap.get();
        }
    }

    public BitmapWrap getBitmapWrap(int frame) {
        if (isAnimation) {
            return animation[frame];
        } else {
            return bitmap;
        }
    }

    public void draw(Canvas canvas, float x, float y, float scale) {
    }

    public void drawCutout(Canvas canvas, RectF cutout, float x, float y, float scale) {
    }

    public float getWidth() {
        return width > 0 ? width : 16;
    }

    public float getHeight() {
        return height > 0 ? height : 16;
    }

    public void resizeAll(float w, float h) {
        width = w;
        height = h;
    }

    public void rescaleAll(float scale) {
        resizeAll((getWidth() * scale), (getHeight() * scale));
    }

    public void fitAllToOneSize() {
        resizeAll(getWidth(), getHeight());
    }

    public void release() {
        if (bitmap != null) {
            bitmap.storeIfNeeded();
        }
        if (animation[0] != null) {
            for (BitmapWrap bmp : animation) {
                bmp.storeIfNeeded();
            }
        }
    }
}
