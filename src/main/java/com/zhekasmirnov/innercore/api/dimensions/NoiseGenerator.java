package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseGeneratorMethods;

public class NoiseGenerator {
    public final long pointer;

    public NoiseGenerator() {
        pointer = nativeConstruct();
    }

    public NoiseGenerator addLayer(NoiseLayer layer) {
        nativeAddLayer(pointer, layer.pointer);
        return this;
    }

    public NoiseGenerator setConversion(NoiseConversion conversion) {
        nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }

    private static long nativeConstruct(){
        return NoiseGeneratorMethods.nativeConstruct();
    }
    private static void nativeAddLayer(long ptr, long layer){
        NoiseGeneratorMethods.nativeAddLayer(ptr, layer);
    }
    private static void nativeSetConversion(long ptr, long conversion){
        NoiseGeneratorMethods.nativeSetConversion(ptr, conversion);
    }
}
