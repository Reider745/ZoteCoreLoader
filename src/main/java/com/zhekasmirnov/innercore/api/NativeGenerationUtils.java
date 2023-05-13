package com.zhekasmirnov.innercore.api;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.Random;

/**
 * Created by zheka on 21.09.2017.
 */

public class NativeGenerationUtils {
    @JSStaticFunction
    public static native boolean isTerrainBlock(int id);

    @JSStaticFunction
    public static native boolean isTransparentBlock(int id);

    @JSStaticFunction
    public static native boolean canSeeSky(int x, int y, int z);

    @JSStaticFunction
    public static native int findSurface(int x, int y, int z);

    public static native void generateOreNative(int x, int y, int z, int id, int data, int amount, boolean whitelist, int[] blockIds, int seed);

    public static native float nativeGetPerlinNoise(float x, float y, float z, int seed, float scale, int numOctaves);


    private static final int[] emptyOreBlacklist = new int[0];
    private static final int[] defaultOreWhitelist = new int[] {1, 87, 121};
    private static final Random defaultRandom = new Random();

    @JSStaticFunction
    public static void generateOre(int x, int y, int z, int id, int data, int amount, boolean dontCheck, int seed) {
        generateOreNative(x, y, z, id, data, amount, !dontCheck, dontCheck ? emptyOreBlacklist : defaultOreWhitelist, seed != 0 ? seed : defaultRandom.nextInt());
    }

    @JSStaticFunction
    public static void generateOreCustom(int x, int y, int z, int id, int data, int amount, boolean whitelist, NativeArray jsIds, int seed) {
        int[] ids = new int[(int) jsIds.getLength()];
        int i = 0;
        for (Object obj : jsIds.toArray()) {
            ids[i++] = ((Number) obj).intValue();
        }
        generateOreNative(x, y, z, id, data, amount, whitelist, ids, seed != 0 ? seed : defaultRandom.nextInt());
    }

    @JSStaticFunction
    public static double getPerlinNoise(double x, double y, double z, int seed, double scale, int numOctaves) {
        if (numOctaves < 1) {
            numOctaves = 1;
        }
        if (scale <= 0) {
            scale = 1;
        }
        if (seed == 0) {
            seed = 6700417;
        }
        return nativeGetPerlinNoise((float) x, (float) y, (float) z, seed, (float)  scale, numOctaves);
    }
}
