package com.zhekasmirnov.innercore.api.dimensions;

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
    

    private static native long nativeGetMainMaterial(long pointer);
    private static native void nativeSetHeightmapNoiseGenerator(long pointer, long generator);
    private static native void nativeSetMainNoiseGenerator(long pointer, long generator);
    private static native void nativeSetYConversion(long pointer, long conversion);
    private static native long nativeAddNewMaterial(long pointer, long noiseGenerator, int priority);
}