package com.zhekasmirnov.innercore.api.mod.ui.types;

import android.graphics.Color;
import com.zhekasmirnov.innercore.api.mod.ui.memory.BitmapWrap;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by zheka on 01.08.2017.
 */

@Deprecated(since = "Zote")
public class UIStyle {
    public static final UIStyle DEFAULT = new UIStyle((UIStyle) null);
    public static final UIStyle LEGACY = new UIStyle((UIStyle) null);
    public static final UIStyle CLASSIC = DEFAULT;

    static {
        LEGACY.setProperty("window_background", Color.rgb(0xc6, 0xc6, 0xc6));
        LEGACY.setProperty("header_background", Color.rgb(0xc6, 0xc6, 0xc6));
        LEGACY.setProperty("tab_background", Color.rgb(0x8c, 0x8c, 0x8c));
        LEGACY.setProperty("tab_background_selected", Color.rgb(0xc6, 0xc6, 0xc6));
        LEGACY.setProperty("default_font_color", Color.rgb(77, 77, 77));
        LEGACY.setProperty("default_font_shadow", 0.0f);
        LEGACY.setProperty("close_button_size", 18);
        LEGACY.setProperty("tab_frame_width", 2);

        LEGACY.addBinding("none", "_default_slot_empty");
        LEGACY.addBinding("slot", "_default_slot_light");
        LEGACY.addBinding("inv_slot", "_default_slot");
        LEGACY.addBinding("close_button_up", "close_button_up");
        LEGACY.addBinding("close_button_down", "close_button_down");
        LEGACY.addBinding("frame_background", "default_frame_bg");
        LEGACY.addBinding("frame_background_border", "default_frame_bg_border");
        LEGACY.addBinding("frame_background_light", "default_frame_bg_light");
        LEGACY.addBinding("frame_background_dark", "default_frame_bg_dark");
        LEGACY.addBinding("frame_tin", "default_frame_tin");
        LEGACY.addBinding("frame_tin_border", "default_frame_tin_border");
        LEGACY.addBinding("frame_tin_inverse", "default_frame_tin_inverse");
        LEGACY.addBinding("frame_slot", "default_frame_slot");
        LEGACY.addBinding("frame_slot_dark", "default_frame_slot_dark");
        LEGACY.addBinding("frame_input", "classic_frame_input");
        LEGACY.addBinding("frame_header", "default_header_frame");
        LEGACY.addBinding("frame_container", "default_container_frame");
        LEGACY.addBinding("frame_tab_left", "default_frame_tab_left");
        LEGACY.addBinding("frame_tab_right", "default_frame_tab_right");
        LEGACY.addBinding("button_up", "default_button_up");
        LEGACY.addBinding("button_hover", "default_button_up");
        LEGACY.addBinding("button_down", "default_button_down");
        LEGACY.addBinding("border_button_up", "default_border_button_up");
        LEGACY.addBinding("border_button_hover", "default_border_button_up");
        LEGACY.addBinding("border_button_down", "default_border_button_down");
        LEGACY.addBinding("selection", "_selection");

        DEFAULT.setProperty("window_background", Color.rgb(0xc6, 0xc6, 0xc6));
        DEFAULT.setProperty("header_background", Color.rgb(0xc6, 0xc6, 0xc6));
        DEFAULT.setProperty("tab_background", Color.rgb(0x8c, 0x8c, 0x8c));
        DEFAULT.setProperty("tab_background_selected", Color.rgb(0xc6, 0xc6, 0xc6));
        DEFAULT.setProperty("default_font_color", Color.rgb(77, 77, 77));
        DEFAULT.setProperty("default_font_shadow", 0.0f);
        DEFAULT.setProperty("close_button_size", 15);
        DEFAULT.setProperty("tab_frame_width", 3);
        DEFAULT.addBinding("slot", "classic_slot");
        DEFAULT.addBinding("inv_slot", "classic_slot");
        DEFAULT.addBinding("close_button_up", "classic_close_button");
        DEFAULT.addBinding("close_button_down", "classic_close_button_down");
        DEFAULT.addBinding("frame_background", "classic_frame_bg");
        DEFAULT.addBinding("frame_background_border", "classic_frame_bg_light_border");
        DEFAULT.addBinding("frame_background_light", "classic_frame_bg_light");
        DEFAULT.addBinding("frame_background_dark", "classic_frame_bg_dark");
        DEFAULT.addBinding("frame_tin", "classic_frame_tin");
        DEFAULT.addBinding("frame_tin_border", "classic_frame_tin_border");
        DEFAULT.addBinding("frame_tin_inverse", "classic_frame_tin_inverse");
        DEFAULT.addBinding("frame_slot", "classic_frame_slot");
        DEFAULT.addBinding("frame_slot_dark", "classic_frame_slot");
        DEFAULT.addBinding("frame_input", "classic_frame_input");
        DEFAULT.addBinding("frame_header", "classic_frame_bg_light");
        DEFAULT.addBinding("frame_container", "default_container_frame");
        DEFAULT.addBinding("frame_tab_left", "classic_frame_tab_left");
        DEFAULT.addBinding("frame_tab_right", "classic_frame_tab_right");
        DEFAULT.addBinding("button_up", "classic_button_up");
        DEFAULT.addBinding("button_hover", "classic_button_hover");
        DEFAULT.addBinding("button_down", "classic_button_down");
        DEFAULT.addBinding("border_button_up", "classic_border_button_up");
        DEFAULT.addBinding("border_button_hover", "classic_border_button_hover");
        DEFAULT.addBinding("selection", "_selection");
    }

