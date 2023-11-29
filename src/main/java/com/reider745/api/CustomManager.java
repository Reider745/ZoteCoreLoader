package com.reider745.api;

import java.util.HashMap;

public class CustomManager {
    private HashMap<String, Object> parameters = new HashMap<>();
    public int id;
    public Class clazz;
    public String type;

    public CustomManager(int id, Class clazz, String type){
        this.id = id;
        this.clazz = clazz;
        this.type = type;
    }

    public <T>T get(String key, T def){
        T value = (T) parameters.get(key);
        if(value == null)
            return def;
        return value;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public <T>T get(String key){
        return (T) parameters.get(key);
    }

    public <T>void put(String key, T value){
        parameters.put(key, value);
    }

    private static HashMap<Integer, CustomManager> storage = new HashMap<>();

    public static CustomManager getFor(int id){
        return storage.get(id);
    }

    public static void put(int id, CustomManager manager){
        storage.put(id, manager);
    }
}
