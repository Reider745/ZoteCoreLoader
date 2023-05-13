package com.zhekasmirnov.innercore.api.dimensions;

import java.util.HashMap;

public class NoiseOctave {
    private static final HashMap<String, Integer> octaveTypeMap = new HashMap<>();
    static {
        octaveTypeMap.put("perlin", 0);
        octaveTypeMap.put("gray", 1);
        octaveTypeMap.put("chess", 2);
        octaveTypeMap.put("sine_x", 10);
        octaveTypeMap.put("sine_y", 11);
        octaveTypeMap.put("sine_z", 12);
        octaveTypeMap.put("sine_xy", 13);
        octaveTypeMap.put("sine_yz", 14);
        octaveTypeMap.put("sine_xz", 15);
        octaveTypeMap.put("sine_xyz", 16);
    }

    private static int getOctaveType(String name) {
        if (!octaveTypeMap.containsKey(name)) {
            StringBuilder builder = new StringBuilder();
            for (String key : octaveTypeMap.keySet()) {
                builder.append(key).append(" ");
            }
            throw new IllegalArgumentException("invalid octave type: " + name + ", valid types: " + builder);
        } 
        return octaveTypeMap.get(name);
    }
    
    public final long pointer;
    
    public NoiseOctave(int type) {
        pointer = nativeConstruct(type);
    }

    public NoiseOctave(String type) {
        this(getOctaveType(type));
    }

    public NoiseOctave() {
        this(0);
    }

    public NoiseOctave setTranslate(float x, float y, float z) {
        nativeSetTranslate(pointer, x, y, z);
        return this;
    }

    public NoiseOctave setScale(float x, float y, float z) {
        nativeSetScale(pointer, x, y, z);
        return this;
    }

    public NoiseOctave setWeight(float w) {
        nativeSetWeight(pointer, w);
        return this;
    }

    public NoiseOctave setSeed(int seed) {
        nativeSetSeed(pointer, seed);
        return this;
    }

    public NoiseOctave setConversion(NoiseConversion conversion) {
        nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }
    
    private static native long nativeConstruct(int type);
    private static native void nativeSetTranslate(long ptr, float x, float y, float z);
    private static native void nativeSetScale(long ptr, float x, float y, float z);
    private static native void nativeSetWeight(long ptr, float w);
    private static native void nativeSetSeed(long ptr, int seed);
    private static native void nativeSetConversion(long ptr, long conversion);
}