package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.mod.ui.types.Texture;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIImageElement extends UIElement {
    public UIImageElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

    public Texture texture = new Texture((Bitmap) null), overlay = new Texture((Bitmap) null);
    public float textureScale = 1, width = 0, height = 0;

    @Override
    public void onSetup(ScriptableObject description) {
    }

    public boolean isAnimated() {
        return false;
    }

    @Override
    public void onDraw(Canvas canvas, float scale) {
    }

    @Override
    public void onBindingUpdated(String name, Object val) {
    }
}
