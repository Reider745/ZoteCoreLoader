package com.zhekasmirnov.innercore.api.mod.ui.window;

/**
 * Created by zheka on 07.08.2017.
 */

@Deprecated(since = "Zote")
public interface IWindowEventListener {
    void onOpen(UIWindow window);

    void onClose(UIWindow window);
}
