package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 03.08.2017.
 */

@Deprecated(since = "Zote")
public class UICustomElement extends UIElement {
    public UICustomElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

    private ScriptableObject elementScope;

    public ScriptableObject getScope() {
        if (elementScope == null) {
            elementScope = ScriptableObjectHelper.createEmpty();
        }
        return elementScope;
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
