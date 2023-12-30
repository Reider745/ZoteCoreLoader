package com.zhekasmirnov.innercore.api;

import android.graphics.Bitmap;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.ICRender;
import com.zhekasmirnov.innercore.api.mod.ui.GuiBlockModel;
import com.zhekasmirnov.innercore.api.mod.ui.GuiRenderMesh;
import com.zhekasmirnov.innercore.api.mod.ui.ItemModelCacheManager;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.api.unlimited.BlockVariant;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class NativeItemModel {
    private static final HashMap<Long, NativeItemModel> modelByPointer = new HashMap<>();

    // globals
    public final long pointer;
    public final int id, data;
    public boolean isOccupied = false;
    public final NativeShaderUniformSet shaderUniformSet;

    // ui model variants
    private NativeRenderMesh mUiRenderMesh = null;
    private ICRender.Model mUiIcRender = null;
    private NativeBlockModel mUiBlockModel = null;
    private String mUiTextureName = null;
    private String mUiMaterialName = null;
    private String mUiGlintMaterialName = null;

    // world model variants
    private NativeRenderMesh mWorldRenderMesh = null;
    private ICRender.Model mWorldIcRender = null;
    private NativeBlockModel mWorldBlockModel = null;
    private String mWorldTextureName = null;
    private String mWorldMaterialName = null;
    private String mWorldGlintMaterialName = null;

    // ui model
    private boolean isUiModelDirty = false;
    private GuiBlockModel mGuiBlockModel = null;
    private GuiRenderMesh mGuiRenderMesh = null;

    // mesh model
    private boolean isWorldModelDirty = false;
    private GuiBlockModel mWorldCompiledBlockModel = null;

    //
    private String mCacheKey = null;

    private String mItemTexturePath = null;

    protected NativeItemModel(long pointer, int id, int data) {
        this.pointer = pointer;
        this.id = id;
        this.data = data;
        shaderUniformSet = new NativeShaderUniformSet(0);
    }

    protected NativeItemModel() {
        pointer = id = data = 0;
        shaderUniformSet = null;
    }

    public void updateCacheGroupToCurrent() {
    }

    @JSStaticFunction
    public static NativeItemModel getFor(int id, int data) {
        NativeItemModel model = modelByPointer.get(0L);
        if (model == null) {
            model = new NativeItemModel(0, id, data);
            modelByPointer.put(0L, model);
        }
        return model;
    };

    @JSStaticFunction
    public static NativeItemModel getForWithFallback(int id, int data) {
        NativeItemModel model = getFor(id, data);
        if (model.isNonExisting()) {
            return getFor(id, 0);
        }
        return model;
    };

    @JSStaticFunction
    public static NativeItemModel newStandalone() {
        NativeItemModel model = new NativeItemModel();
        modelByPointer.put(model.pointer, model);
        return model;
    }

    @JSStaticFunction
    public static void setCurrentCacheGroup(String name, String lock) {
        ItemModelCacheManager.getSingleton().setCurrentCacheGroup(name, lock);
    }

    public static NativeItemModel getByPointer(long pointer) {
        return modelByPointer.get(pointer);
    }

    @JSStaticFunction
    public static Collection<NativeItemModel> getAllModels() {
        return modelByPointer.values();
    }

    @JSStaticFunction
    public static void tryReleaseModelBitmapsOnLowMemory(int bytes) {
    }

    public NativeItemModel occupy() {
        isOccupied = true;
        return this;
    }

    public boolean isSpriteInUi() {
        return mItemTexturePath != null && isEmptyInUi();
    }

    public boolean isEmptyInUi() {
        return mUiBlockModel == null && mUiIcRender == null && mUiRenderMesh == null;
    }

    public boolean isSpriteInWorld() {
        return isEmptyInWorld();
    }

    public boolean isEmptyInWorld() {
        return mUiBlockModel == null && mUiIcRender == null && mUiRenderMesh == null;
    }

    public boolean isEmpty() {
        return !isOccupied;
    }

    public boolean isNonExisting() {
        return !isOccupied && isEmptyInUi() && isEmptyInWorld() && mItemTexturePath == null && mGuiBlockModel == null
                && mWorldCompiledBlockModel == null;
    }

    public boolean overridesHand() {
        return false;
    }

    public boolean overridesUi() {
        return false;
    }

    public NativeShaderUniformSet getShaderUniforms() {
        return shaderUniformSet;
    }

    public NativeItemModel setSpriteUiRender(boolean isSprite) {
        return this;
    }

    public NativeItemModel setSpriteHandRender(boolean isSprite) {
        return this;
    }

    // Hand

    public NativeItemModel setHandModel(NativeRenderMesh mesh, String texture, String material) {
        setHandModel(mesh);
        setHandTexture(texture);
        setHandMaterial(material);
        return this;
    }

    public NativeItemModel setHandModel(NativeRenderMesh mesh, String texture) {
        setHandModel(mesh);
        setHandTexture(texture);
        return this;
    }

    public NativeItemModel setHandModel(NativeRenderMesh mesh) {
        if (mesh != null) {
            mesh.invalidate();
        }
        mWorldRenderMesh = mesh;
        mWorldBlockModel = null;
        mWorldIcRender = null;
        isWorldModelDirty = true;
        return this;
    }

    public NativeItemModel setHandModel(NativeICRender.Model blockRenderer, String material) {
        setHandModel(blockRenderer);
        setHandMaterial(material);
        return this;
    }

    public NativeItemModel setHandModel(NativeICRender.Model blockRenderer) {
        mWorldIcRender = blockRenderer;
        mWorldRenderMesh = null;
        mWorldBlockModel = null;
        isWorldModelDirty = true;
        return this;
    }

    public NativeItemModel setHandModel(NativeBlockModel blockModel, String material) {
        setHandModel(blockModel);
        setHandMaterial(material);
        return this;
    }

    public NativeItemModel setHandModel(NativeBlockModel blockModel) {
        mWorldBlockModel = blockModel;
        mWorldRenderMesh = null;
        mWorldIcRender = null;
        isWorldModelDirty = true;
        return this;
    }

    public NativeItemModel setHandTexture(String texture) {
        mWorldTextureName = texture;
        return this;
    }

    public NativeItemModel setHandMaterial(String material) {
        mWorldMaterialName = material;
        if (mWorldGlintMaterialName == null) {
            setHandGlintMaterial(material);
        }
        return this;
    }

    public NativeItemModel setHandGlintMaterial(String material) {
        mWorldGlintMaterialName = material;
        return this;
    }

    public String getWorldTextureName() {
        if (mWorldTextureName != null) {
            return mWorldTextureName;
        }
        return isSpriteInWorld() ? (mItemTexturePath != null ? mItemTexturePath : "atlas::terrain") : "atlas::terrain";
    }

    public String getWorldMaterialName() {
        return mWorldMaterialName != null ? mWorldMaterialName : "entity_alphatest_custom";
    }

    public String getWorldGlintMaterialName() {
        return mWorldGlintMaterialName != null ? mWorldGlintMaterialName : "entity_alphatest_custom_glint";
    }

    // UI

    public NativeItemModel setUiModel(NativeRenderMesh mesh, String texture, String material) {
        setUiModel(mesh);
        setUiTexture(texture);
        setUiMaterial(material);
        return this;
    }

    public NativeItemModel setUiModel(NativeRenderMesh mesh, String texture) {
        setUiModel(mesh);
        setUiTexture(texture);
        return this;
    }

    public NativeItemModel setUiModel(NativeRenderMesh mesh) {
        mUiRenderMesh = mesh;
        mUiBlockModel = null;
        mUiIcRender = null;
        isUiModelDirty = true;
        if (mCacheKey == null) {
            setCacheKey("item-model");
        }
        return this;
    }

    public NativeItemModel setUiModel(NativeICRender.Model blockRenderer, String material) {
        setUiModel(blockRenderer);
        setUiMaterial(material);
        return this;
    }

    public NativeItemModel setUiModel(NativeICRender.Model blockRenderer) {
        mUiIcRender = blockRenderer;
        mUiRenderMesh = null;
        mUiBlockModel = null;
        isUiModelDirty = true;
        if (mCacheKey == null) {
            setCacheKey("item-model");
        }
        return this;
    }

    public NativeItemModel setUiModel(NativeBlockModel blockModel, String material) {
        setUiModel(blockModel);
        setUiMaterial(material);
        return this;
    }

    public NativeItemModel setUiModel(NativeBlockModel blockModel) {
        mUiBlockModel = blockModel;
        mUiRenderMesh = null;
        mUiIcRender = null;
        isUiModelDirty = true;
        if (mCacheKey == null) {
            setCacheKey("item-model");
        }
        return this;
    }

    public NativeItemModel setUiTexture(String texture) {
        mUiTextureName = texture;
        return this;
    }

    public NativeItemModel setUiMaterial(String material) {
        mUiMaterialName = material;
        if (mUiGlintMaterialName == null) {
            setUiGlintMaterial(material);
        }
        return this;
    }

    public NativeItemModel setUiGlintMaterial(String material) {
        mUiGlintMaterialName = material;
        return this;
    }

    public String getUiTextureName() {
        if (mUiTextureName != null) {
            return mUiTextureName;
        }
        return isSpriteInUi() ? (mItemTexturePath != null ? mItemTexturePath : "atlas::terrain") : "atlas::terrain";
    }

    public String getUiMaterialName() {
        return mUiMaterialName != null ? mUiMaterialName : "ui_custom_item";
    }

    public String getUiGlintMaterialName() {
        return mUiGlintMaterialName != null ? mUiGlintMaterialName : "ui_custom_item_glint";
    }

    // Both

    public NativeItemModel setModel(NativeRenderMesh mesh, String texture, String material) {
        return setHandModel(mesh, texture, material).setUiModel(mesh, texture, material);
    }

    public NativeItemModel setModel(NativeRenderMesh mesh, String texture) {
        return setHandModel(mesh, texture).setUiModel(mesh, texture);
    }

    public NativeItemModel setModel(NativeRenderMesh mesh) {
        return setHandModel(mesh).setUiModel(mesh);
    }

    public NativeItemModel setModel(NativeICRender.Model blockRenderer, String material) {
        return setHandModel(blockRenderer, material).setUiModel(blockRenderer, material);
    }

    public NativeItemModel setModel(NativeICRender.Model blockRenderer) {
        return setHandModel(blockRenderer).setUiModel(blockRenderer);
    }

    public NativeItemModel setModel(NativeBlockModel blockModel, String material) {
        return setHandModel(blockModel, material).setUiModel(blockModel, material);
    }

    public NativeItemModel setModel(NativeBlockModel blockModel) {
        return setHandModel(blockModel).setUiModel(blockModel);
    }

    public NativeItemModel setTexture(String texture) {
        return setHandTexture(texture).setUiTexture(texture);
    }

    public NativeItemModel setMaterial(String material) {
        return setHandMaterial(material).setUiMaterial(material);
    }

    public NativeItemModel setGlintMaterial(String material) {
        return setHandGlintMaterial(material).setUiGlintMaterial(material);
    }

    //

    public GuiBlockModel getGuiBlockModel() {
        if (mGuiBlockModel == null || isUiModelDirty) {
            if (mUiIcRender != null) {
                mGuiBlockModel = mUiIcRender.buildGuiModel(true);
            } else if (mUiBlockModel != null) {
                mGuiBlockModel = mUiBlockModel.buildGuiModel(true);
            } else {
                BlockVariant block = BlockRegistry.getBlockVariant(id, data);
                if (block != null) {
                    mGuiBlockModel = block.getGuiBlockModel();
                }
            }
            // if not mesh mode
            if (mUiRenderMesh == null) {
                isUiModelDirty = false;
            }
        }
        return mGuiBlockModel;
    }

    public GuiBlockModel getWorldBlockModel() {
        if (mWorldCompiledBlockModel == null || isWorldModelDirty) {
            if (mWorldIcRender != null) {
                mWorldCompiledBlockModel = mWorldIcRender.buildGuiModel(false);
            } else if (mWorldBlockModel != null) {
                mWorldCompiledBlockModel = mWorldBlockModel.buildGuiModel(false);
            } else {
                BlockVariant block = BlockRegistry.getBlockVariant(id, data);
                if (block != null) {
                    mWorldCompiledBlockModel = block.getGuiBlockModel();
                }
            }
            // if not mesh mode
            if (mWorldRenderMesh == null) {
                isWorldModelDirty = false;
            }
        }
        return mWorldCompiledBlockModel;
    }

    public GuiRenderMesh getGuiRenderMesh() {
        if (mGuiRenderMesh == null || isUiModelDirty) {
            if (mUiRenderMesh != null) {
                mGuiRenderMesh = mUiRenderMesh.newGuiRenderMesh();
                isUiModelDirty = false;
            }
        }
        return mGuiRenderMesh;
    }

    private boolean isSpriteMeshDirty = false;
    private NativeRenderMesh mSpriteMesh = null;

    public synchronized NativeRenderMesh getSpriteMesh() {
        if (mSpriteMesh == null || isSpriteMeshDirty) {
            if (mSpriteMesh != null) {
                mSpriteMesh.clear();
            } else {
                mSpriteMesh = new NativeRenderMesh();
            }
        }
        return mSpriteMesh;
    }

    public void addToMesh(NativeRenderMesh mesh, float x, float y, float z) {
    }

    public String getMeshTextureName() {
        return isSpriteInWorld() ? (mItemTexturePath != null ? mItemTexturePath : "atlas::terrain") : "atlas::terrain";
    }

    public NativeItemModel setItemTexturePath(String path) {
        if (path != null && !path.endsWith(".png")) {
            path += ".png";
        }
        mItemTexturePath = path;
        return this;
    }

    public NativeItemModel setItemTexture(String name, int id) {
        setItemTexturePath(ResourcePackManager.getItemTextureName(name, id));
        return this;
    }

    public NativeItemModel removeModUiSpriteTexture() {
        return this;
    }

    public NativeItemModel setModUiSpritePath(String path) {
        setItemTexturePath(path);
        return this;
    }

    public NativeItemModel setModUiSpriteName(String name, int index) {
        setItemTexture(name, index);
        return this;
    }

    public NativeItemModel setModUiSpriteBitmap(Bitmap bitmap) {
        return this;
    }

    public void setUiBlockModel(GuiBlockModel model) {
        mGuiBlockModel = model;
        mGuiRenderMesh = null;
        mItemTexturePath = null;
        isUiModelDirty = false;
    }

    public void setWorldBlockModel(GuiBlockModel model) {
        mWorldCompiledBlockModel = model;
        isWorldModelDirty = false;
    }

    public interface IOverrideCallback {
        NativeItemModel overrideModel(ItemInstance extra);
    };

    private IOverrideCallback mModelOverrideCallback = null;

    public NativeItemModel getModelForItemInstance(int id, int count, int data, NativeItemInstanceExtra extra) {
        if (mModelOverrideCallback != null) {
            return mModelOverrideCallback.overrideModel(new ItemInstance(id, count, data, extra));
        }
        return this;
    }

    public NativeItemModel setModelOverrideCallback(IOverrideCallback overrideCallback) {
        mModelOverrideCallback = overrideCallback;
        return this;
    }

    public boolean isUsingOverrideCallback() {
        return mModelOverrideCallback != null;
    }

    public boolean isLazyLoading = false;

    public void releaseIcon() {
    }

    public void reloadIconIfDirty() {
    }

    public Bitmap getIconBitmap() {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap getIconBitmapNoReload() {
        return Bitmap.getSingletonInternalProxy();
    }

    public void reloadIcon(boolean straightToCache) {
    }

    public void reloadIcon() {
        reloadIcon(false);
    }

    public interface IIconRebuildListener {
        public void onIconRebuild(NativeItemModel model, Bitmap newIcon);
    }

    public Bitmap queueReload(IIconRebuildListener listener) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap queueReload() {
        return queueReload(null);
    }

    public void setCacheKey(String key) {
        mCacheKey = key;
    }

    public void setCacheGroup(String group) {
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public void updateForBlockVariant(BlockVariant variant) {
    }

    private static final ArrayList<NativeRenderMesh> itemModelMeshPool = new ArrayList<>();

    @JSStaticFunction
    public static NativeRenderMesh getEmptyMeshFromPool() {
        synchronized (itemModelMeshPool) {
            if (itemModelMeshPool.size() > 0) {
                NativeRenderMesh mesh = itemModelMeshPool.remove(0);
                mesh.clear();
                return mesh;
            } else {
                return new NativeRenderMesh();
            }
        }
    }

    @JSStaticFunction
    public static void releaseMesh(Object mesh) {
        synchronized (itemModelMeshPool) {
            itemModelMeshPool.add((NativeRenderMesh) Context.jsToJava(mesh, NativeRenderMesh.class));
        }
    }

    public NativeRenderMesh getItemRenderMesh(int count, boolean randomize) {
        return getEmptyMeshFromPool();
    }

    @JSStaticFunction
    public static NativeRenderMesh getItemRenderMeshFor(int id, int count, int data, boolean randomize) {
        return getForWithFallback(id, data).getItemRenderMesh(count, randomize);
    }

    @JSStaticFunction
    public static String getItemMeshTextureFor(int id, int data) {
        return getForWithFallback(id, data).getMeshTextureName();
    }
}
