package org.innercore.icstd.scene;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GameObjectManagerBindGenBase;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.ChunkAABB;
import org.innercore.icstd.common.ChunkPos;
import org.innercore.icstd.component.*;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.serialization.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/*
Game object lifecycle:
1. onAddedToScene is called, when object is placed on scene (placeAs* methods, object load, etc.)
2. onCreated is called, if it is the first time object is created
3. onInit is called immediately before object becomes loaded first time this session
4. onLoad is called, as object becomes loaded
5. onUnload is called, as object becomes unloaded or before removal, from here it can wait to become loaded again, or proceed further
6. onDiscarded is called (opposite to onInit)
7. onDestroyed is called, if it is object destruction (opposite to onCreated) (e.g. block entity is destroyed by something)
8. onRemovedFromScene is called, as object is removed from scene
*/

@ApiBinding
public class GameObjectManager extends GameObjectManagerBindGenBase {
    private static final int STATE_NOT_LOADED = 0;
    private static final int STATE_LAZY_LOADED = 1;
    private static final int STATE_LOADED = 2;

    private static final int POS_TYPE_NONE = 0;   // not valid
    private static final int POS_TYPE_GLOBAL = 1; // always loaded if scene loaded
    private static final int POS_TYPE_BLOCK = 2;  // exists at block pos

    static final int OBJECT_STATE_DEAD = 0; // initial state
    static final int OBJECT_STATE_CREATED = 1; // onCreated called
    static final int OBJECT_STATE_INIT = 2; // onInit called
    static final int OBJECT_STATE_LOADED = 3; // onLoaded called


    private final GameObjectScope objectScope;
    private final String uniqueName;
    private int dimensionId = -1;
    private boolean isDefaultScene = false;
    private boolean isScopeLoaded = false;

