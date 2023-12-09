package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;

public class TerrainLayer {
    public final long pointer;
    public final TerrainMaterial material;

    TerrainLayer(long pointer) {
        this.pointer = pointer;
        this.material = new TerrainMaterial(0);
    }

    public TerrainMaterial addNewMaterial(NoiseGenerator generator, int priority) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.addNewMaterial(generator, priority)");
        return new TerrainMaterial(0);
    }

    public TerrainLayer setHeightmapNoise(NoiseGenerator noise) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setHeightmapNoise(noise)");
        return this;
    }

    public TerrainLayer setMainNoise(NoiseGenerator noise) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setMainNoise(noise)");
        return this;
    }

    public TerrainLayer setYConversion(NoiseConversion conversion) {
        InnerCoreServer.useNotCurrentSupport("TerrainMaterial.setYConversion(conversion)");
        return this;
    }

    public TerrainMaterial getMainMaterial() {
        return material;
    }
}
