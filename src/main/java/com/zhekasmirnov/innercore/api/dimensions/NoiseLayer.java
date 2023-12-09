package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.InnerCoreServer;

public class NoiseLayer {
    public final long pointer = 0;

    public NoiseLayer addOctave(NoiseOctave octave) {
        InnerCoreServer.useNotCurrentSupport("NoiseLayer.addOctave(octave)");
        return this;
    }

    public NoiseLayer setConversion(NoiseConversion conversion) {
        InnerCoreServer.useNotCurrentSupport("NoiseLayer.setConversion(conversion)");
        return this;
    }

    public NoiseLayer setGridSize(int gridSize) {
        InnerCoreServer.useNotCurrentSupport("NoiseLayer.setGridSize(gridSize)");
        return this;
    }
}
