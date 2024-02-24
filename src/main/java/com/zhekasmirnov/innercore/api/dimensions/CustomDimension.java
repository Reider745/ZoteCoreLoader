package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;
import com.reider745.world.dimensions.DimensionsMethods;
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
        this.id = prefferedId;
        this.name = name;
        this.pointer = DimensionsMethods.nativeConstruct(id, name);
        dimensionById.put(id, this);
        dimensionByName.put(name, this);
    }

    private CustomDimensionGenerator generator;

    public CustomDimension setGenerator(CustomDimensionGenerator generator) {
        DimensionsMethods.nativeSetGenerator(pointer, generator != null ? generator.pointer : 0);
        this.generator = generator;
        return this;
    }

    public CustomDimensionGenerator getGenerator() {
        return generator;
    }

    private boolean hasSkyLight = true;

    public CustomDimension setHasSkyLight(boolean value) {
        InnerCoreServer.useNotCurrentSupport("CustomDimension.setHasSkyLight(value)");
        this.hasSkyLight = value;
        return this;
    }

    public boolean hasSkyLight() {
        return hasSkyLight;
    }

    @Deprecated(since = "Zote")
    public CustomDimension setSkyColor(float r, float g, float b) {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension resetSkyColor() {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension setFogColor(float r, float g, float b) {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension resetFogColor() {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension setCloudColor(float r, float g, float b) {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension resetCloudColor() {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension setSunsetColor(float r, float g, float b) {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension resetSusetColor() {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension setFogDistance(float start, float end) {
        return this;
    }

    @Deprecated(since = "Zote")
    public CustomDimension resetFogDistance() {
        return this;
    }

    public static void setCustomGeneratorForVanillaDimension(int id, CustomDimensionGenerator generator) {
        DimensionsMethods.nativeOverrideVanillaGenerator(id, generator.pointer);
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
