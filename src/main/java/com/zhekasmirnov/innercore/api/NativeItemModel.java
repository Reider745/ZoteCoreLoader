package com.zhekasmirnov.innercore.api;

import android.util.Pair;
import cn.nukkit.block.Block;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.ICRender;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.api.unlimited.BlockVariant;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

    // mesh model
    private boolean isWorldModelDirty = false;

    // 
    private String mCacheKey = null;
    private String mCacheGroup = null;
    private String mCachePath = null;

    private String mItemTexturePath = null;
    private boolean mIsItemTexturePathForced = false;


    protected NativeItemModel(long pointer, int id, int data) {
        this.pointer = pointer;
        this.id = id;
        this.data = data;
        shaderUniformSet = new NativeShaderUniformSet(nativeGetShaderUniformSet(pointer));
    }

    protected NativeItemModel() {
        pointer = nativeConstructStandalone();
        id = data = 0;
        shaderUniformSet = null;
    }

    @JSStaticFunction
    public static NativeItemModel getFor(int id, int data) {
        long pointer = nativeGetFor(id, data);
        NativeItemModel model = modelByPointer.get(pointer);
        if (model == null) {
            model = new NativeItemModel(pointer, id, data);
            modelByPointer.put(pointer, model);
        }
        return model;
    };

    @JSStaticFunction
    public static NativeItemModel getForWithFallback(int id, int data) {
        return getFor(id, 0);

    };

    @JSStaticFunction
    public static NativeItemModel newStandalone() {
        NativeItemModel model = new NativeItemModel();
        modelByPointer.put(model.pointer, model);
        return model;
    }

    @JSStaticFunction
    public static void setCurrentCacheGroup(String name, String lock) {
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
        int releasedBytes = 0;
        int releasedCount = 0;
        for (NativeItemModel model : modelByPointer.values()) {
            if (releasedBytes >= bytes) {
                break;
            }

        }
        ICLog.d("ItemModels", "released " + releasedCount + " of total " + modelByPointer.size() + " cached item icons (" + releasedBytes + " of required " + bytes + " bytes)");
    }
    public void updateForBlockVariant(BlockVariant variant){

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
        return !isOccupied && nativeIsEmpty(pointer);
    }

    public boolean overridesHand() {
        return nativeOverridesHand(pointer);
    }

    public boolean overridesUi() {
        return nativeOverridesUi(pointer);
    }

    public NativeShaderUniformSet getShaderUniforms() {
        return shaderUniformSet;
    }

    public NativeItemModel setSpriteUiRender(boolean isSprite) {
        nativeSetSpriteInUi(pointer, isSprite);
        return this;
    }

    public NativeItemModel setSpriteHandRender(boolean isSprite) {
        nativeSetSpriteInHand(pointer, isSprite);
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
        nativeSetHandMesh(pointer, mesh != null ? mesh.getPtr() : 0);
        nativeSetHandBlockRender(pointer, 0);
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
        nativeSetHandBlockRender(pointer, blockRenderer != null ? blockRenderer.getPtr() : 0);
        nativeSetHandMesh(pointer, 0);
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
        nativeSetHandBlockRender(pointer, blockModel != null ? blockModel.pointer : 0);
        nativeSetHandMesh(pointer, 0);
        mWorldBlockModel = blockModel;
        mWorldRenderMesh = null;
        mWorldIcRender = null;
        isWorldModelDirty = true;
        return this;
    }

    public NativeItemModel setHandTexture(String texture) {
        nativeSetHandTexture(pointer, texture);
        mWorldTextureName = texture;
        return this;
    }

    public NativeItemModel setHandMaterial(String material) {
        nativeSetHandMaterial(pointer, material);
        mWorldMaterialName = material;
        if (mWorldGlintMaterialName == null) {
            setHandGlintMaterial(material);
        }
        return this;
    }

    public NativeItemModel setHandGlintMaterial(String material) {
        nativeSetHandGlintMaterial(pointer, material);
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
        nativeSetUiMesh(pointer, mesh != null ? mesh.getPtr() : 0);
        nativeSetUiBlockRender(pointer, 0);
        mUiRenderMesh = mesh;
        mUiBlockModel = null;
        mUiIcRender = null;
        isUiModelDirty = true;
        return this;
    }
    
    public NativeItemModel setUiModel(NativeICRender.Model blockRenderer, String material) {
        setUiModel(blockRenderer);
        setUiMaterial(material);
        return this;
    }
    
    public NativeItemModel setUiModel(NativeICRender.Model blockRenderer) {
        nativeSetUiBlockRender(pointer, blockRenderer != null ? blockRenderer.getPtr() : 0);
        nativeSetUiMesh(pointer, 0);
        mUiIcRender = blockRenderer;
        mUiRenderMesh = null;
        mUiBlockModel = null;
        isUiModelDirty = true;

        return this;
    }
    
    public NativeItemModel setUiModel(NativeBlockModel blockModel, String material) {
        setUiModel(blockModel);
        setUiMaterial(material);
        return this;
    }
    
    public NativeItemModel setUiModel(NativeBlockModel blockModel) {
        nativeSetUiBlockRender(pointer, blockModel != null ? blockModel.pointer : 0);
        nativeSetUiMesh(pointer, 0);
        mUiBlockModel = blockModel;
        mUiRenderMesh = null;
        mUiIcRender = null;
        isUiModelDirty = true;
        return this;
    }

    public NativeItemModel setUiTexture(String texture) {
        nativeSetUiTexture(pointer, texture);
        mUiTextureName = texture;
        return this;
    }

    public NativeItemModel setUiMaterial(String material) {
        nativeSetUiMaterial(pointer, material);
        mUiMaterialName = material;
        if (mUiGlintMaterialName == null) {
            setUiGlintMaterial(material);
        }
        return this;
    }

    public NativeItemModel setUiGlintMaterial(String material) {
        nativeSetUiGlintMaterial(pointer, material);
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


    private boolean isSpriteMeshDirty = false;
    private NativeRenderMesh mSpriteMesh = null;

    private void addBoxToSpriteMesh(NativeRenderMesh mesh, float x, float y, float z, float sx, float sy, float sz, float u, float v) {
        sx /= 2;
        sy /= 2;
        sz /= 2;
        
        mesh.setNormal(0, -1, 0);
        mesh.addVertex(x + sx, y - sy, z - sz, u, v);
        mesh.addVertex(x + sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z - sz, u, v);
        mesh.addVertex(x + sx, y - sy, z - sz, u, v);

        mesh.setNormal(0, 1, 0);
        mesh.addVertex(x + sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z + sz, u, v);
        mesh.addVertex(x - sx, y + sy, z + sz, u, v);
        mesh.addVertex(x - sx, y + sy, z + sz, u, v);
        mesh.addVertex(x - sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z - sz, u, v);

        mesh.setNormal(-1, 0, 0);
        mesh.addVertex(x - sx, y - sy, z - sz, u, v);
        mesh.addVertex(x - sx, y + sy, z - sz, u, v);
        mesh.addVertex(x - sx, y + sy, z + sz, u, v);
        mesh.addVertex(x - sx, y + sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z - sz, u, v);

        mesh.setNormal(1, 0, 0);
        mesh.addVertex(x + sx, y - sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z + sz, u, v);
        mesh.addVertex(x + sx, y + sy, z + sz, u, v);
        mesh.addVertex(x + sx, y - sy, z + sz, u, v);
        mesh.addVertex(x + sx, y - sy, z - sz, u, v);

        mesh.setNormal(0, 0, -1);
        mesh.addVertex(x - sx, y - sy, z - sz, u, v);
        mesh.addVertex(x - sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y + sy, z - sz, u, v);
        mesh.addVertex(x + sx, y - sy, z - sz, u, v);
        mesh.addVertex(x - sx, y - sy, z - sz, u, v);

        mesh.setNormal(0, 0, 1);
        mesh.addVertex(x - sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y + sy, z + sz, u, v);
        mesh.addVertex(x + sx, y + sy, z + sz, u, v);
        mesh.addVertex(x + sx, y + sy, z + sz, u, v);
        mesh.addVertex(x + sx, y - sy, z + sz, u, v);
        mesh.addVertex(x - sx, y - sy, z + sz, u, v);
    }

    public String getMeshTextureName() {
        return isSpriteInWorld() ? (mItemTexturePath != null ? mItemTexturePath : "atlas::terrain") : "atlas::terrain";
    }


    public NativeItemModel setItemTexturePath(String path) {
        if (!path.endsWith(".png")) {
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
        isIconBitmapDirty = true;

        mIsItemTexturePathForced = false;
        return this;
    }

    public NativeItemModel setModUiSpritePath(String path) {
        isIconBitmapDirty = true;
        mIsItemTexturePathForced = true;
        setItemTexturePath(path);
        return this;
    }

    public NativeItemModel setModUiSpriteName(String name, int index) {
        isIconBitmapDirty = true;
        mIsItemTexturePathForced = true;
        setItemTexture(name, index);
        return this;
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
        nativeSetHasOverrideCallback(pointer, overrideCallback != null);
        return this;
    }

    public boolean isUsingOverrideCallback() {
        return mModelOverrideCallback != null;
    }


    public boolean isLazyLoading = false;

    private final Object iconLock = new Object();
    private boolean isIconBitmapDirty = false;






    public String getCacheKey() {
        return mCacheKey;
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

    @JSStaticFunction
    public static String getItemMeshTextureFor(int id, int data) {
        return getForWithFallback(id, data).getMeshTextureName();
    }

    private static native long nativeConstructStandalone();
    private static native long nativeGetFor(int id, int data);
    private static native long nativeGetShaderUniformSet(long pointer);
    private static native boolean nativeIsEmpty(long pointer);
    private static native boolean nativeOverridesHand(long pointer);
    private static native boolean nativeOverridesUi(long pointer);
    private static native boolean nativeSetHasOverrideCallback(long pointer, boolean value);

    private static native long nativeSetSpriteInUi(long pointer, boolean isSprite);
    private static native long nativeSetSpriteInHand(long pointer, boolean isSprite);
    private static native long nativeSetHandMesh(long pointer, long mesh);
    private static native long nativeSetHandBlockRender(long pointer, long render);
    private static native long nativeSetHandTexture(long pointer, String name);
    private static native long nativeSetHandMaterial(long pointer, String name);
    private static native long nativeSetHandGlintMaterial(long pointer, String name);
    private static native long nativeSetUiMesh(long pointer, long mesh);
    private static native long nativeSetUiBlockRender(long pointer, long render);
    private static native long nativeSetUiTexture(long pointer, String name);
    private static native long nativeSetUiMaterial(long pointer, String name);
    private static native long nativeSetUiGlintMaterial(long pointer, String name);
}
