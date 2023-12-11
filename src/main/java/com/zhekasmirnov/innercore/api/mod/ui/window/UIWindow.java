package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.view.ViewGroup;
import android.widget.ScrollView;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.ContentProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IElementProvider;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapCache;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.utils.UIUtils;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindow implements IWindow {
    private static final Object LOCK = new Object();

    private UIWindowLocation location;
    private IWindow parentWindow;

    public ViewGroup layout;

    private IElementProvider elementProvider;
    private IBackgroundProvider backgroundProvider;
    private ContentProvider contentProvider;
    private UIStyle style = new UIStyle();

    private boolean isOpened = false;
    private boolean isDynamic = true;
    private boolean isInventoryNeeded = false;

    public void updateWindowLocation() {
        updateScrollDimensions();
        updateWindowPositionAndSize();
    }

    public void updateWindowPositionAndSize() {
        WindowParent.updateWindowPositionAndSize(this);
    }

    public void updateScrollDimensions() {
    }

    public UIWindow(UIWindowLocation location) {
        this.location = location;

        layout = ScrollView.getSingletonInternalProxy();

        contentProvider = new ContentProvider(this);
        backgroundProvider = new UIWindowBackgroundDrawable(this);
        elementProvider = new UIWindowElementDrawable(this);
        elementProvider.setBackgroundProvider(backgroundProvider);
    }

    public UIWindow(ScriptableObject content) {
        this(new UIWindowLocation());
        setContent(content);
    }

    public void open() {
        preOpen();
        postOpen();
    }

    private ArrayList<UIWindow> adjacentWindows = new ArrayList<>();

    public void addAdjacentWindow(UIWindow win) {
        if (!adjacentWindows.contains(win)) {
            adjacentWindows.add(win);
        }
    }

    public void removeAdjacentWindow(UIWindow win) {
        adjacentWindows.remove(win);
    }

    public void preOpen() {
        synchronized (LOCK) {
            setupIfNeeded();

            if (isOpened) {
                return;
            }

            Callback.invokeAPICallback("CustomWindowOpened", this);

            // refreshLocation();
            forceRefresh();
            runCachePreparation(false);

            for (UIWindow adjacentWin : adjacentWindows) {
                if (adjacentWin != null) {
                    adjacentWin.setContainer(getContainer());
                    adjacentWin.preOpen();
                }
            }
        }
    }

    public void postOpen() {
        synchronized (LOCK) {
            if (isOpened) {
                return;
            }
            isOpened = true;

            backgroundProvider.prepareCache();

            UIUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidateBackground();
                    invalidateForeground();
                }
            });

            WindowParent.openWindow(this);
            WindowProvider.instance.onWindowOpened(this);

            if (eventListener != null) {
                eventListener.onOpen(this);
            }

            for (UIWindow adjacentWin : adjacentWindows) {
                if (adjacentWin != null) {
                    adjacentWin.postOpen();
                }
            }
        }
    }

    public void close() {
        synchronized (LOCK) {
            if (!isOpened) {
                return;
            }

            isOpened = false;

            if (eventListener != null) {
                eventListener.onClose(this);
            }

            if (parentWindow != null) {
                parentWindow.close();
            }

            for (UIWindow adjacentWin : adjacentWindows) {
                if (adjacentWin != null) {
                    adjacentWin.close();
                }
            }

            if (this.container != null) {
                this.container.onWindowClosed();
            }

            getElementProvider().resetAll();
            getBackgroundProvider().releaseCache();

            WindowProvider.instance.onWindowClosed(this);
            WindowParent.closeWindow(this);

            BitmapCache.asyncGC();

            Callback.invokeAPICallback("CustomWindowClosed", this);
        }
    }

    private long lastElementRefresh = -1;
    private long lastBackgroundRefresh = -1;

    public void frame(long time) {
        if (!isForegroundDirty) {
            if (isDynamic) {
                if (time - lastElementRefresh > 150) {
                    contentProvider.refreshElements();
                    lastElementRefresh = time;
                }
                if (time - lastBackgroundRefresh > 500) {
                    contentProvider.refreshDrawing();
                    lastBackgroundRefresh = time;
                }
            }

            this.invalidateForeground();
        }
    }

    public void invalidateElements(boolean onCurrentThread) {
        if (isOpened && onCurrentThread) {
            contentProvider.refreshElements();
            lastElementRefresh = System.currentTimeMillis();
            invalidateForeground();
        } else {
            postElementRefresh();
        }
    }

    public void invalidateDrawing(boolean onCurrentThread) {
        if (isOpened && onCurrentThread) {
            contentProvider.refreshDrawing();
            lastBackgroundRefresh = System.currentTimeMillis();
            invalidateBackground();
        } else {
            postBackgroundRefresh();
        }
    }

    @Override
    public boolean isOpened() {
        return isOpened;
    }

    public void postElementRefresh() {
        lastElementRefresh = -1;
    }

    public void postBackgroundRefresh() {
        lastBackgroundRefresh = -1;
    }

    public void forceRefresh() {
        contentProvider.refreshElements();
        contentProvider.refreshDrawing();
    }

    private boolean isTouchable = true;

    public void setTouchable(boolean touchable) {
        isTouchable = touchable;
    }

    public boolean isTouchable() {
        return isTouchable;
    }

    private boolean isBlockingBackground = false;

    public boolean isBlockingBackground() {
        return isBlockingBackground;
    }

    public void setBlockingBackground(boolean blockingBackground) {
        isBlockingBackground = blockingBackground;
    }

    private boolean isInGameOverlay = false;

    public boolean isNotFocusable() {
        return isInGameOverlay;
    }

    public void setAsGameOverlay(boolean inGameOverlay) {
        isInGameOverlay = inGameOverlay;
    }

    public void setBackgroundColor(int color) {
        backgroundProvider.setBackgroundColor(color);
    }

    @Override
    public boolean isInventoryNeeded() {
        return isInventoryNeeded;
    }

    @Override
    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public HashMap<String, UIElement> getElements() {
        return contentProvider.elementMap;
    }

    @Override
    public ScriptableObject getContent() {
        return contentProvider.content;
    }

    private boolean isSetup = false;

    private void setup() {
        isSetup = true;

        contentProvider.setupDrawing();
        contentProvider.setupElements();
        forceRefresh();
    }

    private void setupIfNeeded() {
        if (!isSetup) {
            setup();
        }
    }

    public ScriptableObject content = null;

    public void setContent(ScriptableObject content) {
        location = new UIWindowLocation(ScriptableObjectHelper.getScriptableObjectProperty(content, "location", null));

        contentProvider.setContentObject(content);
        this.content = content;

        ScriptableObject style = ScriptableObjectHelper.getScriptableObjectProperty(content, "style",
                ScriptableObjectHelper.getScriptableObjectProperty(content, "params", null));
        if (style != null) {
            setStyle(new UIStyle(style));
        }

        setup();
        BitmapCache.immediateGC();
    }

    public void setDynamic(boolean val) {
        isDynamic = val;
    }

    public void setInventoryNeeded(boolean val) {
        isInventoryNeeded = val;
    }

    public boolean isBackgroundDirty = false;
    public boolean isForegroundDirty = false;

    public void invalidateBackground() {
        isBackgroundDirty = true;
    }

    public void invalidateForeground() {
        isForegroundDirty = true;
    }

    public UIWindowLocation getLocation() {
        return location;
    }

    public IElementProvider getElementProvider() {
        return elementProvider;
    }

    public IBackgroundProvider getBackgroundProvider() {
        return backgroundProvider;
    }

    public ContentProvider getContentProvider() {
        return contentProvider;
    }

    public float getScale() {
        return location.getDrawingScale();
    }

    public UIStyle getStyle() {
        return style;
    }

    public void setStyle(ScriptableObject scriptable) {
        style.addAllBindings(scriptable);
        invalidateAllContent();
    }

    public void setStyle(UIStyle style) {
        UIStyle old = this.style;
        this.style = style;
        if (old != style) {
            invalidateAllContent();
        }
    }

    public void invalidateAllContent() {
        contentProvider.invalidateAllContent();
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
        ((UIWindowElementDrawable) elementProvider).isDebugEnabled = enabled;
    }

    public void setParentWindow(IWindow parentWindow) {
        this.parentWindow = parentWindow;
    }

    public IWindow getParentWindow() {
        return parentWindow;
    }

    private IWindowEventListener eventListener;

    public void setEventListener(IWindowEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void runCachePreparation(boolean async) {
        this.elementProvider.runCachePreparation();
    }

    public void debug() {
        StringBuilder log = new StringBuilder();
        log.append("starting window debug for ").append(this).append(":\n");
        log.append("\tcontent provider = ").append(contentProvider).append("\n");
        log.append("\telement provider = ").append(elementProvider).append("\n");
        log.append("\tcontainer = ").append(container).append("\n");
        log.append("\ttime since last update elements=" + (System.currentTimeMillis() - lastElementRefresh)
                + " background=" + (System.currentTimeMillis() - lastBackgroundRefresh));
        Logger.debug("UI", log.toString());
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
