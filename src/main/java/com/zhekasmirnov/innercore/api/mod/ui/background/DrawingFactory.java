package com.zhekasmirnov.innercore.api.mod.ui.background;

import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 02.08.2017.
 */

public class DrawingFactory {
    private static HashMap<String, Class<? extends IDrawing>> drawingConstructorMap = new HashMap<>();

    public static void put(String name, Class<? extends IDrawing> element) {
        drawingConstructorMap.put(name, element);
    }

    public static IDrawing construct(ScriptableObject descr, Object style) {
        return null;
    }
}
