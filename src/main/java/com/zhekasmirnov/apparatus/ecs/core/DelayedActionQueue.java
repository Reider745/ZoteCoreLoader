package com.zhekasmirnov.apparatus.ecs.core;

import com.zhekasmirnov.apparatus.ecs.core.collection.IntFlatMap;

import java.util.Arrays;

public class DelayedActionQueue {
    private static final Object REMOVE_TAG = new Object();
    private static final int REMOVED_ENTITY_IDX = -2;

    private int reservedCapacity = 16;

    private EntityManager entityManager;
    private final IntFlatMap entityToIndex = new IntFlatMap(EntityManager.INVALID_ENTITY);
    private int[] entities = null;
    private int[] indices = null;
    private Object[][] components = null;
    private int entityCount = 0;
    private int componentCount = 0;

    private int[] removedEntities = null;
    private int removedEntityCount = 0;

    public DelayedActionQueue(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    DelayedActionQueue() {
        this.entityManager = null;
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int createEntity() {
        return entityManager.createEntity();
    }

    private int assureEntityIndex(int entity) {
        int entityIdx = entityToIndex.get(entity, -1);
        if (entityIdx == -1) {
            entityIdx = entityCount;
            entityToIndex.put(entity, entityIdx);
            reserveEntities(++entityCount);
            entities[entityIdx] = entity;
        }
        return entityIdx;
    }

    private int assureComponentIndex(int typeIndex) {
        int lower = 0;
        int upper = componentCount - 1;
        while (lower <= upper) {
            int mid = (lower + upper) / 2;
            int midValue = indices[mid];
            if (midValue == typeIndex) {
                return mid;
            } else if (midValue > typeIndex) {
                upper = mid - 1;
            } else {
                lower = mid + 1;
            }
        }

        int insertIndex = lower;
        reserveComponents(++componentCount);
        System.arraycopy(indices, insertIndex, indices, insertIndex + 1, componentCount - 1 - insertIndex);
        System.arraycopy(components, insertIndex, components, insertIndex + 1, componentCount - 1 - insertIndex);
        indices[insertIndex] = typeIndex;
        if (componentCount > 0) {
            reserveEntities(1);
            components[insertIndex] = new Object[entities.length];
        }

        return insertIndex;
    }

    public DelayedActionQueue addComponent(int entity, String type, Object value) {
        return addComponent(entity, TypeIndexMap.getTypeIndex(type), value);
    }

    public<T, U extends T> DelayedActionQueue addComponent(int entity, Class<T> type, U value) {
        return addComponent(entity, TypeIndexMap.getTypeIndex(type), value);
    }

    public DelayedActionQueue addComponent(int entity, int type, Object value) {
        int entityIdx = assureEntityIndex(entity);
        if (entityIdx == REMOVED_ENTITY_IDX)
            return this;
        int typeIdx = assureComponentIndex(type);
        components[typeIdx][entityIdx] = value;
        return this;
    }

    public DelayedActionQueue extend(int entity, ComponentCollection cc) {
        return extend(entity, cc.indices, cc.components, cc.count);
    }

    public DelayedActionQueue extend(int entity, int[] types, Object[] values, int count) {
        int entityIdx = assureEntityIndex(entity);
        if (entityIdx == REMOVED_ENTITY_IDX)
            return this;
        int newEntityCapacity = entities.length;

        if (componentCount == 0) {
            reserveComponents(count);
            componentCount = count;
            for (int i = 0; i < count; i++) {
                indices[i] = types[i];
                (components[i] = new Object[newEntityCapacity])[entityIdx] = values[i];
            }
            return this;
        }

        // fill existing table rows and count
        int newComponentCount = 0;
        {
            int i = 0, j = 0;
            while (i < componentCount && j < count) {
                int e1 = indices[i];
                int e2 = types[j];
                if (e2 < e1) {
                    j++;
                } else if (e2 > e1) {
                    i++;
                } else {
                    components[i][entityIdx] = values[j];
                    i++;
                    j++;
                }
                newComponentCount++;
            }
            newComponentCount += (componentCount - i) + (count - j);
        }

        // allocate more rows, if it is required
        if (newComponentCount > componentCount) {
            reserveComponents(newComponentCount);
            Object[] row;
            int i = componentCount - 1, j = count - 1, k = newComponentCount - 1;
            while (i >= 0 && j >= 0) {
                int e1 = indices[i];
                int e2 = types[j];
                if (e1 < e2) {
                    components[k] = row = new Object[newEntityCapacity];
                    row[entityIdx] = values[j];
                    indices[k--] = e2;
                    j--;
                } else if (e1 > e2) {
                    components[k] = components[i];
                    indices[k--] = e1;
                    i--;
                } else {
                    components[k] = components[i];
                    indices[k--] = e1;
                    i--;
                    j--;
                }
            }
            while (i >= 0) {
                components[k] = components[i];
                indices[k--] = indices[i--];
            }
            while (j >= 0) {
                components[k] = row = new Object[newEntityCapacity];
                row[entityIdx] = values[j];
                indices[k--] = types[j--];
            }
            componentCount = newComponentCount;
        }

        return this;
    }

    public DelayedActionQueue removeComponent(int entity, String typeName) {
        return removeComponent(entity, TypeIndexMap.getTypeIndex(typeName));
    }

    public DelayedActionQueue removeComponent(int entity, Class<?> type) {
        return removeComponent(entity, TypeIndexMap.getTypeIndex(type));
    }

    public DelayedActionQueue removeComponent(int entity, int type) {
        int entityIdx = assureEntityIndex(entity);
        if (entityIdx == REMOVED_ENTITY_IDX)
            return this;
        int typeIdx = assureComponentIndex(type);
        components[typeIdx][entityIdx] = REMOVE_TAG;
        return this;
    }

    public DelayedActionQueue shrink(int entity, RemoveComponents rc) {
        return shrink(entity, rc.indices, rc.count);
    }

    private Object[] removeTagSpan = null;
    public DelayedActionQueue shrink(int entity, int[] types, int count) {
        if (removeTagSpan == null || removeTagSpan.length < count) {
            removeTagSpan = new Object[Math.max(count, 8)];
            Arrays.fill(removeTagSpan, REMOVE_TAG);
        }
        return extend(entity, types, removeTagSpan, count);
    }

    public DelayedActionQueue removeEntity(int entity) {
        int entityIdx = entityToIndex.get(entity, -1);
        if (entityIdx != REMOVED_ENTITY_IDX) {
            entityToIndex.put(entity, REMOVED_ENTITY_IDX);
        }
        if (entityIdx >= 0) {
            int lastIdx = --entityCount;
            int movedEntity = entities[entityIdx] = entities[lastIdx];
            if (movedEntity != entity) {
                entityToIndex.put(movedEntity, entityIdx);
            }
            for (Object[] componentTableRow : components) {
                componentTableRow[entityIdx] = componentTableRow[lastIdx];
                componentTableRow[lastIdx] = null;
            }
        }

        reserveRemovedEntities(++removedEntityCount);
        removedEntities[removedEntityCount - 1] = entity;
        return this;
    }

    public void flushNoClear() {
        for (int i = 0; i < removedEntityCount; i++) {
            entityManager.removeEntityInternal(removedEntities[i], false);
        }

        int[] addedIndices = new int[componentCount];
        int[] removedIndices = new int[componentCount];
        Object[] addedComponents = new Object[componentCount];
        for (int entityIdx = 0; entityIdx < entityCount; entityIdx++) {
            int addedCount = 0;
            int removedCount = 0;
            for (int componentIdx = 0; componentIdx < componentCount; componentIdx++) {
                Object c = components[componentIdx][entityIdx];
                if (c == REMOVE_TAG) {
                    removedIndices[removedCount++] = indices[componentIdx];
                } else if (c != null) {
                    addedIndices[addedCount] = indices[componentIdx];
                    addedComponents[addedCount] = c;
                    addedCount++;
                }
            }
            entityManager.modifyEntity(entities[entityIdx], addedIndices, addedComponents, addedCount, removedIndices, removedCount);
        }
    }

    public void flush() {
        flushNoClear();
        clear();
    }

    void clearNoDealloc() {
        entityToIndex.clearNoDealloc();
        entityCount = 0;
        componentCount = 0;
        removedEntityCount = 0;
    }

    public void clear() {
        entityToIndex.clear();
        entities = null;
        components = null;
        removedEntities = null;
        entityCount = 0;
        componentCount = 0;
        removedEntityCount = 0;
        reservedCapacity = 16;
    }

    void reserveEntities(int count) {
        if (entities == null) {
            int capacity = Math.max(count, reservedCapacity);
            entities = new int[capacity];
        } else if (entities.length < count) {
            int newCapacity = entities.length * 2;
            while (newCapacity < count)
                newCapacity *= 2;
            entities = Arrays.copyOf(entities, newCapacity);
            for (int i = 0; i < componentCount; i++) {
                components[i] = Arrays.copyOf(components[i], newCapacity);
            }
        }
    }

    void reserveRemovedEntities(int count) {
        if (removedEntities == null) {
            int capacity = Math.max(count, 16);
            removedEntities = new int[capacity];
        } else if (removedEntities.length < count) {
            int newCapacity = removedEntities.length * 2;
            while (newCapacity < count)
                newCapacity *= 2;
            removedEntities = Arrays.copyOf(removedEntities, newCapacity);
        }
    }

    void reserveComponents(int count) {
        if (components == null) {
            int capacity = Math.max(count, 4);
            components = new Object[capacity][];
            indices = new int[capacity];
        } else if (components.length < count) {
            int newCapacity = entities.length * 2;
            while (newCapacity < count)
                newCapacity *= 2;
            components = Arrays.copyOf(components, newCapacity);
            indices = Arrays.copyOf(indices, newCapacity);
        }
    }

    public void reserve(int entityCount) {
        if (entityCount <= 0)
            return;
        reservedCapacity = Math.max(reservedCapacity, entityCount);
        reserveEntities(entityCount);
    }

    public int getEntityCount() {
        return entityCount;
    }

    int getEntityCapacity() {
        return entities != null ? entities.length : 0;
    }

    int getComponentCount() {
        return componentCount;
    }

    int getComponentCapacity() {
        return components != null ? components.length : 0;
    }
}
