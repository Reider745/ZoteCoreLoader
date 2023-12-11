package com.zhekasmirnov.innercore.api.mod.ui.elements;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import com.zhekasmirnov.innercore.utils.UIUtils;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Created by zheka on 01.08.2017.
 */

public class ElementFactory {
    private static HashMap<String, Class<? extends UIElement>> elementConstructorMap = new HashMap<>();

    public static void put(String name, Class<? extends UIElement> element) {
        elementConstructorMap.put(name, element);
    }

    static {
        put("slot", UISlotElement.class);
        put("invSlot", UIInvSlotElement.class);
        put("invslot", UIInvSlotElement.class);
        put("image", UIImageElement.class);
        put("button", UIButtonElement.class);
        put("closeButton", UICloseButtonElement.class);
        put("close_button", UICloseButtonElement.class);
        put("text", UITextElement.class);
        put("fps", UIFPSTextElement.class);
        put("scale", UIScaleElement.class);
        put("frame", UIFrameElement.class);
        put("tab", UITabElement.class);
        put("switch", UISwitchElement.class);
        put("scroll", UIScrollElement.class);
        put("custom", UICustomElement.class);
    }

    public static UIElement construct(String type, UIWindow win, ScriptableObject descr) {
        if (!elementConstructorMap.containsKey(type)) {
            return null;
        }

        Class<? extends UIElement> clazz = elementConstructorMap.get(type);
        Constructor<?> constructor = clazz.getConstructors()[0];
        try {
            return (UIElement) constructor.newInstance(win, descr);
        } catch (Exception e) {
            UIUtils.log("failed to create element instance: " + type);
            e.printStackTrace();
            return null;
        }
    }

    public static UIElement construct(UIWindow win, ScriptableObject descr) {
        String name = ScriptableObjectHelper.getStringProperty(descr, "type",
                ScriptableObjectHelper.getStringProperty(descr, "TYPE", null));
        if (name != null) {
            return construct(name, win, descr);
        } else {
            return null;
        }
    }
}
