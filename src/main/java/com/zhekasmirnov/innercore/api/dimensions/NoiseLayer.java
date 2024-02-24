package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseLayerMethods;

public class NoiseLayer {
    public final long pointer;

    public NoiseLayer() {
        pointer = NoiseLayerMethods.nativeConstruct();
    }

    public NoiseLayer addOctave(NoiseOctave octave) {
        NoiseLayerMethods.nativeAddOctave(pointer, octave.pointer);
        return this;
    }

    public NoiseLayer setConversion(NoiseConversion conversion) {
        NoiseLayerMethods.nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }

    public NoiseLayer setGridSize(int gridSize) {
        NoiseLayerMethods.nativeSetGridSize(pointer, gridSize);
        return this;
    }
}
