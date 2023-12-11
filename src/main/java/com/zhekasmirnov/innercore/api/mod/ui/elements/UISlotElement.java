package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiVisualSlotImpl;
import com.zhekasmirnov.innercore.api.mod.ui.types.Texture;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

public class UISlotElement extends UIElement {
    public UISlotElement(UIWindow window, ScriptableObject description) {
        super(window, description);
    }

    public String slotName = "";

    public Texture background = new Texture((Bitmap) null);
    public UiVisualSlotImpl source = null;
    public int size = 60;
    public boolean isVisual = false, isDarken = false, isDarkenAtZero = false;
    public int maxStackSize = -1;
    public String textOverride = null;
    public float itemIconSizeScale = 0.82f;
    public boolean disablePixelPerfect = false;

    public int curId = 0, curCount = 0, curData = 0;
    public NativeItemInstanceExtra curExtra;

    protected boolean isSelectionForced = false;
    protected boolean wasSelected = false, isSelected = false;
    protected static UISlotElement currentSelectedSlot = null;

    @Override
    public void onSetup(ScriptableObject description) {
    }

    protected void refresh() {
    }

    @Override
    public void onDraw(Canvas canvas, float scale) {
    }

    @Override
    public void onBindingUpdated(String name, Object val) {
    }

    @Override
    public void setupInitialBindings(UiAbstractContainer container, String elementName) {
    }

    public int getMaxStackSize() {
        return 64;
    }

    public boolean isValidItem(int id, int count, int data, NativeItemInstanceExtra extra) {
        return false;
    }

    public int getMaxItemTransferAmount(UISlotElement slot) {
        return 0;
    }
}
