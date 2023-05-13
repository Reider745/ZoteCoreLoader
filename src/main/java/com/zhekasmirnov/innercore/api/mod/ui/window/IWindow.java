package com.zhekasmirnov.innercore.api.mod.ui.window;

import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 02.08.2017.
 */

public interface IWindow {
    void open();
    void close();
    void frame(long time);
    void invalidateElements(boolean onCurrentThread);
    void invalidateDrawing(boolean onCurrentThread);

    boolean isOpened();
    boolean isInventoryNeeded();
    boolean isDynamic();

    HashMap<String, UIElement> getElements();
    ScriptableObject getContent();
    Object getStyle();
    boolean onBackPressed();

    UiAbstractContainer getContainer();
    void setContainer(UiAbstractContainer con);

    void setDebugEnabled(boolean enabled);
}
