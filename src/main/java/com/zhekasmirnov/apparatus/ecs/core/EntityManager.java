package com.zhekasmirnov.apparatus.ecs.core;

import com.zhekasmirnov.apparatus.ecs.core.collection.IntFlatMap;

import java.util.Arrays;

public class EntityManager {
    public static final int INVALID_ENTITY = Integer.MAX_VALUE;
    public static final Object TAG = new Object();

    private final IntFlatMap archetypeIdxByHash = new IntFlatMap(-1, 255, 0.5f);
    Archetype[] archetypes = new Archetype[64];
    int archetypeCount = 0;

    private short[][] componentToArchetypeIndices = new short[64][];
    private short[] componentToArchetypeIndicesSizes = new short[64];

    // entity -> [ archetype | archetype index ]
    static final int ENTITY_ARCHETYPE_BITS = 14;  // 2^14 archetypes
    static final int ENTITY_INDEX_BITS = 17;      // 2^17 entities per archetype
    final IntFlatMap entityLocationMap = new IntFlatMap(INVALID_ENTITY, 255, 0.5f);

    private int nextEntity = 0;

    public int createEntity() {
        return nextEntity++;
    }

    ComponentCollection removeEntityInternal(int entity, boolean detach) {
        ComponentCollection detachedCC = null;
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation != -1) {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Archetype archetype = archetypes[archetypeIndex];
            entityLocationMap.remove(entity);
            if (detach) {
                detachedCC = archetype.getAllComponents(entityIndex);
            }
            int movedEntity = archetype.removeEntity(entityIndex);
            if (movedEntity != INVALID_ENTITY)
                entityLocationMap.put(movedEntity, entityLocation);
        }
        return detachedCC;
    }

    public void removeEntity(int entity) {
        removeEntityInternal(entity, false);
    }

    public ComponentCollection detachEntity(int entity) {
        return removeEntityInternal(entity, true);
    }

    public void extend(int entity, ComponentCollection cc) {
        if (cc.empty())
            return;
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation == -1) {
            int archetypeIndex = getOrCreateArchetype(cc.indices, cc.count);
            int entityIndex = archetypes[archetypeIndex].addEntity(entity, cc.components, cc.count);
            entityLocationMap.put(entity, (archetypeIndex << ENTITY_INDEX_BITS) | entityIndex);
        } else {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Archetype oldArchetype = archetypes[archetypeIndex];
            archetypeIndex = getOrCreateArchetypeOfUnion(oldArchetype.sortedComponentIndices, oldArchetype.sortedComponentIndices.length, cc.indices, cc.count);
            Archetype newArchetype = archetypes[archetypeIndex];
            int movedEntity = newArchetype.moveEntityFrom(entity, oldArchetype, entityIndex);
            if (movedEntity != INVALID_ENTITY)
                entityLocationMap.put(movedEntity, entityLocation);
            entityIndex = newArchetype.entityCount - 1; // last added entity
            newArchetype.fillComponents(entityIndex, cc.indices, cc.components, cc.count);
            entityLocationMap.put(entity, (archetypeIndex << ENTITY_INDEX_BITS) | entityIndex);
        }
    }

    public void shrink(int entity, RemoveComponents rc) {
        if (rc.empty())
            return;
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation != -1) {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Archetype oldArchetype = archetypes[archetypeIndex];
            archetypeIndex = getOrCreateArchetypeOfDiff(oldArchetype.sortedComponentIndices, oldArchetype.sortedComponentIndices.length, rc.indices, rc.count);
            Archetype newArchetype = archetypes[archetypeIndex];
            int movedEntity = newArchetype.moveEntityFrom(entity, oldArchetype, entityIndex);
            if (movedEntity != INVALID_ENTITY)
                entityLocationMap.put(movedEntity, entityLocation);
            entityIndex = newArchetype.entityCount - 1; // last added entity
            entityLocationMap.put(entity, (archetypeIndex << ENTITY_INDEX_BITS) | entityIndex);
        }
    }

    public void removeAllEntitiesWithComponents(Query query) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            for (int i = 0; i < count; i++) {
                entityLocationMap.remove(entities[i]);
            }
            archetype.clear();
        });
    }

    // added components and removed components MUST NOT OVERLAP
    void modifyEntity(int entity, int[] addedIndices, Object[] addedComponents, int addedCount, int[] removedIndices, int removedCount) {
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation == -1) {
            int archetypeIndex = getOrCreateArchetype(addedIndices, addedCount);
            Archetype archetype = archetypes[archetypeIndex];
            int entityIndex = archetype.allocEntity(entity);
            archetype.fillComponents(entityIndex, addedIndices, addedComponents, addedCount);
            entityLocationMap.put(entity, (archetypeIndex << ENTITY_INDEX_BITS) | entityIndex);
        } else {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Archetype oldArchetype = archetypes[archetypeIndex];
            archetypeIndex = getOrCreateArchetypeOfUnionAndDiff(oldArchetype.sortedComponentIndices, oldArchetype.sortedComponentIndices.length, addedIndices, addedCount, removedIndices, removedCount);
            Archetype newArchetype = archetypes[archetypeIndex];
            int movedEntity = newArchetype.moveEntityFrom(entity, oldArchetype, entityIndex);
            if (movedEntity != INVALID_ENTITY)
                entityLocationMap.put(movedEntity, entityLocation);
            entityIndex = newArchetype.entityCount - 1; // last added entity
            newArchetype.fillComponents(entityIndex, addedIndices, addedComponents, addedCount);
            entityLocationMap.put(entity, (archetypeIndex << ENTITY_INDEX_BITS) | entityIndex);
        }
    }

    private int createArchetype(Archetype newArchetype, int hash, Archetype lastWithSameHash) {
        int idx = archetypeCount;
        if (idx >= (1 << ENTITY_ARCHETYPE_BITS)) {
            throw new RuntimeException("archetype count overflow");
        }

        archetypeCount++;
        if (archetypeCount > archetypes.length) {
            int newCapacity = archetypes.length * 2;
            archetypes = Arrays.copyOf(archetypes, newCapacity);
        }

        archetypes[idx] = newArchetype;
        {
            int requiredSize = newArchetype.sortedComponentIndices[newArchetype.sortedComponentIndices.length - 1] + 1;
            if (componentToArchetypeIndices.length < requiredSize) {
                int newSize = componentToArchetypeIndices.length * 2;
                while (newSize < requiredSize)
                    newSize *= 2;
                componentToArchetypeIndices = Arrays.copyOf(componentToArchetypeIndices, newSize);
                componentToArchetypeIndicesSizes = Arrays.copyOf(componentToArchetypeIndicesSizes, newSize);
            }
            for (int componentIndex : newArchetype.sortedComponentIndices) {
                short[] indices = componentToArchetypeIndices[componentIndex];
                if (indices == null) {
                    indices = new short[8];
                    indices[0] = (short) idx;
                    componentToArchetypeIndicesSizes[componentIndex]++;
                    componentToArchetypeIndices[componentIndex] = indices;
                } else {
                    short count = ++componentToArchetypeIndicesSizes[componentIndex];
                    if (count > indices.length) {
                        componentToArchetypeIndices[componentIndex] = indices = Arrays.copyOf(indices, indices.length * 2);
                    }
                    indices[count - 1] = (short) idx;
                }
            }
        }

        if (lastWithSameHash != null) {
            // if hash collision occurred, add element to linked list
            lastWithSameHash.hashCollisionResolveIdx = idx;
        } else {
            // otherwise, put in a map
            archetypeIdxByHash.put(hash, idx);
        }

        return idx;
    }

    int getOrCreateArchetype(int[] indices, int count) {
        int hash = IndexArrays.arrayHash(indices, count);
        int idx = archetypeIdxByHash.get(hash, -1);

        // search for archetype
        Archetype archetype = null;
        if (idx != -1) {
            archetype = archetypes[idx];
            while (!IndexArrays.arrayMatch(archetype.sortedComponentIndices, archetype.sortedComponentIndices.length, indices, count)) {
                idx = archetype.hashCollisionResolveIdx;
                if (idx != -1) {
                    archetype = archetypes[idx];
                } else {
                    break;
                }
            }
            if (idx != -1) {
                return idx;
            }
        }

        // adding new archetype
        return createArchetype(new Archetype(Arrays.copyOf(indices, count)), hash, archetype);
    }

    int getOrCreateArchetypeOfUnion(int[] indices1, int count1, int[] indices2, int count2) {
        int hash = IndexArrays.unionHash(indices1, count1, indices2, count2);
        int idx = archetypeIdxByHash.get(hash, -1);

        // search for archetype
        Archetype archetype = null;
        if (idx != -1) {
            archetype = archetypes[idx];
            while (!IndexArrays.unionMatch(archetype.sortedComponentIndices, archetype.sortedComponentIndices.length, indices1, count1, indices2, count2)) {
                idx = archetype.hashCollisionResolveIdx;
                if (idx != -1) {
                    archetype = archetypes[idx];
                } else {
                    break;
                }
            }
            if (idx != -1) {
                return idx;
            }
        }

        // adding new archetype
        return createArchetype(new Archetype(IndexArrays.arrayUnion(indices1, count1, indices2, count2)), hash, archetype);
    }

    int getOrCreateArchetypeOfDiff(int[] indices1, int count1, int[] indices2, int count2) {
        int hash = IndexArrays.diffHash(indices1, count1, indices2, count2);
        int idx = archetypeIdxByHash.get(hash, -1);

        // search for archetype
        Archetype archetype = null;
        if (idx != -1) {
            archetype = archetypes[idx];
            while (!IndexArrays.diffMatch(archetype.sortedComponentIndices, archetype.sortedComponentIndices.length, indices1, count1, indices2, count2)) {
                idx = archetype.hashCollisionResolveIdx;
                if (idx != -1) {
                    archetype = archetypes[idx];
                } else {
                    break;
                }
            }
            if (idx != -1) {
                return idx;
            }
        }

        // adding new archetype
        return createArchetype(new Archetype(IndexArrays.arrayDiff(indices1, count1, indices2, count2)), hash, archetype);
    }

    int getOrCreateArchetypeOfUnionAndDiff(int[] indices1, int count1, int[] indices2, int count2, int[] indices3, int count3) {
        int hash = IndexArrays.unionAndDiffHash(indices1, count1, indices2, count2, indices3, count3);
        int idx = archetypeIdxByHash.get(hash, -1);

        // search for archetype
        Archetype archetype = null;
        if (idx != -1) {
            archetype = archetypes[idx];
            while (!IndexArrays.unionAndDiffMatch(archetype.sortedComponentIndices, archetype.sortedComponentIndices.length, indices1, count1, indices2, count2, indices3, count3)) {
                idx = archetype.hashCollisionResolveIdx;
                if (idx != -1) {
                    archetype = archetypes[idx];
                } else {
                    break;
                }
            }
            if (idx != -1) {
                return idx;
            }
        }

        // adding new archetype
        return createArchetype(new Archetype(IndexArrays.arrayUnionAndDiff(indices1, count1, indices2, count2, indices3, count3)), hash, archetype);
    }


    public interface ArchetypeConsumer {
        void accept(Archetype archetype, int[] entities, Object[][] components, int count);
    }

    void iterArchetypes(Query query, ArchetypeConsumer archetypeConsumer) {
        int[] componentIndices = query.indicesAndReorder;
        int componentIndexCount = query.count;

        int minComponentIdx = query.indicesAndReorder[0];
        short minComponentCount = componentToArchetypeIndicesSizes[minComponentIdx];
        for (int i = 1; i < componentIndexCount; i++) {
            int componentIndex = componentIndices[i];
            short count = componentToArchetypeIndicesSizes[componentIndex];
            if (count < minComponentCount) {
                minComponentCount = count;
                minComponentIdx = componentIndex;
            }
        }

        Object[][] componentArrays = new Object[componentIndexCount][];
        short[] archetypeIndices = componentToArchetypeIndices[minComponentIdx];
        for (int i = 0; i < minComponentCount; i++) {
            short archetypeIndex = archetypeIndices[i];
            Archetype archetype = archetypes[archetypeIndex];
            if (archetype.checkContainsAndFill(componentIndices, componentIndexCount, componentArrays)) {
                archetypeConsumer.accept(archetype, archetype.entities, componentArrays, archetype.entityCount);
            }
        }
    }

    public interface VariadicQueryConsumer {
        void accept(int entity, Object[] components);
    }

    public void performVariadicQuery(Query query, VariadicQueryConsumer consumer) {
        final int componentCount = query.count;
        final Object[] entityComponents = new Object[componentCount];
        iterArchetypes(query, (archetype, entities, components, count) -> {
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < componentCount; j++) {
                    entityComponents[j] = components[j][i];
                }
                consumer.accept(entities[i], entityComponents);
            }
        });
    }

    public interface FixedQueryConsumer0 {
        void accept(int entity);
    }

    public interface FixedQueryConsumer1<T1> {
        void accept(int entity, T1 c1);
    }

    public interface FixedQueryConsumer2<T1, T2> {
        void accept(int entity, T1 c1, T2 c2);
    }

    public interface FixedQueryConsumer3<T1, T2, T3> {
        void accept(int entity, T1 c1, T2 c2, T3 c3);
    }

    public interface FixedQueryConsumer4<T1, T2, T3, T4> {
        void accept(int entity, T1 c1, T2 c2, T3 c3, T4 c4);
    }

    public interface FixedQueryConsumer5<T1, T2, T3, T4, T5> {
        void accept(int entity, T1 c1, T2 c2, T3 c3, T4 c4, T5 c5);
    }

    public interface FixedQueryConsumer6<T1, T2, T3, T4, T5, T6> {
        void accept(int entity, T1 c1, T2 c2, T3 c3, T4 c4, T5 c5, T6 c6);
    }

    public void performQuery(Query query, FixedQueryConsumer0 consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            for (int i = 0; i < count; i++) {
                consumer.accept(entities[i]);
            }
        });
    }

    public <T1> void performQuery(Query query, FixedQueryConsumer1<T1> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i]);
            }
        });
    }

    public <T1, T2> void performQuery(Query query, FixedQueryConsumer2<T1, T2> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            Object[] c2 = components[1];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i], (T2) c2[i]);
            }
        });
    }

    public <T1, T2, T3> void performQuery(Query query, FixedQueryConsumer3<T1, T2, T3> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            Object[] c2 = components[1];
            Object[] c3 = components[2];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i], (T2) c2[i], (T3) c3[i]);
            }
        });
    }

    public <T1, T2, T3, T4> void performQuery(Query query, FixedQueryConsumer4<T1, T2, T3, T4> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            Object[] c2 = components[1];
            Object[] c3 = components[2];
            Object[] c4 = components[3];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i], (T2) c2[i], (T3) c3[i], (T4) c4[i]);
            }
        });
    }

    public <T1, T2, T3, T4, T5> void performQuery(Query query, FixedQueryConsumer5<T1, T2, T3, T4, T5> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            Object[] c2 = components[1];
            Object[] c3 = components[2];
            Object[] c4 = components[3];
            Object[] c5 = components[4];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i], (T2) c2[i], (T3) c3[i], (T4) c4[i], (T5) c5[i]);
            }
        });
    }

    public <T1, T2, T3, T4, T5, T6> void performQuery(Query query, FixedQueryConsumer6<T1, T2, T3, T4, T5, T6> consumer) {
        iterArchetypes(query, (archetype, entities, components, count) -> {
            Object[] c1 = components[0];
            Object[] c2 = components[1];
            Object[] c3 = components[2];
            Object[] c4 = components[3];
            Object[] c5 = components[4];
            Object[] c6 = components[5];
            for (int i = 0; i < count; i++) {
                //noinspection unchecked
                consumer.accept(entities[i], (T1) c1[i], (T2) c2[i], (T3) c3[i], (T4) c4[i], (T5) c5[i], (T6) c6[i]);
            }
        });
    }

    public Object[] getComponents(int entity, Query query) {
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation != -1) {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Archetype archetype = archetypes[archetypeIndex];
            Object[] result = new Object[query.count];
            if (archetype.checkContainsAndFillOneEntity(entityIndex, query.indicesAndReorder, query.count, result)) {
                return result;
            }
        }
        return null;
    }

    public Object getComponent(int entity, int componentIndex) {
        int entityLocation = entityLocationMap.get(entity, -1);
        if (entityLocation != -1) {
            int archetypeIndex = entityLocation >> ENTITY_INDEX_BITS;
            int entityIndex = entityLocation & ((1 << ENTITY_INDEX_BITS) - 1);
            Object[] components = archetypes[archetypeIndex].getComponentArrayForIndex(componentIndex);
            return components != null ? components[entityIndex] : null;
        }
        return null;
    }
}
