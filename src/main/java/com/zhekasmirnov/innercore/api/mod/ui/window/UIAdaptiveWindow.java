package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Pair;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.types.Font;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collection;

@Deprecated(since = "Zote")
public class UIAdaptiveWindow extends UIWindowGroup {
    // classic style:
    // - bg: 0x8b8b8b
    // - text: 0x383838
    // - light bg: 0xc5c5c5

    private UIProfile defaultProfile, classicProfile;

    public UIAdaptiveWindow(ScriptableObject content) {
        setContent(content);
    }

    private class WindowOptions {
        private String titleText = null;
        private Font titleFont = null;
        private boolean isCloseButtonShown = true;

        private float height = 500;

        private WindowOptions(Scriptable optionsScriptable) {
            if (optionsScriptable != null) {
                ScriptableObjectWrapper options = new ScriptableObjectWrapper(optionsScriptable);
                isCloseButtonShown = options.getBoolean("close_button", true);

                ScriptableObjectWrapper header = options.getScriptableWrapper("header");
                if (header != null) {
                    titleText = header.getString("text");
                    ScriptableObjectWrapper font = header.getScriptableWrapper("font");
                    if (font != null) {
                        titleFont = new Font(font.getWrappedObject());
                    }
                }

                height = options.getFloat("height", 500);
            }
        }

        private WindowOptions(ScriptableObjectWrapper options) {
            this(options != null ? options.getWrapped() : null);
        }
    }

    private class UIProfile {
        UIStyle style;
        ArrayList<Pair<String, UIWindow>> windows = new ArrayList<>();

        private UIProfile(UIStyle style) {
            this.style = style;
        }

        private void addWindow(String name, UIWindow window) {
            windows.add(new Pair<String, UIWindow>(name, window));
        }

        private UIWindow addWindow(String name, ScriptableObjectWrapper content) {
            UIWindow window = new UIWindow(content.getWrappedObject());
            addWindow(name, window);
            window.setDynamic(true);
            return window;
        }

        private void setProfileTo(UIWindowGroup group) {
            for (Pair<String, UIWindow> window : windows) {
                group.addWindowInstance(window.first, window.second);
            }
            group.setStyle(style);
        }
    }

