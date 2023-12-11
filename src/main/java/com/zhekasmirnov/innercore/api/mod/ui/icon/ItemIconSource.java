package com.zhekasmirnov.innercore.api.mod.ui.icon;

import android.graphics.*;

/**
 * Created by zheka on 31.07.2017.
 */

public class ItemIconSource {
    public static final ItemIconSource instance = new ItemIconSource();

    public static void init() {
    }

    private boolean isGlintAnimationEnabled = true;

    public static boolean isGlintAnimationEnabled() {
        return instance.isGlintAnimationEnabled;
    }

    private ItemIconSource() {
    }

    public void registerIcon(int id, String name) {
        registerIcon(id, -1, name);
    }

    public void registerIcon(int id, int data, String name) {
    }

    public void registerIcon(int id, Bitmap bmp) {
        registerIcon(id, -1, bmp);
    }

    public void registerIcon(int id, int data, Bitmap bmp) {
    }

    public Bitmap checkoutIcon(String _name) {
        return Bitmap.getSingletonInternalProxy();
    }

    public String getIconName(int id, int data) {
        return id + "_" + data;
    }

    public String getIconPath(int id, int data) {
        return getIconName(id, data);
    }

    public Bitmap getNullableIcon(int id, int data) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getIcon(int id, int data, Bitmap icon, boolean enableCache) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getScaledIcon(Bitmap originIcon, int id, int data, int size, int glint) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getScaledIcon(int id, int data, int size) {
        return getScaledIcon(null, id, data, size, -1);
    }

    public static void generateAllModItemModels() {
    }
}
