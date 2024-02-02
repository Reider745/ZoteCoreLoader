package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseLayerMethods;

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

    private static long nativeConstruct(){
        return NoiseLayerMethods.nativeConstruct();
    }
    private static void nativeAddOctave(long ptr, long octave){
        NoiseLayerMethods.nativeAddOctave(ptr, octave);
    }
    private static void nativeSetConversion(long ptr, long conversion){
        NoiseLayerMethods.nativeSetConversion(ptr, conversion);
    }
    private static void nativeSetGridSize(long ptr, int grid){
        NoiseLayerMethods.nativeSetGridSize(ptr, grid);
    }
}
