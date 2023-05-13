package com.zhekasmirnov.innercore.api.mod.ui.window;

import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

public class UITabbedWindow implements IWindow {

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public void frame(long time) {

    }

    @Override
    public void invalidateElements(boolean onCurrentThread) {

    }

    @Override
    public void invalidateDrawing(boolean onCurrentThread) {

    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public boolean isInventoryNeeded() {
        return false;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public HashMap<String, UIElement> getElements() {
        return null;
    }

    @Override
    public ScriptableObject getContent() {
        return null;
    }

    @Override
    public Object getStyle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public UiAbstractContainer getContainer() {
        return null;
    }

    @Override
    public void setContainer(UiAbstractContainer con) {

    }

    @Override
    public void setDebugEnabled(boolean enabled) {

    }
}
