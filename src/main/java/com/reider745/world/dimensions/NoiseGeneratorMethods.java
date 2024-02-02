package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.ArrayList;

public class NoiseGeneratorMethods {
    private static final PointersStorage<NoiseGeneratorDescription> pointers = new PointersStorage<>("noise_generator", new PointerGenFastest(), false);

    public static class NoiseGeneratorDescription {
        public final ArrayList<NoiseLayerMethods.NoiseLayerDescription> layers = new ArrayList<>();
        public NoiseConversionMethods.NoiseConversionDescription conversion;
    }

    public static NoiseGeneratorDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(){
        return pointers.addPointer(new NoiseGeneratorDescription());
    }
    public static void nativeAddLayer(long ptr, long layer){
        pointers.get(ptr).layers.add(NoiseLayerMethods.get(layer));
    }
    public static void nativeSetConversion(long ptr, long conversion){
        pointers.get(ptr).conversion = NoiseConversionMethods.get(conversion);
    }
}