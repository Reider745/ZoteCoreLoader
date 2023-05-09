package com.zhekasmirnov.innercore;

public class NativeShaderUniformSet {
    public final long pointer;

    public NativeShaderUniformSet(long pointer) {
        this.pointer = pointer;
    }

    public NativeShaderUniformSet lock() {
        nativeLock(pointer);
        return this;
    }

    public NativeShaderUniformSet unlock() {
        nativeUnlock(pointer);
        return this;
    }

    public NativeShaderUniformSet setUniformValueArr(String uniformSet, String uniformName, float[] value) {
        nativeSetUniform(pointer, uniformSet, uniformName, value);
        return this;
    }

    public NativeShaderUniformSet setUniformValue(String uniformSet, String uniformName, float... value) {
        return setUniformValueArr(uniformSet, uniformName, value);
    }

    private static native void nativeLock(long pointer);
    private static native void nativeUnlock(long pointer);
    private static native void nativeSetUniform(long pointer, String uniformSet, String uniformName, float[] value);
}