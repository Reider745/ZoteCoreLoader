package com.zhekasmirnov.innercore.api.biomes;


import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.Map;

public class CustomBiome {
    private static final Map<String, CustomBiome> allCustomBiomes = new HashMap<>();

    public static Map<String, CustomBiome> getAllCustomBiomes() {
        return allCustomBiomes;
    }


    public final long pointer;
    public final int id;
    public final String name;

    public CustomBiome(String name) {
        allCustomBiomes.put(name, this);
        this.pointer = nativeRegister(name);
        this.id = nativeGetId(pointer);
        this.name = name;
    }

    public boolean isInvalid() {
        return nativeIsInvalid(pointer);
    }

    public CustomBiome setGrassColor(int color) {
        nativeSetGrassColor(pointer, color);
        return this;
    }

    public CustomBiome setGrassColor(float r, float g, float b) {
        return setGrassColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8) | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setSkyColor(int color) {
        nativeSetSkyColor(pointer, color);
        return this;
    }

    public CustomBiome setSkyColor(float r, float g, float b) {
        return setGrassColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8) | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setFoliageColor(int color) {
        nativeSetFoliageColor(pointer, color);
        return this;
    }

    public CustomBiome setFoliageColor(float r, float g, float b) {
        return setFoliageColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8) | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setWaterColor(int color) {
        nativeSetWaterColor(pointer, color);
        return this;
    }

    public CustomBiome setWaterColor(float r, float g, float b) {
        return setWaterColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8) | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setTemperatureAndDownfall(float temp, float downfall) {
        nativeSetTemperatureAndDownfall(pointer, temp, downfall);
        return this;
    }

    public CustomBiome setCoverBlock(int id, int data) {
        nativeSetCoverBlock(pointer, id, data);
        return this;
    }

    public CustomBiome setSurfaceBlock(int id, int data) {
        nativeSetSurfaceBlock(pointer, id, data);
        return this;
    }

    public CustomBiome setFillingBlock(int id, int data) {
        nativeSetFillingBlock(pointer, id, data);
        return this;
    }

    public CustomBiome setSeaFloorBlock(int id, int data) {
        nativeSetSeaFloorBlock(pointer, id, data);
        return this;
    }

    @Deprecated
    public CustomBiome setAdditionalBlock(int id, int data) {
        return setSeaFloorBlock(id, data);
    }

    public CustomBiome setSeaFloorDepth(int depth) {
        nativeSetSeaFloorDepth(pointer, depth);
        return this;
    }

    @Deprecated
    public CustomBiome setSurfaceParam(int depth) {
        return setSeaFloorDepth(depth);
    }

    public CustomBiome setServerJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            throw new IllegalArgumentException("failed to parse biome server json: " + e.getMessage(), e);
        }
        nativeSetServerJson(pointer, json);
        return this;
    }

    public CustomBiome setClientJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            throw new IllegalArgumentException("failed to parse biome client json: " + e.getMessage(), e);
        }
        nativeSetClientJson(pointer, json);
        return this;
    }


    private static native long nativeRegister(String name);
    private static native int nativeGetId(long pointer);
    private static native boolean nativeIsInvalid(long pointer);
    private static native void nativeSetGrassColor(long pointer, int color);
    private static native void nativeSetSkyColor(long pointer, int color);
    private static native void nativeSetFoliageColor(long pointer, int color);
    private static native void nativeSetWaterColor(long pointer, int color);
    private static native void nativeSetTemperatureAndDownfall(long pointer, float temp, float downfall);
    private static native void nativeSetCoverBlock(long pointer, int id, float data);
    private static native void nativeSetSurfaceBlock(long pointer, int id, float data);
    private static native void nativeSetFillingBlock(long pointer, int id, float data);
    private static native void nativeSetSeaFloorBlock(long pointer, int id, float data);
    private static native void nativeSetSeaFloorDepth(long pointer, int depth);
    private static native void nativeSetClientJson(long pointer, String json);
    private static native void nativeSetServerJson(long pointer, String json);
    
}