package com.zhekasmirnov.innercore.api.mod.ui.window;


import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.ContentProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IElementProvider;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.types.ITouchEventListener;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindow implements IWindow {
    ScriptableObject content;
    public void updateWindowLocation() {
    }

    public void updateWindowPositionAndSize() {
    }

    public void updateScrollDimensions() {
    }


    public UIWindow(UIWindowLocation location) {

    }

    public UIWindow(ScriptableObject content) {
        this(new UIWindowLocation());
        setContent(content);
    }

    public void open() {
    }

    public void addAdjacentWindow(UIWindow win) {

    }

    public void removeAdjacentWindow(UIWindow win) {
    }

    public void preOpen() {

    }

    public void postOpen() {

    }

    public void close() {

    }

    public void frame(long time) {

    }

    public void invalidateElements(boolean onCurrentThread) {

    }

    public void invalidateDrawing(boolean onCurrentThread) {

    }

    @Override
    public boolean isOpened() {
        return false;
    }


    public void postElementRefresh() {
    }

    public void postBackgroundRefresh() {
    }

    public void forceRefresh() {
    }

    public void setTouchable(boolean touchable) {

    }

    public boolean isTouchable() {
        return false;
    }


    public boolean isBlockingBackground() {
        return false;
    }

    public void setBlockingBackground(boolean blockingBackground) {
    }

    public boolean isNotFocusable() {
        return false;
    }

    public void setAsGameOverlay(boolean inGameOverlay) {
    }

    public void setBackgroundColor(int color) {
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
        return new HashMap<>();
    }

    @Override
    public ScriptableObject getContent() {
        return content;
    }

    private boolean isSetup = false;
    private void setup() {
        isSetup = true;

        forceRefresh();
    }

    private void setupIfNeeded() {
        if (!isSetup) {
            setup();
        }
    }

    public void setContent(ScriptableObject content) {
        this.content = content;

    }

    public void setDynamic(boolean val) {
    }

    public void setInventoryNeeded(boolean val) {
    }

    public void invalidateBackground() {
    }

    public void invalidateForeground() {
    }

    public UIWindowLocation getLocation() {
        return new UIWindowLocation();
    }

    public IElementProvider getElementProvider() {
        return null;
    }

    public IBackgroundProvider getBackgroundProvider() {
        return null;
    }

    public ContentProvider getContentProvider() {
        return null;
    }

    public float getScale() {
        return 1;
    }

    public Object getStyle() {
        return null;
    }

    public void setStyle(ScriptableObject scriptable) {
        invalidateAllContent();
    }

    public void setStyle(Object style) {

    }

    public void invalidateAllContent() {
    }


    private HashMap<String, Object> windowProperties = new HashMap<>();

    public Object getProperty(String name) {
        return windowProperties.get(name);
    }

    public void putProperty(String name, Object property) {
        windowProperties.put(name, property);
    }


    private UiAbstractContainer container;

    public UiAbstractContainer getContainer() {
        return container;
    }

    public void setContainer(UiAbstractContainer con) {
        container = con;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {

    }

    public void setParentWindow(IWindow parentWindow) {
    }

    public IWindow getParentWindow() {
        return null;
    }

    private IWindowEventListener eventListener;

    public void setEventListener(IWindowEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void runCachePreparation(boolean async) {

    }

    public void debug() {

    }

    
    
    public boolean closeOnBackPressed = false;
    public void setCloseOnBackPressed(boolean val) {
        closeOnBackPressed = val;
    }

    public boolean onBackPressed() {
        if (closeOnBackPressed) {
            close();
            return true;
        }
        return false;
    }
}
