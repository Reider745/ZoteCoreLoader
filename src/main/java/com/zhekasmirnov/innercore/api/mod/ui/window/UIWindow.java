package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.ContentProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IBackgroundProvider;
import com.zhekasmirnov.innercore.api.mod.ui.IElementProvider;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapCache;
import com.zhekasmirnov.innercore.api.mod.ui.types.ITouchEventListener;
import com.zhekasmirnov.innercore.api.mod.ui.types.TouchEvent;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 31.07.2017.
 */

public class UIWindow implements IWindow {
    private static final Object LOCK = new Object();

    private UIWindowLocation location;
    private IWindow parentWindow;

    private ImageView elementView;
    public ViewGroup layout;

    private TouchEvent touchEvent;
    private IElementProvider elementProvider;
    private IBackgroundProvider backgroundProvider;
    private ContentProvider contentProvider;
    private UIStyle style = new UIStyle();

    private boolean isOpened = false;
    private boolean isDynamic = true;
    private boolean isInventoryNeeded = false;


    private boolean isApplyingInsets = false;
    private synchronized void applyWindowInsets(WindowInsets insets) {
        if (!isApplyingInsets) {
            isApplyingInsets = true;
            try {
                WindowParent.applyWindowInsets(this, insets);
            } catch (Throwable err) {
                err.printStackTrace();
            }
            isApplyingInsets = false;
        }
    }

    private void resizeView(int newWidth, int newHeight) {
        try {
            Constructor<? extends ViewGroup.LayoutParams> ctor = elementView.getLayoutParams().getClass().getDeclaredConstructor(int.class, int.class);
            elementView.setLayoutParams(ctor.newInstance(newWidth, newHeight));
            elementView.setMinimumWidth((int) newWidth);
            elementView.setMinimumHeight((int) newHeight);
        } catch (Exception e) {
            ICLog.e("ERROR", "resizeView error", e);
        }
    }

    private void initializeLayout(Context ctx) {
        return;
        /*if (layout != null) {
            WindowParent.releaseWindowLayout(layout);
            try {
                layout.removeView(elementView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            layout = null;
        }

        ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams((int) (location.scrollX * location.getScale()), (int) (location.scrollY * location.getScale()));
        ViewGroup viewParent = null;
        if (location.scrollY > location.height || location.forceScrollY) {
            ScrollView scrollView = new ScrollView(ctx) {
                @Override
                public WindowInsets onApplyWindowInsets(WindowInsets insets) {
                    applyWindowInsets(insets);
                    return super.onApplyWindowInsets(insets);
                }
            };;
            viewParent = layout = scrollView;
            scrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
        }
        if (location.scrollX > location.width || location.forceScrollX) {
            HorizontalScrollView scrollView = new HorizontalScrollView(ctx) {
                @Override
                public WindowInsets onApplyWindowInsets(WindowInsets insets) {
                    applyWindowInsets(insets);
                    return super.onApplyWindowInsets(insets);
                }
            };
            scrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
            if (layout != null) {
                layout.setMinimumWidth((int) (location.scrollX * location.getScale()));
                layout.setMinimumHeight((int) (location.scrollY * location.getScale()));
                scrollView.addView(layout);
            }
            else {
                viewParent = scrollView;
            }
            layout = scrollView;
        }
        if (layout == null) {
            viewParent = layout = new RelativeLayout(ctx) {
                @Override
                public WindowInsets onApplyWindowInsets(WindowInsets insets) {
                    applyWindowInsets(insets);
                    return super.onApplyWindowInsets(insets);
                }
            };;
        }

        final ViewGroup viewParentFinal = viewParent;
        ((Activity) ctx).runOnUiThread(new Runnable() {
            public void run() {
                elementView.setMinimumWidth((int) (location.scrollX * location.getScale()));
                elementView.setMinimumHeight((int) (location.scrollY * location.getScale()));

                if (elementView.getParent() != null) {
                    ((ViewGroup) elementView.getParent()).removeView(elementView);
                }
                viewParentFinal.addView(elementView, params);
            }
        });*/
    }

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

        elementView = null;

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
        synchronized(LOCK) {
            setupIfNeeded();

            if (isOpened) {
                return;
            }

            Callback.invokeAPICallback("CustomWindowOpened", this);

            //refreshLocation();
            initializeLayout(null);
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
        synchronized(LOCK) {
            if (isOpened) {
                return;
            }
            isOpened = true;

            backgroundProvider.prepareCache();

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
        synchronized(LOCK) {
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
            if (isDynamic ) {
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

        ScriptableObject style = ScriptableObjectHelper.getScriptableObjectProperty(content, "style", ScriptableObjectHelper.getScriptableObjectProperty(content, "params", null));
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
        elementView.invalidate();
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
        if (!async) {
            this.elementProvider.runCachePreparation();
        }
        else {
            final IElementProvider elementProvider = this.elementProvider;
            (new java.lang.Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                    elementProvider.runCachePreparation();
                }
            })).start();
        }
    }

    public void debug() {
        StringBuilder log = new StringBuilder();
        log.append("starting window debug for ").append(this).append(":\n");
        log.append("\tcontent provider = ").append(contentProvider).append("\n");
        log.append("\telement provider = ").append(elementProvider).append("\n");
        log.append("\tcontainer = ").append(container).append("\n");
        log.append("\ttime since last update elements=" + (System.currentTimeMillis() - lastElementRefresh) + " background=" + (System.currentTimeMillis() - lastBackgroundRefresh));
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
