package com.zhekasmirnov.innercore.api.dimension;

import com.reider745.InnerCoreServer;

/**
 * Created by zheka on 12.11.2017.
 */

@Deprecated
public class CustomDimension {
    public final long pointer;

    public final int id;
    public final Region region;

    private Teleporter teleporter;

    public CustomDimension(String strId) {
        pointer = nativeConstruct();

        DimensionRegistry.DimensionData data = DimensionRegistry.registerCustomDimension(strId);
        id = data.getId();
        region = data.getRegion();

        DimensionRegistry.mapCustomDimension(this);

        teleporter = new Teleporter(this);
    }

    public int getId() {
        return id;
    }

    public Region getRegion() {
        return region;
    }

    public Teleporter getTeleporter() {
        return teleporter;
    }

    public void addTerrainLayer(TerrainLayer layer) {
        nativeAddLayer(pointer, layer.pointer);
    }

    public void setSkyColor(float r1, float g1, float b1, float r2, float g2, float b2) {
        nativeSetSkyColor(pointer, r1, g1, b1, r2, g2, b2);
    }

    public void setSkyColor(float r, float g, float b) {
        nativeSetSkyColor(pointer, r, g, b, r, g, b);
    }

    public void setFogColor(float r1, float g1, float b1, float r2, float g2, float b2) {
        nativeSetFogColor(pointer, r1, g1, b1, r2, g2, b2);
    }

    public void setFogColor(float r, float g, float b) {
        nativeSetFogColor(pointer, r, g, b, r, g, b);
    }

    public void setGlobalBiome(int biome) {
        nativeSetGlobalBiome(pointer, biome);
    }

    public void setDecorationEnabled(boolean enabled) {
        nativeSetDecorationEnabled(pointer, enabled);
    }

    public void setDefaultBiomeCoverEnabled(boolean enabled) {
        nativeSetDefaultCoverEnabled(pointer, enabled);
    }

    public static long nativeConstruct() {
        InnerCoreServer.useNotSupport("CustomDimension.nativeConstruct()");
        return 0;
    }

    public static void nativeAddLayer(long self, long terrainLayer) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeAddLayer(self, terrainLayer)");
    }

    public static void nativeSetSkyColor(long self, float r1, float g1, float b1, float r2, float g2, float b2) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeSetSkyColor(self, r1, g1, b1, r2, g2, b2)");
    }

    public static void nativeSetFogColor(long self, float r1, float g1, float b1, float r2, float g2, float b2) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeSetFogColor(self, r1, g1, b1, r2, g2, b2)");
    }

    public static void nativeSetGlobalBiome(long self, int biome) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeSetGlobalBiome(self, biome)");
    }

    public static void nativeSetDecorationEnabled(long self, boolean enabled) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeSetDecorationEnabled(self, enabled)");
    }

    public static void nativeSetDefaultCoverEnabled(long self, boolean enabled) {
        InnerCoreServer.useNotSupport("CustomDimension.nativeSetDefaultCoverEnabled(self, enabled)");
    }
}
