package com.reider745.hooks;

import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.world.BiomesMethods;
import com.zhekasmirnov.innercore.api.NativeCallback;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

@Hooks(className = "cn.nukkit.level.biome.Biome")
public class BiomesHooks implements HookClass {
    private static final int MAX_ID = 512;

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if (name.equals("biomes")) {
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        }
    }

    public static void init() throws NoSuchFieldException {
        ReflectHelper.setField(Biome.class, "biomes", new Biome[MAX_ID]);
    }

    @Inject(className = "cn.nukkit.network.protocol.BiomeDefinitionListPacket", type = TypeHook.BEFORE_REPLACE)
    public static void encode(BiomeDefinitionListPacket packet) {
        BiomesMethods.encode(packet);
    }

    private static final LongObjectMap<Biome[][]> populatingChunks = new LongObjectHashMap<>();

    @Inject(className = "cn.nukkit.level.generator.Normal")
    public static void generateChunk(Normal generator, int chunkX, int chunkZ) {
        populatingChunks.put(Thread.currentThread().getId(), new Biome[16][16]);
        NativeCallback.onBiomeMapGenerated(generator.getDimensionData().getDimensionId(), chunkX, chunkZ);
    }

    @Inject(className = "cn.nukkit.level.generator.Normal")
    public static Biome pickBiome(Normal generator, int x, int z) {
        Biome[][] biomes = populatingChunks.get(Thread.currentThread().getId());
        if (biomes != null) {
            Biome biome = biomes[x & 0xf][z & 0xf];
            if (biome != null) {
                return biome;
            }
        }
        return ((BiomeSelector) ReflectHelper.getField(generator, "selector")).pickBiome(x, z);
    }

    public static void setBiomeMap(int x, int z, int id) {
        Biome[][] biomes = populatingChunks.get(Thread.currentThread().getId());
        if (biomes != null) {
            biomes[x & 0xf][z & 0xf] = id >= 0 && id < MAX_ID ? Biome.biomes[id] : null;
        }
    }
}