    public GameObjectManager(GameObjectScope scope, String uniqueName) {
        this.objectScope = scope;
        this.uniqueName = uniqueName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public GameObjectManager setDimensionId(int dimensionId) {
        this.dimensionId = dimensionId;
        return this;
    }

    GameObjectManager setDefaultScene(boolean isDefaultScene) {
        this.isDefaultScene = isDefaultScene;
        return this;
    }

    @ApiBinding public int getDimensionId() {
        return dimensionId;
    }

    @ApiBinding public boolean isServerSide() {
        return objectScope.isServerSide();
    }

    @ApiBinding public boolean isDefaultScene() {
        return isDefaultScene;
    }

    public GameObjectScope getObjectScope() {
        return objectScope;
    }


    // ADD/UPDATE


    GameObjectHandle createImpl(GameObjectHandle handle, Object object, int initialObjectState) {
        if (handle.isExpired()) {
            throw new IllegalStateException("handle is expired " + handle);
        }
        if (handle.isAlive()) {
            throw new IllegalStateException("handle is already alive");
        }
        if (handle.scope != objectScope) {
            throw new IllegalStateException("handle is from different scope");
        }
        int slotIdx = allocateSlot();
        slots[slotIdx].handle = handle;
        handle.slotIdx = slotIdx;
        handle.scene = this;
        setSlotObjectImpl(slotIdx, object, OBJECT_STATE_DEAD, initialObjectState);
        markObjectForUpdate(slotIdx);
        return handle;
    }

    @ApiBinding
    public GameObjectHandle create(Object object) {
        GameObjectHandle handle = objectScope.createUniqueHandle();
        createImpl(handle, object, OBJECT_STATE_DEAD);
        return handle;
    }

    @ApiHidden
    public GameObjectHandle addCreatedObject(Object object) {
        GameObjectHandle handle = objectScope.createUniqueHandle();
        createImpl(handle, object, OBJECT_STATE_CREATED);
        return handle;
    }

    void placeAsGlobal(int slotIdx) {
        updateObjectPosImpl(slotIdx, slot -> {
            slot.posType = POS_TYPE_GLOBAL;
        });
    }

    void placeAtBlockPos(int slotIdx, BlockPos pos) {
        updateObjectPosImpl(slotIdx, slot -> {
            slot.posType = POS_TYPE_BLOCK;
            slot.x = pos.x;
            slot.y = pos.y;
            slot.z = pos.z;
        });
    }

    private void updateObjectPosImpl(int slotIdx, Consumer<ObjectSlot> slotConsumer) {
        ObjectSlot slot = slots[slotIdx];
        boolean wasPlaced = slot.isPlacedOnScene();
        BlockPos prevBlockPos = slot.getBlockPos();
        ChunkAABB prevAABB = slot.getChunkAABB();
        slotConsumer.accept(slot);
        updateObjectAABB(slotIdx, prevAABB, slot.getChunkAABB());
        updateObjectBlockPos(slotIdx, prevBlockPos, slot.getBlockPos());
        if (wasPlaced != slot.isPlacedOnScene() && slot.object != null) {
            if (wasPlaced) {
                ComponentUtils.safeCallRemovedFromScene(slot.object, slot.handle);
            } else {
                ComponentUtils.safeCallAddedToScene(slot.object, slot.handle);
            }
        }
        if (slot.objectState < OBJECT_STATE_CREATED && slot.object != null) {
            setSlotObjectState(slotIdx, OBJECT_STATE_CREATED);
        }
        markObjectForUpdate(slotIdx);
    }

    void destroyDeferred(int slotIdx) {
        slots[slotIdx].markedForDestroy = true;
        markObjectForUpdate(slotIdx);
    }

    // SCENE LEVEL STATE

    @ApiBinding
    public void runStateUpdates(@Nullable BlockSource region, int maxIterations) {
        boolean anyUpdated;
        do {
            anyUpdated = false;
            anyUpdated = updateAllMarkedChunks(region) || anyUpdated;
            anyUpdated = updateAllMarkedObjects(region) || anyUpdated;
        } while (anyUpdated && --maxIterations > 0);
    }

    @ApiBinding
    public void runDefaultTick(@Nullable BlockSource region, int maxTimeUs) {
        runStateUpdates(region, 2);
        runPeriodicChecks(8, 0.01f, maxTimeUs);
    }

    @ApiBinding
    public void setScopeLoaded(boolean loaded) {
        if (this.isScopeLoaded == loaded) return;
        isScopeLoaded = loaded;
        for (int slotIdx = 0; slotIdx < slotsSize; slotIdx++) {
            if (!loaded) setSlotLoaded(slotIdx, false);
            else if (slots[slotIdx].posType == POS_TYPE_GLOBAL) {
                setSlotLoaded(slotIdx, true);
            }
        }
    }

    @ApiBinding
    public boolean isScopeLoaded() {
        return isScopeLoaded;
    }

    @ApiBinding
    public void clearAllObjects(boolean runLifeCycleCallbacks, boolean isDestroy) {
        for (int slotIdx = 0; slotIdx < slotsSize; slotIdx++) {
            GameObjectHandle handle = slots[slotIdx].handle;
            if (handle == null)
                continue;
            if (runLifeCycleCallbacks)
                setSlotObjectImpl(slotIdx, null, isDestroy ? OBJECT_STATE_DEAD : OBJECT_STATE_CREATED, OBJECT_STATE_DEAD);
            objectScope.expireHandle(handle);
        }
        invalidateQueryGeneration();

        slots = new ObjectSlot[0];
        slotsSize = 0;
        slotsFreeListHead = -1;

        periodicChecksList = new int[0];
        periodicChecksListSize = 0;
        periodicChecksListNextIdx = 0;

        chunks.clear();
        lookupByPos.clear();
        chunksMarkedForUpdate.clear();
        objectsMarkedForUpdate.clear();

        unresolvedSavedObjectsList.clear();
    }

    @ApiBinding
    public void markChunkForUpdate(int x, int z) {
        for (int xi = x - 1; xi <= x + 1; xi++)
            for (int zi = z - 1; zi <= z + 1; zi++)
                markOneChunkForUpdate(new ChunkPos(xi, zi));
    }

    public void markChunkForUpdate(ChunkPos pos) {
        markChunkForUpdate(pos.x, pos.z);
    }

    // GETTERS

    int getObjectState(int slotIdx) {
        return slots[slotIdx].objectState;
    }

    Object getObject(int slotIdx) {
        return slots[slotIdx].object;
    }

    BlockPos getBlockPos(int slotIdx) {
        return slots[slotIdx].getBlockPos();
    }

    // QUERY

    private abstract class BaseQueryImpl<T> implements GameObjectQuery<T> {
        @Override
        public boolean isEmpty() {
            return searchSlot(firstSlot()) == -1;
        }

        @Override
        public T one() {
            int i = searchSlot(firstSlot());
            if (i == -1) return null;
            return (T) slots[i].object;
        }

        @Override
        public GameObjectHandle oneHandle() {
            int i = searchSlot(firstSlot());
            if (i == -1) return null;
            return slots[i].handle;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                final int gen = queryValidationGeneration;
                int i = firstSlot();
                @Override public boolean hasNext() { validateQueryGeneration(gen); i = searchSlot(i); return i != -1; }
                @Override public T next() {
                    validateQueryGeneration(gen);
                    i = searchSlot(i);
                    if (i == -1) return null;
                    ObjectSlot slot = slots[i];
                    i = getNextSlot(i);
                    return (T) slot.object;
                }
            };
        }

        @Override
        public Iterator<GameObjectHandle> iterHandles() {
            return new Iterator<GameObjectHandle>() {
                final int gen = queryValidationGeneration;
                int i = firstSlot();
                @Override public boolean hasNext() { validateQueryGeneration(gen); i = searchSlot(i); return i != -1; }
                @Override public GameObjectHandle next() {
                    validateQueryGeneration(gen);
                    i = searchSlot(i);
                    if (i == -1) return null;
                    ObjectSlot slot = slots[i];
                    i = getNextSlot(i);
                    return slot.handle;
                }
            };
        }

        @Override
        public List<T> toList() {
            List<T> list = new ArrayList<>();
            int i = searchSlot(firstSlot());
            while (i != -1) {
                list.add((T) slots[i].object);
                i = searchSlot(getNextSlot(i));
            }
            return list;
        }

        @Override
        public List<GameObjectHandle> toHandleList() {
            List<GameObjectHandle> list = new ArrayList<>();
            int i = searchSlot(firstSlot());
            while (i != -1) {
                list.add(slots[i].handle);
                i = searchSlot(getNextSlot(i));
            }
            return list;
        }

        protected abstract int firstSlot();
        protected abstract int searchSlot(int idx);
        protected abstract int getNextSlot(int idx);
    }

