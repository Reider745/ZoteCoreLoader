package com.zhekasmirnov.innercore.api.mod.ui.elements;

import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 01.08.2017.
 */

public class  ElementFactory {
    private static HashMap<String, Class<? extends UIElement>> elementConstructorMap = new HashMap<>();

    public static void put(String name, Class<? extends UIElement> element) {
        elementConstructorMap.put(name, element);
    }

    public static UIElement construct(String type, UIWindow win, ScriptableObject descr) {
        return null;
    }

    public static UIElement construct(UIWindow win, ScriptableObject descr) {
        return null;
    }
}
