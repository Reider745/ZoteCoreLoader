package com.zhekasmirnov.innercore.api.mod.ui.elements;

import android.graphics.Canvas;
import android.graphics.Rect;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.types.Texture;
import com.zhekasmirnov.innercore.api.mod.ui.types.TouchEvent;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableWatcher;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 31.07.2017.
 */

public abstract class UIElement {
    public UIWindow window;
    public ScriptableObject description;
    public ScriptableWatcher descriptionWatcher;

    public String elementName;

    public UIElementCleaner cleaner;
    public Rect elementRect;
    protected UiAbstractContainer container;
    protected UIStyle style = UIStyle.DEFAULT;

    public float x, y, z;
    public boolean isDirty = false;

    protected Object optValFromDescription(String key, Object fallback) {
        return ScriptableObjectHelper.getProperty(description, key, fallback);
    }

    protected float optFloatFromDesctiption(String key, float fallback) {
        return ScriptableObjectHelper.getFloatProperty(description, key, fallback);
    }

    protected String optStringFromDesctiption(String key, String fallback) {
        return ScriptableObjectHelper.getStringProperty(description, key, fallback);
    }

    protected boolean optBooleanFromDesctiption(String key, boolean fallback) {
        return ScriptableObjectHelper.getBooleanProperty(description, key, fallback);
    }

    protected void setupDefaultValues() {
        x = optFloatFromDesctiption("x", 0);
        y = optFloatFromDesctiption("y", 0);
        z = optFloatFromDesctiption("z", 0);
        setSize(1, 1);

        if (description.has("clicker", description)) {
            ScriptableObject clicker = ScriptableObjectHelper.getScriptableObjectProperty(description, "clicker", null);
            if (clicker != null) {
                Object onClick = ScriptableObjectHelper.getScriptableObjectProperty(clicker, "onClick", null);
                Object onLongClick = ScriptableObjectHelper.getScriptableObjectProperty(clicker, "onLongClick", null);
                description.put("onClick", description, onClick);
                description.put("onLongClick", description, onLongClick);
            }
        }
    }

    public Texture createTexture(Object description) {
        return new Texture(description, style);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        refreshRect();
    }

    public void setSize(float w, float h) {
        refreshRect();
    }

    private void refreshRect() {
        if (cleaner != null) {
            cleaner.set(elementRect);
        }
    }

    public UIElement(UIWindow window, ScriptableObject description) {
        this.elementRect = new Rect(0, 0, 1, 1);
        this.window = window;
        this.description = description;
        this.cleaner = new UIElementCleaner(this);
        this.container = window.getContainer();
        this.style = window.getElementProvider().getStyleFor(this);
    }

    public void onSetup() {
        if (this.descriptionWatcher == null) {
            this.descriptionWatcher = new ScriptableWatcher(description);
        } else {
            this.descriptionWatcher.setTarget(description);
        }

        setupDefaultValues();
        onSetup(description);
    }

    public UIElementCleaner getCleanerCopy() {
        return cleaner.clone();
    }

    public abstract void onSetup(ScriptableObject description);

    public abstract void onDraw(Canvas canvas, float scale);

    public abstract void onBindingUpdated(String name, Object val);

    private HashMap<String, Object> bindingMap = new HashMap<>();

    public void setBinding(String name, Object val) {
        Object binding = bindingMap.get(name);
        if (binding != null) {
            if (!binding.equals(val)) {
                bindingMap.put(name, val);
                onBindingUpdated(name, val);
            }
        } else if (val != null) {
            bindingMap.put(name, val);
            onBindingUpdated(name, val);
        }
    }

    public Object getBinding(String name) {
        if (name.equals("element_obj")) {
            return this;
        }
        if (name.equals("element_rect")) {
            return elementRect;
        }
        return bindingMap.get(name);
    }

    public void setupInitialBindings(UiAbstractContainer container, String elementName) {
        this.container = container;
        this.elementName = elementName;
    }

    public boolean isTouched = false;

    public void onTouchEvent(TouchEvent event) {
    }

    public void onTouchReleased(TouchEvent event) {
    }

    private boolean isReleased = false;

    public boolean isReleased() {
        return isReleased;
    }

    public void onRelease() {
        isReleased = true;
    }

    public void onReset() {

    }

    public void invalidate() {
        descriptionWatcher.invalidate();
    }

    protected Canvas getCacheCanvas(float w, float h, float scale) {
        return Canvas.getSingletonInternalProxy();
    }

    protected void drawCache(Canvas canvas, float x, float y) {
    }

    protected void drawCache(Canvas canvas, float scale) {
    }

    public void debug(Canvas canvas, float scale) {
    }

    protected Object callDescriptionMethod(String name, Object... args) {
        return null;
    }

    protected Object callDescriptionMethodSafe(String name, Object... args) {
        return null;
    }

    protected boolean hasDescriptionMethod(String name) {
        Object _func = ScriptableObjectHelper.getProperty(description, name, null);
        return (_func != null && _func instanceof Function);
    }

    protected void callFixedOnClick(String name, UiAbstractContainer con, ScriptableObject localPos) {
    }
}
