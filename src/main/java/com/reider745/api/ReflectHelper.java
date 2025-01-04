package com.reider745.api;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Deprecated
public class ReflectHelper {
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> T getField(Object self, String name) {
        try {
            Field field = self.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(self);
        } catch (ClassCastException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public static void setField(Object self, String name, Object v) {
        try {
            Field field = self.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(self, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> T getField(Class<?> self, String name) {
        try {
            Field field = self.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (ClassCastException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public static void setField(Class<?> self, String name, Object v) {
        try {
            Field field = self.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static <T>T invoke(Object self, String name, Object[] args, Class<?>... classes){
        try {
            Method method = self.getClass().getDeclaredMethod(name, classes);
            method.setAccessible(true);
            return (T) method.invoke(self, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public static <T>T invoke(Class<?> self, String name, Object[] args, Class<?>... classes){
        try {
            Method method = self.getDeclaredMethod(name, classes);
            method.setAccessible(true);
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
