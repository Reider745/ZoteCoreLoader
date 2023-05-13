package com.zhekasmirnov.innercore.api.dimensions;

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
    
    private static native long nativeConstruct();
    private static native void nativeAddLayer(long ptr, long layer);
    private static native void nativeSetConversion(long ptr, long conversion);
}