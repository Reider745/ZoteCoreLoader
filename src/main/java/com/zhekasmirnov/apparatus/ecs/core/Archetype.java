package com.zhekasmirnov.apparatus.ecs.core;

import java.util.Arrays;

public class Archetype {
    final Object[][] componentTable;
    final int[] sortedComponentIndices;
    int[] entities;
    int entityCount = 0;

    int hashCollisionResolveIdx = -1;

    Archetype(int[] sortedComponentIndices) {
        this.sortedComponentIndices = sortedComponentIndices;
        componentTable = new Object[sortedComponentIndices.length][];
        entities = null;
    }

    int addEntity(int entity, Object[] components, int componentCount) {
        int idx = entityCount++;
        reserve(entityCount);
        for (int i = 0; i < componentTable.length; i++) {
            componentTable[i][idx] = components[i];
        }
        entities[idx] = entity;
        return idx;
    }

    int allocEntity(int entity) {
        int idx = entityCount++;
        reserve(entityCount);
        entities[idx] = entity;
        return idx;
    }

    ComponentCollection getAllComponents(int entityIndex) {
        Object[] components = new Object[componentTable.length];
        for (int i = 0; i < componentTable.length; i++) {
            components[i] = componentTable[i][entityIndex];
        }
        return new ComponentCollection(Arrays.copyOf(sortedComponentIndices, sortedComponentIndices.length), components, componentTable.length);
    }

    int removeEntity(int entityIndex) {
        int lastIdx = --entityCount;
        int lastEntity = entities[lastIdx];
        entities[entityIndex] = lastEntity;
        for (Object[] componentTableRow : componentTable) {
            componentTableRow[entityIndex] = componentTableRow[lastIdx];
            componentTableRow[lastIdx] = null;
        }
        return lastIdx != entityIndex ? lastEntity : EntityManager.INVALID_ENTITY;
    }

    // Used for fast transfer between archetypes:
    // - removes entity from otherArchetype at index otherEntityIndex
    // - adds same entity to this archetype, the new index is guaranteed to be (thisArchetype.entityCount - 1)
    // - moves all common components to this archetype, discards all components that are not present in this archetype and fills missing components with null
    // - returns entity, that were moved to otherEntityIndex in otherArchetype or INVALID_ENTITY
    int moveEntityFrom(int entity, Archetype otherArchetype, int otherEntityIndex) {
        int otherLastIdx = --otherArchetype.entityCount;
        int otherLastEntity = otherArchetype.entities[otherLastIdx];
        otherArchetype.entities[otherEntityIndex] = otherLastEntity;

        int myEntityIndex = entityCount++;
        reserve(entityCount);
        entities[myEntityIndex] = entity;

        {
            int[] myComponents = sortedComponentIndices;
            int myComponentCount = myComponents.length;
            int[] otherComponents = otherArchetype.sortedComponentIndices;
            int otherComponentCount = otherComponents.length;

            int i = 0, j = 0;
            while (i < myComponentCount && j < otherComponentCount) {
                int myTypeIndex = myComponents[i];
                int otherTypeIndex = otherComponents[j];
                if (myTypeIndex < otherTypeIndex) {
                    i++;
                } else if (myTypeIndex > otherTypeIndex) {
                    Object[] row = otherArchetype.componentTable[j++];
                    row[otherEntityIndex] = row[otherLastIdx];
                    row[otherLastIdx] = null;
                } else {
                    Object[] otherRow = otherArchetype.componentTable[j++];
                    Object[] myRow = componentTable[i++];
                    myRow[myEntityIndex] = otherRow[otherEntityIndex];
                    otherRow[otherEntityIndex] = otherRow[otherLastIdx];
                    otherRow[otherLastIdx] = null;
                }
            }
        }

        return otherLastIdx != otherEntityIndex ? otherLastEntity : EntityManager.INVALID_ENTITY;
    }

    void fillComponents(int entityIndex, int[] indices, Object[] components, int count) {
        for (int i = 0, k = 0; i < count; i++) {
            int idx = indices[i];
            while (sortedComponentIndices[k] != idx)
                k++;
            componentTable[k][entityIndex] = components[i];
            k++;
        }
    }

    boolean checkContainsAndFill(int[] indicesAndReorder, int count, Object[][] componentsArray) {
        int[] allIndices = this.sortedComponentIndices;
        int allIndicesCount = allIndices.length;
        int i = 0, j = count, idx;
        for (int n = 0; n < count; n++) {
            int index = indicesAndReorder[n];
            if (i >= allIndicesCount)
                return false;
            while ((idx = allIndices[i++]) != index) {
                if (idx > index || i >= allIndicesCount) {
                    return false;
                }
            }
            componentsArray[indicesAndReorder[j++]] = componentTable[i - 1];
        }
        return true;
    }

    boolean checkContainsAndFillOneEntity(int entityIndex, int[] indicesAndReorder, int count, Object[] componentsArray) {
        int[] allIndices = this.sortedComponentIndices;
        int allIndicesCount = allIndices.length;
        int i = 0, j = count, idx;
        for (int n = 0; n < count; n++) {
            int index = indicesAndReorder[n];
            if (i >= allIndicesCount)
                return false;
            while ((idx = allIndices[i++]) != index) {
                if (idx > index || i >= allIndicesCount) {
                    return false;
                }
            }
            componentsArray[indicesAndReorder[j++]] = componentTable[i - 1][entityIndex];
        }
        return true;
    }

    Object[] getComponentArrayForIndex(int componentIndex) {
        int lower = 0;
        int upper = sortedComponentIndices.length - 1;
        while (lower <= upper) {
            int mid = (lower + upper) / 2;
            int midIdx = sortedComponentIndices[mid];
            if (midIdx == componentIndex) {
                return componentTable[mid];
            } else if (midIdx < componentIndex) {
                lower = mid + 1;
            } else {
                upper = mid - 1;
            }
        }
        return null;
    }

    void clear() {
        entityCount = 0;
        entities = null;
        Arrays.fill(componentTable, null);
    }

    private void reserve(int count) {
        if (entities == null) {
            int newCount = Math.max(count, 16);
            entities = new int[newCount];
            for (int i = 0; i < componentTable.length; i++) {
                componentTable[i] = new Object[newCount];
            }
        } else if (entities.length < count) {
            int newCount = growPolicy(entities.length, count);
            entities = Arrays.copyOf(entities, newCount);
            for (int i = 0; i < componentTable.length; i++) {
                componentTable[i] = Arrays.copyOf(componentTable[i], newCount);
            }
        }
    }

    private static int growPolicy(int count, int targetCount) {
        // TODO: better grow policy
        do {
            count *= 2;
        } while (count < targetCount);
        return count;
    }

}