    private int queryValidationGeneration = 0;

    private void invalidateQueryGeneration() {
        queryValidationGeneration++;
    }

    private void validateQueryGeneration(int gen) {
        if (queryValidationGeneration != gen) {
            throw new ConcurrentModificationException("game object was destroyed/discarded/moved during query");
        }
    }

    public GameObjectQuery<Object> queryAtBlock(BlockPos pos) {
        return new BaseQueryImpl<Object>() {
            @Override protected int firstSlot() {
                Integer idx = lookupByPos.get(pos);
                return idx != null ? idx : -1;
            }
            @Override protected int searchSlot(int i) {
                ObjectSlot slot;
                while (i != -1 && ((slot = slots[i]).object) == null) {
                    i = slot.nextAtPos;
                }
                return i;
            }
            @Override protected int getNextSlot(int idx) { return slots[idx].nextAtPos; }
        };
    }

    public<T> GameObjectQuery<T> queryAtBlock(BlockPos pos, Class<T> cls) {
        return new BaseQueryImpl<T>() {
            @Override protected int firstSlot() {
                Integer idx = lookupByPos.get(pos);
                return idx != null ? idx : -1;
            }
            @Override protected int searchSlot(int i) {
                ObjectSlot slot;
                while (i != -1 && !cls.isInstance((slot = slots[i]).object)) {
                    i = slot.nextAtPos;
                }
                return i;
            }
            @Override protected int getNextSlot(int i) { return slots[i].nextAtPos; }
        };
    }

    // TODO: optimize global object queries

    public GameObjectQuery<Object> queryGlobal() {
        return new BaseQueryImpl<Object>() {
            @Override protected int firstSlot() { return 0; }
            @Override protected int searchSlot(int i) {
                for (; i < slotsSize && (slots[i] == null || slots[i].posType != POS_TYPE_GLOBAL || slots[i].object == null); i++);
                return i < slotsSize ? i : -1;
            }
            @Override protected int getNextSlot(int idx) { return idx + 1; }
        };
    }

    public<T> GameObjectQuery<T> queryGlobal(Class<T> cls) {
        return new BaseQueryImpl<T>() {
            @Override protected int firstSlot() { return 0; }
            @Override protected int searchSlot(int i) {
                for (; i < slotsSize && (slots[i] == null || slots[i].posType != POS_TYPE_GLOBAL || !cls.isInstance(slots[i].object)); i++);
                return i < slotsSize ? i : -1;
            }
            @Override protected int getNextSlot(int idx) { return idx + 1; }
        };
    }


    // INTERNALS


    private Chunk getOrCreateChunk(ChunkPos pos) {
        Chunk chunk = chunks.get(pos);
        if (chunk == null) {
            chunk = new Chunk();
            chunk.pos = pos;
            chunks.put(pos, chunk);
            markOneChunkForUpdate(chunk);
        }
        return chunk;
    }

    private int allocateSlot() {
        if (slotsFreeListHead == -1) {
            if (slotsSize >= slots.length)
                slots = Arrays.copyOf(slots, Math.max(slots.length * 3 / 2 + 1, 32));
            int idx = slotsSize++;
            slots[idx] = new ObjectSlot();
            return idx;
        }
        int idx = slotsFreeListHead;
        slotsFreeListHead = slots[idx].nextAtPos; // in free list mode it is used as next in free list
        return idx;
    }

    private void freeSlot(int idx, boolean expireHandle) {
        ObjectSlot slot = slots[idx];
        Object o = setSlotObjectImpl(idx, null, OBJECT_STATE_DEAD, OBJECT_STATE_DEAD);
        assert o == null; // must be already cleared when freeSlot is called
        slot.nextAtPos = slotsFreeListHead;
        slot.prevAtPos = -1;
        slot.posType = POS_TYPE_NONE;
        slot.objectState = OBJECT_STATE_DEAD;
        if (slot.handle != null) {
            if (expireHandle) {
                objectScope.expireHandle(slot.handle);
            }
            slot.handle.scene = null;
            slot.handle.slotIdx = -1;
            slot.handle = null;
        }
        slot.markedForDestroy = slot.markedForUpdate = false;
        slotsFreeListHead = idx;
        invalidateQueryGeneration();
    }

