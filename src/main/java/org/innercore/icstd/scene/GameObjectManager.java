package org.innercore.icstd.scene;

import org.jetbrains.annotations.Nullable;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GameObjectManagerBindGenBase;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.ChunkAABB;
import org.innercore.icstd.common.ChunkPos;
import org.innercore.icstd.component.*;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.serialization.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@ApiBinding
public class GameObjectManager extends GameObjectManagerBindGenBase {
    private static final int STATE_NOT_LOADED = 0;
    private static final int STATE_LAZY_LOADED = 1;
    private static final int STATE_LOADED = 2;

    private final String uniqueName;
    private int dimensionId = -1;
    private boolean isServerSide = true;
    private boolean isDefaultScene = false;
    private boolean isScopeLoaded = false;

    public GameObjectManager(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public GameObjectManager setDimensionId(int dimensionId) {
        this.dimensionId = dimensionId;
        return this;
    }

    public GameObjectManager setServerSide(boolean isServerSide) {
        this.isServerSide = isServerSide;
        return this;
    }

    GameObjectManager setDefaultScene(boolean isDefaultScene) {
        this.isDefaultScene = isDefaultScene;
        return this;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public boolean isServerSide() {
        return isServerSide;
    }

    public boolean isDefaultScene() {
        return isDefaultScene;
    }

    // ADD/UPDATE


    private static final AtomicLong nextUniqueHandleValue = new AtomicLong(1);

    @ApiBinding
    public GameObjectHandle create(Object object) {
        GameObjectHandle handle = new GameObjectHandle(this, nextUniqueHandleValue.getAndAdd(1));
        int slotIdx = allocateSlot();
        handleToSlotIdx.put(handle.rawHandle, slotIdx);
        slots[slotIdx].handle = handle.rawHandle;
        setSlotObject(slotIdx, object);
        markObjectForUpdate(slotIdx);
        return handle;
    }

    void placeAsGlobal(long rawHandle) {
        updateObjectPosImpl(rawHandle, slot -> {
            slot.posType = POS_TYPE_GLOBAL;
        });
    }

    void placeAtBlockPos(long rawHandle, BlockPos pos) {
        updateObjectPosImpl(rawHandle, slot -> {
            slot.posType = POS_TYPE_BLOCK;
            slot.x = pos.x;
            slot.y = pos.y;
            slot.z = pos.z;
        });
    }

    private void updateObjectPosImpl(long rawHandle, Consumer<ObjectSlot> slotConsumer) {
        Integer slotIdxOpt = handleToSlotIdx.get(rawHandle);
        if (slotIdxOpt == null) {
            return;
        }
        int slotIdx = slotIdxOpt;
        ObjectSlot slot = slots[slotIdx];
        BlockPos prevBlockPos = slot.getBlockPos();
        ChunkAABB prevAABB = slot.getChunkAABB();
        slotConsumer.accept(slot);
        updateObjectAABB(slotIdx, prevAABB, slot.getChunkAABB());
        updateObjectBlockPos(slotIdx, prevBlockPos, slot.getBlockPos());
        markObjectForUpdate(slotIdx);
    }

    Object discard(long rawHandle) {
        Integer slotIdxOpt = handleToSlotIdx.remove(rawHandle);
        if (slotIdxOpt == null) {
            return null;
        }
        return discardSlotObject(slotIdxOpt);
    }

    boolean destroyNow(long rawHandle) {
        Integer slotIdxOpt = handleToSlotIdx.remove(rawHandle);
        if (slotIdxOpt == null) {
            return false;
        }
        destroySlotImpl(slotIdxOpt);
        return true;
    }

    void destroyDeferred(long rawHandle) {
        Integer slotIdxOpt = handleToSlotIdx.remove(rawHandle);
        if (slotIdxOpt == null) {
            return;
        }
        int slotIdx = slotIdxOpt;
        slots[slotIdx].markedForDestroy = true;
        markObjectForUpdate(slotIdx);
    }

    private void destroySlotImpl(int slotIdx) {
        ObjectSlot slot = slots[slotIdx];
        setSlotObject(slotIdx, null);
        updateObjectBlockPos(slotIdx, slot.getBlockPos(), null);
        updateObjectAABB(slotIdx, slot.getChunkAABB(), null);
        freeSlot(slotIdx);
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
    public void clearAllObjects(boolean callDestroyForObjects) {
        for (int slotIdx = 0; slotIdx < slotsSize; slotIdx++) {
            if (callDestroyForObjects) {
                setSlotObject(slotIdx, null);
            } else {
                discardSlotObject(slotIdx);
            }
        }
        slots = new ObjectSlot[0];
        slotsSize = 0;
        slotsFreeListHead = -1;

        periodicChecksList = new int[0];
        periodicChecksListSize = 0;
        periodicChecksListNextIdx = 0;

        handleToSlotIdx.clear();
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

    boolean isAlive(long handle) {
        return handleToSlotIdx.containsKey(handle);
    }

    boolean isLoaded(long handle) {
        Integer slotIdxOpt = handleToSlotIdx.get(handle);
        return slotIdxOpt != null && slots[slotIdxOpt].isObjectLoaded;
    }

    Object getObject(long handle) {
        Integer slotIdxOpt = handleToSlotIdx.get(handle);
        return slotIdxOpt == null ? null : slots[slotIdxOpt].object;
    }

    BlockPos getBlockPos(long handle) {
        Integer slotIdxOpt = handleToSlotIdx.get(handle);
        return slotIdxOpt == null ? null : slots[slotIdxOpt].getBlockPos();
    }

    boolean setObject(long handle, Object object) {
        Integer slotIdxOpt = handleToSlotIdx.get(handle);
        if (slotIdxOpt == null) {
            return false;
        }
        setSlotObject(slotIdxOpt, object);
        markObjectForUpdate(slotIdxOpt);
        return true;
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
            return new GameObjectHandle(GameObjectManager.this, slots[i].handle);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int i = firstSlot();
                @Override public boolean hasNext() { i = searchSlot(i); return i != -1; }
                @Override public T next() {
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
                int i = firstSlot();
                @Override public boolean hasNext() { i = searchSlot(i); return i != -1; }
                @Override public GameObjectHandle next() {
                    if (i == -1) return null;
                    ObjectSlot slot = slots[i];
                    i = getNextSlot(i);
                    return new GameObjectHandle(GameObjectManager.this, slot.handle);
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
                list.add(new GameObjectHandle(GameObjectManager.this, slots[i].handle));
                i = searchSlot(getNextSlot(i));
            }
            return list;
        }

        protected abstract int firstSlot();
        protected abstract int searchSlot(int idx);
        protected abstract int getNextSlot(int idx);
    }

    public GameObjectQuery<Object> queryAtBlock(BlockPos pos) {
        return new BaseQueryImpl<Object>() {
            @Override protected int firstSlot() {
                Integer idx = lookupByPos.get(pos);
                return idx != null ? idx : -1;
            }
            @Override protected int searchSlot(int idx) { return idx; }
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
                for (; i < slotsSize && (slots[i] == null || slots[i].posType != POS_TYPE_GLOBAL || cls.isInstance(slots[i].object)); i++);
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

    private void freeSlot(int idx) {
        ObjectSlot slot = slots[idx];
        setSlotObject(idx, null);
        slot.nextAtPos = slotsFreeListHead;
        slot.prevAtPos = -1;
        slot.posType = POS_TYPE_NONE;
        slot.handle = 0;
        slot.markedForDestroy = slot.markedForUpdate = false;
        slotsFreeListHead = idx;
    }

    private static final int POS_TYPE_NONE = 0;   // not valid
    private static final int POS_TYPE_GLOBAL = 1; // always loaded if scene loaded
    private static final int POS_TYPE_BLOCK = 2;  // exists at block pos

    private static class ObjectSlot {
        long handle = 0;
        int x, y, z;
        int posType = POS_TYPE_NONE;
        Object object;
        boolean markedForUpdate = false;
        boolean markedForDestroy = false;
        boolean isObjectInitialized = false;
        boolean isObjectLoaded = false;

        int nextAtPos = -1;
        int prevAtPos = -1;

        int periodicChecksListIdx = -1;

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

        boolean isSerializationNeeded(SerializationContext ctx) {
            if (object == null)
                return false;
            return ObjectSerializer.getSerializerTypeIdFromObject(object) != null;
        }
    }

    private ObjectSlot[] slots = new ObjectSlot[0];
    int slotsSize = 0;
    int slotsFreeListHead = -1;

    private void setSlotLoaded(int slotIdx, boolean isLoaded) {
        ObjectSlot slot = slots[slotIdx];
        if (slot.isObjectLoaded != isLoaded) {
            // ICLog.d("GameObjectManager", "setSlotLoaded idx=" + slotIdx + " o=" + slots[slotIdx].object + " loaded=" + isLoaded + " callInit=" + (isLoaded && !slot.isObjectInitialized) + " pos=" + slots[slotIdx].getBlockPos());
            Object o = slot.object;
            if (isLoaded && !slot.isObjectInitialized) {
                // make sure object is initialized before being loaded
                ComponentUtils.safeCallInitFor(o);
                slot.isObjectInitialized = true;
            }
            slot.isObjectLoaded = isLoaded;
            ComponentUtils.safeCallLoadOrUnloadFor(o, isLoaded);
            setPeriodicChecksEnables(slotIdx, slot, isLoaded && o instanceof PeriodicObjectCheckComponent);
        }
    }

    private void setSlotObject(int slotIdx, Object object) {
        ObjectSlot slot = slots[slotIdx];
        if (slot.object != object) {
            setSlotLoaded(slotIdx, false);
            if (slot.isObjectInitialized) {
                ComponentUtils.safeCallDestroyFor(slot.object);
            }
            ComponentUtils.safeCallFinalizeObjectFor(slot.object);
            slot.object = object;
            slot.isObjectInitialized = false;
        }
    }

    private Object discardSlotObject(int slotIdx) {
        ObjectSlot slot = slots[slotIdx];
        Object o = null;
        if (slot.object != null) {
            setSlotLoaded(slotIdx, false);
            if (slot.isObjectInitialized) {
                ComponentUtils.safeCallDiscardFor(slot.object);
            }
            ComponentUtils.safeCallFinalizeObjectFor(slot.object);
            o = slot.object;
            slot.object = null;
            slot.isObjectInitialized = false;
        }
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
    private HashMap<Long, Integer> handleToSlotIdx = new HashMap<>();
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
        }
        if (newPos != null) {
            Integer firstSlotIdx = lookupByPos.get(newPos);
            if (firstSlotIdx != null) {
                ObjectSlot first = slots[slotIdx];
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
        writer.addLong(slot.handle);
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
            // no saver registered for this type, keep serialized object to avoid loosing data
            SerializationUtils.readerToWriter(scope, unresolvedList.beginScope());
            scope.endScope();
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
                handle = create(object).placeAsGlobal();
                break;
            case POS_TYPE_BLOCK:
                if (reader.size() < 7) {
                    ctx.addError("failed to deserialize object block pos");
                    return null;
                }
                int x = reader.getInt(4, Integer.MAX_VALUE);
                int y = reader.getInt(5, Integer.MAX_VALUE);
                int z = reader.getInt(6, Integer.MAX_VALUE);
                if (x == Integer.MAX_VALUE || y == Integer.MAX_VALUE || z == Integer.MAX_VALUE) {
                    ctx.addError("failed to deserialize object block pos");
                    return null;
                }
                handle = create(object).placeAtBlockPos(new BlockPos(x, y, z));
                break;
        }
        if (handle != null) {
            ComponentUtils.safeCallCreateObjectFor(object, handle);
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
            return slot.isObjectLoaded;
        } else if (slot.posType == POS_TYPE_GLOBAL) {
            setSlotLoaded(slotIdx, isScopeLoaded);
        } else if (slot.posType == POS_TYPE_NONE) {
            setSlotLoaded(slotIdx, false);
        }
        return slot.isObjectLoaded;
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
                destroySlotImpl(slotIdx);
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
        periodicChecksListNextIdx %= periodicChecksListSize;
        for (int i = 0; i < numChecks; i++) {
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
        }
    }


    // GLOBAL


    // - uses server-side block source, not loaded and not saved on client
    // - objects are saved
    @ApiBinding
    public static GameObjectManager getForDimension(int dimensionId) {
        GameObjectManager scene = defaultMgrForDimension.get(dimensionId);
        if (scene == null) {
            defaultMgrForDimension.put(dimensionId, scene = new GameObjectManager("default-" + dimensionId).setDimensionId(dimensionId));
        }
        return scene;
    }
    private static final HashMap<Integer, GameObjectManager> defaultMgrForDimension = new HashMap<>();
    private static final GameObjectManager defaultServerMgr = new GameObjectManager("default-server-global");

    @ApiBinding
    public static GameObjectManager getGlobalForServer() { return defaultServerMgr; }

    // - on client uses client-side block source
    // - cleared when going between dimensions
    // - not loaded on server
    @ApiBinding
    public static GameObjectManager getClientSide() { return clientSideScene; }
    private static final GameObjectManager clientSideScene = new GameObjectManager("default-client");

    public static void clearAllDefaultServerScenes(boolean callDestroyForObjects) {
        for (GameObjectManager mgr : defaultMgrForDimension.values()) {
            mgr.clearAllObjects(callDestroyForObjects);
        }
        defaultMgrForDimension.clear();
        defaultServerMgr.clearAllObjects(callDestroyForObjects);
    }

    public static void clearAllDefaultClientScenes(boolean callDestroyForObjects) {
        clientSideScene.clearAllObjects(callDestroyForObjects);
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

    void deserializeAllObjectsAndKeepUnresolved(IScopedReader scope, SerializationContext ctx) {
        if (scope.isArray()) {
            unresolvedSavedObjectsList.clear();
            IArrayWriter unresolvedList = unresolvedSavedObjectsList.beginWrite().asArray();
            deserializeAllObjects(scope.asArray(), ctx, unresolvedList);
            unresolvedList.endScope();
        }
        scope.endScope();
    }

    void serializeAllObjectsAndAddUnresolved(IScopedWriter scope, SerializationContext ctx) {
        IArrayWriter writer = scope.asArray();

        if (!unresolvedSavedObjectsList.isEmpty()) {
            IArrayReader unresolvedObjectsList = unresolvedSavedObjectsList.beginRead().asArray();
            for (int i = 0; i < unresolvedObjectsList.size(); i++) {
                SerializationUtils.readerToWriter(unresolvedObjectsList.beginScope(i), writer.beginScope());
            }
            unresolvedObjectsList.endScope();
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
                    } else if (key.startsWith("dim-")) {
                        int dimensionId;
                        try {
                            dimensionId = Integer.parseInt(key.substring(4));
                        } catch (NumberFormatException e) {
                            ctx.addError("invalid key in default game object scopes " + key);
                            continue;
                        }
                        getForDimension(dimensionId).deserializeAllObjectsAndKeepUnresolved(defaultScopes.beginScope(key), ctx);
                    }
                }
            }

            @Override
            public void write(IScopedWriter io, SerializationContext ctx) {
                IObjectWriter defaultScopes = io.asObject();
                defaultServerMgr.serializeAllObjectsAndAddUnresolved(defaultScopes.beginScope("global"), ctx);
                for (GameObjectManager mgr : defaultMgrForDimension.values()) {
                    mgr.serializeAllObjectsAndAddUnresolved(defaultScopes.beginScope("dim-" + mgr.dimensionId), ctx);
                }
                io.endScope();
            }
        });
    }
}
