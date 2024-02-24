package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public interface IElementProvider {
    void setBackgroundProvider(IBackgroundProvider provider);

    void addOrRefreshElement(UIElement element);

    void removeElement(UIElement element);

    void invalidateAll();

    void releaseAll();

    void resetAll();

    void runCachePreparation();

    UIStyle getStyleFor(UIElement element);

    void setWindowStyle(UIStyle style);
}
