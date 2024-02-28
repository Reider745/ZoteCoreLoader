package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import com.reider745.InnerCoreServer;
import com.reider745.api.CallbackHelper;
import com.reider745.api.ZoteOnly;
import com.reider745.entity.EntityMethod;
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
import java.util.Map;

public class NativeBlockSource {
    private static final Map<Integer, NativeBlockSource> defaultBlockSourceForDimensions = new HashMap<>();

    public static NativeBlockSource getDefaultForDimension(int dimension) {
        synchronized (defaultBlockSourceForDimensions) {
            try {
                return Java8BackComp.computeIfAbsent(defaultBlockSourceForDimensions, dimension,
                        key -> new NativeBlockSource(dimension));
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return null;
            }
        }
    }

    @ZoteOnly
    public static NativeBlockSource getDefault() {
        Level level = BlockSourceMethods.getDefaultLevel();
        return level != null ? getFromCallbackPointer(level) : null;
    }

    public static NativeBlockSource getDefaultForActor(long actor) {
        Entity entity = EntityMethod.getEntityById(actor);
        return entity != null ? getFromCallbackPointer(entity.getLevel()) : null;
    }

    @ZoteOnly
    public static NativeBlockSource getDefaultForLevel(String name) {
        Level level = BlockSourceMethods.getLevelForName(name);
        return level != null ? getFromCallbackPointer(level) : null;
    }

    public static NativeBlockSource getCurrentWorldGenRegion() {
        Level level = CallbackHelper.getForCurrentThread();
        return level != null ? new NativeBlockSource(level, false) : null;
    }

    @Deprecated(since = "Zote")
    public static NativeBlockSource getCurrentClientRegion() {
        InnerCoreServer.useIncomprehensibleMethod("NativeBlockSource.getCurrentClientRegion()");
        return null;
    }

    public static NativeBlockSource getFromCallbackPointer(Level level) {
        return new NativeBlockSource(level, false);
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

    private Level level;

    private boolean allowUpdate = true;
    private boolean destroyParticles = true;
    private int updateType = 3;

    public NativeBlockSource(Level level, boolean isFinalizable) {
        if (level == null) {
            throw new IllegalArgumentException("cannot pass null pointer to NativeBlockSource constructor");
        }
        this.level = level;
    }

    private NativeBlockSource(Level level, boolean isFinalizable, Object nullablePointerTag) {
        this.level = level;
    }

    public Level getPointer() {
        return level;
    }

    @ZoteOnly
    public String getLevelName() {
        return level.getName();
    }

    public NativeBlockSource(int dimension, boolean b1, boolean b2) {
        this(BlockSourceMethods.getLevelForDimension(dimension), true);
    }

    public NativeBlockSource(int dimension) {
        this(dimension, true, false);
    }

    private void setNewPointer(Level level, boolean isFinalizable) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NativeBlockSource that = (NativeBlockSource) o;
        return level == that.level;
    }

    @Override
    public int hashCode() {
        return level.hashCode();
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
        return BlockSourceMethods.canSeeSky(level, x, y, z);
    }

    public int getBiome(int x, int z) {
        return BlockSourceMethods.getBiome(level, x, 64, z);
    }

    public float getBiomeTemperatureAt(int x, int y, int z) {
        return BlockSourceMethods.getBiomeTemperatureAt(level, x, y, z);
    }

    public float getBiomeDownfallAt(int x, int y, int z) {
        return BlockSourceMethods.getBiomeDownfallAt(level, x, y, z);
    }

    public int getLightLevel(int x, int y, int z) {
        return BlockSourceMethods.getBrightness(level, x, y, z);
    }

    public int getBlockId(int x, int y, int z) {
        return BlockSourceMethods.getBlockId(level, x, y, z);
    }

    public int getBlockID(int x, int y, int z) {
        return BlockSourceMethods.getBlockId(level, x, y, z);
    }

    public int getBlockData(int x, int y, int z) {
        return BlockSourceMethods.getBlockData(level, x, y, z);
    }

    public BlockState getBlock(int x, int y, int z) {
        return new BlockState(BlockSourceMethods.getBlockIdDataAndState(level, x, y, z));
    }

    public BlockState getExtraBlock(int x, int y, int z) {
        return new BlockState(BlockSourceMethods.getExtraBlockIdDataAndState(level, x, y, z));
    }

    public void setBlock(int x, int y, int z, int id, int data) {
        BlockSourceMethods.setBlock(level, x, y, z, id, data, allowUpdate, updateType);
    }

