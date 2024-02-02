package com.reider745.world.dimensions;

import cn.nukkit.level.Level;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;
import com.reider745.world.BlockSourceMethods;

import java.util.ArrayList;

public class MonoBiomeTerrainMethods {
    private static final PointersStorage<MonoBiomeDescription> pointers = new PointersStorage<>("mono_biome_terrain", new PointerGenFastest(), false);

    public static class MonoBiomeDescription implements BaseTerrainGenerator {
        public int baseBiome = 0;
        public final ArrayList<TerrainLayerMethods.TerrainLayerDescription> layers = new ArrayList<>();

        @Override
        public void generateChunk(DimensionsMethods.CustomDimensionGenerator generator, int chunkX, int chunkZ) {

        }

        @Override
        public void populateChunk(DimensionsMethods.CustomDimensionGenerator generator, int chunkX, int chunkZ) {
            final Noise.Data data = new Noise.Data();
            data.prepare_noise_data((int) generator.seed);
            final Level level = BlockSourceMethods.getLevelForDimension(generator.getId());

            for(final TerrainLayerMethods.TerrainLayerDescription layer : layers)
                layer.rebuildThresholds();

            for(final TerrainLayerMethods.TerrainLayerDescription layer : layers)
                layer.buildLayer(data, level, chunkX, chunkZ, 0, 0, 16, 16, 256);
        }
    }

    public static MonoBiomeDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(){
        return pointers.addPointer(new MonoBiomeDescription());
    }
    public static long nativeAddLayer(long pointer, int minY, int maxY){
        MonoBiomeDescription self = pointers.get(pointer);
        TerrainLayerMethods.TerrainLayerDescription description = TerrainLayerMethods.newLayer(self, minY, maxY);
        self.layers.add(description);
        return description.ptr;
    }
    public static void nativeSetBaseBiome(long pointer, int id){
        pointers.get(pointer).baseBiome = id;
    }
}
