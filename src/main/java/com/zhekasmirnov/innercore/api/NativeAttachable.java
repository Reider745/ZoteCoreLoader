package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;

public class NativeAttachable {
    public final long pointer = 0;

    public NativeAttachable(long actorUid) {
    }

    public NativeShaderUniformSet getUniformSet() {
        return null;
    }

    public NativeAttachable setRenderer(NativeActorRenderer renderer) {
        return this;
    }

    public NativeAttachable setTexture(String texture) {
        return this;
    }

    public NativeAttachable setMaterial(String material) {
        return this;
    }

    public boolean isAttached() {
        return false;
    }

    public void destroy() {
    }

    public static void attachRendererToItem(int id, NativeActorRenderer renderer, String texture, String material) {
        InnerCoreServer.useClientMethod("NativeAttachable.attachRendererToItem(id, renderer, texture, material)");
    }

    public static void detachRendererFromItem(int id) {
        InnerCoreServer.useClientMethod("NativeAttachable.detachRendererFromItem(id)");
    }
}
