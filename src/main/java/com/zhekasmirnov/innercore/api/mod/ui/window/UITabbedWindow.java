package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.Color;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.mod.ui.types.WindowContentAdapter;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 05.08.2017.
 */

@Deprecated(since = "Zote")
public class UITabbedWindow implements IWindow {
    private UIWindow backgroundWindow;
    private UIWindow[] tabWindows = new UIWindow[12];

    private boolean hasTabsOnRight = false;
    private boolean hasTabsOnLeft = false;
    private int tabSize;

    private UIWindowLocation location;
    private float padding = 0, frameWidth = 1;
    private float onePixel = 0;

    public void setLocation(UIWindowLocation location) {
        this.location = location.copy();
        this.location.removeScroll();
        this.backgroundWindow.getLocation().set(this.location);

        this.padding = this.location.height / 71.25f / 1.4f;
        this.frameWidth = this.location.globalToWindow(this.padding);
        this.tabSize = (int) (this.location.height / 6f);
        this.onePixel = 1 / this.location.getDrawingScale();

        refreshWindowLocation();
    }

    private UIWindowLocation innerWinLoc = new UIWindowLocation();

    private void refreshWindowLocation() {
        UIWindowLocation location = new UIWindowLocation();
        location.set(this.location);
        int x1 = (int) (location.x + padding + (hasTabsOnLeft ? tabSize : 0));
        int x2 = (int) (location.x + location.width - (padding + (hasTabsOnRight ? tabSize : 0)));
        int y1 = (int) (location.y + padding * 3);
        int y2 = (int) (location.y + location.height - padding * 3);

        innerWinLoc.set(x1, y1, x2 - x1, y2 - y1);

        for (UIWindow window : tabWindows) {
            if (window != null) {
                UIWindowLocation loc = window.getLocation();
                loc.x = x1;
                loc.y = y1;
                if (loc.scrollX != loc.width)
                    loc.scrollX = Math.max(loc.scrollX, x2 - x1);
                else
                    loc.scrollX = x2 - x1;
                if (loc.scrollY != loc.height)
                    loc.scrollY = Math.max(loc.scrollY, y2 - y1);
                else
                    loc.scrollY = y2 - y1;
                loc.width = x2 - x1;
                loc.height = y2 - y1;
            }
        }
    }

    public float getInnerWindowWidth() {
        return innerWinLoc.width;
    }

    public float getInnerWindowHeight() {
        return innerWinLoc.height;
    }

    public float getWindowTabSize() {
        return this.location.globalToWindow(this.tabSize) - frameWidth;
    }

    public float getGlobalTabSize() {
        return this.tabSize - this.location.windowToGlobal(frameWidth);
    }

    private boolean hadInitialBackgroundSetup = false;

    private void setupBackgroundContents() {
        float windowTabSize = getWindowTabSize();

        WindowContentAdapter content = new WindowContentAdapter(backgroundWindow.getContent());
        content.setLocation(this.location);

        if (!hadInitialBackgroundSetup) {
            hadInitialBackgroundSetup = true;
            ScriptableObject transparentBackground = ScriptableObjectHelper.createEmpty();
            transparentBackground.put("type", transparentBackground, "color");
            transparentBackground.put("color", transparentBackground, 0);
            content.addDrawing(transparentBackground);
        }

        float x1 = hasTabsOnLeft ? windowTabSize - frameWidth : 0;
        float x2 = 1000 - (hasTabsOnRight ? windowTabSize - frameWidth : 0);
        ScriptableObject backgroundFrame = ScriptableObjectHelper.createEmpty();
        backgroundFrame.put("type", backgroundFrame, "frame");
        backgroundFrame.put("x", backgroundFrame, x1);
        backgroundFrame.put("y", backgroundFrame, 0);
        backgroundFrame.put("width", backgroundFrame, x2 - x1);
        backgroundFrame.put("height", backgroundFrame, this.location.getWindowHeight());
        backgroundFrame.put("scale", backgroundFrame,
                this.frameWidth / getStyleSafe().getIntProperty("tab_frame_width", 2));
        backgroundFrame.put("bitmap", backgroundFrame, "style:frame_background");
        backgroundFrame.put("color", backgroundFrame, getStyleSafe().getIntProperty("window_background", Color.BLACK));
        content.addElement("tabbedWinBackground", backgroundFrame);

        this.backgroundWindow.setContent(content.getContent());
    }

    public UITabbedWindow(UIWindowLocation location) {
        this.backgroundWindow = new UIWindow(location);
        this.backgroundWindow.setParentWindow(this);

        this.setLocation(location);
        this.setupBackgroundContents();
    }

