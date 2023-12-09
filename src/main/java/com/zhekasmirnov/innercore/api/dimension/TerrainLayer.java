package com.zhekasmirnov.innercore.api.dimension;

import com.reider745.InnerCoreServer;

import android.graphics.Bitmap;

/**
 * Created by zheka on 12.11.2017.
 */

@Deprecated
public class TerrainLayer {
    public final long pointer;

    public TerrainLayer(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Unable to create TerrainLayer: min >= max");
        }

        if (min < 0 || max < 0 || min > 256 || max > 256) {
            throw new IllegalArgumentException(
                    "Unable to create TerrainLayer: min or max out of range: " + min + ", " + max);
        }

        pointer = nativeConstruct(min, max);
    }

    public void addNoiseMap(Noise.Map map) {
        nativeAddNoiseMap(pointer, map.pointer);
    }

    public void addHeightMap(Noise.Map map) {
        nativeAddHeightMap(pointer, map.pointer);
    }

    public void setYGradient(Noise.Gradient gradient) {
        nativeSetGradient(pointer, gradient.pointer);
    }

    public void setupTerrain(int id, int data) {
        nativeSetupTerrain(pointer, id, data);
    }

    public void setupCover(int height, int id1, int data1, int id2, int data2) {
        nativeSetupCover(pointer, height, id1, data1, id2, data2);
    }

    public void setupFilling(int height, int id, int data) {
        nativeSetupFilling(pointer, height, id, data);
    }

    public Bitmap visualizeSlice(int size, int stride, int color) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap visualizeMap(int size, int stride, int color) {
        return Bitmap.getSingletonInternalProxy();
    }

    public Bitmap visualizeAndShow(int width, int stride, int color, boolean mode) {
        return Bitmap.getSingletonInternalProxy();
    }

    public static long nativeConstruct(int min, int max) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeConstruct(min, max)");
        return 0;
    }

    public static void nativeAddNoiseMap(long self, long map) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeAddNoiseMap(self, map)");
    }

    public static void nativeAddHeightMap(long self, long map) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeAddHeightMap(self, map)");
    }

    public static void nativeSetGradient(long self, long gradient) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeSetGradient(self, gradient)");
    }

    public static void nativeSetupTerrain(long self, int id, int data) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeSetupTerrain(self, id, data)");
    }

    public static void nativeSetupCover(long self, int height, int id1, int data1, int id2, int data2) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeSetupCover(self, height, id1, data1, id2, data2)");
    }

    public static void nativeSetupFilling(long self, int height, int id, int data) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeSetupFilling(self, height, id, data)");
    }

    public static void nativeDebugStart(long self) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeDebugStart(self)");
    }

    public static float nativeDebugValue(long self, float x, float y, float z) {
        InnerCoreServer.useNotSupport("TerrainLayer.nativeDebugValue(self, x, y, z)");
        return 0;
    }
}
