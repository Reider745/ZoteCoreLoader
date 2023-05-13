package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;

/**
 * Created by zheka on 31.07.2017.
 */

public interface IElementProvider {
    void setBackgroundProvider(IBackgroundProvider provider);
    void addOrRefreshElement(UIElement element);
    void removeElement(UIElement element);
    void invalidateAll();
    void releaseAll();
    void resetAll();
    void runCachePreparation();

    Object getStyleFor(UIElement element);
    void setWindowStyle(Object style);
}
