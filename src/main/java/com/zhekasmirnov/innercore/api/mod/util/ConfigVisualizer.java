package com.zhekasmirnov.innercore.api.mod.util;

import android.graphics.Color;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.types.Font;
import com.zhekasmirnov.innercore.mod.build.Config;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;

/**
 * Created by zheka on 15.08.2017.
 */

@Deprecated(since = "Zote")
public class ConfigVisualizer {
    private Config config;

    private static Font font, font2;
    static {
        font = new Font(Color.WHITE, 30, .45f);
        font2 = new Font(Color.WHITE, 30, .45f);
        font2.alignment = Font.ALIGN_END;
    }

    public ConfigVisualizer(Config config, String prefix) {
        this.config = config;
        this.prefix = prefix;
    }

    public ConfigVisualizer(Config config) {
        this(config, "config_vis");
    }

    private String prefix;
    private static int uniqueIndex = 0;

    private String getElementName() {
        return prefix + "_" + (uniqueIndex++);
    }

    private void visualizeName(float x, float y, float z, ScriptableObject elements, String name) {
        ScriptableObject nameElement = ScriptableObjectHelper.createEmpty();
        nameElement.put("type", nameElement, "text");
        nameElement.put("x", nameElement, x);
        nameElement.put("y", nameElement, y + 22.5);
        nameElement.put("z", nameElement, z);
        nameElement.put("text", nameElement, name);
        nameElement.put("font", nameElement, font.asScriptable());
        elements.put(getElementName(), elements, nameElement);
    }

    private void visualizeElement(float x, float y, float z, ScriptableObject elements, Config unit, String name) {
        visualizeName(x, y, z, elements, name);

        float nameLen = font.getTextWidth(name, 1);
        float valueLen;

        Object val = unit.get(name);
        if (val instanceof Boolean) {
            valueLen = 120;

            ScriptableObject valElement = ScriptableObjectHelper.createEmpty();
            valElement.put("type", valElement, "switch");
            valElement.put("x", valElement, 860);
            valElement.put("y", valElement, y + 4.5);
            valElement.put("z", valElement, z);
            valElement.put("state", valElement, val);
            valElement.put("configValue", valElement, unit.getValue(name));
            elements.put(getElementName(), elements, valElement);
        } else {
            String text = val + "";
            if (val instanceof CharSequence) {
                text = "\"" + text + "\"";
            }
            valueLen = font.getTextWidth(text, 1) + 20;

            ScriptableObject valElement = ScriptableObjectHelper.createEmpty();
            valElement.put("type", valElement, "text");
            valElement.put("x", valElement, 960);
            valElement.put("y", valElement, y + 22.5);
            valElement.put("z", valElement, z);
            valElement.put("font", valElement, font2.asScriptable());
            valElement.put("text", valElement, text);
            elements.put(getElementName(), elements, valElement);
        }

        float lineLen = 940 - nameLen - valueLen - x;
        float lineOffset = x + nameLen + 20;

        ScriptableObject lineElement = ScriptableObjectHelper.createEmpty();
        lineElement.put("type", lineElement, "image");
        lineElement.put("bitmap", lineElement, "default_horizontal_line_template");
        lineElement.put("x", lineElement, lineOffset);
        lineElement.put("y", lineElement, y + 32.5);
        lineElement.put("z", lineElement, z);
        lineElement.put("width", lineElement, lineLen);
        lineElement.put("height", lineElement, 10);
        elements.put(getElementName(), elements, lineElement);

    }

    public void clearVisualContent(ScriptableObject elements) {
        Object[] keys = elements.getAllIds();
        for (Object key : keys) {
            if ((key + "").contains(prefix + "_")) {
                elements.put((key + ""), elements, null);
            }
        }
    }

    public void createVisualContent(ScriptableObject elements, ScriptableObject prefs) {
        if (prefs == null) {
            prefs = ScriptableObjectHelper.createEmpty();
        }
        float offX = ScriptableObjectHelper.getFloatProperty(prefs, "x", 0);
        float offY = ScriptableObjectHelper.getFloatProperty(prefs, "y", 0);
        float offZ = ScriptableObjectHelper.getFloatProperty(prefs, "z", 0);

        currentYOffset = 0;
        createVisualContent(elements, config, offX, offY, offZ);
    }

    private float currentYOffset = 0;

    private void createVisualContent(ScriptableObject elements, Config cfg, float x, float y, float z) {
        ArrayList<String> names = cfg.getNames();

        for (String name : names) {
            Object val = cfg.get(name);
            if (val instanceof Config) {
                visualizeName(x, y + currentYOffset, z, elements, name + ": ");
                currentYOffset += 75;
                createVisualContent(elements, (Config) val, x + 75, y, z);
            } else {
                visualizeElement(x, y + currentYOffset, z, elements, cfg, name);
                currentYOffset += 75;
            }
        }
    }
}
