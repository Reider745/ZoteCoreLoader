package com.reider745.api;

import java.util.HashMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class CustomManager {
    private HashMap<String, Object> parameters = new HashMap<>();
    public int id;
    public Class<?> clazz;
    public String type;

    public CustomManager(int id, Class<?> clazz, String type) {
        this.id = id;
        this.clazz = clazz;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        if (!parameters.containsKey(key))
            return def;
        Object value = parameters.get(key);
        if (def == null || value == null)
            return def != null ? def : null;
        if (def.getClass().isInstance(value))
            return (T) value;
        return def;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) parameters.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void put(String key, Object value) {
        parameters.put(key, value);
    }

    public boolean containsKey(String key) {
        return parameters.containsKey(key);
    }

    public void remove(String key) {
        parameters.remove(key);
    }

    private static Int2ObjectOpenHashMap<CustomManager> storage = new Int2ObjectOpenHashMap<>();

    public static CustomManager getFor(int id) {
        return storage.get(id);
    }

    public static void put(int id, CustomManager manager) {
        storage.put(id, manager);
    }
}
