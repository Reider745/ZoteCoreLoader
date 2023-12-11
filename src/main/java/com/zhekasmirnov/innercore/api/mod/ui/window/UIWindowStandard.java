package com.zhekasmirnov.innercore.api.mod.ui.window;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.api.mod.ui.types.Font;
import com.zhekasmirnov.innercore.api.mod.ui.types.UIStyle;
import com.zhekasmirnov.innercore.api.mod.ui.types.WindowContentAdapter;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 04.08.2017.
 */

public abstract class UIWindowStandard extends UIWindowGroup {
    private ScriptableObject content;
    private ScriptableObject standardContent;

    public UIWindowStandard(ScriptableObject content) {
        setContent(content);
        setCloseOnBackPressed(true);
    }

    private int paddingTop = 0;
    private int paddingLeft = 0;

    protected abstract boolean isLegacyFormat();

    private void setupMainWindow() {
        removeWindow("main");
        removeWindow("content");

        WindowContentAdapter mainWinContent = new WindowContentAdapter(this.content);

        // setup location
        UIWindowLocation loc = new UIWindowLocation(
                ScriptableObjectHelper.getScriptableObjectProperty(this.content, "location", null));
        loc.setPadding(UIWindowLocation.PADDING_TOP, paddingTop);

        // setup background
        if (standardContent != null) {
            // prepare height parameter
            int minHeight = ScriptableObjectHelper.getIntProperty(standardContent, "minHeight", 0);

            // setup separate content window if required
            ScriptableObject contentWindowData = ScriptableObjectHelper.getScriptableObjectProperty(standardContent,
                    "contentWindow", !isLegacyFormat() ? ScriptableObjectHelper.createEmpty() : null);
            if (contentWindowData != null) {
                WindowContentAdapter contentWinContent = mainWinContent;
                mainWinContent = new WindowContentAdapter();

                int padding = ScriptableObjectHelper.getIntProperty(contentWindowData, "padding", 20);
                int width = ScriptableObjectHelper.getIntProperty(contentWindowData, "width", 640);

                UIWindowLocation contentLoc = new UIWindowLocation();
                contentLoc.setPadding(UIWindowLocation.PADDING_LEFT, 1000 - width - padding);
                contentLoc.setPadding(UIWindowLocation.PADDING_RIGHT, padding);
                contentLoc.setPadding(UIWindowLocation.PADDING_TOP, paddingTop + padding + 20);
                contentLoc.setPadding(UIWindowLocation.PADDING_BOTTOM, padding);
                contentLoc.width = loc.scrollX = width;
                if (minHeight > contentLoc.height) {
                    contentLoc.scrollY = (int) Math.max(contentLoc.height, width * minHeight / 1000f);
                }

                contentWinContent.setLocation(contentLoc);
                addWindow("content", contentWinContent.getContent()).setBackgroundColor(0);
            } else if (minHeight > loc.height) {
                loc.scrollY = minHeight;
            }

            ScriptableObject background = ScriptableObjectHelper.getScriptableObjectProperty(standardContent,
                    "background", null);
            if (background != null) {
                int color = ScriptableObjectHelper.getIntProperty(background, "color", Color.WHITE);
                String bitmap = ScriptableObjectHelper.getStringProperty(background, "bitmap", null);
                ScriptableObject frame = ScriptableObjectHelper.getScriptableObjectProperty(background, "frame", null);

                boolean isStandard = ScriptableObjectHelper.getBooleanProperty(background, "standard",
                        ScriptableObjectHelper.getBooleanProperty(background, "standart", false));
                if (isStandard) {
                    color = getStyleSafe().getIntProperty("window_background", 0);
                    frame = ScriptableObjectHelper.createEmpty();
                }

                if (frame != null) {
                    ScriptableObject frameDrawing = ScriptableObjectHelper.createEmpty();
                    frameDrawing.put("type", frameDrawing, "frame");
                    frameDrawing.put("x", frameDrawing, 0);
                    frameDrawing.put("y", frameDrawing, 0);
                    frameDrawing.put("width", frameDrawing, loc.getWindowWidth());
                    frameDrawing.put("height", frameDrawing, loc.getWindowHeight());
                    frameDrawing.put("scale", frameDrawing, ScriptableObjectHelper.getFloatProperty(frame, "scale", 3));
                    frameDrawing.put("bitmap", frameDrawing, ScriptableObjectHelper.getStringProperty(frame, "bitmap",
                            getStyleSafe().getBinding("frame", "style:frame_background_border")));
                    mainWinContent.insertDrawing(frameDrawing);
                }

                if (bitmap != null) {
                    Bitmap bmp = TextureSource.instance.getSafe(bitmap);
                    float scale = 1000 / bmp.getWidth();

                    ScriptableObject imageDrawing = ScriptableObjectHelper.createEmpty();
                    imageDrawing.put("type", imageDrawing, "bitmap");
                    imageDrawing.put("x", imageDrawing, 0);
                    imageDrawing.put("y", imageDrawing, 0);
                    imageDrawing.put("scale", imageDrawing, scale);
                    imageDrawing.put("bitmap", imageDrawing, bitmap);
                    mainWinContent.insertDrawing(imageDrawing);
                }

                ScriptableObject colorDrawing = ScriptableObjectHelper.createEmpty();
                colorDrawing.put("type", colorDrawing, "color");
                colorDrawing.put("color", colorDrawing, color);
                mainWinContent.insertDrawing(colorDrawing);
            }

            ScriptableObject header = ScriptableObjectHelper.getScriptableObjectProperty(standardContent, "header",
                    null);
            if (header != null && !ScriptableObjectHelper.getBooleanProperty(header, "hideShadow", false)) {
                ScriptableObject shadow = ScriptableObjectHelper.createEmpty();
                shadow.put("type", shadow, "bitmap");
                shadow.put("x", shadow, 0);
                shadow.put("y", shadow, 15);
                shadow.put("scale", shadow, 2);
                shadow.put("bitmap", shadow, "_standart_header_shadow");
                mainWinContent.addDrawing(shadow);
            }
        }

        mainWinContent.setLocation(loc);

        // setup window
        addWindow("main", mainWinContent.getContent());
        moveOnTop("inventory");
        moveOnTop("content");
        moveOnTop("header");
    }

