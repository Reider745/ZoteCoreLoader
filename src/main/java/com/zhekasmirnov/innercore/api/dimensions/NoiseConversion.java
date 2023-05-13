package com.zhekasmirnov.innercore.api.dimensions;

public class NoiseConversion {
    public final long pointer;

    public NoiseConversion() {
        pointer = nativeConstruct();
    }

    public NoiseConversion addNode(float x, float y) {
        nativeAddNode(pointer, x, y);
        return this;
    }
    
    private static native long nativeConstruct();
    private static native void nativeAddNode(long ptr, float x, float y);
}