package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimension;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimensionGenerator;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zheka on 07.09.2017.
 */

public class WorldGen {
    public static final int LEVEL_PRE_VANILLA = 0;
    public static final int LEVEL_POST_VANILLA = 1;
    public static final List<Integer> allLevels = new ArrayList<>();
    static {
        allLevels.add(LEVEL_PRE_VANILLA);
        allLevels.add(LEVEL_POST_VANILLA);
    }

    private static final int MODE_SURFACE = 0;
    private static final int MODE_NETHER = 1;
    private static final int MODE_END = 2;
    private static final int MODE_CUSTOM = 3;

    private static void invokeGenerateChunkCallback(String name, int chunkX, int chunkZ, int dimensionId) {
        /* int */ long worldSeed = NativeAPI.getSeed();
        long seed = (long) worldSeed + (chunkX ^ 836430234L) * (chunkZ ^ 827774123L);
        Random random = new Random(seed);
        List<Runnable> callbacks = Callback.getCallbackAsRunnableList(name, new Object[] { chunkX, chunkZ, random,
                dimensionId, seed, worldSeed, worldSeed ^ (dimensionId * 131071) });
        for (Runnable callback : callbacks) {
            try {
                callback.run();
            } catch (Throwable err) {
                Logger.error("error occurred in chunk generation " + chunkX + ", " + chunkZ
                        + " for dimension " + dimensionId, err);
            }
        }
    }

    private static void generateChunkByLevelAndMode(int chunkX, int chunkZ, int dimensionId, int level, int mode) {
        // legacy generation callbacks called when post processing
        if (level == LEVEL_POST_VANILLA) {
            switch (mode) {
                case MODE_SURFACE:
                    invokeGenerateChunkCallback("GenerateChunk", chunkX, chunkZ, dimensionId);
                    invokeGenerateChunkCallback("GenerateChunkUnderground", chunkX, chunkZ, dimensionId);
                    break;
                case MODE_NETHER:
                    invokeGenerateChunkCallback("GenerateNetherChunk", chunkX, chunkZ, dimensionId);
                    break;
                case MODE_END:
                    invokeGenerateChunkCallback("GenerateEndChunk", chunkX, chunkZ, dimensionId);
                    break;
                case MODE_CUSTOM:
                    invokeGenerateChunkCallback("GenerateCustomDimensionChunk", chunkX, chunkZ, dimensionId);
                    break;
            }

            // execute legacy universal generation
            invokeGenerateChunkCallback("GenerateChunkUniversal", chunkX, chunkZ, dimensionId);
        }

        // execute world generation by level (new universal generation)
        switch (level) {
            case LEVEL_PRE_VANILLA:
                invokeGenerateChunkCallback("PreProcessChunk", chunkX, chunkZ, dimensionId);
                break;
            case LEVEL_POST_VANILLA:
                invokeGenerateChunkCallback("PostProcessChunk", chunkX, chunkZ, dimensionId);
                break;
        }
    }

    private static int dimensionIdToGenerationMode(int id) {
        switch (id) {
            case 0:
                return MODE_SURFACE;
            case 1:
                return MODE_NETHER;
            case 2:
                return MODE_END;
            default:
                return MODE_CUSTOM;
        }
    }

    public static void generateChunk(int x, int z, int level) {
        NativeBlockSource blockSource = NativeBlockSource.getCurrentWorldGenRegion();
        int dimension = blockSource != null ? blockSource.getDimension() : -1;
        if (dimension == -1) {
            ICLog.i("ERROR", "generating chunk without block source");
            return;
        }

        // if feature is not supported, call every generation level at once
        if (!MinecraftVersions.getCurrent()
                .isFeatureSupported(MinecraftVersion.FEATURE_VANILLA_WORLD_GENERATION_LEVELS)) {
            for (int lvl : allLevels) {
                generateChunk(x, z, dimension, lvl);
            }
        } else {
            generateChunk(x, z, dimension, level);
        }
    }

    public static void generateChunk(int x, int z, int dimensionId, int level) {
        // execute base vanilla dimension world gen
        CustomDimension customDimension = CustomDimension.getDimensionById(dimensionId);
        if (customDimension != null) {
            CustomDimensionGenerator generator = customDimension.getGenerator();
            if (generator != null) {
                int baseDimensionId = generator.getModGenerationBaseDimension();
                if (baseDimensionId != -1) {
                    generateChunkByLevelAndMode(x, z, baseDimensionId, level,
                            dimensionIdToGenerationMode(baseDimensionId));
                }
            }
        }

        // execute dimension based generation
        generateChunkByLevelAndMode(x, z, dimensionId, level, dimensionIdToGenerationMode(dimensionId));
    }

    public static void onBiomeMapGenerated(int dimensionId, int x, int z) {
        invokeGenerateChunkCallback("GenerateBiomeMap", x, z, dimensionId);
    }

    public static class ChunkPos {
        public final int dimension, x, z;

        public ChunkPos(int dimension, int x, int z) {
            this.dimension = dimension;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ChunkPos) {
                ChunkPos other = (ChunkPos) obj;
                return x == other.x && z == other.z && dimension == other.dimension;
            }
            return false;
        }
    }
}
