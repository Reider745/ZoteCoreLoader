package com.zhekasmirnov.innercore.api.mod.ui.background;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 02.08.2017.
 */

public interface IDrawing {
    void onSetup(ScriptableObject description, UIStyle style);

    void onDraw(Canvas canvas, float scale);
}
