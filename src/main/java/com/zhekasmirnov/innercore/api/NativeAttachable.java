package com.zhekasmirnov.innercore.api;

public class NativeAttachable {
    public final long pointer;
    private final NativeShaderUniformSet uniformSet;

    public NativeAttachable(long actorUid) {
        this.pointer = nativeConstruct(actorUid);
        long uniformSetPtr = nativeGetUniformSet(pointer);
        uniformSet = uniformSetPtr != 0 ? new NativeShaderUniformSet(uniformSetPtr) : null;
        // setMaterial("entity_alphatest_custom");
    }

    public NativeShaderUniformSet getUniformSet() {
        return uniformSet;
    }

    public NativeAttachable setRenderer(NativeActorRenderer renderer) {
        nativeSetRenderer(pointer, renderer != null ? renderer.pointer : 0);
        return this;
    }

    public NativeAttachable setTexture(String texture) {
        nativeSetTexture(pointer, texture != null ? texture : "");
        return this;
    }

    public NativeAttachable setMaterial(String material) {
        nativeSetMaterial(pointer, material != null ? material : "");
        return this;
    }


    public boolean isAttached() {
        return nativeIsAttached(pointer);
    }

    public void destroy() {
        nativeDestroy(pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        nativeFinalize(pointer);
    }

    private static long nativeConstruct(long actorUid){
        return 0;
    }
    private static long nativeGetUniformSet(long pointer){
        return 0;
    }
    private static void nativeDestroy(long pointer){

    }
    private static void nativeFinalize(long pointer){

    }
    private static boolean nativeIsAttached(long pointer){
        return false;
    }
    private static void nativeSetRenderer(long pointer, long renderer){

    }
    private static void nativeSetTexture(long pointer, String name){

    }
    private static void nativeSetMaterial(long pointer, String name){

    }


    public static void attachRendererToItem(int id, NativeActorRenderer renderer, String texture, String material) {
        nativeAttachToItem(id, renderer != null ? renderer.pointer : 0, texture != null ? texture : "", material != null ? material : "");
    }

    public static void detachRendererFromItem(int id) {
        nativeDetachFromItem(id);
    }

    private static native void nativeAttachToItem(int id, long renderer, String texture, String material);
    private static native void nativeDetachFromItem(int id);
}
