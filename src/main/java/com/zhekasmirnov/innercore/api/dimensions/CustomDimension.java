package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.HashMap;

public class CustomDimension {
    private static final HashMap<String, CustomDimension> dimensionByName = new HashMap<>();
    private static final HashMap<Integer, CustomDimension> dimensionById = new HashMap<>();

    public final long pointer = 0;
    public final int id;
    public final String name;

    public CustomDimension(String name, int prefferedId) {
        if (prefferedId < 3) {
            throw new IllegalArgumentException("preffered dimension id must be >= 3");
        }
        if (dimensionByName.containsKey(name)) {
            ICLog.i("WARNING", "dimensions have duplicate name: " + name);
        }
        this.id = prefferedId;
        this.name = name;
        dimensionById.put(id, this);
        dimensionByName.put(name, this);
    }

    private CustomDimensionGenerator generator;

    public CustomDimension setGenerator(CustomDimensionGenerator generator) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setGenerator(generator)");
        this.generator = generator;
        return this;
    }

    public CustomDimensionGenerator getGenerator() {
        return generator;
    }

    private boolean hasSkyLight = true;

    public CustomDimension setHasSkyLight(boolean value) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setHasSkyLight(value)");
        return this;
    }

    public boolean hasSkyLight() {
        return hasSkyLight;
    }

    public CustomDimension setSkyColor(float r, float g, float b) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setSkyColor(r, g, b)");
        return this;
    }

    public CustomDimension resetSkyColor() {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.resetSkyColor()");
        return this;
    }

    public CustomDimension setFogColor(float r, float g, float b) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setFogColor(r, g, b)");
        return this;
    }

    public CustomDimension resetFogColor() {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.resetFogColor()");
        return this;
    }

    public CustomDimension setCloudColor(float r, float g, float b) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setCloudColor(r, g, b)");
        return this;
    }

    public CustomDimension resetCloudColor() {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.resetCloudColor()");
        return this;
    }

    public CustomDimension setSunsetColor(float r, float g, float b) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setSunsetColor(r, g, b)");
        return this;
    }

    public CustomDimension resetSusetColor() {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.resetSusetColor()");
        return this;
    }

    public CustomDimension setFogDistance(float start, float end) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setFogDistance(start, end)");
        return this;
    }

    public CustomDimension resetFogDistance() {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.resetFogDistance()");
        return this;
    }

    public static void setCustomGeneratorForVanillaDimension(int id, CustomDimensionGenerator generator) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setCustomGeneratorForVanillaDimension(id, generator)");
    }

    public static CustomDimension getDimensionByName(String name) {
        return dimensionByName.get(name);
    }

    public static CustomDimension getDimensionById(int id) {
        return dimensionById.get(id);
    }

    public static boolean isLimboId(int id) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.isLimboId(id)");
        return false;
    }
}
