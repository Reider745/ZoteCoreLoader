package com.zhekasmirnov.innercore.api.dimensions;

import com.reider745.world.dimensions.NoiseConversionMethods;

public class NoiseConversion {
    public final long pointer;

    public NoiseConversion() {
        pointer = nativeConstruct();
    }

    public NoiseConversion addNode(float x, float y) {
        nativeAddNode(pointer, x, y);
        return this;
    }

    private static long nativeConstruct(){
        return NoiseConversionMethods.nativeConstruct();
    }
    private static void nativeAddNode(long ptr, float x, float y){
        NoiseConversionMethods.nativeAddNode(ptr, x, y);
    }
}
