package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.util.HashMap;

/**
 * Created by zheka on 31.07.2017.
 */

public class TextureSource {
    public static final TextureSource instance;

    static {
        LoadingUI.setTip("Loading UI Resources");
        instance = new TextureSource();
        instance.loadAllStandartAssets();
        LoadingUI.setTip("");
    }

    public void loadAllStandartAssets() {
        loadAsset("innercore/ui/missing_texture.png");
        loadAsset("innercore/ui/missing_mod_icon.png");

        String[] uiResources = FileTools.listAssets("innercore/ui");
        for (String resource : uiResources) {
            loadAsset("innercore/ui/" + resource);
        }

        if (get("missing_texture") == null) {
            //put("missing_texture", Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888));
        }
    }

    private HashMap<String, Object> loadedTextures = new HashMap<>();


    public void put(String name, Object bmp) {
        loadedTextures.put(name, bmp);
    }

    public Object get(String name) {
        return loadedTextures.get(name);
    }

    public Object getSafe(String name) {
        Object bmp = get(name);
        if (bmp == null)
            return get("missing_texture");
        return bmp;
    }

    public void loadFile(File file, String namePrefix) {
        if (namePrefix == null)
            namePrefix = "";

        if (file.exists()) {
            String name = file.getName();
            name = namePrefix + name.substring(0, name.lastIndexOf("."));
            Object bmp = FileTools.readFileAsBitmap(file.getAbsolutePath());

            loadedTextures.put(name, bmp);
        }
    }

    public void loadAsset(String name) {
        Object bmp = null;
        bmp = FileTools.getAssetAsBitmap(name);
        if (bmp != null) {
            name = name.substring(name.lastIndexOf("/") + 1);
            name = name.substring(0, name.lastIndexOf("."));
            loadedTextures.put(name, bmp);
        }
    }

    public void loadDirectory(File dir) {
        loadDirectory(dir, "");
    }

    public void loadDirectory(File dir, String namePrefix) {
        if (!dir.exists() || !dir.isDirectory()) {

            return;
        }

        if (namePrefix.length() > 0) {
            namePrefix += ".";
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                loadDirectory(file, namePrefix + file.getName());
            }
            else if (file.getName().endsWith(".png")) {
                loadFile(file, namePrefix);
            }
        }
    }
}
