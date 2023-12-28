package com.zhekasmirnov.innercore.api.biomes;

import com.reider745.world.BiomesMethods;
import org.json.JSONException;
import org.json.JSONObject;

import com.reider745.InnerCoreServer;

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
        InnerCoreServer.useNotCurrentSupport("CustomBiome.isInvalid()");
        return false;
    }

    public CustomBiome setGrassColor(int color) {
        return this;
    }

    public CustomBiome setGrassColor(float r, float g, float b) {
        return setGrassColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8)
                | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setSkyColor(int color) {
        return this;
    }

    public CustomBiome setSkyColor(float r, float g, float b) {
        return setGrassColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8)
                | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setFoliageColor(int color) {
        return this;
    }

    public CustomBiome setFoliageColor(float r, float g, float b) {
        return setFoliageColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8)
                | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setWaterColor(int color) {
        return this;
    }

    public CustomBiome setWaterColor(float r, float g, float b) {
        return setWaterColor(((((Math.round(r * 255) & 0xFF) << 8) | (Math.round(g * 255) & 0xFF)) << 8)
                | (Math.round(b * 255) & 0xFF));
    }

    public CustomBiome setTemperatureAndDownfall(float temp, float downfall) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setTemperatureAndDownfall(temp, downfall)");
        return this;
    }

    public CustomBiome setCoverBlock(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setCoverBlock(id, data)");
        return this;
    }

    public CustomBiome setSurfaceBlock(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setSurfaceBlock(id, data)");
        return this;
    }

    public CustomBiome setFillingBlock(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setFillingBlock(id, data)");
        return this;
    }

    public CustomBiome setSeaFloorBlock(int id, int data) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setSeaFloorBlock(id, data)");
        return this;
    }

    @Deprecated
    public CustomBiome setAdditionalBlock(int id, int data) {
        return setSeaFloorBlock(id, data);
    }

    public CustomBiome setSeaFloorDepth(int depth) {
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setSeaFloorDepth(depth)");
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
        InnerCoreServer.useNotCurrentSupport("CustomBiome.setServerJson(json)");
        return this;
    }

    public CustomBiome setClientJson(String json) {
        return this;
    }

    private static long nativeRegister(String name){
        return BiomesMethods.nativeRegister(name);
    }
    private static int nativeGetId(long pointer){
        return BiomesMethods.nativeGetId(pointer);
    }
}
