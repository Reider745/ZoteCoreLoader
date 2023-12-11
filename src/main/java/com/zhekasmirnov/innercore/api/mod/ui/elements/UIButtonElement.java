package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 02.08.2017.
 */

public class UIButtonElement extends UIElement {
    public UIButtonElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

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