    public void setBlock(int x, int y, int z, int id) {
        setBlock(x, y, z, id, 0);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        if (state.runtimeId > 0) {
            BlockSourceMethods.setBlockByRuntimeId(level, x, y, z, state.runtimeId, allowUpdate, updateType);
        } else {
            BlockSourceMethods.setBlock(level, x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }

    public void setExtraBlock(int x, int y, int z, int id, int data) {
        BlockSourceMethods.setExtraBlock(level, x, y, z, id, data, allowUpdate, updateType);
    }

    public void setExtraBlock(int x, int y, int z, int id) {
        setExtraBlock(x, y, z, id, 0);
    }

    public void setExtraBlock(int x, int y, int z, BlockState state) {
        if (state.runtimeId > 0) {
            BlockSourceMethods.setExtraBlockByRuntimeId(level, x, y, z, state.runtimeId, allowUpdate, updateType);
        } else {
            BlockSourceMethods.setExtraBlock(level, x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }

    @Deprecated(since = "Zote")
    public int getGrassColor(int x, int z) {
        return BlockSourceMethods.getGrassColor(level, x, 64, z);
    }

    public NativeTileEntity getBlockEntity(int x, int y, int z) {
        BlockEntity ptr = BlockSourceMethods.getBlockEntity(level, x, y, z);
        return ptr != null ? new NativeTileEntity(ptr) : null;
    }

    public int getDimension() {
        return BlockSourceMethods.getDimension(level);
    }

    @ZoteOnly
    public void setRespawnCoords(int x, int y, int z) {
        BlockSourceMethods.setRespawnCoords(level, x, y, z);
    }

    @ZoteOnly
    public void playSound(float x, float y, float z, String name, float volume, float pitch, long[] players) {
        BlockSourceMethods.playSound(level, name, (int) x, (int) y, (int) z, volume, pitch, players != null ? EntityMethod.getPlayersByIds(players) : null);
    }

    private static long[] toLongArray(NativeArray array) {
        LongArrayList result = new LongArrayList();
        for (Object obj : array.toArray()) {
            try {
                if (obj != null)
                    result.add((long) obj);
            } catch (ClassCastException e) {
            }
        }
        return result.toArray(new long[0]);
    }

    @ZoteOnly
    public void playSound(float x, float y, float z, String name, float volume, float pitch, NativeArray players) {
        playSound(x, y, z, name, volume, pitch, players != null ? toLongArray(players) : null);
    }

    @ZoteOnly
    public void playSound(float x, float y, float z, String name, float volume, float pitch) {
        playSound(x, y, z, name, volume, pitch, (long[]) null);
    }

    public void setBiome(int chunkX, int chunkZ, int id) {
        BlockSourceMethods.setBiome(level, chunkX, chunkZ, id);
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return BlockSourceMethods.isChunkLoaded(level, chunkX, chunkZ);
    }

    public boolean isChunkLoadedAt(int x, int z) {
        return BlockSourceMethods.isChunkLoaded(level, (int) Math.floor((double) x / 16),
                (int) Math.floor((double) z / 16));
    }

    public int getChunkState(int chunkX, int chunkZ) {
        return BlockSourceMethods.getChunkState(level, chunkX, chunkZ);
    }

    public int getChunkStateAt(int x, int z) {
        return BlockSourceMethods.getChunkState(level, (int) Math.floor((double) x / 16),
                (int) Math.floor((double) z / 16));
    }

    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay, int unknown) {
        BlockSourceMethods.addToTickingQueue(level, x, y, z, block.getRuntimeId(), delay, unknown);
    }

    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay) {
        addToTickingQueue(x, y, z, block, delay, 0);
    }

    public void addToTickingQueue(int x, int y, int z, int delay, int unknown) {
        BlockSourceMethods.addToTickingQueue(level, x, y, z, -1, delay, unknown);
    }

    public void addToTickingQueue(int x, int y, int z, int delay) {
        BlockSourceMethods.addToTickingQueue(level, x, y, z, -1, delay, 0);
    }

    public void destroyBlock(int x, int y, int z, boolean drop) {
        BlockSourceMethods.destroyBlock(level, x, y, z, drop, updateType, destroyParticles);
    }

    public void destroyBlock(int x, int y, int z) {
        destroyBlock(x, y, z, false);
    }

    public void breakBlock(int x, int y, int z, boolean isDropAllowed, long actor, ItemStack item) {
        Coords coords = new Coords(x, y, z);
        BlockState block = getBlock(x, y, z);
        Callback.invokeAPICallback("BreakBlock", this, coords, block, isDropAllowed, actor, item.asScriptable());
        if (!NativeAPI.isDefaultPrevented() && StaticEntity.exists(actor)
                && StaticEntity.getDimension(actor) == getDimension()) {
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
        BlockSourceMethods.explode(level, x, y, z, power, fire);
    }

    public long clip(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] output) {
        return BlockSourceMethods.clip(level, x1, y1, z1, x2, y2, z2, mode, output);
    }

    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType, boolean flag) {
        return BlockSourceMethods.fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2, backCompEntityType, flag);
    }

    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false);
    }

    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false);
    }

    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            String namespace, String type) {
        return BlockSourceMethods.fetchEntitiesOfTypeInAABB(level, x1, y1, z1, x2, y2, z2, namespace, type);
    }

    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            String fullType) {
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

    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType, boolean flag) {
        return entityListToScriptArray(fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, flag));
    }

    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType) {
        return listEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false);
    }

    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return listEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false);
    }

    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            String namespace, String type) {
        return entityListToScriptArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, namespace, type));
    }

    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2,
            String fullType) {
        return entityListToScriptArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, fullType));
    }

    public long spawnEntity(float x, float y, float z, int type) {
        return BlockSourceMethods.spawnEntity(level, type, x, y, z);
    }

    public long spawnEntity(float x, float y, float z, String str1, String str2, String str3) {
        return BlockSourceMethods.spawnNamespacedEntity(level, x, y, z, str1, str2, str3);
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

    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data,
            NativeItemInstanceExtra extra) {
        return BlockSourceMethods.spawnDroppedItem(level, x, y, z, id, count, data, extra);
    }

    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data) {
        return spawnDroppedItem(x, y, z, id, count, data, null);
    }

    public void spawnExpOrbs(float x, float y, float z, int amount) {
        BlockSourceMethods.spawnExpOrbs(level, x, y, z, amount);
    }
}
