package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.background.DrawingFactory;
import com.zhekasmirnov.innercore.api.mod.ui.background.IDrawing;
import com.zhekasmirnov.innercore.api.mod.ui.elements.ElementFactory;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableWatcher;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 01.08.2017.
 */

@Deprecated(since = "Zote")
public class ContentProvider {
    public UIWindow window;

    public ScriptableObject content;
    public ScriptableObject elements;
    public ScriptableObject drawing;

    public ScriptableWatcher drawingWatcher;
    public HashMap<String, UIElement> elementMap = new HashMap<>();

    public ContentProvider(UIWindow window) {
        this.window = window;
    }

    public void setContentObject(ScriptableObject content) {
        this.content = content;

        if (content != null) {
            drawing = ScriptableObjectHelper.getScriptableObjectProperty(content, "drawing", null);
            drawingWatcher = new ScriptableWatcher(drawing);

            elements = ScriptableObjectHelper.getScriptableObjectProperty(content, "elements", null);
        }
    }

    private UIStyle buildWindowStyle() {
        UIStyle style = new UIStyle();

        IWindow window = this.window;
        while (window != null) {
            if (window instanceof UIWindow) {
                style.addStyle(window.getStyle());
                window = ((UIWindow) window).getParentWindow();
            } else {
                style.inherit(window.getStyle());
                break;
            }
        }

        return style;
    }

    public void setupElements() {
        IElementProvider provider = window.getElementProvider();
        provider.releaseAll();
        provider.setWindowStyle(buildWindowStyle());

        elementMap.clear();
        if (elements == null) {
            return;
        }

        Object[] keys = elements.getAllIds();
        for (Object key : keys) {
            String name = key.toString();
            Object descr = elements.get(key);

            if (descr != null && descr instanceof ScriptableObject) {
                UIElement element = ElementFactory.construct(window, (ScriptableObject) descr);
                if (element != null) {
                    elementMap.put(name, element);
                    provider.addOrRefreshElement(element);
                    if (window.getContainer() != null) {
                        window.getContainer().addElementInstance(element, name);
                    }
                }
            }
        }
    }

    public synchronized void refreshElements() {
        if (elements == null) {
            return;
        }

        IElementProvider provider = window.getElementProvider();
        provider.setWindowStyle(buildWindowStyle());

        Object[] keys = elements.getAllIds();
        for (Object key : keys) {
            String name = key.toString();
            Object descr = elements.get(key);

            if (elementMap.containsKey(name)) {
                UIElement oldElement = elementMap.get(name);
                ScriptableWatcher descrWatcher = oldElement.descriptionWatcher;
                if (descr == null) {
                    provider.removeElement(oldElement);
                    elementMap.remove(name);
                }
                if (descr instanceof ScriptableObject) {
                    descrWatcher.setTarget((ScriptableObject) descr);
                    descrWatcher.refresh();
                    if (descrWatcher.isDirty()) {
                        descrWatcher.validate();
                        UIElement newElement = ElementFactory.construct(window, (ScriptableObject) descr);
                        if (newElement == null) {
                            ICLog.i("ERROR", "failed to construct ui element");
                            continue;
                        }
                        elementMap.put(name, newElement);
                        newElement.descriptionWatcher = descrWatcher;
                        provider.addOrRefreshElement(newElement);
                        provider.removeElement(oldElement);
                        if (window.getContainer() != null) {
                            window.getContainer().addElementInstance(newElement, name);
                        }
                    }
                }
            } else if (descr != null && descr instanceof ScriptableObject) {
                UIElement element = ElementFactory.construct(window, (ScriptableObject) descr);
                if (element != null) {
                    elementMap.put(name, element);
                    provider.addOrRefreshElement(element);
                    if (window.getContainer() != null) {
                        window.getContainer().addElementInstance(element, name);
                    }
                }
            }
        }
    }

    public void setupDrawing() {
        IBackgroundProvider backgroundProvider = window.getBackgroundProvider();
        backgroundProvider.clearAll();

        UIStyle style = buildWindowStyle();
        if (drawing != null) {
            Object[] keys = drawing.getAllIds();
            for (Object key : keys) {
                Object descr = drawing.get(key);

                if (descr instanceof ScriptableObject) {
                    IDrawing drawingElement = DrawingFactory.construct((ScriptableObject) descr, style);
                    if (drawingElement != null) {
                        backgroundProvider.addDrawing(drawingElement);
                    }
                }
            }
        }

        window.invalidateBackground();
    }

    public synchronized void refreshDrawing() {
        drawingWatcher.refresh();
        if (drawingWatcher.isDirty()) {
            setupDrawing();
            drawingWatcher.validate();
        }
    }

    public void invalidateAllContent() {
        drawingWatcher.invalidate();
        window.getElementProvider().invalidateAll();
    }

    @Override
    public String toString() {
        int nonNull = 0;
        Object[] keys = elements.getAllIds();
        for (Object key : keys) {
            Object descr = elements.get(key);
            if (descr != null)
                nonNull++;
        }

        return "[ContentProvider displayed=" + elementMap.size() + " contented=" + nonNull + "(" + keys.length + ")]";
    }
}
