package com.zhekasmirnov.innercore.api;

public class NativeShaderUniformSet {
    public NativeShaderUniformSet(long pointer) {

    }

    public NativeShaderUniformSet lock() {

        return this;
    }

    public NativeShaderUniformSet unlock() {

        return this;
    }

    public NativeShaderUniformSet setUniformValueArr(String uniformSet, String uniformName, float[] value) {

        return this;
    }

    public NativeShaderUniformSet setUniformValue(String uniformSet, String uniformName, float... value) {
        return this;
    }
}