package com.reider745.world;

import android.security.AppUriAuthenticationPolicy;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.reider745.api.CallbackHelper;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.Random;

public class GenerationUtilsMethods {
    public static boolean isTerrainBlock(int id){
        return id == BlockID.GRASS;
    }

    @JSStaticFunction
    public static boolean isTransparentBlock(int id){
        return Block.transparent[id];
    }

    @JSStaticFunction
    public static boolean canSeeSky(int x, int y, int z){
        return NativeBlockSource.getCurrentWorldGenRegion().canSeeSky(x, y, z);
    }

    @JSStaticFunction
    public static int findSurface(int x, int y, int z){
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if(region == null) throw new RuntimeException("Region null");
        for(int i = y;i < 256;i++){
            if(region.getBlockId(x, i, z) == 0)
                return i;
        }
        return y;
    }

    private static boolean isReplace(int[] ids, int id, boolean whitelist){
        if(whitelist){
            for(int id_ : ids)
                if(id_ == id)
                    return true;
            return false;
        }
        for(int id_ : ids)
            if(id_ == id)
                return false;
        return true;
    }

    public static void generateOreNative(int x, int y, int z, int id, int data, int clusterSize, boolean whitelist, int[] blockIds, int seed){
        Level level = CallbackHelper.getForCurrentThread();

        if(level == null) throw new RuntimeException("Region null");

        final NukkitRandom rand = new NukkitRandom();

        final float piScaled = rand.nextFloat() * (float) Math.PI;
        final double scaleMaxX = (float) (x + 8) + MathHelper.sin(piScaled) * (float) clusterSize / 8.0F;
        final double scaleMinX = (float) (x + 8) - MathHelper.sin(piScaled) * (float) clusterSize / 8.0F;
        final double scaleMaxZ = (float) (z + 8) + MathHelper.cos(piScaled) * (float) clusterSize / 8.0F;
        final double scaleMinZ = (float) (z + 8) - MathHelper.cos(piScaled) * (float) clusterSize / 8.0F;
        final double scaleMaxY = y + rand.nextBoundedInt(3) - 2;
        final double scaleMinY = y + rand.nextBoundedInt(3) - 2;

        for (int i = 0; i < clusterSize; ++i) {
            final float sizeIncr = (float) i / (float) clusterSize;
            final double scaleX = scaleMaxX + (scaleMinX - scaleMaxX) * (double) sizeIncr;
            final double scaleY = scaleMaxY + (scaleMinY - scaleMaxY) * (double) sizeIncr;
            final double scaleZ = scaleMaxZ + (scaleMinZ - scaleMaxZ) * (double) sizeIncr;
            final double randSizeOffset = rand.nextDouble() * (double) clusterSize / 16.0D;
            final double randVec1 = (double) (MathHelper.sin((float) Math.PI * sizeIncr) + 1.0F) * randSizeOffset + 1.0D;
            final double randVec2 = (double) (MathHelper.sin((float) Math.PI * sizeIncr) + 1.0F) * randSizeOffset + 1.0D;
            final int minX = NukkitMath.floorDouble(scaleX - randVec1 / 2.0D);
            final int minY = NukkitMath.floorDouble(scaleY - randVec2 / 2.0D);
            final int minZ = NukkitMath.floorDouble(scaleZ - randVec1 / 2.0D);
            final int maxX = NukkitMath.floorDouble(scaleX + randVec1 / 2.0D);
            final int maxY = NukkitMath.floorDouble(scaleY + randVec2 / 2.0D);
            final int maxZ = NukkitMath.floorDouble(scaleZ + randVec1 / 2.0D);

            for (int xSeg = minX; xSeg <= maxX; ++xSeg) {
                final double xVal = ((double) xSeg + 0.5D - scaleX) / (randVec1 / 2.0D);

                if (xVal * xVal < 1.0D) {
                    for (int ySeg = minY; ySeg <= maxY; ++ySeg) {
                        final double yVal = ((double) ySeg + 0.5D - scaleY) / (randVec2 / 2.0D);

                        if (xVal * xVal + yVal * yVal < 1.0D) {
                            for (int zSeg = minZ; zSeg <= maxZ; ++zSeg) {
                                final double zVal = ((double) zSeg + 0.5D - scaleZ) / (randVec1 / 2.0D);

                                if (xVal * xVal + yVal * yVal + zVal * zVal < 1.0D) {
                                    int block_replace = level.getBlock(xSeg, ySeg, zSeg).getId();
                                    if (block_replace != BlockID.AIR && isReplace(blockIds, block_replace, whitelist)) {
                                        level.setBlock(new Vector3(x, y, z), Block.get(id, data));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static float nativeGetPerlinNoise(float x, float y, float z, int seed, float scale, int numOctaves){
        return (float) Math.random();
    }
}
