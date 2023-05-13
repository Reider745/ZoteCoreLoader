package com.zhekasmirnov.innercore.api.dimensions;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.HashMap;


public class CustomDimension {
    private static final HashMap<String, CustomDimension> dimensionByName = new HashMap<>();
    private static final HashMap<Integer, CustomDimension> dimensionById = new HashMap<>();
    
    public final long pointer;
    public final int id;
    public final String name;

    public CustomDimension(String name, int prefferedId) {
        if (prefferedId < 3) {
            throw new IllegalArgumentException("preffered dimension id must be >= 3");
        }
        if (dimensionByName.containsKey(name)) {
            ICLog.i("WARNING", "dimensions have duplicate name: " + name);
        }
        int id = prefferedId;
        while (nativeGetCustomDimensionById(id) != 0 && !nativeIsLimboDimensionId(id)) {
            id++;
        }
        this.id = id;
        this.name = name;
        pointer = nativeConstruct(id, name);
        dimensionById.put(id, this);
        dimensionByName.put(name, this);
    }

    private CustomDimensionGenerator generator;
    public CustomDimension setGenerator(CustomDimensionGenerator generator) {
        nativeSetGenerator(pointer, generator != null ? generator.pointer : 0);
        this.generator = generator;
        return this;
    }

    public CustomDimensionGenerator getGenerator() {
        return generator;
    }

    private boolean hasSkyLight = true;
    public CustomDimension setHasSkyLight(boolean value) {
        nativeSetHasSkyLight(pointer, value);
        return this;
    }

    public boolean hasSkyLight() {
        return hasSkyLight;
    }
    
    public CustomDimension setSkyColor(float r, float g, float b) {
        nativeSetSkyColor(pointer, r, g, b);
        return this;
    }
    
    public CustomDimension resetSkyColor() {
        nativeResetSkyColor(pointer);
        return this;
    }
    
    public CustomDimension setFogColor(float r, float g, float b) {
        nativeSetFogColor(pointer, r, g, b);
        return this;
    }
    
    public CustomDimension resetFogColor() {
        nativeResetFogColor(pointer);
        return this;
    }
    
    public CustomDimension setCloudColor(float r, float g, float b) {
        nativeSetCloudColor(pointer, r, g, b);
        return this;
    }
    
    public CustomDimension resetCloudColor() {
        nativeResetCloudColor(pointer);
        return this;
    }
    
    public CustomDimension setSunsetColor(float r, float g, float b) {
        nativeSetSunsetColor(pointer, r, g, b);
        return this;
    }
    
    public CustomDimension resetSusetColor() {
        nativeResetSunsetColor(pointer);
        return this;
    }
    
    public CustomDimension setFogDistance(float start, float end) {
        nativeSetFogDistance(pointer, start, end);
        return this;
    }
    
    public CustomDimension resetFogDistance() {
        nativeResetFogDistance(pointer);
        return this;
    }


    public static void setCustomGeneratorForVanillaDimension(int id, CustomDimensionGenerator generator) {
        nativeOverrideVanillaGenerator(id, generator.pointer);
    }

    public static CustomDimension getDimensionByName(String name) {
        return dimensionByName.get(name);
    }

    public static CustomDimension getDimensionById(int id) {
        return dimensionById.get(id);
    }

    public static boolean isLimboId(int id) {
        return nativeIsLimboDimensionId(id);
    }

    private static native long nativeConstruct(int id, String name);
    private static native long nativeGetCustomDimensionById(int id);
    private static native boolean nativeIsLimboDimensionId(int id);
    private static native void nativeOverrideVanillaGenerator(int id, long generator);
    private static native void nativeSetGenerator(long ptr, long generator);
    private static native void nativeSetHasSkyLight(long ptr, boolean value);
    private static native void nativeSetSkyColor(long ptr, float r, float g, float b);
    private static native void nativeResetSkyColor(long ptr);
    private static native void nativeSetFogColor(long ptr, float r, float g, float b);
    private static native void nativeResetFogColor(long ptr);
    private static native void nativeSetCloudColor(long ptr, float r, float g, float b);
    private static native void nativeResetCloudColor(long ptr);
    private static native void nativeSetSunsetColor(long ptr, float r, float g, float b);
    private static native void nativeResetSunsetColor(long ptr);
    private static native void nativeSetFogDistance(long ptr, float start, float end);
    private static native void nativeResetFogDistance(long ptr);
}