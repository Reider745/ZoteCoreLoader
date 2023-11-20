package com.zhekasmirnov.innercore.api.mod.ui.window;

import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by zheka on 04.08.2017.
 */

public class UIWindowGroup implements IWindow {
    private static final Object LOCK = new Object();

    private ArrayList<UIWindow> windows = new ArrayList<>();
    private HashMap<String, UIWindow> windowByName = new HashMap<>();
    private HashMap<String, UIElement> allElements = new HashMap<>();

    private boolean isOpened = false;

    public void removeWindow(String name) {
        if (windowByName.containsKey(name)) {
            UIWindow window = windowByName.get(name);

            window.setParentWindow(null);

            if (window.isOpened()) {
                window.close();
            }

            windows.remove(window);
            windowByName.remove(name);
        }
    }

    public void addWindowInstance(String name, IWindow _window) {
        if (_window instanceof UIWindow) {
            removeWindow(name);

            UIWindow window = (UIWindow) _window;
            windowByName.put(name, window);
            windows.add(window);

            if (window.isOpened()) {
                window.close();
            }
            if (isOpened) {
                this.container.openAs(window);
            }

            window.setParentWindow(this);
        }
        else {
            throw new IllegalArgumentException("only default window (UIWindow instance) can be added to the window group.");
        }
    }

    public UIWindow addWindow(String name, ScriptableObject content) {
        UIWindow win = new UIWindow(content);
        addWindowInstance(name, win);
        return win;
    }

    public UIWindow getWindow(String name) {
        return new UIWindow((UIWindowLocation) null);
    }

    public ScriptableObject getWindowContent(String name) {
        UIWindow window = getWindow(name);
        if (window != null) {
            return window.getContent();
        }
        return null;
    }

    public void setWindowContent(String name, ScriptableObject content) {
        UIWindow window = getWindow(name);
        if (window != null) {
            window.setContent(content);
        }
    }

    public Collection<UIWindow> getAllWindows() {
        return windows;
    }

    public Collection<String> getWindowNames() {
        return windowByName.keySet();
    }

    public void refreshWindow(String name) {
        UIWindow window = getWindow(name);
        if (window != null) {
            window.forceRefresh();
        }
    }

    public void refreshAll() {
        for (String name : windowByName.keySet()) {
            refreshWindow(name);
        }
    }

    public void moveOnTop(String name) {
        UIWindow window = getWindow(name);
        if (window != null) {
            windows.remove(window);
            windows.add(window);
        }
    }



    @Override
    public void open() {
        synchronized(LOCK) {
            isOpened = true;

            long timeStart = System.currentTimeMillis();
            allElements.clear();

            for (UIWindow window : windows) {
                window.preOpen();
                HashMap<String, UIElement> elements = window.getElements();
                for (String key : elements.keySet()) {
                    allElements.put(key, elements.get(key));
                }
            }
            for (UIWindow window : windows) {
                window.postOpen();
            }

            WindowProvider.instance.onWindowOpened(this);

            long timeEnd = System.currentTimeMillis();
        }
    }

    @Override
    public void close() {
        synchronized(LOCK) {
            isOpened = false;
            for (UIWindow window : windows) {
                window.close();
            }
            allElements.clear();

            WindowProvider.instance.onWindowClosed(this);
        }
    }

    @Override
    public void frame(long time) {

    }

    @Override
    public boolean isOpened() {
        return isOpened;
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
        return allElements;
    }

    @Override
    public ScriptableObject getContent() {
        return null;
    }

    private UiAbstractContainer container;
    @Override
    public UiAbstractContainer getContainer() {
        return container;
    }

    @Override
    public void setContainer(UiAbstractContainer con) {
        container = con;
        for (UIWindow window : windows) {
            window.setContainer(con);
        }
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        for (UIWindow window : windows) {
            window.setDebugEnabled(enabled);
        }
    }

    public void invalidateAllContent() {
        for (UIWindow window : windows) {
            window.invalidateAllContent();
        }
    }

    public void setStyle(Object style) {

    }

    public void setStyle(ScriptableObject scriptable) {

    }

    public Object getStyle() {
        return null;
    }

    public void setBlockingBackground(boolean val) {
        if (windows.size() > 0) {
            windows.get(0).setBlockingBackground(val);
        }
    }


    public void invalidateElements(boolean onCurrentThread) {
        for (UIWindow window : windows) {
            window.invalidateElements(onCurrentThread);
        }
    }

    public void invalidateDrawing(boolean onCurrentThread) {
        for (UIWindow window : windows) {
            window.invalidateDrawing(onCurrentThread);
        }
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
