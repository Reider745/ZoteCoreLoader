package com.zhekasmirnov.innercore.api.mod.ui.icon;

import android.graphics.Bitmap;
import com.zhekasmirnov.innercore.api.NativeRenderer;
import com.zhekasmirnov.innercore.api.mod.ui.GuiBlockModel;
import com.zhekasmirnov.innercore.api.unlimited.BlockShape;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.util.HashMap;

/**
 * Created by zheka on 04.09.2017.
 */

@Deprecated(since = "Zote")
public class ItemModels {
    public static final String CACHE_DIR = FileTools.DIR_WORK + "cache/item-models/";

    public static final String ATLAS_NAME = "textures/entity/camera_tripod";
    public static final String ATLAS_PATH = ResourcePackManager.getSourcePath() + ATLAS_NAME + ".png";

    public static class ModelInfo {
        private boolean isSprite;
        private String spritePath;
        private GuiBlockModel model;
        private boolean isCustomized = false;

        private ModelInfo(String idKey) {
            modelInfoMap.put(idKey, this);
        }

        private void setSpritePath(String spritePath) {
            this.spritePath = spritePath;
        }

        private void setModel(GuiBlockModel model) {
            this.model = model;
        }

        public GuiBlockModel getModel() {
            return model;
        }

        public boolean isSprite() {
            return isSprite;
        }

        public boolean isCustomized() {
            return isCustomized;
        }

        private void setSprite(boolean sprite) {
            isSprite = sprite;
        }

        public String getSkinName() {
            return isSprite ? spritePath : ATLAS_NAME;
        }

        public Bitmap getCache() {
            return Bitmap.getSingletonInternalProxy();
        }

        public void writeToCache(Bitmap bmp) {
        }

        public void setShape(BlockShape shape) {
        }
    }

    private static HashMap<String, ModelInfo> modelInfoMap = new HashMap<>();

    public static ModelInfo prepareModelInfo(String idKey) {
        ModelInfo info = modelInfoMap.get(idKey);
        if (info != null) {
            return info;
        }

        return info = new ModelInfo(idKey);
    }

    public static ModelInfo prepareModelInfo(String idKey, String spitePath) {
        ModelInfo info = prepareModelInfo(idKey);

        info.setSprite(true);
        info.setSpritePath(spitePath);
        return info;
    }

    public static ModelInfo prepareModelInfo(String idKey, GuiBlockModel model) {
        ModelInfo info = prepareModelInfo(idKey);

        info.setSprite(false);
        info.setModel(model);
        return info;
    }

    public static class AtlasUnit {
        public final int pos;
        public final int size;
        public final Bitmap bitmap;

        public AtlasUnit(Bitmap bitmap, int pos, int size) {
            this.pos = pos;
            this.size = size;
            this.bitmap = bitmap;
        }
    }

    public static int createAtlasLink(String formattedName, Bitmap bmp) {
        return -1;
    }

    public static int createAtlasLink(String path) {
        return -1;
    }

    static void createAtlas() {
    }

    public static AtlasUnit getAtlasUnit(String iconName) {
        return null;
    }

    public static void init() {
    }

    public static int getAtlasWidth() {
        return 1;
    }

    public static int getAtlasHeight() {
        return 1;
    }

    public static ModelInfo getModelInfo(String idKey) {
        return modelInfoMap.get(idKey);
    }

    public static ModelInfo getModelInfo(int id, int data) {
        if (modelInfoMap.containsKey(id + ":" + data)) {
            return modelInfoMap.get(id + ":" + data);
        }
        return modelInfoMap.get(id + "");
    }

    public static void updateBlockShape(int id, int data, BlockShape shape) {
    }

    public static void setCustomUiModel(int id, int data, GuiBlockModel model) {
    }

    private static final NativeRenderer.RenderPool renderPool = new NativeRenderer.RenderPool();

    public static NativeRenderer.Renderer getItemOrBlockModel(int id, int count, int data, double scale, double rX,
            double rY, double rZ, boolean randomize) {
        return renderPool.getRender();
    }
}
