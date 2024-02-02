package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

public class CustomGeneratorMethods {
    private static final PointersStorage<CustomGeneratorDescription> pointers = new PointersStorage<>("generators", new PointerGenFastest(), false);

    public static class CustomGeneratorDescription {
        private final int type;
        public BaseTerrainGenerator terrainGenerator;
        public boolean buildVanillaSurfaces, generateVanillaStructures, generateModStructures, caves, underwaterCaves;

        public CustomGeneratorDescription(int type){
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    public static CustomGeneratorDescription get(long ptr) {
        return pointers.get(ptr);
    }

    public static long nativeConstruct(int baseType){
        return pointers.addPointer(new CustomGeneratorDescription(baseType));
    }

    public static void nativeSetTerrainGenerator(long pointer, long generator){
        pointers.get(pointer).terrainGenerator = MonoBiomeTerrainMethods.get(generator);
    }
    public static void nativeSetBuildVanillaSurfaces(long pointer, boolean value){
        pointers.get(pointer).buildVanillaSurfaces = value;
    }
    public static void nativeSetGenerateVanillaStructures(long pointer, boolean value){
        pointers.get(pointer).generateVanillaStructures = value;
    }
    public static void nativeSetGenerateModStructures(long pointer, boolean value){
        pointers.get(pointer).generateModStructures = value;
    }
    public static void nativeSetGenerateCaves(long pointer, boolean caves, boolean underwaterCaves){
        CustomGeneratorDescription description = pointers.get(pointer);
        description.caves = caves;
        description.underwaterCaves = underwaterCaves;
    }
}
