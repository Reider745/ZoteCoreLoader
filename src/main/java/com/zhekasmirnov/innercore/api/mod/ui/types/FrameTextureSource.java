package com.zhekasmirnov.innercore.api.mod.ui.types;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import android.graphics.Bitmap;

import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 05.08.2017.
 */

@Deprecated(since = "Zote")
public class FrameTextureSource {
    private static HashMap<String, FrameTexture> loadedTextures = new HashMap<>();

    public static FrameTexture getFrameTexture(String name, UIStyle style) {
        style = style != null ? style : UIStyle.DEFAULT;
        name = style.getBitmapName(name);
        if (loadedTextures.containsKey(name)) {
            return loadedTextures.get(name);
        }

        FrameTexture texture = new FrameTexture((Bitmap) null);
        loadedTextures.put(name, texture);
        return texture;
    }

    public static FrameTexture getFrameTexture(String name) {
        return getFrameTexture(name, null);
    }

    public static boolean[] scriptableAsSides(ScriptableObject obj) {
        if (obj == null) {
            return new boolean[] { true, true, true, true };
        } else {
            boolean[] sides = new boolean[4];
            sides[FrameTexture.SIDE_TOP] = ScriptableObjectHelper.getBooleanProperty(obj, "up", true);
            sides[FrameTexture.SIDE_BOTTOM] = ScriptableObjectHelper.getBooleanProperty(obj, "down", true);
            sides[FrameTexture.SIDE_LEFT] = ScriptableObjectHelper.getBooleanProperty(obj, "left", true);
            sides[FrameTexture.SIDE_RIGHT] = ScriptableObjectHelper.getBooleanProperty(obj, "right", true);
            return sides;
        }
    }
}
