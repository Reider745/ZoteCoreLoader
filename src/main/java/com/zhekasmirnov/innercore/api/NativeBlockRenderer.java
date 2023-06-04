package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.commontypes.FullBlock;
import com.zhekasmirnov.innercore.api.constants.ChatColor;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;

/**
 * Created by zheka on 07.08.2017.
 */

public class NativeBlockRenderer {

    public static void nativeSetCollisionShape(int id, int data, long collisionShape){

    }

    public static void nativeSetRaycastShape(int id, int data, long raycastShape){

    }

    public static void nativeEnableCustomRender(int id, int data){

    }

    public static void nativeDisableCustomRender(int id, int data){

    }

    public static void nativeEnableStaticModel(int id, int data, long icRender){

    }

    public static void nativeEnableCoordMapping(int id, int data, long icRender){

    }

    public static void nativeMapModelAtCoords(int x, int y, int z, long model){

    }

    public static void nativeMapICRenderAtCoords(int x, int y, int z, long icRender){

    }

    public static void nativeMapCollisionModelAtCoords(int dimension, int x, int y, int z, long model){

    }

    public static void nativeMapRaycastModelAtCoords(int dimension, int x, int y, int z, long model){

    }

    public static void nativeUnmapAtCoords(int x, int y, int z){

    }



    public static void renderBoxPtr(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, long block, int data, boolean b){

    }

