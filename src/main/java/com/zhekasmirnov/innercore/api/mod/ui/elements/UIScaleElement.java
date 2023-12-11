package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 03.08.2017.
 */

public class UIScaleElement extends UIElement {
    public UIScaleElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 3;
    public static final int DIRECTION_RIGHT = 0;
    public static final int DIRECTION_LEFT = 2;

    @Override
    public void onSetup(ScriptableObject description) {
    }

    @Override
    public void onDraw(Canvas canvas, float scale) {
    }

    @Override
    public void onBindingUpdated(String name, Object val) {
    }
}