    private void setupHeader() {
        paddingTop = 0;
        removeWindow("header"); // cleanup

        if (standardContent == null) {
            return;
        }

        ScriptableObject header = ScriptableObjectHelper.getScriptableObjectProperty(standardContent, "header", null);
        if (header == null) {
            return;
        }

        WindowContentAdapter content = new WindowContentAdapter();

        int height = ScriptableObjectHelper.getIntProperty(header, "height",
                ScriptableObjectHelper.getIntProperty(header, "width", 80));
        paddingTop = height - 20;

        UIWindowLocation location = new UIWindowLocation();
        location.set(0, 0, 1000, height);
        content.setLocation(location);

        String frameName = ScriptableObjectHelper.getStringProperty(header, "frame",
                getStyleSafe().getBinding("headerFrame", "style:frame_background_border"));
        int frameColor = ScriptableObjectHelper.getIntProperty(header, "color",
                getStyleSafe().getIntProperty("header_background", 0));

        ScriptableObject transparentBg = ScriptableObjectHelper.createEmpty();
        transparentBg.put("type", transparentBg, "color");
        transparentBg.put("color", transparentBg, Color.argb(0, 0, 0, 0));
        content.addDrawing(transparentBg);

        ScriptableObject frame = ScriptableObjectHelper.createEmpty();
        frame.put("type", frame, "frame");
        frame.put("x", frame, 0);
        frame.put("y", frame, 0);
        frame.put("width", frame, 1000);
        frame.put("height", frame, height);
        frame.put("bitmap", frame, frameName);
        frame.put("color", frame, frameColor);
        frame.put("scale", frame, 3);
        content.addDrawing(frame);

        ScriptableObject textDescr = ScriptableObjectHelper.getScriptableObjectProperty(header, "text", null);
        if (textDescr != null) {
            ScriptableObject text = ScriptableObjectHelper.createEmpty();
            text.put("type", text, "text");
            text.put("x", text, 500);
            text.put("y", text, height * .5f);
            text.put("text", text, ScriptableObjectHelper.getStringProperty(textDescr, "text", "No Title"));

            ScriptableObject font = ScriptableObjectHelper.getScriptableObjectProperty(textDescr, "font", textDescr);
            font.put("align", font, Font.ALIGN_CENTER);
            font.put("size", font, ScriptableObjectHelper.getProperty(font, "size", height * .25f));
            font.put("color", font, ScriptableObjectHelper.getProperty(font, "color",
                    getStyleSafe().getIntProperty("default_font_color", Color.BLACK)));
            font.put("shadow", font, ScriptableObjectHelper.getProperty(font, "shadow",
                    getStyleSafe().getFloatProperty("default_font_shadow", 0)));
            text.put("font", text, font);

            content.addDrawing(text);
        }

        boolean isButtonHidden = ScriptableObjectHelper.getBooleanProperty(header, "hideButton", false);
        if (!isButtonHidden) {
            ScriptableObject button = ScriptableObjectHelper.createEmpty();
            button.put("type", button, "closeButton");
            button.put("x", button, 994 - height * .75);
            button.put("y", button, 15);
            button.put("scale", button, height / 18 * .75);
            button.put("bitmap", button, getStyleSafe().getBitmapName("style:close_button_up"));
            button.put("bitmap2", button, getStyleSafe().getBitmapName("style:close_button_down"));
            content.addElement("default-close-button", button);
        }

        addWindow("header", content.getContent()).setDynamic(false);
    }