    static class ObjectSlot {
        GameObjectHandle handle;
        int x, y, z;
        int posType = POS_TYPE_NONE;
        Object object;
        boolean markedForUpdate = false;
        boolean markedForDestroy = false;
        int objectState = OBJECT_STATE_DEAD;

        int nextAtPos = -1;
        int prevAtPos = -1;

        int periodicChecksListIdx = -1;

        boolean isObjectLoaded() { return objectState >= OBJECT_STATE_LOADED; }
        boolean isPlacedOnScene() {
            return posType != POS_TYPE_NONE;
        }

        BlockPos getBlockPos() {
            if (posType == POS_TYPE_BLOCK) return new BlockPos(x, y, z);
            return null;
        }

        ChunkAABB getChunkAABB() {
            if (posType == POS_TYPE_BLOCK) {
                ChunkPos p = new ChunkPos(new BlockPos(x, y, z));
                return new ChunkAABB(p.x, p.z, p.x + 1, p.z + 1);
            }
            return null;
        }
    }

    private ObjectSlot[] slots = new ObjectSlot[0];
    int slotsSize = 0;
    int slotsFreeListHead = -1;


    private void setSlotObjectState(int slotIdx, int state) {
        ObjectSlot slot = slots[slotIdx];
        if (slot.objectState == state)
            return;
        assert slot.object != null || state == OBJECT_STATE_DEAD;
        boolean wasLoaded = slot.isObjectLoaded();
        if (slot.objectState < state) {
            while (slot.objectState < state) {
                slot.objectState++;
                switch (slot.objectState) {
                    case OBJECT_STATE_CREATED: { ComponentUtils.safeCallCreatedFor(slot.object); break; }
                    case OBJECT_STATE_INIT: { ComponentUtils.safeCallInitFor(slot.object); break; }
                    case OBJECT_STATE_LOADED: { ComponentUtils.safeCallLoadOrUnloadFor(slot.object, true); break; }
                }
            }
        } else if (slot.objectState > state) {
            while (slot.objectState > state) {
                slot.objectState--;
                switch (slot.objectState + 1) {
                    case OBJECT_STATE_CREATED: { ComponentUtils.safeCallDestroyFor(slot.object); break; }
                    case OBJECT_STATE_INIT: { ComponentUtils.safeCallDiscardFor(slot.object); break; }
                    case OBJECT_STATE_LOADED: { ComponentUtils.safeCallLoadOrUnloadFor(slot.object, false); break; }
                }
            }
        }
        if (slot.isObjectLoaded() != wasLoaded)
            setPeriodicChecksEnables(slotIdx, slot, slot.isObjectLoaded() && slot.object instanceof PeriodicObjectCheckComponent);
    }

    private void setSlotLoaded(int slotIdx, boolean isLoaded) {
        ObjectSlot slot = slots[slotIdx];
        if (isLoaded) {
            if (slot.object != null)
                setSlotObjectState(slotIdx, OBJECT_STATE_LOADED);
        } else if (slot.objectState >= OBJECT_STATE_LOADED) {
            setSlotObjectState(slotIdx, OBJECT_STATE_INIT);
        }
    }

    private Object setSlotObjectImpl(int slotIdx, Object object, int reduceToState, int newObjectState) {
        ObjectSlot slot = slots[slotIdx];
        Object o = slot.object;
        if (slot.object != object) {
            setSlotObjectState(slotIdx, Math.min(slot.objectState, reduceToState));
            if (slot.isPlacedOnScene()) {
                ComponentUtils.safeCallRemovedFromScene(slot.object, slot.handle);
            }
            slot.object = object;
            slot.objectState = newObjectState;
            if (slot.isPlacedOnScene()) {
                ComponentUtils.safeCallAddedToScene(slot.object, slot.handle);
            }
        }
        return o;
    }

    Object unlinkSlotImpl(int slotIdx, int reduceToState) {
        ObjectSlot slot = slots[slotIdx];
        Object o = setSlotObjectImpl(slotIdx, null, reduceToState, OBJECT_STATE_DEAD);
        updateObjectBlockPos(slotIdx, slot.getBlockPos(), null);
        updateObjectAABB(slotIdx, slot.getChunkAABB(), null);
        freeSlot(slotIdx, reduceToState == OBJECT_STATE_DEAD);
        return o;
    }


    public class Chunk {
        ChunkPos pos;
        // TODO: implement heap for logN add/remove
        int[] objects;
        int objectCnt = 0;
        int loadState = STATE_NOT_LOADED;
        boolean markedForUpdate = false;

