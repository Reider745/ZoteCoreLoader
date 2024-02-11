package com.reider745.hooks;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorOctavesF;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.world.BiomesMethods;
import com.zhekasmirnov.innercore.api.NativeCallback;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

import java.lang.reflect.Field;
import java.util.List;

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
    public static void encode(BiomeDefinitionListPacket self){
        BiomesMethods.encode(self);
    }

    private static final LongObjectMap<Biome[][]> threads_biomes_replace = new LongObjectHashMap<>();

    @Inject(className = "cn.nukkit.level.generator.Normal")
    public static void generateChunk(Normal self, final int chunkX, final int chunkZ)  {
        threads_biomes_replace.put(Thread.currentThread().getId(), new Biome[16][16]);
        NativeCallback.onBiomeMapGenerated(self.getDimension(), chunkX, chunkZ);
    }

    @Inject(className = "cn.nukkit.level.generator.Normal")
    public static Biome pickBiome(Normal self, int x, int z) {
        final Biome[][] biomes_override = threads_biomes_replace.get(Thread.currentThread().getId());
        if(biomes_override == null)
            return ((BiomeSelector) ReflectHelper.getField(self, "selector")).pickBiome(x, z);

        final Biome biome = biomes_override[Math.abs(x % 16)][Math.abs(z % 16)];
        if(biome != null)
            return biome;

        return ((BiomeSelector) ReflectHelper.getField(self, "selector")).pickBiome(x, z);
    }

    public static void setBiomeMap(int x, int z, int id){
        final Biome[][] biomes_override = threads_biomes_replace.get(Thread.currentThread().getId());
        if(biomes_override != null){
            biomes_override[x % 16][z % 16] = Biome.getBiome(id);
        }
    }
}