    public static void renderBoxPtrHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2, long block, int data, boolean b){

    }

    public static void renderBoxId(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, int blockId, int data, boolean b){

    }

    public static void renderBoxIdHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2, int blockId, int data, boolean b){

    }

    public static void renderBoxTexture(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, String tex, int id){

    }

    public static void renderBoxTextureHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2, String tex, int id){

    }

    public static void renderBoxTextureSet(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5, int id5, String tex6, int id6){

    }

    public static void renderBoxTextureSetHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2, String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5, int id5, String tex6, int id6){

    }

    public static void renderBlock(long tess, int x, int y, int z, int blockId, int data, boolean b){

    }

    public static void renderBlockHere(long tess, int blockId, int data, boolean b){

    }

    public static void renderModel(long tess, int x, int y, int z, long model){

    }

    public static void renderModelHere(long tess, long model){

    }



    @JSStaticFunction
    public static void setCustomCollisionShape(int id, Object data, final Object _shape) {
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
                NativeBlockRenderer.nativeSetCollisionShape(id, data, shape != null ? shape.getPtr() : 0);
            }
        });
    }

    @JSStaticFunction
    public static void setCustomRaycastShape(int id, Object data, final Object _shape) {
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
                NativeBlockRenderer.nativeSetRaycastShape(id, data, shape != null ? shape.getPtr() : 0);
            }
        });
    }

    @JSStaticFunction
    public static void setCustomCollisionAndRaycastShape(int id, Object data, final Object _shape) {
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
                NativeBlockRenderer.nativeSetCollisionShape(id, data, shape != null ? shape.getPtr() : 0);
                NativeBlockRenderer.nativeSetRaycastShape(id, data, shape != null ? shape.getPtr() : 0);
            }
        });
    }

    @JSStaticFunction
    public static void enableCustomRender(int id, Object data) {
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                NativeBlockRenderer.nativeEnableCustomRender(id, data);
            }
        });
    }

    @JSStaticFunction
    public static void disableCustomRender(int id, Object data) {
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                NativeBlockRenderer.nativeDisableCustomRender(id, data);
            }
        });
    }

    private static void registerIcRenderItemModel(int id, int data, NativeICRender.Model icRender) {
        /*NativeItemModel model = NativeItemModel.getFor(id, data);
        if (model.isEmpty()) {
            model.setModel(icRender);
            if (model.getCacheKey() == null) {
                model.setCacheKey("icrender-modded");
            }
        }*/
    }

    @JSStaticFunction
    public static void setStaticICRender(int id, Object data, Object _icRender) {
        final NativeICRender.Model icRender = (NativeICRender.Model) Context.jsToJava(_icRender, NativeICRender.Model.class);
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            boolean isModelRegistered = false;
            @Override
            public void onIdDataIterated(int id, int data) {
                if (!isModelRegistered) {
                    isModelRegistered = true;
                    registerIcRenderItemModel(id, data, icRender);
                }
                NativeBlockRenderer.nativeEnableStaticModel(id, data, icRender.getPtr());
            }
        });
    }

    @JSStaticFunction
    public static void enableCoordMapping(int id, Object data, Object _icRender) {
        final NativeICRender.Model icRender = (NativeICRender.Model) Context.jsToJava(_icRender, NativeICRender.Model.class);
        NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
            boolean isModelRegistered = false;
            @Override
            public void onIdDataIterated(int id, int data) {
                if (!isModelRegistered) {
                    isModelRegistered = true;
                    registerIcRenderItemModel(id, data, icRender);
                }
                NativeBlockRenderer.nativeEnableCoordMapping(id, data, icRender.getPtr());
            }
        });
    }

    @JSStaticFunction
    public static void mapAtCoords(int x, int y, int z, Object _icRender, boolean preventRebuild) {
        /*final NativeICRender.Model icRender = (NativeICRender.Model) Context.jsToJava(_icRender, NativeICRender.Model.class);
        nativeMapICRenderAtCoords(x, y, z, icRender.getPtr());
        if (!preventRebuild) {
            NativeAPI.forceRenderRefresh(x, y, z, 0);
        }*/
    }

    @JSStaticFunction
    public static void unmapAtCoords(int x, int y, int z, boolean preventRebuild) {
        /*nativeUnmapAtCoords(x, y, z);
        if (!preventRebuild) {
            NativeAPI.forceRenderRefresh(x, y, z, 0);
        }*/
    }

    @JSStaticFunction
    public static void mapCollisionModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
        nativeMapCollisionModelAtCoords(dimension, x, y, z, shape != null ? shape.getPtr() : 0);
    }

    @JSStaticFunction
    public static void mapRaycastModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
        nativeMapRaycastModelAtCoords(dimension, x, y, z, shape != null ? shape.getPtr() : 0);
    }

    @JSStaticFunction
    public static void mapCollisionAndRaycastModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        NativeICRender.CollisionShape shape = (NativeICRender.CollisionShape) Context.jsToJava(_shape, NativeICRender.CollisionShape.class);
        nativeMapCollisionModelAtCoords(dimension, x, y, z, shape != null ? shape.getPtr() : 0);
        nativeMapRaycastModelAtCoords(dimension, x, y, z, shape != null ? shape.getPtr() : 0);
    }

    @JSStaticFunction
    public static void unmapCollisionModelAtCoords(int dimension, int x, int y, int z) {
        nativeMapCollisionModelAtCoords(dimension, x, y, z, 0);
    }

    @JSStaticFunction
    public static void unmapRaycastModelAtCoords(int dimension, int x, int y, int z) {
        nativeMapRaycastModelAtCoords(dimension, x, y, z, 0);
    }

    @JSStaticFunction
    public static void unmapCollisionAndRaycastModelAtCoords(int dimension, int x, int y, int z) {
        nativeMapCollisionModelAtCoords(dimension, x, y, z, 0);
        nativeMapRaycastModelAtCoords(dimension, x, y, z, 0);
    }



    public static class RenderAPI {
        private long pointer;

        public RenderAPI(long ptr) {
            pointer = ptr;
        }

        public long getAddr() {
            return pointer;
        }

        public void renderBoxId(int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, int blockId, int data) {
            NativeBlockRenderer.renderBoxId(pointer, x, y, z, x1, y1, z1, x2, y2, z2, blockId, data, true);
        }

        public void renderBoxIdHere(float x1, float y1, float z1, float x2, float y2, float z2, int blockId, int data) {
            NativeBlockRenderer.renderBoxIdHere(pointer, x1, y1, z1, x2, y2, z2, blockId, data, true);
        }

        public void renderBoxTexture(int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2, String name, int id) {
            NativeBlockRenderer.renderBoxTexture(pointer, x, y, z, x1, y1, z1, x2, y2, z2, name, id);
        }

        public void renderBoxTextureHere(float x1, float y1, float z1, float x2, float y2, float z2, String name, int id) {
            NativeBlockRenderer.renderBoxTextureHere(pointer, x1, y1, z1, x2, y2, z2, name, id);
        }

        public void renderBlock(int x, int y, int z, int blockId, int data, boolean b) {
            NativeBlockRenderer.renderBlock(pointer, x, y, z, blockId, data, b);
        }

        public void renderBlockHere(int blockId, int data, boolean b) {
            NativeBlockRenderer.renderBlockHere(pointer, blockId, data, b);
        }

        public void renderBlock(int x, int y, int z, int blockId, int data) {
            NativeBlockRenderer.renderBlock(pointer, x, y, z, blockId, data, true);
        }

        public void renderBlockHere(int blockId, int data) {
            NativeBlockRenderer.renderBlockHere(pointer, blockId, data, true);
        }

        public void renderModel(int x, int y, int z, NativeBlockModel model) {
            NativeBlockRenderer.renderModel(pointer, x, y, z, model.pointer);
        }

        public void renderModelHere(NativeBlockModel model) {
            NativeBlockRenderer.renderModelHere(pointer, model.pointer);
        }
    }



    private static HashMap<Integer, Function> jsRenderCallbacks = new HashMap<>();

    public static void _addRenderCallback(int id, Function callback) {
        jsRenderCallbacks.put(id, callback);
    }

    public static void onRenderCall(RenderAPI api, Coords coords, FullBlock block, boolean b) {
        Callback.invokeAPICallback("CustomBlockTessellation", api, coords, block, b);

        Function callback = jsRenderCallbacks.get(block.id);
        if (callback != null) {
            try {
                callback.call(Compiler.assureContextForCurrentThread(), callback.getParentScope(), callback.getParentScope(), new Object[]{api, coords, block, b});
            } catch (Exception e) {
                ICLog.e("TESSELLATION", "error occurred in block tessellation id=" + block.id + " data=" + block.data, e);
                if (LevelInfo.isLoaded()) {
                   // NativeAPI.clientMessage(ChatColor.RED + "error occurred in block tessellation.");
                }
            }
        }

    }

}
