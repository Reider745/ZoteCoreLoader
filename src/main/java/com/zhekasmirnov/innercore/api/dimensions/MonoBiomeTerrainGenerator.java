package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.MonoBiomeTerrainMethods;

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

    private static long nativeConstruct(){
        return MonoBiomeTerrainMethods.nativeConstruct();
    }
    private static long nativeAddLayer(long pointer, int minY, int maxY){
        return MonoBiomeTerrainMethods.nativeAddLayer(pointer, minY, maxY);
    }
    private static void nativeSetBaseBiome(long pointer, int id){
        MonoBiomeTerrainMethods.nativeSetBaseBiome(pointer, id);
    }
}