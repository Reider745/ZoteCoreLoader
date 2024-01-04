package com.reider745.world;

import cn.nukkit.Server;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.HashMap;
import java.util.Map;

public class DimensionsMethods {
    public static class CustomDimensionGenerator extends Generator {
        private ChunkManager manager;
        private int dimensionId;
        private Vector3 spawn = new Vector3(0, 0, 0);
        private String name;
        private final Map<String, Object> settings;
        private final DimensionData data;

        public CustomDimensionGenerator(Map<String, Object> settings){
            this.settings = settings;

            dimensionId = (int) settings.get("id");
            name = (String) settings.get("name");
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
        }

        @Override
        public void populateStructure(int chunkX, int chunkZ) {

        }

        @Override
        public void generateChunk(int chunkX, int chunkZ) {
            final BaseFullChunk chunk = manager.getChunk(chunkX, chunkZ);
            chunk.setBlock(0, 0, 0, 1, 0);
        }

        @Override
        public void populateChunk(int chunkX, int chunkZ) {

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

    private static final PointersStorage<CustomDimensionDescription> customDimensionsPointers = new PointersStorage<>("dimensions", new PointerGenFastest(), false);
    public static final Map<String, CustomDimensionDescription> descriptions = new HashMap<>();

    public static long nativeConstruct(int id, String name) {
        CustomDimensionDescription description = new CustomDimensionDescription(id, name);
        descriptions.put(name, description);
        return customDimensionsPointers.addPointer(description);
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