    public UITabbedWindow(ScriptableObject content) {
        this(new UIWindowLocation(ScriptableObjectHelper.getScriptableObjectProperty(content, "location", null)));

        if (!ScriptableObjectHelper.getBooleanProperty(content, "isButtonHidden", false)) {
            ScriptableObject overlay = ScriptableObjectHelper.createEmpty();

            float buttonSize = getWindowTabSize() / 1.72f;
            ScriptableObject button = ScriptableObjectHelper.createEmpty();
            button.put("type", button, "closeButton");
            button.put("x", button, buttonSize / -2);
            button.put("y", button, buttonSize / -2);
            button.put("scale", button, buttonSize / 18.0f);
            button.put("bitmap", button, "style:close_button_up");
            button.put("bitmap2", button, "style:close_button_down");
            overlay.put("closeButton", overlay, button);

            setFakeTab(0, overlay);
        }
    }

    private int defaultTabIndex = -1;

    public void setTab(int index, ScriptableObject tabOverlay, ScriptableObject tabContent, boolean isAlwaysSelected) {
        if (index < 0 || index > 11) {
            throw new IllegalArgumentException("tab index is invalid: it need to be between 0 and 11");
        }

        int yIndex = index % 6;
        boolean isRightSide = index > 5;
        boolean needBackgroundRefresh = false;

        if (isRightSide && !hasTabsOnRight) {
            hasTabsOnRight = true;
            needBackgroundRefresh = true;
        }
        if (!isRightSide && !hasTabsOnLeft) {
            hasTabsOnLeft = true;
            needBackgroundRefresh = true;
        }

        float windowTabSize = getWindowTabSize();

        WindowContentAdapter content = new WindowContentAdapter(backgroundWindow.getContent());

        ScriptableObject tabFrameSides = ScriptableObjectHelper.createEmpty();
        tabFrameSides.put(isRightSide ? "left" : "right", tabFrameSides, false);

        int pixelsPerFrame = getStyleSafe().getIntProperty("tab_frame_width", 2);
        float w = windowTabSize + onePixel;
        float h = windowTabSize;
        float x = isRightSide ? 1000 - w : 0;
        float y = yIndex * (windowTabSize + frameWidth);

        ScriptableObject tab = ScriptableObjectHelper.createEmpty();
        tab.put("type", tab, "tab");
        tab.put("x", tab, x);
        tab.put("y", tab, y);
        tab.put("width", tab, w);
        tab.put("height", tab, h);
        tab.put("tabIndex", tab, index);
        tab.put("isAlwaysSelected", tab, isAlwaysSelected);
        tab.put("bitmap", tab, isRightSide ? "style:frame_tab_right" : "style:frame_tab_left");
        tab.put("deselectedColor", tab, getStyleSafe().getIntProperty("tab_background", Color.BLACK));
        tab.put("selectedColor", tab, getStyleSafe().getIntProperty("tab_background_selected", Color.BLACK));
        tab.put("scale", tab, frameWidth / pixelsPerFrame);
        tab.put("sides", tab, tabFrameSides);
        content.addElement("windowTab" + index, tab);

        Object[] overlayKeys = tabOverlay.getAllIds();
        for (Object key : overlayKeys) {
            if (key instanceof String) {
                String name = (String) key;
                ScriptableObject element = ScriptableObjectHelper.getScriptableObjectProperty(tabOverlay, name, null);
                if (element != null) {
                    element.put("x", element, ScriptableObjectHelper.getFloatProperty(element, "x", 0) + (x + w / 2f));
                    element.put("y", element, ScriptableObjectHelper.getFloatProperty(element, "y", 0) + (y + h / 2f));
                    element.put("z", element, ScriptableObjectHelper.getFloatProperty(element, "z", 0) + 2);
                    content.addElement("tab" + index + "_" + name, element);
                }
            }
        }

        if (tabContent != null) {
            UIWindow tabWindow = new UIWindow(tabContent);
            tabWindow.setParentWindow(this);
            tabWindows[index] = tabWindow;

            if (!isAlwaysSelected && defaultTabIndex == -1) {
                defaultTabIndex = index;
                tab.put("isSelected", tab, true);
            }
        }

        refreshWindowLocation();

        if (needBackgroundRefresh) {
            setupBackgroundContents();
        }
    }

    public void setTab(int index, ScriptableObject tabOverlay, ScriptableObject tabContent) {
        setTab(index, tabOverlay, tabContent, false);
    }

    public void setFakeTab(int index, ScriptableObject tabOverlay) {
        setTab(index, tabOverlay, null, true);
    }

    public UIWindow getWindowForTab(int index) {
        return tabWindows[index];
    }