    private void setupInventory() {
        paddingLeft = 0;
        removeWindow("inventory"); // cleanup

        if (standardContent == null) {
            return;
        }

        ScriptableObject inventory = ScriptableObjectHelper.getScriptableObjectProperty(standardContent, "inventory",
                null);
        if (inventory == null) {
            return;
        }

        int width = ScriptableObjectHelper.getIntProperty(inventory, "width", 300);
        int padding = ScriptableObjectHelper.getIntProperty(inventory, "padding", 20);

        WindowContentAdapter content = new WindowContentAdapter();

        UIWindowLocation loc = new UIWindowLocation();
        loc.setPadding(UIWindowLocation.PADDING_LEFT, paddingLeft + padding);
        loc.setPadding(UIWindowLocation.PADDING_TOP, paddingTop + padding + 20);
        loc.setPadding(UIWindowLocation.PADDING_BOTTOM, padding);
        loc.width = loc.scrollX = width;
        loc.scrollY = (int) Math.max(loc.height, width * 2.25f);
        content.setLocation(loc);

        ScriptableObject background = ScriptableObjectHelper.createEmpty();
        background.put("type", background, "color");
        background.put("color", background, Color.BLACK);
        content.addDrawing(background);

        int slotSize = 250;
        for (int i = 0; i < 36; i++) {
            ScriptableObject slot = ScriptableObjectHelper.createEmpty();
            slot.put("type", slot, "invSlot");
            slot.put("x", slot, (i % 4) * slotSize);
            slot.put("y", slot, (i / 4) * slotSize);
            slot.put("size", slot, slotSize + 1);
            slot.put("index", slot, i);
            content.addElement("__invSlot" + i, slot);
        }

        UIWindow win = addWindow("inventory", content.getContent());
        // addWindowInstance("inventory", win);
        win.setDynamic(false);
        win.setInventoryNeeded(true);
    }

    @Override
    public ScriptableObject getContent() {
        return content;
    }

    public UIStyle getStyleSafe() {
        UIStyle style = super.getStyle();
        return style != null ? style : UIStyle.DEFAULT;
    }

    public void setContent(ScriptableObject content) {
        this.content = content;
        this.standardContent = ScriptableObjectHelper.getScriptableObjectProperty(content, "standard",
                ScriptableObjectHelper.getScriptableObjectProperty(content, "standart", null));

        ScriptableObject style = ScriptableObjectHelper.getScriptableObjectProperty(content, "style",
                ScriptableObjectHelper.getScriptableObjectProperty(content, "params", null));
        if (style != null) {
            setStyle(new UIStyle(style));
        }

        setupHeader();
        setupInventory();
        setupMainWindow();
    }
}
