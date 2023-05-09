package com.zhekasmirnov.innercore.api;

/**
 * Created by zheka on 15.09.2017.
 */

public class NativeFurnaceRegistry {
    public static native void nativeAddRecipe(int inputId, int inputData, int outputId, int outputData);
    public static native void nativeRemoveRecipe(int inputId, int inputData);
    public static native void nativeAddFuel(int id, int data, int burnDuration);
    public static native void nativeRemoveFuel(int id, int data);
}
