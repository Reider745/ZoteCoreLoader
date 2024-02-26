package com.reider745.world;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.math.NukkitRandom;
import com.reider745.InnerCoreServer;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class BiomesMethods {
    public static class NukkitCustomBiome extends Biome {
        private final String name;
        private final Biome base;

        public NukkitCustomBiome(Biome base, String name){
            this.name = name;
            this.base = base;
        }

        @Override
        public String getName() {
            return name;
        }

        protected void reg(int id){
            register(id, this);
        }

        @Override
        public void populateChunk(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random) {
            super.populateChunk(level, chunkX, chunkZ, random);

            //new BiomeSelector(random).pickBiome(chunkX * 16 + 7, chunkZ * 16 + 7).populateChunk(level, chunkX, chunkZ, random);
            /*final int POS_CHUNK_X = chunkX * 16;
            final int POS_CHUNK_Z = chunkZ * 16;
            final BiomeSelector selector = ;

            for(int x = 0;x < 16;x++) {
                final int X_POS = POS_CHUNK_X + x;

                for (int z = 0; z < 16; z++) {
                    final Biome biome = selector.pickBiome(X_POS, POS_CHUNK_Z + z);
                    biome.populateChunk();
                }
            }*/
        }
    }

    private static final PointersStorage<NukkitCustomBiome> customBiomesPointers = new PointersStorage<>("biomes", new PointerGenFastest(), false);
    private static final HashMap<Integer, NukkitCustomBiome> customBiomes = new HashMap<>();
    /*private static final CompoundTag TAG_419;
    private static byte[] sendBiomes;

    static {
        try {
            TAG_419 = NBTIO.read((byte[]) Objects.requireNonNull(ReflectHelper.getField(BiomeDefinitionListPacket.class, "TAG_419")), ByteOrder.LITTLE_ENDIAN, true);
            sendBiomes = NBTIO.write(TAG_419, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    private static final ArrayList<Integer> AvailableIDS = new ArrayList<>();

    static {
        try {
            JSONArray json = new JSONArray(new String(InnerCoreServer.class.getClassLoader().getResourceAsStream("available_ids_biomes.json").readAllBytes()));
            for (int i = 0; i < json.length(); i++)
                AvailableIDS.add(json.getInt(i));

            for(int i = 0;i < 50;i++)
                AvailableIDS.remove(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static JSONObject getCustomBiomes() {
        JSONObject json = new JSONObject();
        customBiomes.forEach((k, v) -> json.put(v.name, k));
        return json;
    }

    public static long nativeRegister(String name) {
        if(AvailableIDS.isEmpty()){
            throw new RuntimeException("MAX BIOMES!");
        }
        NukkitCustomBiome biome = new NukkitCustomBiome(null, name);
        biome.reg(AvailableIDS.remove(0));
        customBiomes.put(biome.getId(), biome);

        // без этого дерьма работает
        /*try{
            final CompoundTag tag = new CompoundTag();
            tag.putFloat("ash", .0f);
            tag.putFloat("blue_spores", .0f);
            tag.putFloat("red_spores", .0f);
            tag.putFloat("temperature", .5f);
            tag.putFloat("downfall", .5f);
            tag.putFloat("white_ash", .0f);

            final CompoundTag climate = new CompoundTag();
            climate.putFloat("ash", .0f);
            climate.putFloat("downfall", .5f);
            climate.putFloat("red_spores", .0f);
            climate.putFloat("temperature", .5f);
            climate.putFloat("blue_spores", .0f);
            climate.putFloat("white_ash", .0f);
            tag.put("minecraft:climate", climate);

            TAG_419.put(name, tag);
            sendBiomes = NBTIO.write(TAG_419, ByteOrder.LITTLE_ENDIAN, true);
        }catch (Exception e){
            throw new RuntimeException(e);
        }*/
        return customBiomesPointers.addPointer(biome);
    }

    public static int nativeGetId(long pointer) {
        return customBiomesPointers.get(pointer).getId();
    }

    /*public static void encode(BiomeDefinitionListPacket self) {
        self.reset();
        self.put(sendBiomes);
    }*/
}