    private UIStyle parent = null;
    private HashMap<String, String> styleBindings = new HashMap<>();
    private ArrayList<UIStyle> additionalStyles = new ArrayList<>();

    public void addBinding(String key, String name) {
        styleBindings.put(key, name);
    }

    public String getBinding(String key, String fallback) {
        for (UIStyle style : additionalStyles) {
            if (style.styleBindings.containsKey(key)) {
                return style.styleBindings.get(key);
            }
        }

        if (styleBindings.containsKey(key)) {
            return styleBindings.get(key);
        }

        if (parent != null) {
            return parent.getBinding(key, fallback);
        }

        return fallback;
    }

    public void addStyle(UIStyle style) {
        if (style != null) {
            additionalStyles.add(style);
        }
    }

    private UIStyle(UIStyle style) {
        inherit(style);
    }

    public UIStyle() {
        this(DEFAULT);
    }

    public UIStyle(ScriptableObject obj) {
        this();
        addAllBindings(obj);
    }

    public UIStyle copy() {
        UIStyle style = new UIStyle(parent);
        for (String key : styleBindings.keySet()) {
            style.addBinding(key, styleBindings.get(key));
        }
        return style;
    }

    public void inherit(UIStyle style) {
        if (style != null) {
            parent = style;
        }
    }

    public void addAllBindings(ScriptableObject obj) {
        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            String name = key.toString();
            Object val = obj.get(key);
            if (val instanceof CharSequence) {
                addBinding(name, val.toString());
            }
        }
    }

    public Collection<String> getAllBindingNames() {
        return styleBindings.keySet();
    }

    public String getBitmapName(String name) {
        if (name.startsWith("style:")) {
            name = name.substring(6);
            return getBinding(name, name);
        } else {
            return name;
        }
    }

    private final HashMap<String, Object> properties = new HashMap<>();

    public int getIntProperty(String name, int defaultValue) {
        return properties.containsKey(name) ? ((Number) properties.get(name)).intValue()
                : (parent != null ? parent.getIntProperty(name, defaultValue) : defaultValue);
    }

    public float getFloatProperty(String name, float defaultValue) {
        return properties.containsKey(name) ? ((Number) properties.get(name)).floatValue()
                : (parent != null ? parent.getFloatProperty(name, defaultValue) : defaultValue);
    }

    public double getDoubleProperty(String name, double defaultValue) {
        return properties.containsKey(name) ? ((Number) properties.get(name)).doubleValue()
                : (parent != null ? parent.getDoubleProperty(name, defaultValue) : defaultValue);
    }

    public String getStringProperty(String name, String defaultValue) {
        return properties.containsKey(name) ? properties.get(name) + ""
                : (parent != null ? parent.getStringProperty(name, defaultValue) : defaultValue);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        return properties.containsKey(name) ? ((Boolean) properties.get(name)).booleanValue()
                : (parent != null ? parent.getBooleanProperty(name, defaultValue) : defaultValue);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public static BitmapWrap getBitmapByDescription(UIStyle style, String description) {
        return BitmapWrap.wrap("missing_bitmap");
    }
}
