package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import com.reider745.world.BlockSourceMethods;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockBreakResult;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.NativeTileEntity;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NativeBlockSource {
    private static final Map<Integer, NativeBlockSource> defaultBlockSourceForDimensions = new HashMap<>();
    public static Level level_current;

    private static String NAME = "cn.nukkit.level.Level";

    public static NativeBlockSource getDefaultForDimension(int dimension) {
        synchronized (defaultBlockSourceForDimensions) {
            try {
                return Java8BackComp.computeIfAbsent(defaultBlockSourceForDimensions, dimension, key -> new NativeBlockSource(dimension));
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return null;
            }
        }
    }

    public static NativeBlockSource getDefaultForActor(long actor) {
        if (!NativeStaticUtils.isExistingEntity(actor)) {
            Logger.debug("not actor return null blocksource");
            return null;
        }
        int dimension = NativeAPI.getEntityDimension(actor);
        return getDefaultForDimension(dimension);
    }

    public static NativeBlockSource getCurrentWorldGenRegion() {
        Level pointer = nativeGetForCurrentThread();
        return pointer != null ? new NativeBlockSource(pointer, false) : null;
    }

    private static final NativeBlockSource clientBlockSource = new NativeBlockSource(null, false, null);
    public static NativeBlockSource getCurrentClientRegion() {
        return null;
    }


    public static NativeBlockSource getFromCallbackPointer(Level ptr) {
        return new NativeBlockSource(ptr, false);
    }

    private static final NativeBlockSource serverCallbackBlockSource = new NativeBlockSource(null, false, null);
    public static NativeBlockSource getFromServerCallbackPointer(Level ptr) {
        serverCallbackBlockSource.setNewPointer(ptr, false);
        return serverCallbackBlockSource;
    }

    public static void resetDefaultBlockSources() {
        ICLog.d("BlockSource", "resetting default block sources");
        synchronized (defaultBlockSourceForDimensions) {
            defaultBlockSourceForDimensions.clear();
        }
    }


    private Level pointer;
    private boolean isFinalizable;

    private boolean allowUpdate = true;
    private boolean destroyParticles = true;
    private int updateType = 3;

    public NativeBlockSource(Level pointer, boolean isFinalizable) {
        if (pointer == null) {
            throw new IllegalArgumentException("cannot pass null pointer to NativeBlockSource constructor");
        }
        this.pointer = pointer;
        this.isFinalizable = isFinalizable;
    }

    private NativeBlockSource(Level pointer, boolean isFinalizable, Object nullablePointerTag) {
        this.pointer = pointer;
        this.isFinalizable = isFinalizable;
    }

    public Level getPointer() {
        return pointer;
    }

    public NativeBlockSource(int dimension, boolean b1, boolean b2) {
        this(constructNew(dimension, b1, b2), true);
    }

    public NativeBlockSource(int dimension) {
        this(dimension, true, false);
    }

    private void setNewPointer(Level pointer, boolean isFinalizable) {
        this.pointer = pointer;
        this.isFinalizable = isFinalizable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NativeBlockSource that = (NativeBlockSource) o;
        return pointer == that.pointer;
    }

    @Override
    public int hashCode() {
        return pointer.hashCode();
    }


    public void setBlockUpdateAllowed(boolean allowed) {
        this.allowUpdate = allowed;
    }

    public boolean getBlockUpdateAllowed() {
        return allowUpdate;
    }

    public void setBlockUpdateType(int type) {
        this.updateType = type;
    }

    public int getBlockUpdateType() {
        return updateType;
    }

    public void setDestroyParticlesEnabled(boolean enabled) {
        destroyParticles = enabled;
    }

    public boolean getDestroyParticlesEnabled() {
        return destroyParticles;
    }
    
    public boolean canSeeSky(int x, int y, int z) {
        return canSeeSky(pointer, x, y, z);
    }
    
    public int getBiome(int x, int z) {
        return getBiome(pointer, x, 64, z);
    }
    
    public float getBiomeTemperatureAt(int x, int y, int z) {
        return getBiomeTemperatureAt(pointer, x, y, z);
    }

    public float getBiomeDownfallAt(int x, int y, int z) {
        return getBiomeDownfallAt(pointer, x, y, z);
    }

    public int getLightLevel(int x, int y, int z) {
        return getBrightness(pointer, x, y, z);
    }

    public int getBlockId(int x, int y, int z) {
        return getBlockId(pointer, x, y, z);
    }

    public int getBlockID(int x, int y, int z) {
        return getBlockId(pointer, x, y, z);
    }

    public int getBlockData(int x, int y, int z) {
        return getBlockData(pointer, x, y, z);
    }

    public BlockState getBlock(int x, int y, int z) {
        return new BlockState(getBlockIdDataAndState(pointer, x, y, z));
    }

    public BlockState getExtraBlock(int x, int y, int z) {
        return new BlockState(getExtraBlockIdDataAndState(pointer, x, y, z));
    }

    public void setBlock(int x, int y, int z, int id, int data) {
        setBlock(pointer, x, y, z, id, data, allowUpdate, updateType);
    }

    public void setBlock(int x, int y, int z, int id) {
        setBlock(x, y, z, id, 0);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        if (state.runtimeId > 0) {
            setBlockByRuntimeId(pointer, x, y, z, state.runtimeId, allowUpdate, updateType);
        } else {
            setBlock(pointer, x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }

    public void setExtraBlock(int x, int y, int z, int id, int data) {
        setExtraBlock(pointer, x, y, z, id, data, allowUpdate, updateType);
    }

    public void setExtraBlock(int x, int y, int z, int id) {
        setExtraBlock(x, y, z, id, 0);
    }

    public void setExtraBlock(int x, int y, int z, BlockState state) {
        if (state.runtimeId > 0) {
            setExtraBlockByRuntimeId(pointer, x, y, z, state.runtimeId, allowUpdate, updateType);
        } else {
            setExtraBlock(pointer, x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }
    
    public int getGrassColor(int x, int z) {
        return getGrassColor(pointer, x, 64, z);
    }
    
    public NativeTileEntity getBlockEntity(int x, int y, int z) {
        long ptr = getBlockEntity(pointer, x, y, z);
        return ptr != 0 ? new NativeTileEntity(ptr) : null;
    }
    
    public int getDimension() {
        return getDimension(pointer);
    }
    
    public void setBiome(int chunkX, int chunkZ, int id) {
        setBiome(pointer, chunkX, chunkZ, id);
    }
    
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return isChunkLoaded(pointer, chunkX, chunkZ);
    }

    public boolean isChunkLoadedAt(int x, int z) {
        return isChunkLoaded(pointer, (int) Math.floor((double) x / 16), (int) Math.floor((double) z / 16));
    }
    
    public int getChunkState(int chunkX, int chunkZ) {
        return getChunkState(pointer, chunkX, chunkZ);
    }

    public int getChunkStateAt(int x, int z) {
        return getChunkState(pointer, (int) Math.floor((double) x / 16), (int) Math.floor((double) z / 16));
    }

    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay, int unknown) {
        addToTickingQueue(pointer, x, y, z, block.getRuntimeId(), delay, unknown);
    }

    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay) {
        addToTickingQueue(x, y, z, block, delay, 0);
    }

    public void addToTickingQueue(int x, int y, int z, int delay, int unknown) {
        addToTickingQueue(pointer, x, y, z, -1, delay, unknown);
    }

    public void addToTickingQueue(int x, int y, int z, int delay) {
        addToTickingQueue(pointer, x, y, z, -1, delay, 0);
    }

    public void destroyBlock(int x, int y, int z, boolean drop) {
        //setBlock(x, y, z, 0);
        destroyBlock(pointer, x, y, z, drop, updateType, destroyParticles);
    }

    public void destroyBlock(int x, int y, int z) {
        destroyBlock(x, y, z, false);
    }

    public void breakBlock(int x, int y, int z, boolean isDropAllowed, long actor, ItemStack item) {
        Coords coords = new Coords(x, y, z);
        BlockState block = getBlock(x, y, z);
        Callback.invokeAPICallback("BreakBlock", this, coords, block, isDropAllowed, actor, item.asScriptable());
        if (!NativeAPI.isDefaultPrevented() && StaticEntity.exists(actor) && StaticEntity.getDimension(actor) == getDimension()) {
            Callback.invokeAPICallback("DestroyBlock", coords, block, actor);
        }
        destroyBlock(x, y, z, isDropAllowed);
    }

    public void breakBlock(int x, int y, int z, boolean isDropAllowed, long actor) {
        breakBlock(x, y, z, isDropAllowed, actor, StaticEntity.getCarriedItem(actor));
    }

    public void breakBlock(int x, int y, int z, boolean isDropAllowed, ItemStack item) {
        breakBlock(x, y, z, isDropAllowed, -1, item);
    }

    public void breakBlock(int x, int y, int z, boolean isDropAllowed) {
        breakBlock(x, y, z, isDropAllowed, -1, new ItemStack());
    }

    private static final Object breakBlockSyncLock = new Object();

    public BlockBreakResult breakBlockForResult(int x, int y, int z, long actor, ItemStack item) {
        synchronized (breakBlockSyncLock) {
            NativeCallback.startOverrideBlockBreakResult();
            breakBlock(x, y, z, true, actor, item);
            return NativeCallback.endOverrideBlockBreakResult();
        }
    }

    public BlockBreakResult breakBlockForResult(int x, int y, int z, long actor) {
        return breakBlockForResult(x, y, z, actor, StaticEntity.getCarriedItem(actor));
    }

    public BlockBreakResult breakBlockForResult(int x, int y, int z, ItemStack item) {
        return breakBlockForResult(x, y, z, -1, item);
    }

    public BlockBreakResult breakBlockForResult(int x, int y, int z) {
        return breakBlockForResult(x, y, z, -1, new ItemStack());
    }

    public ScriptableObject breakBlockForJsResult(int x, int y, int z, long actor, ScriptableObject item) {
        return breakBlockForResult(x, y, z, actor, new ItemStack(item)).asScriptable();
    }

    public ScriptableObject breakBlockForJsResult(int x, int y, int z, long actor) {
        return breakBlockForResult(x, y, z, actor).asScriptable();
    }

    public ScriptableObject breakBlockForJsResult(int x, int y, int z, ScriptableObject item) {
        return breakBlockForResult(x, y, z, new ItemStack(item)).asScriptable();
    }

    public ScriptableObject breakBlockForJsResult(int x, int y, int z) {
        return breakBlockForResult(x, y, z).asScriptable();
    }

    public void explode(float x, float y, float z, float power, boolean fire) {
        explode(pointer, x, y, z, power, fire);
    }

    public long clip(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] output) {
        return clip(pointer, x1, y1, z1, x2, y2, z2, mode, output);
    }
    
    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag) {
        return fetchEntitiesInAABB(pointer, x1, y1, z1, x2, y2, z2, backCompEntityType, flag);
    }

    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false);
    }

    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false);
    }

    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String type) {
        return fetchEntitiesOfTypeInAABB(pointer, x1, y1, z1, x2, y2, z2, namespace, type);
    }

    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String fullType) {
        String namespace = "minecraft";
        int sep = fullType.indexOf(':');
        if (sep != -1) {
            namespace = fullType.substring(0, sep);
            fullType = fullType.substring(sep + 1);
        }
        return fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, namespace, fullType);
    }

    private static NativeArray entityListToScriptArray(long[] ents) {
        Object[] result = new Object[ents.length];
        int i = 0;
        for (long ent : ents) {
            result[i++] = ent;
        }
        return ScriptableObjectHelper.createArray(result);
    }

    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag) {
        return entityListToScriptArray(fetchEntitiesInAABB(pointer, x1, y1, z1, x2, y2, z2, backCompEntityType, flag));
    }
    
    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType) {
        return entityListToScriptArray(fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false));
    }

    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return entityListToScriptArray(fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false));
    }

    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String type) {
        return entityListToScriptArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, namespace, type));
    }

    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String fullType) {
        return entityListToScriptArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, fullType));
    }

    public long spawnEntity(float x, float y, float z, int type) {
        return spawnEntity(pointer, type, x, y, z);
    }

    public long spawnEntity(float x, float y, float z, String str1, String str2, String str3) {
        return spawnNamespacedEntity(pointer, x, y, z, str1, str2, str3);
    }

    public long spawnEntity(float x, float y, float z, String type) {
        if (type == null || type.length() == 0) {
            return -1;
        }
        String[] parts = type.split(":");
        if (parts.length >= 3) {
            return spawnEntity(x, y, z, parts[0], parts[1], parts[2]);
        } else if (parts.length == 2) {
            return spawnEntity(x, y, z, parts[0], parts[1], "");
        } else if (parts.length == 1) {
            return spawnEntity(x, y, z, "minecraft", parts[0], "");
        }
        return -1;
    }

    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data, NativeItemInstanceExtra extra) {
        return spawnDroppedItem(pointer, x, y, z, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
    }

    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data) {
        return spawnDroppedItem(x, y, z, id, count, data, null);
    }

    public void spawnExpOrbs(float x, float y, float z, int amount) {
        spawnExpOrbs(pointer, x, y, z, amount);
    }


    private static Level constructNew(int dimension, boolean b1, boolean b2){
        return BlockSourceMethods.getLevelForDimension(dimension);
    }
    private static Level nativeGetForCurrentThread(){
        return level_current;
    }
    private static Long nativeGetForClientSide(){
        return null;
    }
    private static native void nativeFinalize(long pointer);

    private static native boolean canSeeSky(Level pointer, int x, int y, int z);
    private static native int getBiome(Level pointer, int x, int y, int z);
    private static native float getBiomeTemperatureAt(Level pointer, int x, int y, int z);
    private static native float getBiomeDownfallAt(Level pointer, int x, int y, int z);
    private static native int getBrightness(Level Level, int x, int y, int z);
    private static int getBlockId(Level pointer, int x, int y, int z){
        return pointer.getBlockIdAt(x, y, z);
    }
    private static int getBlockData(Level pointer, int x, int y, int z){
        return pointer.getBlockDataAt(x, y, z);
    }
    private static Block getBlockIdDataAndState(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z);
    }
    private static Block getExtraBlockIdDataAndState(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z);
    }
    private static void setBlock(Level pointer, int x, int y, int z, int id, int data, boolean allowUpdate, int updateType){
        pointer.setBlock(x, y, z, Block.get(id, data).clone(), false, allowUpdate);
    }
    private static native void setBlockByRuntimeId(Level pointer, int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType);
    private static native void setExtraBlock(Level pointer, int x, int y, int z, int id, int data, boolean allowUpdate, int updateType);
    private static native void setExtraBlockByRuntimeId(Level pointer, int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType);
    private static native int getGrassColor(Level pointer, int x, int y, int z);
    private static native long getBlockEntity(Level pointer, int x, int y, int z);
    private static int getDimension(Level pointer){
        return pointer.getDimension();
    }
    private static void setBiome(Level pointer, int chunkX, int chunkZ, int id){
        pointer.setBiomeId(chunkX, chunkZ, (byte) id);
    }
    private static native boolean isChunkLoaded(Level pointer, int chunkX, int chunkZ);
    private static native int getChunkState(Level pointer, int chunkX, int chunkZ);
    private static native void addToTickingQueue(Level pointer, int x, int y, int z, int runtimeId /* = -1 */, int delay, int unknown /* = 0 */);

    private static native void explode(Level pointer, float x, float y, float z, float power, boolean fire);
    private static native void destroyBlock(Level pointer, int x, int y, int z, boolean drop, int updateType, boolean destroyParticles);
    private static native long clip(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] joutput);

    private static native long[] fetchEntitiesInAABB(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag);
    private static native long[] fetchEntitiesOfTypeInAABB(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String name);
    private static native long spawnEntity(Level pointer, int type, float x, float y, float z);
    private static native long spawnNamespacedEntity(Level pointer, float x, float y, float z, String str1, String str2, String str3);
    private static native long spawnExpOrbs(Level pointer, float x, float y, float z, int amount);
    private static native long spawnDroppedItem(Level pointer, float x, float y, float z, int id, int count, int data, long extra);
}
