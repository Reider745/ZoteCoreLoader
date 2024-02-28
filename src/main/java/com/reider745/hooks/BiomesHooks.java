package com.reider745.hooks;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.level.generator.noise.nukkit.f.SimplexF;
import cn.nukkit.math.NukkitRandom;
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


//Забейте хуй товарищи, нам этот класс больше не нужен
//Нахуя он тут?
//А нахуй он тут не нужен

@Hooks(className = "cn.nukkit.level.biome.Biome")
public class BiomesHooks implements HookClass {
    /*
    Биомов не может быть больше 256, слишком много ограничений внутри Nukkit-Mot и вероятно это ограничено и клиентом
     */
    //private static final int MAX_ID = Biome.MAX_BIOMES;
    /*

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if (name.equals("biomes")) {
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        }
    }

    public static void init() throws NoSuchFieldException {
        ReflectHelper.setField(Biome.class, "biomes", new Biome[MAX_ID]);
    }*/

    /*@Inject(className = "cn.nukkit.network.protocol.BiomeDefinitionListPacket", type = TypeHook.BEFORE_REPLACE)
    public static void encode(BiomeDefinitionListPacket packet) {
        BiomesMethods.encode(packet);
    }*/



    /*@Inject(className = "cn.nukkit.level.generator.Normal")
    public static void generateChunk(Normal generator, int chunkX, int chunkZ) {
        populatingChunks.put(Thread.currentThread().getId(), new Biome[16][16]);
        NativeCallback.onBiomeMapGenerated(generator.getDimensionData().getDimensionId(), chunkX, chunkZ);
    }*/

   /* public static class CustomBiomeSelector extends BiomeSelector {
        public CustomBiomeSelector(NukkitRandom random) {
            super(random);
        }

        @Override
        public Biome pickBiome(int x, int z) {
            Biome[][] biomes =  BiomesMethods.getBiomesChunk();
            if (biomes != null) {
                Biome biome = biomes[x & 0xf][z & 0xf];
                if (biome != null)
                    return biome;
            }
            return super.pickBiome(x, z);
        }
    }*/

    /*@Inject(className = "cn.nukkit.level.generator.Normal", type = TypeHook.AFTER_NOT_REPLACE)
    public static void init(Normal generator, ChunkManager manager, NukkitRandom random) {
        ReflectHelper.setField(generator, "selector", new CustomBiomeSelector(random));
    }*/
}
