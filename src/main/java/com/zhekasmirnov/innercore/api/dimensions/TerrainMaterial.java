package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;

public class TerrainMaterial {
    public final long pointer;

    TerrainMaterial(long pointer) {
        this.pointer = pointer;
    }

    public TerrainMaterial setBase(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setBase(id, data)");
        return this;
    }

    public TerrainMaterial setCover(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setCover(id, data)");
        return this;
    }

    public TerrainMaterial setSurface(int width, int id, int data) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setSurface(width, id, data)");
        return this;
    }

    public TerrainMaterial setFilling(int width, int id, int data) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setFilling(width, id, data)");
        return this;
    }

    public TerrainMaterial setDiffuse(float value) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setDiffuse(value)");
        return this;
    }
}
