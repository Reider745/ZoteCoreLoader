package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.TerrainLayerMethods;

public class TerrainLayer {
    public final long pointer;
    public final TerrainMaterial material;

    TerrainLayer(long pointer) {
        this.pointer = pointer;
        material = new TerrainMaterial(nativeGetMainMaterial(pointer));
    }

    public TerrainMaterial addNewMaterial(NoiseGenerator generator, int priority) {
        return new TerrainMaterial(nativeAddNewMaterial(pointer, generator.pointer, priority));
    }

    public TerrainLayer setHeightmapNoise(NoiseGenerator noise) {
        nativeSetHeightmapNoiseGenerator(pointer, noise.pointer);
        return this;
    }

    public TerrainLayer setMainNoise(NoiseGenerator noise) {
        nativeSetMainNoiseGenerator(pointer, noise.pointer);
        return this;
    }

    public TerrainLayer setYConversion(NoiseConversion conversion) {
        nativeSetYConversion(pointer, conversion.pointer);
        return this;
    }

    public TerrainMaterial getMainMaterial() {
        return material;
    }

    private static long nativeGetMainMaterial(long pointer) {
        return TerrainLayerMethods.nativeGetMainMaterial(pointer);
    }

    private static void nativeSetHeightmapNoiseGenerator(long pointer, long generator) {
        TerrainLayerMethods.nativeSetHeightmapNoiseGenerator(pointer, generator);
    }

    private static void nativeSetMainNoiseGenerator(long pointer, long generator) {
        TerrainLayerMethods.nativeSetMainNoiseGenerator(pointer, generator);
    }

    private static void nativeSetYConversion(long pointer, long conversion) {
        TerrainLayerMethods.nativeSetYConversion(pointer, conversion);
    }

    private static long nativeAddNewMaterial(long pointer, long noiseGenerator, int priority) {
        return TerrainLayerMethods.nativeAddNewMaterial(pointer, noiseGenerator, priority);
    }
}
