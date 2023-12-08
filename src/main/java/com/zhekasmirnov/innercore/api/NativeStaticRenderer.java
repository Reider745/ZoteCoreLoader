package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeStaticRenderer {
    public static final float HUMANOID_MODEL_OFFSET = 1.62000274658203f;

    private NativeStaticRenderer(long ptr, boolean isHumanoidModel) {
        this.uniformSet = new NativeShaderUniformSet(0);
    }

    public boolean exists() {
        return false;
    }

    public void setPos(float x, float y, float z) {
    }

    public void setBlockLightPos(float x, float y, float z) {
    }

    public void resetBlockLightPos() {
    }

    public void setInterpolationEnabled(boolean enabled) {
    }

    public void setIgnoreBlocklight(boolean ignore) {
    }

    public void setScale(float scale) {
    }

    public void setSkin(String skin) {
    }

    public void setRenderer(int id) {
    }

    public void setRenderer(NativeRenderer.Renderer renderer) {
    }

    public void setMesh(NativeRenderMesh mesh) {
    }

    public void setMaterial(String name) {
    }

    public void remove() {
    }

    public final Transform transform = new Transform();
    public final NativeShaderUniformSet uniformSet;

    public NativeShaderUniformSet getShaderUniforms() {
        return uniformSet;
    }

    public class Transform {
        public Transform clear() {
            return this;
        }

        public Transform lock() {
            return this;
        }

        public Transform unlock() {
            return this;
        }

        public Transform matrix(float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7,
                float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
            return this;
        }

        public Transform scale(float x, float y, float z) {
            return this;
        }

        public Transform scaleLegacy(float scale) {
            return this;
        }

        public Transform rotate(double x, double y, double z) {
            return this;
        }

        public Transform translate(double x, double y, double z) {
            return this;
        }
    }

    public static NativeStaticRenderer createStaticRenderer(NativeRenderer.Renderer renderer, float x, float y,
            float z) {
        return new NativeStaticRenderer(0, renderer != null ? renderer.isHumanoid : false);
    }

    /*
     * native part
     */
    public static long createStaticRenderer(long rendererPointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.createStaticRenderer(rendererPointer, x, y, z)");
        return 0;
    }

    public static long nativeGetShaderUniformSet(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.nativeGetShaderUniformSet(pointer)");
        return 0;
    }

    public static boolean nativeExists(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.nativeExists(pointer)");
        return false;
    }

    public static void setPos(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setPos(pointer, x, y, z)");
    }

    public static void setInterpolationEnabled(long pointer, boolean enabled) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setInterpolationEnabled(pointer, enabled)");
    }

    public static void setIgnoreBlocklight(long pointer, boolean ignore) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setIgnoreBlocklight(pointer, ignore)");
    }

    public static void setBlockLightPos(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setBlockLightPos(pointer, x, y, z)");
    }

    public static void resetBlockLightPos(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.resetBlockLightPos(pointer)");
    }

    public static void setScale(long pointer, float scale) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setScale(pointer, scale)");
    }

    public static void transformClear(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformClear(pointer)");
    }

    public static void transformLock(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformLock(pointer)");
    }

    public static void transformUnlock(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformUnlock(pointer)");
    }

    public static void transformAddTransform(long pointer, float f0, float f1, float f2, float f3, float f4, float f5,
            float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
        InnerCoreServer.useClientMethod(
                "NativeStaticRenderer.transformAddTransform(pointer, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15)");
    }

    public static void transformScale(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformScale(pointer, x, y, z)");
    }

    public static void transformScaleLegacy(long pointer, float scale) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformScaleLegacy(pointer, scale)");
    }

    public static void transformRotate(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformRotate(pointer, x, y, z)");
    }

    public static void transformTranslate(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.transformTranslate(pointer, x, y, z)");
    }

    public static void setSkin(long pointer, String skin) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setSkin(pointer, skin)");
    }

    public static void setRenderer(long pointer, long rendererPointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setRenderer(pointer, rendererPointer)");
    }

    public static void setMesh(long pointer, long meshPointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setMesh(pointer, meshPointer)");
    }

    public static void setMeshMaterial(long pointer, String material) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.setMeshMaterial(pointer, material)");
    }

    public static void remove(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.remove(pointer)");
    }

    public static void finalizeNative(long pointer) {
        InnerCoreServer.useClientMethod("NativeStaticRenderer.finalizeNative(pointer)");
    }
}
