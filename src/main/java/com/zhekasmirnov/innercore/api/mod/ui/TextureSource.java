package com.zhekasmirnov.innercore.api.mod.ui;

import android.graphics.Bitmap;
import java.io.File;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class TextureSource {
    public static final TextureSource instance = new TextureSource();

    public void loadAllStandartAssets() {
    }

    public void put(String name, Bitmap bmp) {
    }

    public Bitmap get(String name) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getSafe(String name) {
        return get(name);
    }

    public void loadFile(File file, String namePrefix) {
    }

    public void loadAsset(String name) {
    }

    public void loadDirectory(File dir) {
    }

    public void loadDirectory(File dir, String namePrefix) {
    }
}
