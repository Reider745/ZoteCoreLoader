package org.innercore.icstd.modules;

import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.DimensionsBindGenBase;

@ApiBinding("Dimensions")
public class DimensionsModule extends DimensionsBindGenBase {
	// ?

    @ApiBinding
    public static void setEvaporatesLiquids(int dimensionId, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("Dimension.setEvaporatesLiquids");
    }

    @ApiBinding
    public static void setWeatherSeasons(int dimensionId, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("Dimension.setWeatherSeasons");
    }

    @ApiBinding
    public static void setHasSkylight(int dimensionId, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("Dimension.setHasSkylight");
    }

    @ApiBinding
    public static int getMoonPhase(int dimensionId) {
        InnerCoreServer.useNotCurrentSupport("Dimension.getMoonPhase");
        return 0;
    }

    @ApiBinding
    public static float getMoonBrightness(int dimensionId) {
        InnerCoreServer.useNotCurrentSupport("Dimension.getMoonBrightness");
        return 0;
    }

    @ApiBinding
    @ClientOnly
    public static void setShouldRenderStars(int dimensionId, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setShouldRenderSun(int dimensionId, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setShouldRenderMoon(int dimensionId, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setShouldRenderClouds(int dimensionId, boolean enabled) {}

    @ApiBinding
    @ClientOnly
    public static void setStarBrightness(int dimensionId, float brightness) {}

    @ApiBinding
    @ClientOnly
    public static void resetStarBrightness(int dimensionId) {}

}
