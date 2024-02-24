package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.commontypes.FullBlock;
import com.zhekasmirnov.innercore.api.constants.ChatColor;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;

/**
 * Created by zheka on 07.08.2017.
 */

@Deprecated(since = "Zote")
public class NativeBlockRenderer {

    public static void nativeSetCollisionShape(int id, int data, long collisionShape) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeSetCollisionShape(id, data, collisionShape)");
    }

    public static void nativeSetRaycastShape(int id, int data, long raycastShape) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeSetRaycastShape(id, data, raycastShape)");
    }

    public static void nativeEnableCustomRender(int id, int data) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeEnableCustomRender(id, data)");
    }

    public static void nativeDisableCustomRender(int id, int data) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeDisableCustomRender(id, data)");
    }

    public static void nativeEnableStaticModel(int id, int data, long icRender) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeEnableStaticModel(id, data, icRender)");
    }

    public static void nativeEnableCoordMapping(int id, int data, long icRender) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeEnableCoordMapping(id, data, icRender)");
    }

    public static void nativeMapModelAtCoords(int x, int y, int z, long model) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeMapModelAtCoords(x, y, z, model)");
    }

    public static void nativeMapICRenderAtCoords(int x, int y, int z, long icRender) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeMapICRenderAtCoords(x, y, z, icRender)");
    }

    public static void nativeMapCollisionModelAtCoords(int dimension, int x, int y, int z, long model) {
        InnerCoreServer
                .useClientMethod("NativeBlockRenderer.nativeMapCollisionModelAtCoords(dimension, x, y, z, model)");
    }

    public static void nativeMapRaycastModelAtCoords(int dimension, int x, int y, int z, long model) {
        InnerCoreServer
                .useClientMethod("NativeBlockRenderer.nativeMapRaycastModelAtCoords(dimension, x, y, z, model)");
    }

    public static void nativeUnmapAtCoords(int x, int y, int z) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.nativeUnmapAtCoords(x, y, z)");
    }

    public static void renderBoxPtr(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2,
            float z2, long block, int data, boolean b) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxPtr(tess, x, y, z, x1, y1, z1, x2, y2, z2, block, data, b)");
    }

    public static void renderBoxPtrHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2,
            long block, int data, boolean b) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxPtrHere(tess, x1, y1, z1, x2, y2, z2, block, data, b)");
    }

    public static void renderBoxId(long tess, int x, int y, int z, float x1, float y1, float z1, float x2, float y2,
            float z2, int blockId, int data, boolean b) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxId(tess, x, y, z, x1, y1, z1, x2, y2, z2, blockId, data, b)");
    }

    public static void renderBoxIdHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2,
            int blockId, int data, boolean b) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxIdHere(tess, x1, y1, z1, x2, y2, z2, blockId, data, b)");
    }

    public static void renderBoxTexture(long tess, int x, int y, int z, float x1, float y1, float z1, float x2,
            float y2, float z2, String tex, int id) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxTexture(tess, x, y, z, x1, y1, z1, x2, y2, z2, tex, id)");
    }

    public static void renderBoxTextureHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2,
            String tex, int id) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxTextureHere(tess, x1, y1, z1, x2, y2, z2, tex, id)");
    }

    public static void renderBoxTextureSet(long tess, int x, int y, int z, float x1, float y1, float z1, float x2,
            float y2, float z2, String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4,
            String tex5, int id5, String tex6, int id6) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxTextureSet(tess, x, y, z, x1, y1, z1, x2, y2, z2, tex1, id1, tex2, id2, tex3, id3, tex4, id4, tex5, id5, tex6, id6)");
    }

    public static void renderBoxTextureSetHere(long tess, float x1, float y1, float z1, float x2, float y2, float z2,
            String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5,
            int id5, String tex6, int id6) {
        InnerCoreServer.useClientMethod(
                "NativeBlockRenderer.renderBoxTextureSetHere(tess, x1, y1, z1, x2, y2, z2, tex1, id1, tex2, id2, tex3, id3, tex4, id4, tex5, id5, tex6, id6)");
    }

    public static void renderBlock(long tess, int x, int y, int z, int blockId, int data, boolean b) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.renderBlock(tess, x, y, z, blockId, data, b)");
    }

    public static void renderBlockHere(long tess, int blockId, int data, boolean b) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.renderBlockHere(tess, blockId, data, b)");
    }

    public static void renderModel(long tess, int x, int y, int z, long model) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.renderModel(tess, x, y, z, model)");
    }

    public static void renderModelHere(long tess, long model) {
        InnerCoreServer.useClientMethod("NativeBlockRenderer.renderModelHere(tess, model)");
    }

    @JSStaticFunction
    public static void setCustomCollisionShape(int id, Object data, final Object _shape) {
    }

    @JSStaticFunction
    public static void setCustomRaycastShape(int id, Object data, final Object _shape) {
    }

    @JSStaticFunction
    public static void setCustomCollisionAndRaycastShape(int id, Object data, final Object _shape) {
    }

    @JSStaticFunction
    public static void enableCustomRender(int id, Object data) {
    }

    @JSStaticFunction
    public static void disableCustomRender(int id, Object data) {
    }

    @JSStaticFunction
    public static void setStaticICRender(int id, Object data, Object _icRender) {
    }

    @JSStaticFunction
    public static void enableCoordMapping(int id, Object data, Object _icRender) {
    }

    @JSStaticFunction
    public static void mapAtCoords(int x, int y, int z, Object _icRender, boolean preventRebuild) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockRenderer.mapAtCoords(x, y, z, _icRender, preventRebuild)");
    }

    @JSStaticFunction
    public static void unmapAtCoords(int x, int y, int z, boolean preventRebuild) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockRenderer.unmapAtCoords(x, y, z, preventRebuild)");
    }

    @JSStaticFunction
    public static void mapCollisionModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        InnerCoreServer
                .useNotCurrentSupport("NativeBlockRenderer.mapCollisionModelAtCoords(dimension, x, y, z, _shape)");
    }

    @JSStaticFunction
    public static void mapRaycastModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockRenderer.mapRaycastModelAtCoords(dimension, x, y, z, _shape)");
    }

    @JSStaticFunction
    public static void mapCollisionAndRaycastModelAtCoords(int dimension, int x, int y, int z, Object _shape) {
        InnerCoreServer.useNotCurrentSupport(
                "NativeBlockRenderer.mapCollisionAndRaycastModelAtCoords(dimension, x, y, z, _shape)");
    }

    @JSStaticFunction
    public static void unmapCollisionModelAtCoords(int dimension, int x, int y, int z) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockRenderer.unmapCollisionModelAtCoords(dimension, x, y, z)");
    }

    @JSStaticFunction
    public static void unmapRaycastModelAtCoords(int dimension, int x, int y, int z) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockRenderer.unmapRaycastModelAtCoords(dimension, x, y, z)");
    }

    @JSStaticFunction
    public static void unmapCollisionAndRaycastModelAtCoords(int dimension, int x, int y, int z) {
        InnerCoreServer
                .useNotCurrentSupport("NativeBlockRenderer.unmapCollisionAndRaycastModelAtCoords(dimension, x, y, z)");
    }

    public static class RenderAPI {
        public RenderAPI(long ptr) {
        }

        public long getAddr() {
            return 0;
        }

        public void renderBoxId(int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2,
                int blockId, int data) {
        }

        public void renderBoxIdHere(float x1, float y1, float z1, float x2, float y2, float z2, int blockId, int data) {
        }

        public void renderBoxTexture(int x, int y, int z, float x1, float y1, float z1, float x2, float y2, float z2,
                String name, int id) {
        }

        public void renderBoxTextureHere(float x1, float y1, float z1, float x2, float y2, float z2, String name,
                int id) {
        }

        public void renderBlock(int x, int y, int z, int blockId, int data, boolean b) {
        }

        public void renderBlockHere(int blockId, int data, boolean b) {
        }

        public void renderBlock(int x, int y, int z, int blockId, int data) {
        }

        public void renderBlockHere(int blockId, int data) {
        }

        public void renderModel(int x, int y, int z, NativeBlockModel model) {
        }

        public void renderModelHere(NativeBlockModel model) {
        }
    }

    private static HashMap<Integer, Function> jsRenderCallbacks = new HashMap<>();

    public static void _addRenderCallback(int id, Function callback) {
        jsRenderCallbacks.put(id, callback);
    }

    @Deprecated(since = "Zote")
    public static void onRenderCall(RenderAPI api, Coords coords, FullBlock block, boolean b) {
        Callback.invokeAPICallback("CustomBlockTessellation", api, coords, block, b);

        Function callback = jsRenderCallbacks.get(block.id);
        if (callback != null) {
            try {
                callback.call(Compiler.assureContextForCurrentThread(), callback.getParentScope(),
                        callback.getParentScope(), new Object[] { api, coords, block, b });
            } catch (Exception e) {
                ICLog.e("TESSELLATION", "error occurred in block tessellation id=" + block.id + " data=" + block.data,
                        e);
                if (LevelInfo.isLoaded()) {
                    NativeAPI.clientMessage(ChatColor.RED + "error occurred in block tessellation.");
                }
            }
        }
    }
}
