package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.types.FrameTexture;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 06.08.2017.
 */

public class UIFrameElement extends UIElement {
    public UIFrameElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

    protected FrameTexture frame = new FrameTexture(null);
    protected float width = 16, height = 16;
    protected float scale = 1;
    protected int color = -1;
    protected boolean[] sides = new boolean[] { true, true, true, true };

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
