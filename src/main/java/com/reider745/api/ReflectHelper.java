package com.reider745.api;

import java.lang.reflect.Field;

public class ReflectHelper {
    public static <T>T getField(Object self, String name) {
        try{
            Field field = self.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(self);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T>void setField(Object self, String name, T v) {
        try{
            Field field = self.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(self, v);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static <T>T getField(Class<?> self, String name) {
        try{
            Field field = self.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T>void setField(Class<?> self, String name, T v) {
        try{
            Field field = self.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, v);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
