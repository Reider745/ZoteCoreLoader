package com.zhekasmirnov.innercore.api.dimensions;

import java.util.HashMap;

import com.reider745.InnerCoreServer;

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

    public final long pointer = 0;
    public final int baseType;

    public CustomDimensionGenerator(int baseType) {
        if (baseType > 4 || baseType < 0) {
            throw new IllegalArgumentException("invalid base generator type: " + baseType);
        }
        this.baseType = baseType;
    }

    public CustomDimensionGenerator(String baseType) {
        this(getGeneratorType(baseType));
    }

    public CustomDimensionGenerator setBuildVanillaSurfaces(boolean value) {
        InnerCoreServer.useNotCurrentSupport("CustomDimensionGenerator.setBuildVanillaSurfaces(value)");
        return this;
    }

    public CustomDimensionGenerator setGenerateVanillaStructures(boolean value) {
        InnerCoreServer.useNotCurrentSupport("CustomDimensionGenerator.setGenerateVanillaStructures(value)");
        return this;
    }

    public CustomDimensionGenerator setGenerateCaves(boolean caves, boolean underwaterCaves) {
        InnerCoreServer.useNotCurrentSupport("CustomDimensionGenerator.setGenerateCaves(caves, underwaterCaves)");
        return this;
    }

    public CustomDimensionGenerator setGenerateCaves(boolean caves) {
        return setGenerateCaves(caves, false);
    }

    private int modGenerationBaseDimension = -1;
    private boolean modGenerationEnabled = true;

    public CustomDimensionGenerator setGenerateModStructures(boolean value) {
        InnerCoreServer.useNotCurrentSupport("CustomDimensionGenerator.setGenerateModStructures(value)");
        modGenerationEnabled = value;
        if (!value) {
            modGenerationBaseDimension = -1;
        }
        return this;
    }

    public CustomDimensionGenerator setModGenerationBaseDimension(int id) {
        if (id != -1 && id != 0 && id != 1 && id != 2) {
            throw new IllegalArgumentException(
                    "setModGenerationBaseDimension must receive vanilla id or -1, not " + id);
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
        InnerCoreServer.useNotCurrentSupport("CustomDimensionGenerator.setTerrainGenerator(generator)");
        return this;
    }
}
