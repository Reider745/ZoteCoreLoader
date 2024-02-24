package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.TerrainMaterialMethods;

public class TerrainMaterial {
    public final long pointer;

    TerrainMaterial(long pointer) {
        this.pointer = pointer;
    }

    public TerrainMaterial setBase(int id, int data) {
        TerrainMaterialMethods.nativeSetBase(pointer, id, data);
        return this;
    }

    public TerrainMaterial setCover(int id, int data) {
        TerrainMaterialMethods.nativeSetCover(pointer, id, data);
        return this;
    }

    public TerrainMaterial setSurface(int width, int id, int data) {
        TerrainMaterialMethods.nativeSetSurface(pointer, width, id, data);
        return this;
    }

    public TerrainMaterial setFilling(int width, int id, int data) {
        TerrainMaterialMethods.nativeSetFilling(pointer, width, id, data);
        return this;
    }

    public TerrainMaterial setDiffuse(float value) {
        TerrainMaterialMethods.nativeSetDiffuse(pointer, value);
        return this;
    }
}
