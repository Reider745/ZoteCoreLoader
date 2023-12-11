package com.zhekasmirnov.innercore.api.mod.ui.background;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Created by zheka on 02.08.2017.
 */

public class DrawingFactory {
    private static HashMap<String, Class<? extends IDrawing>> drawingConstructorMap = new HashMap<>();

    public static void put(String name, Class<? extends IDrawing> element) {
        drawingConstructorMap.put(name, element);
    }

    static {
        put("background", DrawColor.class);
        put("color", DrawColor.class);
        put("bitmap", DrawImage.class);
        put("frame", DrawFrame.class);
        put("text", DrawText.class);
        put("line", DrawLine.class);
        put("custom", DrawCustom.class);
    }

    public static IDrawing construct(ScriptableObject descr, UIStyle style) {
        String type = ScriptableObjectHelper.getStringProperty(descr, "type",
                ScriptableObjectHelper.getStringProperty(descr, "TYPE", null));

        if (type == null || !drawingConstructorMap.containsKey(type)) {
            return null;
        }

        Class<? extends IDrawing> clazz = drawingConstructorMap.get(type);
        Constructor<?> constructor = clazz.getConstructors()[0];
        try {
            IDrawing drawing = (IDrawing) constructor.newInstance();
            drawing.onSetup(descr, style);
            return drawing;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
