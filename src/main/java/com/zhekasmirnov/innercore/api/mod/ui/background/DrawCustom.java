package com.zhekasmirnov.innercore.api.mod.ui.background;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 05.08.2017.
 */

public class DrawCustom implements IDrawing {
    @Override
    public void onSetup(ScriptableObject description, UIStyle style) {
    }

    @Override
    public void onDraw(Canvas canvas, float scale) {
    }

    protected Object callDescriptionMethodSafe(String name, Object... args) {
        return null;
    }
}
