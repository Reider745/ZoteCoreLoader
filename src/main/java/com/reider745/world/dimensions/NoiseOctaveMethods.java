package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

public class NoiseOctaveMethods {
    private static final PointersStorage<NoiseOctaveDescription> pointers = new PointersStorage<>("noise_octave", new PointerGenFastest(), false);

    public static class NoiseOctaveDescription {
        public final int type;
        public float translateX, translateY, translateZ;
        public float scaleX, scaleY, scaleZ;
        public float weight;
        public int seed;
        public NoiseConversionMethods.NoiseConversionDescription conversion;

        public NoiseOctaveDescription(int type){
            this.type = type;
        }
    }

    public static NoiseOctaveDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(int type){
        return pointers.addPointer(new NoiseOctaveDescription(type));
    }
    public static void nativeSetTranslate(long ptr, float x, float y, float z){
        NoiseOctaveDescription description = pointers.get(ptr);
        description.translateX = x;
        description.translateY = y;
        description.translateZ = z;
    }
    public static void nativeSetScale(long ptr, float x, float y, float z){
        NoiseOctaveDescription description = pointers.get(ptr);
        description.scaleX = x;
        description.scaleY = y;
        description.scaleZ = z;
    }
    public static void nativeSetWeight(long ptr, float w){
        pointers.get(ptr).weight = w;
    }
    public static void nativeSetSeed(long ptr, int seed){
        pointers.get(ptr).seed = seed;
    }
    public static void nativeSetConversion(long ptr, long conversion){
        pointers.get(ptr).conversion = NoiseConversionMethods.get(conversion);
    }
}
