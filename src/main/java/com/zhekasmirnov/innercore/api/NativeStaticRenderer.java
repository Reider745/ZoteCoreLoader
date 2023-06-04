package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeStaticRenderer {
    public static final float HUMANOID_MODEL_OFFSET = 1.62000274658203f;

    private long pointer;
    private boolean isHumanoidModel;
    private float posX = 0, posY = 0, posZ = 0;
    private boolean isLightLocked = false;
    private boolean isPositionInterpolationEnabled = false;

    private NativeStaticRenderer(long ptr, boolean isHumanoidModel) {
        this.pointer = ptr;
        this.uniformSet = new NativeShaderUniformSet(nativeGetShaderUniformSet(ptr));
        this.isHumanoidModel = isHumanoidModel;
    }

    public boolean exists() {
        return nativeExists(pointer);
    }

    public void setPos(float x, float y, float z) {
        posX = x;
        posY = y;
        posZ = z;
        if (!isLightLocked) {
            NativeStaticRenderer.setBlockLightPos(pointer, x, y, z);
        }
        if (isHumanoidModel) {
            y += HUMANOID_MODEL_OFFSET;
        }

        if (isPositionInterpolationEnabled) {
            final float y0 = y;
            MainThreadQueue.localThread.enqueue(() -> NativeStaticRenderer.setPos(pointer, x, y0, z));
        } else {
            NativeStaticRenderer.setPos(pointer, x, y, z);
        }
    }
    
    public void setBlockLightPos(float x, float y, float z) {
        NativeStaticRenderer.setBlockLightPos(pointer, x, y, z);
        isLightLocked = true;
    }
    
    public void resetBlockLightPos() {
        NativeStaticRenderer.resetBlockLightPos(pointer);
        isLightLocked = false;
    }

    public void setInterpolationEnabled(boolean enabled) {
        NativeStaticRenderer.setInterpolationEnabled(pointer, enabled);
        isPositionInterpolationEnabled = enabled;
    }

    public void setIgnoreBlocklight(boolean ignore) {
        NativeStaticRenderer.setIgnoreBlocklight(pointer, ignore);
    }

    public void setScale(float scale) {
        NativeStaticRenderer.setScale(pointer, scale);
    }

    public void setSkin(String skin) {
        NativeStaticRenderer.setSkin(pointer, skin);
    }

    public void setRenderer(int id) {
        if (id == -1) {
            setRenderer(null);
            return;
        }
        NativeRenderer.Renderer renderer = NativeRenderer.getRendererById(id);
        if (renderer != null) {
            setRenderer(renderer);
        }
        else {
            throw new IllegalArgumentException("invalid renderer id " + id + ", id must belong only to custom renderer");
        }
    }

    public void setRenderer(NativeRenderer.Renderer renderer) {
        if (renderer != null) {
            NativeStaticRenderer.setRenderer(pointer, renderer.pointer);
            isHumanoidModel = renderer.isHumanoid;
        }
        else {
            NativeStaticRenderer.setRenderer(pointer, 0);
            isHumanoidModel = false;
        }
        setPos(posX, posY, posZ);
    }

    public void setMesh(NativeRenderMesh mesh) {
        if (mesh != null) {
            NativeStaticRenderer.setMesh(pointer, mesh.getPtr());
        } else {
            NativeStaticRenderer.setMesh(pointer, 0);
        }
    }

    public void setMaterial(String name) {
        if (name != null) {
            NativeStaticRenderer.setMeshMaterial(pointer, name);
        } else {
            NativeStaticRenderer.setMeshMaterial(pointer, "entity_alphatest_custom");
        }
    }

    public void remove() {
        NativeStaticRenderer.remove(pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        if (pointer != 0) {
            finalizeNative(pointer);
        }
        super.finalize();
    }

    

    public final Transform transform = new Transform();
    public final NativeShaderUniformSet uniformSet;
    
    public NativeShaderUniformSet getShaderUniforms() {
        return uniformSet;
    }

    public class Transform {
        public Transform clear(){
            NativeStaticRenderer.transformClear(pointer);
            return this;
        }

        public Transform lock(){
            NativeStaticRenderer.transformLock(pointer);
            return this;
        }
        
        public Transform unlock(){
            NativeStaticRenderer.transformUnlock(pointer);
            return this;
        }

        public Transform matrix(float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15){
            NativeStaticRenderer.transformAddTransform(pointer, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15);
            return this;
        }

        public Transform scale(float x, float y, float z){
            NativeStaticRenderer.transformScale(pointer, x, y, z);
            return this;
        }

        public Transform scaleLegacy(float scale){
            NativeStaticRenderer.transformScaleLegacy(pointer, scale);
            return this;
        }

        public Transform rotate(double x, double y, double z){
            NativeStaticRenderer.transformRotate(pointer, (float) x, (float) y, (float) z);
            return this;
        }

        public Transform translate(double x, double y, double z){
            NativeStaticRenderer.transformTranslate(pointer, (float) x, (float) y, (float) z);
            return this;
        }
    }


    public static NativeStaticRenderer createStaticRenderer(NativeRenderer.Renderer renderer, float x, float y, float z) {
        NativeStaticRenderer staticRenderer = new NativeStaticRenderer(createStaticRenderer(renderer != null ? renderer.getPointer() : 0, x, y, z), renderer != null ? renderer.isHumanoid : false);
        // double call to ignore interpolation
        staticRenderer.setPos(x, y, z);
        staticRenderer.setPos(x, y, z);
        return staticRenderer;
    }

    /*
     * native part
     */

    public static long createStaticRenderer(long rendererPointer, float x, float y, float z){
        return 0;
    }
    
    public static long nativeGetShaderUniformSet(long pointer){
        return 0;
    }

    public static boolean nativeExists(long pointer){
        return false;
    }

    public static void setPos(long pointer, float x, float y, float z){

    }

    public static void setInterpolationEnabled(long pointer, boolean enabled){

    }

    public static void setIgnoreBlocklight(long pointer, boolean ignore){

    }

    public static void setBlockLightPos(long pointer, float x, float y, float z){

    }

    public static void resetBlockLightPos(long pointer){

    }

    public static void setScale(long pointer, float scale){

    }

    public static void transformClear(long pointer){

    }
    
    public static void transformLock(long pointer){}


    public static void transformUnlock(long pointer){

    }

    public static void transformAddTransform(long pointer, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15){

    }

    public static void transformScale(long pointer, float x, float y, float z){

    }


    public static void transformScaleLegacy(long pointer, float scale){

    }


    public static void transformRotate(long pointer, float x, float y, float z){

    }

    public static void transformTranslate(long pointer, float x, float y, float z){}


    public static void setSkin(long pointer, String skin){

    }

    public static void setRenderer(long pointer, long rendererPointer){

    }

    public static void setMesh(long pointer, long meshPointer){

    }

    public static void setMeshMaterial(long pointer, String material){

    }

    public static void remove(long pointer){

    }

    public static void finalizeNative(long pointer){

    }
}
