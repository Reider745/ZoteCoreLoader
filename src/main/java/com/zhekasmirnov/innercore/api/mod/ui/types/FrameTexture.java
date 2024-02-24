package com.zhekasmirnov.innercore.api.mod.ui.types;

import android.graphics.*;

/**
 * Created by zheka on 04.08.2017.
 */

@Deprecated(since = "Zote")
public class FrameTexture {
    public static final int SIDE_LEFT = 0;
    public static final int SIDE_RIGHT = 1;
    public static final int SIDE_TOP = 2;
    public static final int SIDE_BOTTOM = 3;
    public static final int CORNER_TOP_LEFT = 4;
    public static final int CORNER_TOP_RIGHT = 5;
    public static final int CORNER_BOTTOM_LEFT = 6;
    public static final int CORNER_BOTTOM_RIGHT = 7;

    public FrameTexture(Bitmap source) {
    }

    public Bitmap expandSide(int sideId, int pixels) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap expand(int w, int h, int color, boolean[] sides) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap expand(int w, int h, int color) {
        return expand(w, h, color, new boolean[] { true, true, true, true });
    }

    public Bitmap expandAndScale(float w, float h, float scale, int color, boolean[] sides) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap expandAndScale(float w, float h, float scale, int color) {
        return expandAndScale(w, h, scale, color, new boolean[] { true, true, true, true });
    }

    public Bitmap getSource() {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getSideSource(int side) {
        return Bitmap.getSingletonInternalProxy();
    }

    public int getCentralColor() {
        return 0;
    }

    public void draw(Canvas canvas, RectF rect, float scale, int color, boolean[] sides) {
    }
}