    @SuppressLint("DefaultLocale")
    private void initializeDefaultProfile(ScriptableObjectWrapper content) {
        WindowOptions options = new WindowOptions(content.getScriptableWrapper("options"));

        UIWindowLocation locationBg = new UIWindowLocation();
        UIWindowLocation locationInv = new UIWindowLocation();
        locationInv.setPadding(110, 30, 30, 690);
        locationInv.setScroll(0, (int) locationInv.windowToGlobal(2250));
        UIWindowLocation locationMain = new UIWindowLocation();
        locationMain.setPadding(100, 20, 320, 0);
        locationMain.setScroll(0, (int) locationMain.windowToGlobal(options.height));

        ScriptableObjectWrapper style = content.getScriptableWrapper("style");

        // setup background
        ScriptableObjectWrapper background = new ScriptableObjectWrapper("{\"elements\": {}, \"drawing\":[]}");
        if (style != null) {
            background.put("style", style);
        }

        // background drawing
        ScriptableObjectWrapper backgroundDrawing = background.getScriptableWrapper("drawing");
        backgroundDrawing.insert(0, new ScriptableObjectWrapper(
                "{\"type\": \"color\", \"color\": 0}"));
        backgroundDrawing.insert(1, new ScriptableObjectWrapper(
                String.format(
                        "{\"type\": \"frame\", \"bitmap\": \"style:frame_background_border\", \"x\": 0, \"y\": 0, \"scale\": 3, \"width\": %d, \"height\": %d}",
                        1000, locationBg.getWindowHeight())));
        backgroundDrawing.insert(2, new ScriptableObjectWrapper(
                "{\"type\": \"image\", \"bitmap\": \"_standart_header_shadow\", \"scale\": 2, \"x\": 0, \"y\": 75}"));
        backgroundDrawing.insert(3, new ScriptableObjectWrapper(
                "{\"type\": \"frame\", \"bitmap\": \"style:frame_header\", \"x\": 0, \"y\": 0, \"scale\": 3, \"width\": 1000, \"height\": 80}"));
        backgroundDrawing.insert(4, new ScriptableObjectWrapper(
                String.format(
                        "{\"type\": \"frame\", \"bitmap\": \"style:frame_container\", \"x\": 20, \"y\": 100, \"scale\": 3, \"width\": %d, \"height\": %d}",
                        300, locationBg.height - 120)));

        // background elements
        ScriptableObjectWrapper backgroundElements = background.getScriptableWrapper("elements");
        backgroundElements.put("_adapted_close_button", new ScriptableObjectWrapper(
                "{\"type\":\"close_button\", \"bitmap\":\"style:close_button_up\", \"bitmap2\":\"style:close_button_down\", \"scale\": 3, \"x\": 933, \"y\": 13}"));

        Font font = options.titleFont;
        if (font == null) {
            font = new Font(Color.WHITE, 22, .65f);
        }
        font.alignment = Font.ALIGN_CENTER;

        if (options.titleText != null) {
            ScriptableObjectWrapper title = new ScriptableObjectWrapper(
                    String.format("{\"type\": \"text\", \"x\": 500, \"y\": 20, \"text\": \"%s\"}", options.titleText));
            title.put("font", font.asScriptable());
            backgroundElements.put("_adapted_window_title", title);
        }

        // setup inventory
        ScriptableObjectWrapper inventory = new ScriptableObjectWrapper("{\"elements\": {}, \"drawing\":[]}");
        if (style != null) {
            inventory.put("style", style);
        }

        // inventory drawing
        ScriptableObjectWrapper inventoryDrawing = inventory.getScriptableWrapper("drawing");
        inventoryDrawing.insert(0, new ScriptableObjectWrapper(
                "{\"type\": \"color\", \"color\": 0}"));

        // inventory elements
        ScriptableObjectWrapper inventoryElements = inventory.getScriptableWrapper("elements");
        int index = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 4; x++) {
                inventoryElements.put("_adapted_inv_slot" + index, new ScriptableObjectWrapper(
                        String.format("{\"type\": \"invslot\", \"x\": %f, \"y\": %f, \"size\": 250, \"index\": %d}",
                                x * 250f,
                                y * 250f,
                                index + 9)));
                index++;
            }
        }

        // create profile
        defaultProfile = new UIProfile(UIStyle.DEFAULT);
        defaultProfile.addWindow("background", background).getLocation().set(locationBg);
        defaultProfile.addWindow("inventory", inventory).getLocation().set(locationInv);
        UIWindow mainWindow = defaultProfile.addWindow("main", content);
        mainWindow.getLocation().set(locationMain);
        mainWindow.setInventoryNeeded(true);
    }

    @SuppressLint("DefaultLocale")
    private void initializeClassicProfile(ScriptableObjectWrapper content) {
        WindowOptions options = new WindowOptions(content.getScriptableWrapper("options"));
        float contentHeight = Math.max(options.height, 150);
        float headerHeight = 100;
        float inventoryHeight = 500;
        float windowHeight = contentHeight + inventoryHeight + headerHeight;

        UIWindowLocation locationBg = new UIWindowLocation();
        float screenWidth = locationBg.windowToGlobal(locationBg.getWindowWidth());
        float screenHeight = locationBg.windowToGlobal(locationBg.getWindowHeight());
        float paddingX = screenWidth * 0.28f;
        locationBg.setPadding(0, 0, paddingX, paddingX);
        float paddingY = (screenHeight - locationBg.windowToGlobal(windowHeight)) / 2;
        locationBg.setPadding(paddingY, paddingY, paddingX, paddingX);

        UIWindowLocation locationMain = new UIWindowLocation();
        locationMain.setPadding(paddingY + locationBg.windowToGlobal(headerHeight),
                paddingY + locationBg.windowToGlobal(inventoryHeight), paddingX, paddingX);

        ScriptableObjectWrapper background = new ScriptableObjectWrapper("{\"elements\": {}, \"drawing\":[]}");
        ScriptableObjectWrapper style = content.getScriptableWrapper("style");
        if (style != null) {
            background.put("style", style);
        }

        // setup drawing
        ScriptableObjectWrapper drawing = background.getScriptableWrapper("drawing");
        drawing.insert(0, new ScriptableObjectWrapper("{\"type\": \"color\", \"color\": 0}"));
        drawing.insert(1, new ScriptableObjectWrapper(String.format(
                "{\"type\": \"frame\", \"bitmap\": \"style:frame_background_border\", \"x\": 0, \"y\": 0, \"scale\": 5.555, \"width\": %f, \"height\": %f}",
                1000f, windowHeight)));

        // setup elements
        ScriptableObjectWrapper elements = background.getScriptableWrapper("elements");
        if (elements == null) {
            elements = new ScriptableObjectWrapper("{}");
            background.put("elements", elements);
        }

        if (options.isCloseButtonShown) {
            elements.put("_adapted_close_button", new ScriptableObjectWrapper(
                    "{\"type\":\"close_button\", \"bitmap\":\"style:close_button_up\", \"bitmap2\":\"style:close_button_down\", \"scale\": 8, \"x\": 910, \"y\": 50}"));
        }

        Font font = options.titleFont;
        if (font == null) {
            font = new Font(Color.parseColor("#383838"), 45, 0);
        }

        if (options.titleText != null) {
            ScriptableObjectWrapper title = new ScriptableObjectWrapper(
                    String.format("{\"type\": \"text\", \"x\": 36, \"y\": 40, \"text\": \"%s\"}", options.titleText));
            title.put("font", font.asScriptable());
            elements.put("_adapted_window_title", title);
        }

        ScriptableObjectWrapper inventoryLabel = new ScriptableObjectWrapper(
                String.format("{\"type\": \"text\", \"x\": 36, \"y\": %f, \"text\": \"%s\"}",
                        contentHeight + headerHeight - 28, NameTranslation.translate("Inventory")));
        inventoryLabel.put("font", font.asScriptable());
        elements.put("_adapted_inv_label", inventoryLabel);

        int index = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                elements.put("_adapted_inv_slot" + index, new ScriptableObjectWrapper(
                        String.format("{\"type\": \"invslot\", \"x\": %f, \"y\": %f, \"size\": 104, \"index\": %d}",
                                32f + x * 104f,
                                y == 3 ? windowHeight - 136f : windowHeight - 256f - 104 * y,
                                y == 3 ? index - 18 : 18 + index % 9 + (2 - index / 9) * 9)));
                index++;
            }
        }

        classicProfile = new UIProfile(UIStyle.CLASSIC);
        UIWindow backgroundWindow = classicProfile.addWindow("background", background);
        backgroundWindow.getLocation().set(locationBg);
        backgroundWindow.setBlockingBackground(true);
        UIWindow mainWindow = classicProfile.addWindow("main", content);
        mainWindow.getLocation().set(locationMain);
        mainWindow.setInventoryNeeded(true);
    }

    private void initializeTransparentBackground(ScriptableObjectWrapper content) {
        // setup transparent background for main window
        ScriptableObjectWrapper drawing = content.getScriptableWrapper("drawing");
        if (drawing == null || !drawing.isArray()) {
            drawing = new ScriptableObjectWrapper("[]");
            content.put("drawing", drawing);
        }

        drawing.insert(0, new ScriptableObjectWrapper("{\"type\": \"color\", \"color\": 0}"));
    }

    public void setContent(ScriptableObject content) {
        ScriptableObjectWrapper wrap = new ScriptableObjectWrapper(content);
        initializeTransparentBackground(wrap);
        initializeDefaultProfile(wrap);
        initializeClassicProfile(wrap);
    }

    private void setProfile(UIProfile profile) {
        if (profile == null) {
            return;
        }

        boolean isOpened = isOpened();
        UiAbstractContainer container = getContainer();
        if (isOpened) {
            close();
        }

        Collection<String> names = getWindowNames();
        while (names.size() > 0) {
            removeWindow(names.iterator().next());
        }

        profile.setProfileTo(this);

        if (isOpened) {
            if (container != null) {
                container.openAs(this);
            } else {
                open();
            }
        }
    }

    public void setProfile(int profile) {
        setProfile(profile == 0 ? classicProfile : defaultProfile);
    }

    private int forcedProfile = -1;

    public void setForcedProfile(int profile) {
        forcedProfile = profile;
    }

    @Override
    public void open() {
        if (!isOpened()) {
            setProfile(forcedProfile != -1 ? forcedProfile : NativeAPI.getUiProfile() & 1);
        }
        super.open();
    }
}
