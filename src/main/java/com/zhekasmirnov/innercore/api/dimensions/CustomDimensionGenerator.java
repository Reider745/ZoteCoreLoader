package com.zhekasmirnov.innercore.api.dimensions;

import java.util.HashMap;

import com.reider745.InnerCoreServer;
import com.reider745.world.dimensions.CustomGeneratorMethods;

public class CustomDimensionGenerator {
    private static final HashMap<String, Integer> generatorTypeMap = new HashMap<>();
    static {
        generatorTypeMap.put("overworld", 0);
        generatorTypeMap.put("overworld1", 1);
        generatorTypeMap.put("flat", 2);
        generatorTypeMap.put("nether", 3);
        generatorTypeMap.put("end", 4);
    }

    private static int getGeneratorType(String name) {
        if (!generatorTypeMap.containsKey(name)) {
            StringBuilder builder = new StringBuilder();
            for (String key : generatorTypeMap.keySet()) {
                builder.append(key).append(" ");
            }
            throw new IllegalArgumentException("invalid base generator type: " + name + ", valid types: " + builder);
        }
        return generatorTypeMap.get(name);
    }

    public final long pointer;
    public final int baseType;

    public CustomDimensionGenerator(int baseType) {
        if (baseType > 4 || baseType < 0) {
            throw new IllegalArgumentException("invalid base generator type: " + baseType);
        }
        pointer = nativeConstruct(baseType);
        this.baseType = baseType;
    }

    public CustomDimensionGenerator(String baseType) {
        this(getGeneratorType(baseType));
    }

    public CustomDimensionGenerator setBuildVanillaSurfaces(boolean value) {
        nativeSetBuildVanillaSurfaces(pointer, value);
        return this;
    }

    public CustomDimensionGenerator setGenerateVanillaStructures(boolean value) {
        nativeSetGenerateVanillaStructures(pointer, value);
        return this;
    }

    public CustomDimensionGenerator setGenerateCaves(boolean caves, boolean underwaterCaves) {
        nativeSetGenerateCaves(pointer, caves, underwaterCaves);
        return this;
    }

    public CustomDimensionGenerator setGenerateCaves(boolean caves) {
        return setGenerateCaves(caves, false);
    }


    private int modGenerationBaseDimension = -1;
    private boolean modGenerationEnabled = true;

    public CustomDimensionGenerator setGenerateModStructures(boolean value) {
        nativeSetGenerateModStructures(pointer, value);
        modGenerationEnabled = value;
        if (!value) {
            modGenerationBaseDimension = -1;
        }
        return this;
    }

    public CustomDimensionGenerator setModGenerationBaseDimension(int id) {
        if (id != -1 && id != 0 && id != 1 && id != 2) {
            throw new IllegalArgumentException("setModGenerationBaseDimension must receive vanilla id or -1, not " + id);
        }
        setGenerateModStructures(true);
        modGenerationBaseDimension = id;
        return this;
    }

    public CustomDimensionGenerator removeModGenerationBaseDimension() {
        return setModGenerationBaseDimension(-1);
    }

    public boolean isModGenerationEnabled() {
        return modGenerationEnabled;
    }

    public int getModGenerationBaseDimension() {
        return modGenerationBaseDimension;
    }



    public CustomDimensionGenerator setTerrainGenerator(AbstractTerrainGenerator generator) {
        nativeSetTerrainGenerator(pointer, generator != null ? generator.getPointer() : 0);
        return this;
    }

    private static long nativeConstruct(int baseType){
        return CustomGeneratorMethods.nativeConstruct(baseType);
    }
    private static void nativeSetTerrainGenerator(long pointer, long generator){
        CustomGeneratorMethods.nativeSetTerrainGenerator(pointer, generator);
    }
    private static void nativeSetBuildVanillaSurfaces(long pointer, boolean value){
        CustomGeneratorMethods.nativeSetBuildVanillaSurfaces(pointer, value);
    }
    private static void nativeSetGenerateVanillaStructures(long pointer, boolean value){
        CustomGeneratorMethods.nativeSetGenerateVanillaStructures(pointer, value);
    }
    private static void nativeSetGenerateModStructures(long pointer, boolean value){
        CustomGeneratorMethods.nativeSetGenerateModStructures(pointer, value);
    }
    private static void nativeSetGenerateCaves(long pointer, boolean caves, boolean underwaterCaves){
        CustomGeneratorMethods.nativeSetGenerateCaves(pointer, caves, underwaterCaves);
    }
}
