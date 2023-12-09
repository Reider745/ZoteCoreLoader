package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;

public class NoiseGenerator {
    public final long pointer = 0;

    public NoiseGenerator addLayer(NoiseLayer layer) {
        InnerCoreServer.useNotCurrentSupport("NoiseGenerator.addLayer(layer)");
        return this;
    }

    public NoiseGenerator setConversion(NoiseConversion conversion) {
        InnerCoreServer.useNotCurrentSupport("NoiseGenerator.setConversion(conversion)");
        return this;
    }
}
