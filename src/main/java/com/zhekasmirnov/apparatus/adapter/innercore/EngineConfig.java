package com.zhekasmirnov.apparatus.adapter.innercore;

import com.zhekasmirnov.innercore.api.InnerCoreConfig;
public class EngineConfig {
    public interface PropertyValidator<T> {
        T validate(T input);
    }

    public static void reload() {
        InnerCoreConfig.reload();
    }

    public static boolean isDeveloperMode() {
        return InnerCoreConfig.getBool("developer_mode");
    }

    public static<T> T get(String name, Class<T> type, PropertyValidator<T> validator) {
        Object data = InnerCoreConfig.get(name);
        try {
            //noinspection unchecked
            return validator != null ? validator.validate((T) data) : (T) data;
        } catch (ClassCastException e) {
            //noinspection ConstantConditions
            return validator != null ? validator.validate(null) : null;
        }
    }

    public static String getString(String name, String fallback){
        return get(name, String.class, value -> (value != null ? value : fallback));
    }

    public static boolean getBoolean(String name, boolean fallback) {
        //noinspection ConstantConditions
        return get(name, Boolean.class, value -> (value != null ? value : fallback));
    }

    public static Number getNumber(String name, Number fallback) {
        return get(name, Number.class, value -> (value != null ? value : fallback));
    }

    public static int getInt(String name, int fallback) {
        return getNumber(name, fallback).intValue();
    }

    public static float getFloat(String name, float fallback) {
        return getNumber(name, fallback).floatValue();
    }

    public static double getDouble(String name, double fallback) {
        return getNumber(name, fallback).doubleValue();
    }

    public static long getLong(String name, long fallback) {
        return getNumber(name, fallback).longValue();
    }
}
