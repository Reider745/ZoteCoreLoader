package com.zhekasmirnov.innercore.api.dimensions;

public class TerrainMaterial {
    public final long pointer;

    TerrainMaterial(long pointer) {
        this.pointer = pointer;
    }

    public TerrainMaterial setBase(int id, int data) {
        nativeSetBase(pointer, id, data);
        return this;
    }

    public TerrainMaterial setCover(int id, int data) {
        nativeSetCover(pointer, id, data);
        return this;
    }

    public TerrainMaterial setSurface(int width, int id, int data) {
        nativeSetSurface(pointer, width, id, data);
        return this;
    }

    public TerrainMaterial setFilling(int width, int id, int data) {
        nativeSetFilling(pointer, width, id, data);
        return this;
    }

    public TerrainMaterial setDiffuse(float value) {
        nativeSetDiffuse(pointer, value);
        return this;
    }
    

    private static native void nativeSetBase(long pointer, int id, int data);
    private static native void nativeSetCover(long pointer, int id, int data);
    private static native void nativeSetSurface(long pointer, int width, int id, int data);
    private static native void nativeSetFilling(long pointer, int width, int id, int data);
    private static native void nativeSetDiffuse(long pointer, float diffuse);
}