package com.reider745.world.dimensions;

import cn.nukkit.Server;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.reider745.InnerCoreServer;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.HashMap;
import java.util.Map;

public class DimensionsMethods {
    public static class CustomDimensionGenerator extends Generator {
        public ChunkManager manager;
        public int dimensionId;
        public Vector3 spawn = new Vector3(0, 0, 0);
        public long seed;
        public String name;
        public final Map<String, Object> settings;
        public final DimensionData data;
        public final CustomDimensionDescription description;

        public CustomDimensionGenerator(Map<String, Object> settings){
            this.settings = settings;

            dimensionId = (int) settings.get("id");
            name = (String) settings.get("name");
            description = descriptions.get(name);
            data = new DimensionData(dimensionId, 0, 256);
        }

        @Override
        public DimensionData getDimensionData() {
            return data;
        }

        @Override
        public int getId() {
            return dimensionId;
        }

        @Override
        public void init(ChunkManager chunkManager, NukkitRandom nukkitRandom) {
            this.manager = chunkManager;
            this.seed = nukkitRandom.getSeed();
        }

        @Override
        public void populateStructure(int chunkX, int chunkZ) {

        }

        @Override
        public void generateChunk(int chunkX, int chunkZ) {
            description.generator.terrainGenerator.generateChunk(this, chunkX, chunkZ);
        }

        @Override
        public void populateChunk(int chunkX, int chunkZ) {
            description.generator.terrainGenerator.populateChunk(this, chunkX, chunkZ);
        }

        @Override
        public Map<String, Object> getSettings() {
            return settings;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Vector3 getSpawn() {
            return spawn;
        }

        @Override
        public ChunkManager getChunkManager() {
            return manager;
        }
    }

    public static class CustomDimensionDescription {
        private final int id;
        private final String name;
        private CustomGeneratorMethods.CustomGeneratorDescription generator;

        public CustomDimensionDescription(int id, String name){
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static final PointersStorage<CustomDimensionDescription> pointers = new PointersStorage<>("dimensions", new PointerGenFastest(), false);
    public static final Map<String, CustomDimensionDescription> descriptions = new HashMap<>();

    public static long nativeConstruct(int id, String name) {
        CustomDimensionDescription description = new CustomDimensionDescription(id, name);
        descriptions.put(name, description);
        return pointers.addPointer(description);
    }

    public static void nativeOverrideVanillaGenerator(int id, long generator) {
        InnerCoreServer.useNotCurrentSupport("nativeOverrideVanillaGenerator");
    }

    public static void nativeSetGenerator(long ptr, long generator) {
        pointers.get(ptr).generator = CustomGeneratorMethods.get(generator);
    }

    public static void init(){
        Generator.addGenerator(CustomDimensionGenerator.class, "inner_core_generator", 666);
    }

    public static void initLevel(Level level) {
        final String name = level.getName();
        CustomDimensionDescription description = descriptions.get(name);
        if(description == null) return;

        final Map<String, Object> options = level.requireProvider().getGeneratorOptions();

        options.put("id", description.getId());
        options.put("name", name);
    }

    public static void initLevels() {
        final Server server = Server.getInstance();

        for(CustomDimensionDescription description : descriptions.values()){
            final String name = description.getName();

            if (server.getLevelByName(name) == null) {
                server.generateLevel(name, System.currentTimeMillis(), CustomDimensionGenerator.class);
                server.loadLevel(name);
            }
        }
    }
}