        int getCurLoadedState(BlockSource region) {
            int x = pos.x, z = pos.z;
            if (region == null || !region.isChunkLoaded(x, z))
                return STATE_NOT_LOADED;
            return region.isChunkLoaded(x - 1, z - 1) &&
                    region.isChunkLoaded(x + 1, z + 1) &&
                    region.isChunkLoaded(x - 1, z + 1) &&
                    region.isChunkLoaded(x + 1, z - 1) ? STATE_LOADED : STATE_LAZY_LOADED;
        }

        void add(int idx) {
            if (objects == null || objectCnt == objects.length) {
                if (objects == null) {
                    objects = new int[slots.length];
                } else {
                    objects = Arrays.copyOf(objects, objects.length * 3 / 2 + 1);
                }
            }
            objects[objectCnt++] = idx;
        }

        void remove(int idx) {
            for (int i = 0; i < objectCnt; i++) {
                if (objects[i] == idx) {
                    objects[i] = objects[--objectCnt];
                }
            }
        }
    }

    private HashMap<ChunkPos, Chunk> chunks = new HashMap<>();
    private HashMap<BlockPos, Integer> lookupByPos = new HashMap<>();

    private void updateObjectBlockPos(int slotIdx, BlockPos prevPos, BlockPos newPos) {
        if (prevPos != null && prevPos.equals(newPos)) {
            return;
        }
        ObjectSlot slot = slots[slotIdx];
        if (prevPos != null) {
            Integer slotIdxByPos = lookupByPos.get(prevPos);
            if (slotIdxByPos != null && slotIdxByPos == slotIdx) lookupByPos.remove(prevPos);
            if (slot.prevAtPos != -1) slots[slot.prevAtPos].nextAtPos = slot.nextAtPos;
            if (slot.nextAtPos != -1) slots[slot.nextAtPos].prevAtPos = slot.prevAtPos;
            slot.prevAtPos = slot.nextAtPos = -1;
            invalidateQueryGeneration();
        }
        if (newPos != null) {
            Integer firstSlotIdx = lookupByPos.get(newPos);
            if (firstSlotIdx != null) {
                ObjectSlot first = slots[firstSlotIdx];
                slot.nextAtPos = first.nextAtPos;
                if (first.nextAtPos != -1) slots[first.nextAtPos].prevAtPos = slotIdx;
                first.nextAtPos = slotIdx;
                slot.prevAtPos = firstSlotIdx;
            } else {
                lookupByPos.put(newPos, slotIdx);
            }
        }
    }

    private void updateObjectAABB(int slotIdx, ChunkAABB prevAABB, ChunkAABB newAABB) {
        if (newAABB != null && newAABB.equals(prevAABB)) {
            return;
        }
        if (prevAABB != null)
            for (int x = prevAABB.x1, x2 = prevAABB.x2; x < x2; x++) {
                for (int z = prevAABB.z1, z2 = prevAABB.z2; z < z2; z++) {
                    if (newAABB == null || !newAABB.contains(x, z)) {
                        Chunk chunk = chunks.get(new ChunkPos(x, z));
                        if (chunk != null) {
                            chunk.remove(slotIdx);
                            if (chunk.objectCnt == 0)
                                chunks.remove(new ChunkPos(x, z));
                        }
                    }
                }
            }
        if (newAABB != null)
            for (int x = newAABB.x1, x2 = newAABB.x2; x < x2; x++) {
                for (int z = newAABB.z1, z2 = newAABB.z2; z < z2; z++) {
                    if (prevAABB == null || !prevAABB.contains(x, z)) {
                        getOrCreateChunk(new ChunkPos(x, z)).add(slotIdx);
                    }
                }
            }
    }

    private void serializeOneObject(IScopedWriter scope, ObjectSlot slot, String typeId, SerializationContext ctx) {
        IArrayWriter writer = scope.asArray();
        writer.addInt(slot.posType);
        writer.addString(typeId);
        writer.addLong(slot.handle.value);
        ObjectSerializer.serializeObjectUnpacked(writer.beginScope(), slot.object, typeId, ctx, true);
        switch (slot.posType) {
            case POS_TYPE_GLOBAL:
                break;
            case POS_TYPE_BLOCK:
                writer.addInt(slot.x).addInt(slot.y).addInt(slot.z);
                break;
        }
        scope.endScope();
    }

    private GameObjectHandle deserializeAndInitOneObject(IScopedReader scope, SerializationContext ctx, IArrayWriter unresolvedList) {
        try {
            return deserializeAndInitOneObjectImpl(scope, ctx, unresolvedList);
        } catch (Throwable err) {
            ctx.addError(err, null, "failed to deserialize game object");
            scope.forceEndScope(ctx);
            return null;
        }
    }

