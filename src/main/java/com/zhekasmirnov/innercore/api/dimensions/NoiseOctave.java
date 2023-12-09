package com.zhekasmirnov.innercore.api.dimensions;

import java.util.HashMap;

import com.reider745.InnerCoreServer;

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

    public final long pointer = 0;

    public NoiseOctave(int type) {
    }

    public NoiseOctave(String type) {
        this(getOctaveType(type));
    }

    public NoiseOctave() {
        this(0);
    }

    public NoiseOctave setTranslate(float x, float y, float z) {
        InnerCoreServer.useNotCurrentSupport("NoiseOctave.setTranslate(x, y, z)");
        return this;
    }

    public NoiseOctave setScale(float x, float y, float z) {
        InnerCoreServer.useNotCurrentSupport("NoiseOctave.setScale(x, y, z)");
        return this;
    }

    public NoiseOctave setWeight(float w) {
        InnerCoreServer.useNotCurrentSupport("NoiseOctave.setWeight(w)");
        return this;
    }

    public NoiseOctave setSeed(int seed) {
        InnerCoreServer.useNotCurrentSupport("NoiseOctave.setSeed(seed)");
        return this;
    }

    public NoiseOctave setConversion(NoiseConversion conversion) {
        InnerCoreServer.useNotCurrentSupport("NoiseOctave.setConversion(conversion)");
        return this;
    }
}
