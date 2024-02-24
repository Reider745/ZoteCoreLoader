package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseGeneratorMethods;

public class NoiseGenerator {
    public final long pointer;

    public NoiseGenerator() {
        pointer = NoiseGeneratorMethods.nativeConstruct();
    }

    public NoiseGenerator addLayer(NoiseLayer layer) {
        NoiseGeneratorMethods.nativeAddLayer(pointer, layer.pointer);
        return this;
    }

    public NoiseGenerator setConversion(NoiseConversion conversion) {
        NoiseGeneratorMethods.nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }
}