    private GameObjectHandle deserializeAndInitOneObjectImpl(IScopedReader scope, SerializationContext ctx, IArrayWriter unresolvedList) {
        IArrayReader reader = scope.asArray();
        if (reader == null || reader.size() < 4) {
            ctx.addError("unexpected data format for game object");
            scope.endScope();
            return null;
        }
        int posType = reader.getInt(0, POS_TYPE_NONE);
        String typeId = reader.getString(1, null);
        long rawHandle = reader.getLong(2, -1);
        if (posType == POS_TYPE_NONE || typeId == null || rawHandle < 0 || !reader.getValueType(3).isScope()) {
            ctx.addError("unexpected data for game object posType=" + posType + " typeId=" + typeId + " rawHandle=" + rawHandle);
            scope.endScope();
            return null;
        }
        if (!ObjectSerializer.isTypeRegistered(typeId)) {
            // no saver registered for this type, keep serialized object to avoid losing data
            SerializationUtils.readerToWriter(scope, unresolvedList.beginScope());
            return null;
        }
        Object object = ObjectSerializer.deserializeObjectUnpacked(reader.beginScope(3), typeId, Object.class, ctx, true);
        if (object == null) {
            // object deserialized into null - it doesn't want to be added to scene or failed
            scope.endScope();
            return null;
        }
        GameObjectHandle handle = null;
        switch (posType) {
            case POS_TYPE_GLOBAL:
                handle = createImpl(objectScope.getOrCreateHandle(rawHandle), object, OBJECT_STATE_CREATED).placeAsGlobal();
                break;
            case POS_TYPE_BLOCK:
                if (reader.size() < 7) {
                    ctx.addError("failed to deserialize object block pos");
                    scope.endScope();
                    return null;
                }
                int x = reader.getInt(4, Integer.MAX_VALUE);
                int y = reader.getInt(5, Integer.MAX_VALUE);
                int z = reader.getInt(6, Integer.MAX_VALUE);
                if (x == Integer.MAX_VALUE || y == Integer.MAX_VALUE || z == Integer.MAX_VALUE) {
                    ctx.addError("failed to deserialize object block pos");
                    scope.endScope();
                    return null;
                }
                handle = createImpl(objectScope.getOrCreateHandle(rawHandle), object, OBJECT_STATE_CREATED).placeAtBlockPos(new BlockPos(x, y, z));
                break;
        }
        scope.endScope();
        return handle;
    }

    private void deserializeAllObjects(IArrayReader objects, SerializationContext ctx, IArrayWriter unresolvedList) {
        for (int i = 0; i < objects.size(); i++) {
            deserializeAndInitOneObject(objects.beginScope(i), ctx, unresolvedList);
        }
    }

    private final List<Integer> objectsMarkedForUpdate = new ArrayList<>();
    private final List<Chunk> chunksMarkedForUpdate = new ArrayList<>();

    private void markOneChunkForUpdate(ChunkPos pos) {
        Chunk chunk = chunks.get(pos);
        if (chunk != null)
            markOneChunkForUpdate(chunk);
    }

    private void markOneChunkForUpdate(Chunk chunk) {
        if (!chunk.markedForUpdate) {
            chunksMarkedForUpdate.add(chunk);
            chunk.markedForUpdate = true;
        }
    }

    private void markObjectForUpdate(int slotIdx) {
        ObjectSlot slot = slots[slotIdx];
        if (!slot.markedForUpdate) {
            objectsMarkedForUpdate.add(slotIdx);
            slot.markedForUpdate = true;
        }
    }

    private boolean updateObjectLoadedStateInChunk(int slotIdx, Chunk baseChunk, ChunkAABB chunkAABB) {
        boolean isObjectLoaded = true;
        int baseX = baseChunk.pos.x, baseZ = baseChunk.pos.z;
        for (int x = chunkAABB.x1, x2 = chunkAABB.x2; x < x2 && isObjectLoaded; x++) {
            for (int z = chunkAABB.z1, z2 = chunkAABB.z2; z < z2 && isObjectLoaded; z++) {
                Chunk c = x == baseX && z == baseZ ? baseChunk : chunks.get(new ChunkPos(x, z));
                isObjectLoaded = c.loadState == STATE_LOADED; // TODO: finer state logic support
            }
        }
        setSlotLoaded(slotIdx, isObjectLoaded);
        return isObjectLoaded;
    }

    private boolean updateObjectLoadedState(int slotIdx, ObjectSlot slot) {
        if (slot.posType == POS_TYPE_BLOCK) {
            Chunk chunk = chunks.get(new ChunkPos(slot.x, slot.y, slot.z));
            if (chunk != null) {
                return updateObjectLoadedStateInChunk(slotIdx, chunk, slot.getChunkAABB());
            }
            // should not happen
            return slot.isObjectLoaded();
        } else if (slot.posType == POS_TYPE_GLOBAL) {
            setSlotLoaded(slotIdx, isScopeLoaded);
        } else if (slot.posType == POS_TYPE_NONE) {
            setSlotLoaded(slotIdx, false);
        }
        return slot.isObjectLoaded();
    }

