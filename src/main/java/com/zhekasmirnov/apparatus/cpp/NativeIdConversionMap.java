package com.zhekasmirnov.apparatus.cpp;

public class NativeIdConversionMap {
    public static native void clearAll();

    public static native void mapConversion(int staticId, int dynamicId);

    public static native int dynamicToStatic(int dynamicId);

    public static native int staticToDynamic(int staticId);
}
