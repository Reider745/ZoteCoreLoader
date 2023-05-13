package com.zhekasmirnov.innercore.api.mod.ui.types;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

public class Font {
    public static final int ALIGN_DEFAULT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_END = 2;
    public static final int ALIGN_CENTER_HORIZONTAL = 3;

    public int color;
    public float size, shadow;
    public int alignment = ALIGN_DEFAULT;
    public boolean isBold, isCursive, isUnderlined;

    public Font(int color, float size, float shadow) {
        this.color = color;
        this.size = size;
        this.shadow = shadow;

    }

    public Font(ScriptableObject obj) {
        this.color = ScriptableObjectHelper.getIntProperty(obj, "color", 0);
        this.size = ScriptableObjectHelper.getFloatProperty(obj, "size", 20);
        this.shadow = ScriptableObjectHelper.getFloatProperty(obj, "shadow", 0);
        this.alignment = ScriptableObjectHelper.getIntProperty(obj, "alignment", ScriptableObjectHelper.getIntProperty(obj, "align", ALIGN_DEFAULT));
        if (this.alignment != ALIGN_CENTER && this.alignment != ALIGN_END) {
            this.alignment = ALIGN_DEFAULT;
        }

        if (!obj.has("color", obj) && !obj.has("size", obj) && !obj.has("shadow", obj)) {
            this.color = 0;
            this.shadow = .45f;
        }

        isBold = ScriptableObjectHelper.getBooleanProperty(obj, "bold", false);
        isCursive = ScriptableObjectHelper.getBooleanProperty(obj, "cursive", false);
        isUnderlined = ScriptableObjectHelper.getBooleanProperty(obj, "underline", false);

    }

    private float[] getAlignOffset(String text) {
        if (alignment == ALIGN_DEFAULT) {
            return new float[]{0, 0};
        }
        return new float[]{0, 0};
    }

    public void drawText(Object canvas, float x, float y, String text, float scale) {
        float size = this.size * scale;

        float[] offset = getAlignOffset(text);
        x += offset[0];
        y += offset[1];
    }

    public Object getBounds(String text, float x, float y, float scale) {
        return null;
    }

    public float getTextWidth(String text, float scale) {
        return 1;
    }

    public float getTextHeight(String text, float x, float y, float scale) {
        return 1;
    }

    public ScriptableObject asScriptable() {
        ScriptableObject obj = ScriptableObjectHelper.createEmpty();
        obj.put("size", obj, size);
        obj.put("color", obj, color);
        obj.put("shadow", obj, shadow);
        obj.put("alignment", obj, alignment);
        obj.put("bold", obj, isBold);
        obj.put("cursive", obj, isCursive);
        obj.put("underline", obj, isUnderlined);
        return obj;
    }
}
