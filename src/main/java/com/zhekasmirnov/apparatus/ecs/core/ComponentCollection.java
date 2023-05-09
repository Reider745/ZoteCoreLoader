package com.zhekasmirnov.apparatus.ecs.core;

import java.util.Arrays;

public class ComponentCollection {
    Object[] components;
    int[] indices;
    int count = 0;

    ComponentCollection(int[] indices, Object[] components, int count) {
        this.components = components;
        this.indices = indices;
        this.count = count;
    }

    public ComponentCollection(int reserve) {
        components = new Object[reserve];
        indices = new int[reserve];
    }

    public ComponentCollection() {
        this(4);
    }

    public ComponentCollection addComponent(int index, Object component) {
        for (int i = 0; i < count; i++) {
            int idx = indices[i];
            if (idx > index) {
                reserve(++count);
                for (int j = count - 1; j > i; j--) {
                    indices[j] = indices[j - 1];
                }
                indices[i] = index;
                components[i] = component;
                return this;
            } else if (idx == index) {
                components[i] = component;
                return this;
            }
        }
        reserve(++count);
        indices[count - 1] = index;
        components[count - 1] = component;
        return this;
    }

    public ComponentCollection addComponent(String type, Object value) {
        return addComponent(TypeIndexMap.getTypeIndex(type), value);
    }

    public<T, U extends T> ComponentCollection addComponent(Class<T> type, U value) {
        return addComponent(TypeIndexMap.getTypeIndex(type), value);
    }

    public ComponentCollection setTypes(int... types) {
        count = types.length;
        reserve(count);
        Arrays.sort(types);
        System.arraycopy(types, 0, indices, 0, count);
        Arrays.fill(components, null);
        return this;
    }

    public ComponentCollection setTypes(String... types) {
        count = types.length;
        reserve(count);
        for (int i = 0; i < count; i++) {
            indices[i] = TypeIndexMap.getTypeIndex(types[i]);
        }
        Arrays.sort(indices, 0, count);
        Arrays.fill(components, null);
        return this;
    }

    public ComponentCollection setTypes(Class<?>... types) {
        count = types.length;
        reserve(count);
        for (int i = 0; i < count; i++) {
            indices[i] = TypeIndexMap.getTypeIndex(types[i]);
        }
        Arrays.sort(indices, 0, count);
        Arrays.fill(components, null);
        return this;
    }

    public ComponentCollection setValues(Object... values) {
        if (values.length > count)
            throw new IllegalArgumentException("invalid count of components in setValues");
        System.arraycopy(values, 0, components, 0, values.length);
        return this;
    }

    private void reserve(int count) {
        if (count > indices.length) {
            int newCapacity = indices.length * 2;
            components = Arrays.copyOf(components, newCapacity);
            indices = Arrays.copyOf(indices, newCapacity);
        }
    }

    public boolean empty() {
        return count == 0;
    }
}
