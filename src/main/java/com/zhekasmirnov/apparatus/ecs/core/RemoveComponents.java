package com.zhekasmirnov.apparatus.ecs.core;

import java.util.Arrays;

public class RemoveComponents {
    int[] indices;
    int count = 0;

    public RemoveComponents(int reserve) {
        indices = new int[reserve];
    }

    public RemoveComponents() {
        this(4);
    }

    public RemoveComponents addComponent(int index) {
        for (int i = 0; i < count; i++) {
            int idx = indices[i];
            if (idx > index) {
                reserve(++count);
                for (int j = count - 1; j > i; j--) {
                    indices[j] = indices[j - 1];
                }
                indices[i] = index;
                return this;
            } else if (idx == index) {
                return this;
            }
        }
        reserve(++count);
        indices[count - 1] = index;
        return this;
    }

    public RemoveComponents addComponent(String typeName) {
        return addComponent(TypeIndexMap.getTypeIndex(typeName));
    }

    public RemoveComponents addComponent(Class<?> type) {
        return addComponent(TypeIndexMap.getTypeIndex(type));
    }

    public RemoveComponents setTypes(int... types) {
        count = types.length;
        reserve(count);
        Arrays.sort(types);
        System.arraycopy(types, 0, indices, 0, count);
        return this;
    }

    public RemoveComponents setTypes(String... types) {
        count = types.length;
        reserve(count);
        for (int i = 0; i < count; i++) {
            indices[i] = TypeIndexMap.getTypeIndex(types[i]);
        }
        Arrays.sort(indices, 0, count);
        return this;
    }

    public RemoveComponents setTypes(Class<?>... types) {
        count = types.length;
        reserve(count);
        for (int i = 0; i < count; i++) {
            indices[i] = TypeIndexMap.getTypeIndex(types[i]);
        }
        Arrays.sort(indices, 0, count);
        return this;
    }

    private void reserve(int capacity) {
        if (capacity > indices.length) {
            int newCapacity = indices.length * 2;
            while (newCapacity < capacity)
                newCapacity *= 2;
            indices = Arrays.copyOf(indices, newCapacity);
        }
    }

    public boolean empty() {
        return count == 0;
    }
}
