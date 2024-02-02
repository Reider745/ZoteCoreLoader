package com.reider745.world.dimensions;

public interface BaseTerrainGenerator {
    void generateChunk(DimensionsMethods.CustomDimensionGenerator generator, int chunkX, int chunkZ);
    void populateChunk(DimensionsMethods.CustomDimensionGenerator generator, int chunkX, int chunkZ);
}
