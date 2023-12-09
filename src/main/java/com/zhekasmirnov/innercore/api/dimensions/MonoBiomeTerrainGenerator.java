package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;

public class MonoBiomeTerrainGenerator extends AbstractTerrainGenerator {
    public final long pointer = 0;

    public MonoBiomeTerrainGenerator() {
    }

    public long getPointer() {
        return pointer;
    }

    public TerrainLayer addTerrainLayer(int minY, int maxY) {
        InnerCoreServer.useNotCurrentSupport("MonoBiomeTerrainGenerator.addTerrainLayer(minY, maxY)");
        return new TerrainLayer(0);
    }

    public MonoBiomeTerrainGenerator setBaseBiome(int id) {
        InnerCoreServer.useNotCurrentSupport("MonoBiomeTerrainGenerator.setBaseBiome(id)");
        return this;
    }
}