    private boolean updateAllMarkedChunks(BlockSource region) {
        if (chunksMarkedForUpdate.isEmpty())
            return false;
        ArrayList<Chunk> chunksToUpdate = new ArrayList<>();
        for (Chunk chunk : chunksMarkedForUpdate) {
            chunk.markedForUpdate = false;
            int state = chunk.getCurLoadedState(region);
            if (state != chunk.loadState) {
                //ICLog.d("GameObjectManager", "chunk state changed pos=" + chunk.pos + " state=" + chunk.loadState + "->" + state);
                chunk.loadState = state;
                chunksToUpdate.add(chunk);
            }
        }
        chunksMarkedForUpdate.clear();
        for (Chunk chunk : chunksToUpdate) {
            for (int i = 0; i < chunk.objectCnt; i++) {
                int slotIdx = chunk.objects[i];
                ObjectSlot slot = slots[slotIdx];
                slot.markedForUpdate = false;
                if (slot.object == null) continue;
                ChunkAABB chunkAABB = slot.getChunkAABB();
                if (chunkAABB == null) continue;
                updateObjectLoadedStateInChunk(slotIdx, chunk, chunkAABB);
            }
        }
        return !chunksToUpdate.isEmpty();
    }

    private boolean updateAllMarkedObjects(BlockSource region) {
        if (objectsMarkedForUpdate.isEmpty())
            return false;
        for (int slotIdx : objectsMarkedForUpdate) {
            ObjectSlot slot = slots[slotIdx];
            if (slot.markedForDestroy) {
                unlinkSlotImpl(slotIdx, OBJECT_STATE_DEAD);
                continue;
            }
            if (slot.markedForUpdate) {
                updateObjectLoadedState(slotIdx, slot);
                slot.markedForUpdate = false;
            }
        }
        objectsMarkedForUpdate.clear();
        return true;
    }


    // PERIODIC CHECKS


    private int[] periodicChecksList = new int[0];
    private int periodicChecksListSize = 0;
    private int periodicChecksListNextIdx = 0;

    private void setPeriodicChecksEnables(int slotIdx, ObjectSlot slot, boolean isEnabled) {
        if ((slot.periodicChecksListIdx != -1) == isEnabled)
            return;
        if (isEnabled) {
            // insert
            slot.periodicChecksListIdx = periodicChecksListSize++;
            if (periodicChecksListSize >= periodicChecksList.length) {
                periodicChecksList = Arrays.copyOf(periodicChecksList, Math.max(periodicChecksList.length * 3 / 2 + 1, periodicChecksListSize + 32));
            }
            periodicChecksList[slot.periodicChecksListIdx] = slotIdx;
        } else {
            // unordered erase
            int lastIdx = periodicChecksListSize - 1;
            if (lastIdx != slot.periodicChecksListIdx) {
                slots[periodicChecksList[lastIdx]].periodicChecksListIdx = slot.periodicChecksListIdx;
                periodicChecksList[slot.periodicChecksListIdx] = periodicChecksList[lastIdx];
            }
            slot.periodicChecksListIdx = -1;
            periodicChecksListSize--;
        }
    }

    public void runPeriodicChecks(int maxChecksAbs, float maxChecksRel, int maxTimeUs) {
        int numChecks = Math.min(maxChecksAbs, Math.max(1, (int) (periodicChecksListSize * maxChecksRel)));
        if (numChecks <= 0 || periodicChecksListSize == 0)
            return;
        long endTimeNs = maxTimeUs > 0 ? System.nanoTime() + maxTimeUs * 1000L : Long.MAX_VALUE;
        periodicChecksListNextIdx %= periodicChecksListSize;
        for (int i = 0; i < numChecks && periodicChecksListSize > 0; i++) {
            int idx = periodicChecksListNextIdx;
            int slotIdx = periodicChecksList[idx];
            ObjectSlot slot = slots[slotIdx];
            Object o = slot.object;
            if (o instanceof PeriodicObjectCheckComponent) {
                ComponentUtils.safeCallPeriodicCheckFor(o);
                ++periodicChecksListNextIdx;
            } else {
                setPeriodicChecksEnables(slotIdx, slot, false);
            }
            if (periodicChecksListNextIdx >= periodicChecksListSize)
                periodicChecksListNextIdx = 0;
            if (System.nanoTime() >= endTimeNs)
                break;
        }
    }


    // GLOBAL


