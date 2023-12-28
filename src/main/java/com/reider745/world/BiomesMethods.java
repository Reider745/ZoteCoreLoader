package com.reider745.world;

import cn.nukkit.level.biome.Biome;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import com.reider745.api.ReflectHelper;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Objects;

/*
format BiomeDefinitionListPacket
name: {
    temperature
    downfall
}
 */
public class BiomesMethods {
    private static class NukkitCustomBiome extends Biome {
        private final String name;

        public NukkitCustomBiome(String name){
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        protected void reg(int id){
            register(id, this);
        }
    }

    private static final PointersStorage<NukkitCustomBiome> customBiomesPointers = new PointersStorage<>("biomes", new PointerGenFastest(), false);
    private static final HashMap<Integer, NukkitCustomBiome> customBiomes = new HashMap<>();
    private static final CompoundTag TAG_419;
    private static byte[] sendBiomes;

    static {
        try {
            TAG_419 = NBTIO.read((byte[]) Objects.requireNonNull(ReflectHelper.getField(BiomeDefinitionListPacket.class, "TAG_419")), ByteOrder.LITTLE_ENDIAN, true);
            sendBiomes = NBTIO.write(TAG_419, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int pre_id = 256;

    public static long nativeRegister(String name) {
        NukkitCustomBiome biome = new NukkitCustomBiome(name);
        biome.reg(pre_id++);
        customBiomes.put(biome.getId(), biome);
        try{
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
        }
        return customBiomesPointers.addPointer(biome);
    }

    public static int nativeGetId(long pointer) {
        return customBiomesPointers.get(pointer).getId();
    }

    public static void encode(BiomeDefinitionListPacket self) {
        self.reset();
        self.put(sendBiomes);
    }
}
