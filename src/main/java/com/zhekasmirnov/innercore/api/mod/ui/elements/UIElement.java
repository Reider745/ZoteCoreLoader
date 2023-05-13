package com.zhekasmirnov.innercore.api.mod.ui.elements;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableWatcher;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
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

    }

    public Object createTexture(Object description) {
        return null;
    }

    public void setPosition(float x, float y) {
    }

    public void setSize(float w, float h) {
    }

    private void refreshRect() {
    }

    public UIElement(UIWindow window, ScriptableObject description) {
    }

    public void onSetup() {
        if (this.descriptionWatcher == null) {
            this.descriptionWatcher = new ScriptableWatcher(description);
        }
        else {
            this.descriptionWatcher.setTarget(description);
        }

        setupDefaultValues();
        onSetup(description);
    }

    public Object getCleanerCopy() {
        return null;
    }

    public abstract void onSetup(ScriptableObject description);
    public abstract void onDraw(Object canvas, float scale);
    public abstract void onBindingUpdated(String name, Object val);

    private HashMap<String, Object> bindingMap = new HashMap<>();

    public void setBinding(String name, Object val) {
        Object binding = bindingMap.get(name);
        if (binding != null){
            if (!binding.equals(val)) {
                bindingMap.put(name, val);
                onBindingUpdated(name, val);
            }
        }
        else if (val != null) {
            bindingMap.put(name, val);
            onBindingUpdated(name, val);
        }
    }

    public Object getBinding(String name) {
        if (name.equals("element_obj")) {
            return this;
        }
        if (name.equals("element_rect")) {
            return null;
        }
        return bindingMap.get(name);
    }

    public void setupInitialBindings(UiAbstractContainer container, String elementName) {
        this.elementName = elementName;
    }


    public boolean isTouched = false;

    public void onTouchEvent(Object event) {

    }

    public void onTouchReleased(Object event) {

    }


    private boolean isReleased = false;

    public boolean isReleased() {
        return isReleased;
    }

    public void onRelease() {

    }

    public void onReset() {

    }

    public void invalidate() {
        descriptionWatcher.invalidate();
    }


    protected Object getCacheCanvas(float w, float h, float scale) {
        return null;
    }

    protected void drawCache(Object canvas, float x, float y) {

    }

    protected void drawCache(Object canvas, float scale) {
    }

    public void debug(Object canvas, float scale) {
    }

    protected Object callDescriptionMethod(String name, Object... args) {
        return null;
    }

    protected Object callDescriptionMethodSafe(String name, Object... args) {
        return null;
    }

    protected boolean hasDescriptionMethod(String name) {
        Object _func = ScriptableObjectHelper.getProperty(description, name, null);
        return  (_func != null && _func instanceof Function);
    }

    protected void callFixedOnClick(String name, UiAbstractContainer con, ScriptableObject localPos) {

    }
}

