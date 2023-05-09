package com.zhekasmirnov.apparatus.ecs.core;

import java.util.HashMap;

public class TypeIndexMap {
    private static final HashMap<Object, Integer> typeIndexByName = new HashMap<>();
    private static final HashMap<Integer, String> typeNameByIndex = new HashMap<>();
    private static int nextTypeIndex = 1;

    public static int getTypeIndex(String typeName) {
        return typeIndexByName.computeIfAbsent(typeName, key -> {
            int typeId = nextTypeIndex++;
            typeNameByIndex.put(typeId, typeName);
            return typeId;
        });
    }

    public static int getTypeIndex(Class<?> typeClass) {
        Integer typeId = typeIndexByName.get(typeClass);
        if (typeId == null) {
            String strTypeName = "#" + typeClass.getCanonicalName();
            typeId = typeIndexByName.get(strTypeName);
            if (typeId == null) {
                typeId = nextTypeIndex++;
                typeIndexByName.put(strTypeName, typeId);
                typeNameByIndex.put(typeId, strTypeName);
            }
            typeIndexByName.put(typeClass, typeId);
        }
        return typeId;
    }

    public static String getTypeName(int index) {
        return typeNameByIndex.get(index);
    }
}