    // - uses server-side block source, not loaded and not saved on client
    // - objects are saved
    @ApiBinding
    public static GameObjectManager getForDimension(int dimensionId) {
        GameObjectManager scene = defaultMgrForDimension.get(dimensionId);
        if (scene == null) {
            defaultMgrForDimension.put(dimensionId, scene = new GameObjectManager(GameObjectScope.SERVER_SCOPE, "default-" + dimensionId).setDimensionId(dimensionId).setDefaultScene(true));
        }
        return scene;
    }
    private static final HashMap<Integer, GameObjectManager> defaultMgrForDimension = new HashMap<>();
    private static final GameObjectManager defaultServerMgr = new GameObjectManager(GameObjectScope.SERVER_SCOPE, "default-server-global").setDefaultScene(true);

    @ApiBinding
    public static GameObjectManager getGlobalForServer() { return defaultServerMgr; }

    // - on client uses client-side block source
    // - cleared when going between dimensions
    // - not loaded on server
    @ApiBinding
    public static GameObjectManager getClientSide() { return clientSideScene; }
    private static final GameObjectManager clientSideScene = new GameObjectManager(GameObjectScope.CLIENT_SCOPE, "default-client").setDefaultScene(true);

    public static void clearAllDefaultServerScenes(boolean runLifeCycleCallbacks, boolean isDestroy) {
        for (GameObjectManager mgr : defaultMgrForDimension.values()) {
            mgr.clearAllObjects(runLifeCycleCallbacks, isDestroy);
        }
        defaultMgrForDimension.clear();
        defaultServerMgr.clearAllObjects(runLifeCycleCallbacks, isDestroy);
    }

    public static void clearAllDefaultClientScenes(boolean runLifeCycleCallbacks, boolean isDestroy) {
        clientSideScene.clearAllObjects(runLifeCycleCallbacks, isDestroy);
    }

    /*
    @ApiBinding
    public Scene getPersistent() { return globalPersistent; }
    private final Scene globalPersistent = new Scene("default-persistent");
    */

    public static HashMap<Integer, GameObjectManager> getForAllExistingDimensions() { return defaultMgrForDimension; }

    public static void runTickForDefaultsOnClient() {
        clientSideScene.runDefaultTick(BlockSource.getCurrentClientRegion(), 500);
    }

    public static void runTickForDefaultsOnServer() {
        defaultServerMgr.runDefaultTick(null, 500);
        for (GameObjectManager mgr : defaultMgrForDimension.values()) {
            mgr.runDefaultTick(BlockSource.getDefaultForDimension(mgr.getDimensionId()), 500);
        }
    }


    // SAVES


    private final OpaqueSerializedData unresolvedSavedObjectsList = new OpaqueSerializedData();

    @ApiHidden
    public void deserializeAllObjectsAndKeepUnresolved(IScopedReader scope, SerializationContext ctx) {
        if (scope.isArray()) {
            unresolvedSavedObjectsList.clear();
            IArrayWriter unresolvedList = unresolvedSavedObjectsList.beginWrite().asArray();
            deserializeAllObjects(scope.asArray(), ctx, unresolvedList);
            unresolvedList.endScope();
        }
        scope.endScope();
    }

    @ApiHidden
    public void serializeAllObjectsAndAddUnresolved(IScopedWriter scope, SerializationContext ctx) {
        IArrayWriter writer = scope.asArray();;

        if (!unresolvedSavedObjectsList.isEmpty()) {
            IScopedReader unresolvedScope = unresolvedSavedObjectsList.beginRead();
            try {
                IArrayReader unresolvedObjectsList = unresolvedScope.asArray();
                for (int i = 0; i < unresolvedObjectsList.size(); i++) {
                    SerializationUtils.readerToWriter(unresolvedObjectsList.beginScope(i), writer.beginScope());
                }
            } finally {
                unresolvedScope.forceEndScope(ctx);
            }
        }

        for (int i = 0; i < slotsSize; i++) {
            ObjectSlot slot = slots[i];
            if (slot.object == null || slot.posType == POS_TYPE_NONE) continue;
            String typeId = ObjectSerializer.getSerializerTypeIdFromObject(slot.object);
            if (typeId != null)
                serializeOneObject(writer.beginScope(), slot, typeId, ctx);
        }
        scope.endScope();
    }

    public static void loadClass() {}

    static {
        WorldDataScopeRegistry.getInstance().addScope("ic::game_objects", new WorldDataScopeRegistry.SaverScope() {
            @Override
            public void read(IScopedReader io, SerializationContext ctx) {
                IObjectReader defaultScopes = io.asObject();
                if (defaultScopes == null) {
                    io.endScope();
                    return;
                }
                for (String key : defaultScopes.iterKeys()) {
                    if (key.equals("global")) {
                        defaultServerMgr.deserializeAllObjectsAndKeepUnresolved(defaultScopes.beginScope(key), ctx);
                    }
                }
            }

            @Override
            public void write(IScopedWriter io, SerializationContext ctx) {
                IObjectWriter defaultScopes = io.asObject();
                defaultServerMgr.serializeAllObjectsAndAddUnresolved(defaultScopes.beginScope("global"), ctx);
                io.endScope();
            }
        });
    }
}
