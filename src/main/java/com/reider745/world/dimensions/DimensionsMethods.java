package com.reider745.world.dimensions;

import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.reider745.InnerCoreServer;
import com.reider745.api.ReflectHelper;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;
import com.reider745.world.FakeDimensions;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionsMethods {
    private static boolean LEVELS_LOADED = false;

    public static class CustomDimensionGenerator extends Generator {
        public ChunkManager manager;
        public int dimensionId;
        public Vector3 spawn = new Vector3(0, 0, 0);
        public long seed;
        public String name;
        public final Map<String, Object> settings;
        public DimensionData data;
        public CustomDimensionDescription description;

        public CustomDimensionGenerator(Map<String, Object> settings){
            this.settings = settings;

            if(settings.containsKey("id")){
                this.updateDescription((String) settings.get("name"), (int) settings.get("id"));
            }else{
                this.updateDescription(UUID.randomUUID().toString(), Level.DIMENSION_THE_END);
                Logger.debug("NOT INFORMATION LOADED DIMENSIONS!!!");
            }
        }

        public void updateDescription(String name, int id){
            this.dimensionId = id;
            this.name = name;
            description = descriptions.get(name);
            if(description == null){
                description = new CustomDimensionDescription(id, name);
                description.generator = LIMBO_GENERATOR;
            }
            this.data = new DimensionData(id, 0, 255);
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

        public void loadLevel(Server server) {
            final String name = this.getName();

            if (server.getLevelByName(name) == null) {
                server.generateLevel(name, System.currentTimeMillis(), CustomDimensionGenerator.class);
                server.loadLevel(name);
            }
            this.initLevel(server.getLevelByName(name));
        }

        public void initLevel(Level level){
            final Map<String, Object> options = level.requireProvider().getGeneratorOptions();
            final int id = this.getId();

            options.put("id", id);
            options.put("name", name);

            if(!FakeDimensions.canIgnore(id))
                FakeDimensions.initLevel(id, level);
        }
    }

    private static final PointersStorage<CustomDimensionDescription> pointers = new PointersStorage<>("dimensions", new PointerGenFastest(), false);
    public static final Map<String, CustomDimensionDescription> descriptions = new HashMap<>();

    private static final HashMap<String, Integer> dimensionsId = new LinkedHashMap<>();
    private static final CustomGeneratorMethods.CustomGeneratorDescription LIMBO_GENERATOR;

    static {
        try{
            final File file = new File("dimensionsids.json");
            if(file.exists()){
                final JSONObject json = FileUtils.readJSON(file);

                for(String key : json.keySet())
                    dimensionsId.put(key, json.getInt(key));
            }
        }catch (IOException e){
            Logger.error("Save dimensions id", e);
            throw new RuntimeException("Error loaded ZoteCoreLoader");
        }

        LIMBO_GENERATOR = new CustomGeneratorMethods.CustomGeneratorDescription(0);
        var monobiome = new MonoBiomeTerrainMethods.MonoBiomeDescription();
        monobiome.layers.add(TerrainLayerMethods.newLayer(monobiome, 0, 128));
        LIMBO_GENERATOR.terrainGenerator = monobiome;
    }

    public static long nativeConstruct(int id, String name) {
        FakeDimensions.registerIgnore(id);
        CustomDimensionDescription description = new CustomDimensionDescription(id, name);
        descriptions.put(name, description);

        final Level level = Server.getInstance().getLevelByName(name);
        if(level != null && level.getGenerator() instanceof CustomDimensionGenerator generator) {
            generator.updateDescription(name, id);
            description.initLevel(level);
            ReflectHelper.setField(level, "dimensionData", generator.data);
        }else if(LEVELS_LOADED){
            description.loadLevel(Server.getInstance());
        }

        dimensionsId.put(name, id);
        try{
            FileUtils.writeJSON(new File("dimensionsids.json"), new JSONObject(dimensionsId));
        }catch (IOException e){
            Logger.error("Save dimensions id", e);
        }

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

    public static void initLevels() {
        final Server server = Server.getInstance();

        for(String key : dimensionsId.keySet()) {
            final Level level = server.getLevelByName(key);
            if(level != null && !descriptions.containsKey(key)){
                final int id = dimensionsId.get(key);
                Logger.debug("Loaded limbo "+key+", id: "+id);
                CustomDimensionDescription description = new CustomDimensionDescription(id, key);
                description.generator = LIMBO_GENERATOR;
                description.initLevel(level);
                descriptions.put(key, description);
            }
        }

        for(CustomDimensionDescription description : descriptions.values())
            description.loadLevel(server);

        LEVELS_LOADED = true;
    }
}
