package com.zhekasmirnov.innercore.api.dimensions;

public class NoiseLayer {
    public final long pointer;

    public NoiseLayer() {
        pointer = nativeConstruct();
    }

    public NoiseLayer addOctave(NoiseOctave octave) {
        nativeAddOctave(pointer, octave.pointer);
        return this;
    }

    public NoiseLayer setConversion(NoiseConversion conversion) {
        nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }

    public NoiseLayer setGridSize(int gridSize) {
        nativeSetGridSize(pointer, gridSize);
        return this;
    }
    
    private static native long nativeConstruct();
    private static native void nativeAddOctave(long ptr, long octave);
    private static native void nativeSetConversion(long ptr, long conversion);
    private static native void nativeSetGridSize(long ptr, int grid);
    
}