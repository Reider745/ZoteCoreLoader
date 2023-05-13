package com.zhekasmirnov.innercore.api.dimensions;


public class MonoBiomeTerrainGenerator extends AbstractTerrainGenerator {
    public final long pointer;

    public MonoBiomeTerrainGenerator() {
        pointer = nativeConstruct();
    }

    public long getPointer() {
        return pointer;
    }

    public TerrainLayer addTerrainLayer(int minY, int maxY) {
        return new TerrainLayer(nativeAddLayer(pointer, minY, maxY));
    }

    public MonoBiomeTerrainGenerator setBaseBiome(int id) {
        nativeSetBaseBiome(pointer, id);
        return this;
    }

    private static native long nativeConstruct();
    private static native long nativeAddLayer(long pointer, int minY, int maxY);
    private static native void nativeSetBaseBiome(long pointer, int id);
}