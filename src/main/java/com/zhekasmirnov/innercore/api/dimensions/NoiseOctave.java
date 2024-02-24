package com.zhekasmirnov.innercore.api.dimensions;

import java.util.HashMap;

import com.reider745.world.dimensions.NoiseOctaveMethods;

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
        pointer = NoiseOctaveMethods.nativeConstruct(type);
    }

    public NoiseOctave(String type) {
        this(getOctaveType(type));
    }

    public NoiseOctave() {
        this(0);
    }

    public NoiseOctave setTranslate(float x, float y, float z) {
        NoiseOctaveMethods.nativeSetTranslate(pointer, x, y, z);
        return this;
    }

    public NoiseOctave setScale(float x, float y, float z) {
        NoiseOctaveMethods.nativeSetScale(pointer, x, y, z);
        return this;
    }

    public NoiseOctave setWeight(float w) {
        NoiseOctaveMethods.nativeSetWeight(pointer, w);
        return this;
    }

    public NoiseOctave setSeed(int seed) {
        NoiseOctaveMethods.nativeSetSeed(pointer, seed);
        return this;
    }

    public NoiseOctave setConversion(NoiseConversion conversion) {
        NoiseOctaveMethods.nativeSetConversion(pointer, conversion != null ? conversion.pointer : 0);
        return this;
    }
}
