package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;

public class NativeShaderUniformSet {
    public final long pointer;

    public NativeShaderUniformSet(long pointer) {
        this.pointer = pointer;
    }

    public NativeShaderUniformSet lock() {
        InnerCoreServer.useClientMethod("NativeShaderUniformSet.lock()");
        return this;
    }

    public NativeShaderUniformSet unlock() {
        InnerCoreServer.useClientMethod("NativeShaderUniformSet.unlock()");
        return this;
    }

    public NativeShaderUniformSet setUniformValueArr(String uniformSet, String uniformName, float[] value) {
        InnerCoreServer.useClientMethod("NativeShaderUniformSet.setUniformValueArr(uniformSet, uniformName, value)");
        return this;
    }

    public NativeShaderUniformSet setUniformValue(String uniformSet, String uniformName, float... value) {
        return setUniformValueArr(uniformSet, uniformName, value);
    }
}
