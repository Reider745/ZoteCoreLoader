package com.zhekasmirnov.innercore.api.mod.ui.background;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 02.08.2017.
 */

public interface IDrawing {
    void onSetup(ScriptableObject description, Object style);
    void onDraw(Object canvas, float scale);
}
