package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 15.08.2017.
 */

@Deprecated(since = "Zote")
public class UISwitchElement extends UIElement {
    public UISwitchElement(UIWindow window, ScriptableObject description) {
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
