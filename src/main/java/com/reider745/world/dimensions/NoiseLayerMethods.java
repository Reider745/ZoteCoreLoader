package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.ArrayList;

public class NoiseLayerMethods {
    private static final PointersStorage<NoiseLayerDescription> pointers = new PointersStorage<>("noise_layers", new PointerGenFastest(), false);

    public static class NoiseLayerDescription {
        public final ArrayList<NoiseOctaveMethods.NoiseOctaveDescription> octaves = new ArrayList<>();
        public NoiseConversionMethods.NoiseConversionDescription conversion;
        public int grid;
    }

    public static NoiseLayerDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(){
        return pointers.addPointer(new NoiseLayerDescription());
    }
    public static void nativeAddOctave(long ptr, long octave){
        pointers.get(ptr).octaves.add(NoiseOctaveMethods.get(octave));
    }
    public static void nativeSetConversion(long ptr, long conversion){
        pointers.get(ptr).conversion = NoiseConversionMethods.get(conversion);
    }
    public static void nativeSetGridSize(long ptr, int grid){
        pointers.get(ptr).grid = grid;
    }
}