package com.zhekasmirnov.apparatus.ecs.core;

import java.util.Arrays;

public class Query {
    final int[] indicesAndReorder;
    final int count;

    private void sortAndBuildReorder() {
        for (int i = 0; i < count; i++) {
            indicesAndReorder[i + count] = i;
        }

        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                if (indicesAndReorder[j - 1] > indicesAndReorder[j]) {
                    int t1 = indicesAndReorder[j - 1];
                    int t2 = indicesAndReorder[j - 1 + count];
                    indicesAndReorder[j - 1] = indicesAndReorder[j];
                    indicesAndReorder[j - 1 + count] = indicesAndReorder[j + count];
                    indicesAndReorder[j] = t1;
                    indicesAndReorder[j + count] = t2;
                }
            }
        }
    }

    public Query(int[] indices) {
        count = indices.length;
        indices = Arrays.copyOf(indices, indices.length * 2);
        this.indicesAndReorder = indices;
        sortAndBuildReorder();
    }

    public Query(String... componentTypes) {
        count = componentTypes.length;
        int[] indices = new int[componentTypes.length * 2];
        int i = 0;
        for (String type : componentTypes) {
            indices[i++] = TypeIndexMap.getTypeIndex(type);
        }
        this.indicesAndReorder = indices;
        sortAndBuildReorder();
    }

    public Query(Class<?>... componentTypes) {
        count = componentTypes.length;
        int[] indices = new int[componentTypes.length * 2];
        int i = 0;
        for (Class<?> type : componentTypes) {
            indices[i++] = TypeIndexMap.getTypeIndex(type);
        }
        this.indicesAndReorder = indices;
        sortAndBuildReorder();
    }

    public Query(Object... componentTypes) {
        count = componentTypes.length;
        int[] indices = new int[componentTypes.length * 2];
        int i = 0;
        for (Object type : componentTypes) {
            if (type instanceof Integer) {
                indices[i++] = (Integer) type;
            } else if (type instanceof String) {
                indices[i++] = TypeIndexMap.getTypeIndex((String) type);
            } else if (type instanceof Class<?>) {
                indices[i++] = TypeIndexMap.getTypeIndex((Class<?>) type);
            } else {
                throw new IllegalArgumentException(type.toString());
            }
        }
        Arrays.sort(indices);
        this.indicesAndReorder = indices;
    }

    Query(String[] tags, Class<?>... componentTypes) {
        count = componentTypes.length + tags.length;
        int[] indices = new int[count * 2];
        int i = 0;
        for (Class<?> type : componentTypes) {
            indices[i++] = TypeIndexMap.getTypeIndex(type);
        }
        for (String tag : tags) {
            indices[i++] = TypeIndexMap.getTypeIndex(tag);
        }
        this.indicesAndReorder = indices;
        sortAndBuildReorder();
    }

    Query(String[] tags, String... componentTypes) {
        count = componentTypes.length + tags.length;
        int[] indices = new int[count * 2];
        int i = 0;
        for (String type : componentTypes) {
            indices[i++] = TypeIndexMap.getTypeIndex(type);
        }
        for (String tag : tags) {
            indices[i++] = TypeIndexMap.getTypeIndex(tag);
        }
        this.indicesAndReorder = indices;
        sortAndBuildReorder();
    }
}
