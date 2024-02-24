package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.innercore.api.mod.ui.background.IDrawing;

/**
 * Created by zheka on 01.08.2017.
 */

@Deprecated(since = "Zote")
public interface IBackgroundProvider {
    void setBackgroundColor(int color);

    void addDrawing(IDrawing drawing);

    void clearAll();

    void prepareCache();

    void releaseCache();
}
