package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.MonoBiomeTerrainMethods;

public class MonoBiomeTerrainGenerator extends AbstractTerrainGenerator {
    public final long pointer;

    public MonoBiomeTerrainGenerator() {
        pointer = MonoBiomeTerrainMethods.nativeConstruct();
    }

    public long getPointer() {
        return pointer;
    }

    public TerrainLayer addTerrainLayer(int minY, int maxY) {
        return new TerrainLayer(MonoBiomeTerrainMethods.nativeAddLayer(pointer, minY, maxY));
    }

    public MonoBiomeTerrainGenerator setBaseBiome(int id) {
        MonoBiomeTerrainMethods.nativeSetBaseBiome(pointer, id);
        return this;
    }
}
