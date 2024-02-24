package com.reider745.world.dimensions;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.*;

public class TerrainLayerMethods {
    private static final PointersStorage<TerrainLayerDescription> pointers = new PointersStorage<>("terrain_layer", new PointerGenFastest(), false);

    public static class TerrainLayerDescription {
        public final long ptr;
        public final BaseTerrainGenerator generator;
        public final int minY, maxY;
        public NoiseConversionMethods.NoiseConversionDescription noiseConversion;
        public NoiseGeneratorMethods.NoiseGeneratorDescription noiseGeneratorHeightmap;
        public NoiseGeneratorMethods.NoiseGeneratorDescription noiseGeneratorMain;
        public final TerrainMaterialMethods.TerrainMaterialDescription mainMaterial;
        private float thresholds[] = null;

        public TerrainLayerDescription(BaseTerrainGenerator generator, int minY, int maxY){
            this.generator = generator;

            if(maxY <= minY){
                this.maxY = this.minY = minY;
                thresholds = null;
            }else{
                this.minY = minY;
                this.maxY = maxY;
                thresholds = new float[maxY - minY];
            }

            mainMaterial = TerrainMaterialMethods.newMaterial();
            mainMaterial.baseId = 1;
            mainMaterial.baseData = 0;

            this.ptr = pointers.addPointer(this);
        }


        public final HashMap<Integer, Map.Entry<TerrainMaterialMethods.TerrainMaterialDescription, NoiseGeneratorMethods.NoiseGeneratorDescription>> materials_map = new HashMap<>();
        @SuppressWarnings("unchecked")
        public Map.Entry<Integer, Map.Entry<TerrainMaterialMethods.TerrainMaterialDescription, NoiseGeneratorMethods.NoiseGeneratorDescription>>[] materials = new Map.Entry[0];


        @SuppressWarnings("unchecked")
        public TerrainMaterialMethods.TerrainMaterialDescription addMaterial(NoiseGeneratorMethods.NoiseGeneratorDescription generator, int priority){
            TerrainMaterialMethods.TerrainMaterialDescription des = TerrainMaterialMethods.newMaterial();
            materials_map.put(priority, new AbstractMap.SimpleEntry<>(des, generator));
            var sortedList = new ArrayList<>(materials_map.entrySet());
            sortedList.sort(Map.Entry.comparingByKey());
            materials = sortedList.toArray(new Map.Entry[0]);
            return des;
        }

        private static final int GRID = 4;

        public void buildLayer(Noise.Data noise_data, Level level, int chunkX, int chunkZ, int regOffsetX, int regOffsetZ, int regSizeX, int regSizeZ, int volumeHeight){
            final int minY = this.minY;
            int maxY = this.maxY;
            if (maxY > volumeHeight) {
                maxY = volumeHeight;
            }

            final int x1 = (chunkX << 4) + regOffsetX;
            final int z1 = (chunkZ << 4) + regOffsetZ;
            final int x2 = (chunkX << 4) + regOffsetX + regSizeX;
            final int z2 = (chunkZ << 4) + regOffsetZ + regSizeZ;


            // fill main buffer
            final Noise.Buffer main_buffer = new Noise.Buffer();
            final NoiseGeneratorMethods.NoiseGeneratorDescription main_noise = this.noiseGeneratorMain;
            if (main_noise != null) {
                main_buffer.realloc(x1, minY, z1, regSizeX, maxY - minY, regSizeZ, GRID/*main_noise->grid*/);
                Noise.generate_noise_buffer(main_noise, noise_data, main_buffer);
            }

            // fill heightmap buffer
            final Noise.Buffer heightmap_buffer = new Noise.Buffer();
            final NoiseGeneratorMethods.NoiseGeneratorDescription heightmap = this.noiseGeneratorHeightmap;
            if (heightmap != null) {
                heightmap_buffer.realloc(x1, 0, z1, regSizeX, 1, regSizeZ, GRID/*heightmap->grid*/);
                Noise.generate_noise_buffer(heightmap, noise_data, heightmap_buffer);
            }

            // fill material buffers
            final int max_materials = 16;
            int material_count = 0;
            final TerrainMaterialMethods.TerrainMaterialDescription[] materials = new TerrainMaterialMethods.TerrainMaterialDescription[max_materials];
            final Noise.Buffer[] material_buffers = new Noise.Buffer[max_materials];

            for(var material : this.materials) {
                var entry = material.getValue();
                TerrainMaterialMethods.TerrainMaterialDescription materialType = entry.getKey();

                materials[material_count] = materialType;
                material_buffers[material_count] = new Noise.Buffer(x1, minY, z1, regSizeX, maxY - minY, regSizeZ, GRID);
                Noise.generate_noise_buffer(entry.getValue(), noise_data, material_buffers[material_count]);

                if (++material_count == max_materials) {
                    break;
                }
            }

            // place blocks
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    final float heightmap_value = heightmap != null ? heightmap_buffer.get(x, 0, z) : 0;

                    for (int y = maxY - 1; y >= minY;) {
                        if (thresholds[y - minY] > (main_noise != null ? main_buffer.get(x, y, z) : 1) + heightmap_value) {
                            y--;
                            continue;
                        }
                        int searchY = y;
                        while (searchY >= minY && thresholds[searchY - minY] <= (main_noise != null ? main_buffer.get(x, searchY, z) : 1) + heightmap_value) {
                            searchY--;
                        }

                        int depth = 0;
                        final int maxDepth = y - searchY;
                        while (y > searchY) {
                            Block block = null;
                            if (material_count != 0) {
                                for (int i = 0; i < material_count; i++) {
                                    final var material = materials[i];
                                    if (material_buffers[i].get(x, y, z) + (Math.random() * 2f - 1f) * material.diffuse > 0.5) {
                                        block = material.getBlock(depth, maxDepth);
                                        break;
                                    }
                                }
                                if(block == null)
                                    block = mainMaterial.getBlock(depth, maxDepth);
                            } else {
                                block = mainMaterial.getBlock(depth, maxDepth);
                            }
                            level.setBlock(new Vector3(x, y, z), block);
                            y--;
                            depth++;
                        }
                    }
                }
            }

        }

        public void rebuildThresholds() {
            final float height = maxY - minY - 1f;
            for (int y = minY; y < maxY; y++) {
                thresholds[y - minY] = this.noiseConversion != null ? 0.5f - this.noiseConversion.convert((y - minY) / height) : 0.5f;
            }
        }
    }

    public static TerrainLayerDescription newLayer(BaseTerrainGenerator generator, int minY, int maxY){
        return new TerrainLayerDescription(generator, minY, maxY);
    }

    public static long nativeGetMainMaterial(long pointer){
        return TerrainMaterialMethods.ptr_main;
    }
    public static void nativeSetHeightmapNoiseGenerator(long pointer, long generator){
        pointers.get(pointer).noiseGeneratorHeightmap = NoiseGeneratorMethods.get(generator);
    }
    public static void nativeSetMainNoiseGenerator(long pointer, long generator){
        pointers.get(pointer).noiseGeneratorMain = NoiseGeneratorMethods.get(generator);
    }
    public static void nativeSetYConversion(long pointer, long conversion){
        pointers.get(pointer).noiseConversion = NoiseConversionMethods.get(conversion);
    }
    public static long nativeAddNewMaterial(long pointer, long noiseGenerator, int priority){
        return pointers.get(pointer).addMaterial(NoiseGeneratorMethods.get(noiseGenerator), priority).ptr;
    }
}