    private boolean isOpened = false;

    private UIWindow lastOpenedWindow = null;

    @Override
    public void open() {
        isOpened = true;

        if (backgroundWindow != null) {
            backgroundWindow.preOpen();
        }

        if (defaultTabIndex >= 0 && defaultTabIndex < 11) {
            UIWindow window = tabWindows[defaultTabIndex];
            if (window != null) {
                currentTab = defaultTabIndex;
                lastOpenedWindow = window;
                window.preOpen();
            }
        }

        if (backgroundWindow != null) {
            backgroundWindow.postOpen();
        }

        if (lastOpenedWindow != null) {
            lastOpenedWindow.postOpen();
        }

        WindowProvider.instance.onWindowOpened(this);
    }

    @Override
    public void close() {
        isOpened = false;
        if (backgroundWindow != null) {
            backgroundWindow.close();
        }
        if (lastOpenedWindow != null) {
            lastOpenedWindow.close();
        }
        lastOpenedWindow = null;

        WindowProvider.instance.onWindowClosed(this);
    }

    @Override
    public void frame(long time) {

    }

    public void invalidateElements(boolean onCurrentThread) {
        if (isOpened) {
            if (backgroundWindow != null) {
                backgroundWindow.invalidateElements(onCurrentThread);
            }
            if (lastOpenedWindow != null) {
                lastOpenedWindow.invalidateElements(onCurrentThread);
            }
        }
    }

    public void invalidateDrawing(boolean onCurrentThread) {
        if (isOpened) {
            if (backgroundWindow != null) {
                backgroundWindow.invalidateDrawing(onCurrentThread);
            }
            if (lastOpenedWindow != null) {
                lastOpenedWindow.invalidateDrawing(onCurrentThread);
            }
        }
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
        HashMap<String, UIElement> elements = new HashMap<>();
        for (UIWindow window : tabWindows) {
            if (window != null) {
                HashMap<String, UIElement> winElements = window.getElements();
                for (String name : winElements.keySet()) {
                    elements.put(name, winElements.get(name));
                }
            }
        }
        if (backgroundWindow != null) {
            HashMap<String, UIElement> winElements = backgroundWindow.getElements();
            for (String name : winElements.keySet()) {
                elements.put(name, winElements.get(name));
            }
        }
        return elements;
    }

    @Override
    public ScriptableObject getContent() {
        return backgroundWindow == null ? null : backgroundWindow.getContent();
    }

    @Override
    public UiAbstractContainer getContainer() {
        return backgroundWindow == null ? null : backgroundWindow.getContainer();
    }

    @Override
    public void setContainer(UiAbstractContainer con) {
        if (backgroundWindow != null) {
            backgroundWindow.setContainer(con);
        }
        for (UIWindow window : tabWindows) {
            if (window != null) {
                window.setContainer(con);
            }
        }
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        if (backgroundWindow != null) {
            backgroundWindow.setDebugEnabled(enabled);
        }
        for (UIWindow window : tabWindows) {
            if (window != null) {
                window.setDebugEnabled(enabled);
            }
        }
    }

    public void setEventListener(IWindowEventListener listener) {
        backgroundWindow.setEventListener(listener);
    }

    public void setTabEventListener(int index, IWindowEventListener listener) {
        UIWindow win = getWindowForTab(index);
        if (win != null) {
            win.setEventListener(listener);
        }
    }

    public int currentTab = -1;

    public void onTabSelected(int index) {
        if (isOpened()) {
            if (index >= 0 && index < 12) {
                UIWindow window = tabWindows[index];
                if (window != null && window != lastOpenedWindow) {
                    currentTab = index;
                    window.open();
                    if (lastOpenedWindow != null) {
                        lastOpenedWindow.setParentWindow(null); // prevent closing parent (this)
                        lastOpenedWindow.close();
                        lastOpenedWindow.setParentWindow(this);
                    }
                    lastOpenedWindow = window;
                }
            }
        }
    }

    public void setBlockingBackground(boolean b) {
        backgroundWindow.setBlockingBackground(b);
    }

    public int getDefaultTab() {
        return defaultTabIndex;
    }

    public void setDefaultTab(int defaultTabIndex) {
        this.defaultTabIndex = defaultTabIndex;
    }

    private UIStyle style = new UIStyle();

    public void setStyle(UIStyle style) {
        this.style = style;
    }

    public void setStyle(ScriptableObject scriptable) {
        style.addAllBindings(scriptable);
    }

    @Override
    public UIStyle getStyle() {
        return style;
    }

    public UIStyle getStyleSafe() {
        UIStyle style = getStyle();
        return style != null ? style : UIStyle.DEFAULT;
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
