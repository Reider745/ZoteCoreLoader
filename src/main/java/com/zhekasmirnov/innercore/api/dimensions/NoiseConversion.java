package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseConversionMethods;

public class NoiseConversion {
    public final long pointer;

    public NoiseConversion() {
        pointer = NoiseConversionMethods.nativeConstruct();
    }

    public NoiseConversion addNode(float x, float y) {
        NoiseConversionMethods.nativeAddNode(pointer, x, y);
        return this;
    }
}
