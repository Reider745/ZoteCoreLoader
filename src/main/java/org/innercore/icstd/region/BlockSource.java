package org.innercore.icstd.region;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.StopSoundPacket;
import com.reider745.InnerCoreServer;
import com.reider745.api.CallbackHelper;
import com.reider745.api.ClientOnly;
import com.reider745.api.ReflectHelper;
import com.reider745.api.ZoteOnly;
import com.reider745.block.BlockMethods;
import com.reider745.block.BlockStateStorage;
import com.reider745.entity.EntityMethod;
import com.reider745.hooks.item.ItemUtils;
import com.reider745.world.FakeDimensions;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockBreakResult;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.mcpe.NativeStaticUtils;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeCallback;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.blockentity.BlockEntityRegistry;
import org.innercore.icstd.blockentity.script.ScriptBlockEntity;
import org.innercore.icstd.common.ChunkPos;
import org.innercore.icstd.component.ComponentUtils;
import org.innercore.icstd.component.DestroyBlockListenerComponent;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.NativeTileEntity;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.BlockSourceBindGenBase;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.innercore.icstd.blockentity.BlockEntityComponent;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.scene.GameObjectHandle;
import org.innercore.icstd.scene.GameObjectManager;
import org.innercore.icstd.script.ScriptObjectComponent;
import org.innercore.icstd.world.DestroyBlockContext;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApiBinding("BlockSource")
public class BlockSource extends BlockSourceBindGenBase {
    private static final Map<Integer, BlockSource> defaultBlockSourceForDimensions = new HashMap<>();
    private static final BlockSource serverCallbackBlockSource = new BlockSource(0, false, null);
    private static final Object breakBlockSyncLock = new Object();

    @ApiBinding
    public static BlockSource getDefaultForDimension(int dimension) {
        synchronized (defaultBlockSourceForDimensions) {
            try {
                return Java8BackComp.computeIfAbsent(defaultBlockSourceForDimensions, dimension, key -> new BlockSource(dimension));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @ApiBinding
    public static BlockSource getDefaultForActor(long actor) {
        if (!NativeStaticUtils.isExistingEntity(actor)) {
            return null;
        }
        int dimension = NativeAPI.getEntityDimension(actor);
        return getDefaultForDimension(dimension);
    }

    @ApiBinding
    public static BlockSource getCurrentWorldGenRegion() {
        Level level = CallbackHelper.getForCurrentThread();
        if(level == null) level = Server.getInstance().getDefaultLevel();
        return level != null ? new BlockSource(level.getId()) : null;
    }

    @ApiBinding
    @ClientOnly
    public static NativeBlockSource getCurrentClientRegion() {
        InnerCoreServer.useIncomprehensibleMethod("NativeBlockSource.getCurrentClientRegion()");
        return null;
    }

    @ApiBinding
    public static BlockSource getFromCallbackPointer(long ptr) {
        return new BlockSource(ptr, false);
    }

    @ApiBinding
    public static BlockSource getFromServerCallbackPointer(long ptr) {
        serverCallbackBlockSource.setNewPointer(ptr, false);
        return serverCallbackBlockSource;
    }

    public static BlockSource getFromServerCallbackPointer(Level level) {
        return getDefaultForDimension(FakeDimensions.getFakeIdForLevel(level));
    }

    @ApiBinding
    public static void resetDefaultBlockSources() {
        ICLog.d("BlockSource", "resetting default block sources");
        synchronized (defaultBlockSourceForDimensions) {
            defaultBlockSourceForDimensions.clear();
        }
    }

    private static void defDestroy(Level level, Block target) {
        BlockEntity blockEntity = level.getBlockEntity(target);
        if (blockEntity != null) {
            blockEntity.onBreak();
            blockEntity.close();
            level.updateComparatorOutputLevel(target);
        }
    }


    private boolean isFinalizable;

    private boolean allowUpdate = true;
    private boolean destroyParticles = true;
    private int updateType = 3;
    private GameObjectManager scene = null;
    private Level level;

    public BlockSource(long ptr, boolean isFinalizable) {
        Level level = Server.getInstance().getLevel((int) ptr);
        if (level == null) {
            throw new RuntimeException("cannot pass null pointer to NativeBlockSource constructor");
        }
        this.level = level;
    }

    private BlockSource(long ptr, boolean isFinalizable, Object nullablePointerTag) {
        this.level = Server.getInstance().getLevel((int) ptr);
    }

    public BlockSource(int dimension, boolean b1, boolean b2) {
        this(FakeDimensions.getLevelForFakeId(dimension).getId(), true);
    }

    public BlockSource(int dimension) {
        this(dimension, true, false);
    }

    @ApiBinding
    public boolean isServerSide() {
        return true;
    }

    @ApiBinding
    public boolean canSeeSky(int x, int y, int z) {
        return level.canBlockSeeSky(new Vector3(x, y, z));
    }

    @ApiBinding
    public int getBiome(int x, int z) {
        return level.getBiomeId(x, z);
    }

    @ApiBinding
    public float getBiomeTemperatureAt(int x, int y, int z) {
        InnerCoreServer.useNotCurrentSupport("BlockSource.getBiomeTemperatureAt");
        return 0;
    }

    @ApiBinding
    public float getBiomeDownfallAt(int x, int y, int z) {
        InnerCoreServer.useNotCurrentSupport("BlockSource.getBiomeDownfallAt");
        return 0;
    }

    @ApiBinding
    public int getLightLevel(int x, int y, int z) {
        if (level.isDaytime() && level.canBlockSeeSky(new Vector3(x, y, z))) {
            return 15;
        }
        return level.getBlockLightAt(x, y, z);
    }

    @ApiBinding
    public int getBlockId(int x, int y, int z) {
        if(y >= level.getMinBlockY() && y <= level.getMaxBlockY()) {
            return level.getBlock(x, y, z).getId();
        }
        return 0;
    }

    @ApiBinding
    public int getBlockID(int x, int y, int z) {
        return this.getBlockId(x, y, z);
    }

    @ApiBinding
    public int getBlockData(int x, int y, int z) {
        if(y >= level.getMinBlockY() && y <= level.getMaxBlockY()) {
            return level.getBlockDataAt(x, y, z);
        }
        return 0;
    }

    @ApiBinding
    public int getGrassColor(int x, int y, int z) {
        InnerCoreServer.useNotCurrentSupport("BlockSource.getBiomeDownfallAt");
        return 0;
    }

    @ApiBinding
    public float getRainLevel() {
        return level.isRaining() ? 1 : 0;
    }

    @ApiBinding
    public void setRainLevel(float rainLevel) {
        level.setRaining(rainLevel != 0);
    }

    @ApiBinding
    public float getLightningLevel() {
        return level.isThundering() ? 1 : 0;
    }

    @ApiBinding
    public void setLightningLevel(float lightningLevel) {
        level.setThundering(lightningLevel != 0);
    }

    @ApiBinding
    public boolean containsMaterial(float x1, float y1, float z1, float x2, float y2, float z2, int material) {
        for(Block block : level.getCollisionBlocks(new SimpleAxisAlignedBB(x1, y1, z1, x2, y2, z2))) {
            if(BlockMethods.getMaterial(block.getId()) == material) {
                return true;
            }
        }

        return false;
    }

    @ApiBinding
    public int getRedstoneSignal(int x, int y, int z) {
        return level.getStrongPower(new Vector3(x, y, z));
    }

    @ApiBinding
    public void setRedstoneSignal(int x, int y, int z, int level, int delay, int facing) {
        InnerCoreServer.useNotCurrentSupport("NativeBlockSource.setRedstoneSignal");
    }

    @ApiBinding
    public void setRedstoneSignal(int x, int y, int z, int level, int delay) {
        setRedstoneSignal(x, y, z, level, delay, -1);
    }

    @ApiBinding
    public void setRedstoneSignal(int x, int y, int z, int level) {
        setRedstoneSignal(x, y, z, level, 4);
    }

    @ApiBinding
    public int getDimension() {
        return FakeDimensions.getFakeIdForLevel(level);
    }

    private static final boolean[] randomTickBlocks = ReflectHelper.getField(Level.class, "randomTickBlocks");

    @ApiBinding
    public void randomTick(int x, int y, int z) {
        final Block block = level.getBlock(x, y, z);

        assert randomTickBlocks != null;

        if(randomTickBlocks[block.getId()]) {
            block.onUpdate(Level.BLOCK_UPDATE_RANDOM);
        }
    }

    @ApiBinding
    public void setBiome(int chunkX, int chunkZ, int id) {
        level.setBiomeId(chunkX, chunkZ, (byte) id);
    }

    @ApiBinding
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return level.isChunkLoaded(chunkX, chunkZ);
    }

    @ApiBinding
    public boolean isChunkLoadedAt(int x, int z) {
        return this.isChunkLoaded((int) Math.floor((double) x / 16), (int) Math.floor((double) z / 16));
    }

    @ApiBinding
    public int getChunkState(int chunkX, int chunkZ) {
        InnerCoreServer.useNotCurrentSupport("BlockSourceMethods.getChunkState(level, chunkX, chunkZ)");
        return 0;
    }

    @ApiBinding
    public int getChunkStateAt(int x, int z) {
        return this.getChunkState((int) Math.floor((double) x / 16), (int) Math.floor((double) z / 16));
    }

    @ApiBinding
    public void destroyBlock(int x, int y, int z, boolean drop, int updateType, boolean destroyParticles) {
        boolean update = updateType == 3;
        Block block = level.getBlock(x, y, z);
        if (destroyParticles) {
            Map<Integer, Player> players = level.getChunkPlayers(x >> 4, z >> 4);
            level.addParticle(new DestroyBlockParticle(block.add(0.5), block), players.values());
        }
        if (drop && block.getId() < 8000) {
            Item[] drops = block.getDrops(Item.get(0));
            Vector3 pos = new Vector3(x, y, z);
            for (Item item : drops)
                level.dropItem(pos, item);
        }
        defDestroy(level, block);
        level.setBlock(x, y, z, Block.get(0), true, update);
    }

    @ApiBinding
    public void explode(float x, float y, float z, float power, boolean fire) {
        // TODO: support fire
        final Block block = level.getBlock((int) x, (int) y, (int) z);
        final Explosion explosion = new Explosion(new Position(x, y, z, level), power, block);

        explosion.explodeA();
        explosion.explodeB();
    }

    @ApiBinding
    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data, long extra) {
        EntityItem item = level.dropAndGetItem(new Vector3(x, y, z), ItemUtils.get(id, count, data, ItemInstanceExtra.getExtraOrNull(extra)));
        if (item == null) {
            return 0;
        }
        return item.getId();
    }

    @ApiBinding
    public long spawnEntity(float x, float y, float z, int type) {
        Entity entity = Entity.createEntity(type, new Position(x, y, z, level));
        if (entity != null) {
            entity.spawnToAll();
            return EntityMethod.getIdForEntity(entity);
        }
        return -1;
    }

    @ApiBinding
    public long spawnNamespacedEntity(float x, float y, float z, String namespaceId, String str2, String extra) {
        if (str2 == null) {
            return -1;
        }
        // TODO: Quite BETTER name to identifier conversion!
        Entity entity = Entity.createEntity(str2.substring(0, 1).toUpperCase() + str2.substring(1).replace("_", ""),
                new Position(x, y, z, level));
        if (entity != null) {
            entity.spawnToAll();
            return EntityMethod.getIdForEntity(entity);
        }
        return -1;
    }

    @ApiBinding
    public void spawnExpOrbs(float x, float y, float z, int amount) {
        if(amount > 0) {
            final Position source = new Position(x, y, z, level);
            final CompoundTag nbt = Entity.getDefaultNBT(source, new Vector3(0, 0, 0));

            nbt.putShort("Value", amount);
            nbt.putShort("PickupDelay", 10);

            final Entity entity = Entity.createEntity("XpOrb", level.getChunk(source.getChunkX(), source.getChunkZ()), nbt);
            if (entity != null) {
                entity.spawnToAll();
            }
        }
    }

    public Block getBlockIdDataAndState(int x, int y, int z) {
        return level.getBlock(x, y, z);
    }

    public Block getExtraBlockIdDataAndState(int x, int y, int z) {
        return level.getBlock(new Vector3(x, y, z), 1);
    }

    public void setBlock(int x, int y, int z, int id, int data, boolean allowUpdate, int updateType) {
        Block block = level.getBlock(x, y, z);
        defDestroy(level, block);
        level.setBlock(x, y, z, Block.get(id, data), true, allowUpdate);
    }

    public void setBlockByRuntimeId(int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType) {
        int[] block = BlockStateStorage.getBlockForRuntimeId(runtimeId);
        setBlock(x, y, z, block[0], block[1], allowUpdate, updateType);
    }

    public void setExtraBlock(int x, int y, int z, int id, int data, boolean allowUpdate, int updateType) {
        level.setBlock(x, y, z, 1, Block.get(id, data), true, allowUpdate);
    }

    public void setExtraBlockByRuntimeId(int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType) {
        int[] block = BlockStateStorage.getBlockForRuntimeId(runtimeId);
        setExtraBlock(x, y, z, block[0], block[1], allowUpdate, updateType);
    }

    @ApiBinding
    public GameObjectManager getScene() {
        if (scene == null) {
            scene = isServerSide() ? GameObjectManager.getForDimension(getDimension()) : GameObjectManager.getClientSide();
        }
        return scene;
    }

    private void setNewPointer(long pointer, boolean isFinalizable) {
        this.level = Server.getInstance().getLevel((int) pointer);
        this.isFinalizable = isFinalizable;
        scene = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (isFinalizable) {
            // TODO: watch for possible fix, leave without GC for now
            // nativeFinalize(pointer);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockSource that = (BlockSource) o;
        return level.getId() == that.level.getId();
    }

    @Override
    public int hashCode() {
        return level.getId() ^ (level.getId() >>> 32);
    }

    @ApiBinding
    public void setBlockUpdateAllowed(boolean allowed) {
        this.allowUpdate = allowed;
    }

    @ApiBinding
    public boolean getBlockUpdateAllowed() {
        return allowUpdate;
    }

    @ApiBinding
    public void setBlockUpdateType(int type) {
        this.updateType = type;
    }

    @ApiBinding
    public int getBlockUpdateType() {
        return updateType;
    }

    @ApiBinding
    public void setDestroyParticlesEnabled(boolean enabled) {
        destroyParticles = enabled;
    }

    @ApiBinding
    public boolean getDestroyParticlesEnabled() {
        return destroyParticles;
    }

    @ApiBinding
    public BlockState getBlock(int x, int y, int z) {
        return new BlockState(getBlockIdDataAndState(x, y, z));
    }

    @ApiBinding
    public BlockState getExtraBlock(int x, int y, int z) {
        return new BlockState(getExtraBlockIdDataAndState(x, y, z));
    }

    @ApiBinding
    public void setBlock(int x, int y, int z, int id, int data) {
        setBlock(x, y, z, id, data, allowUpdate, updateType);
    }

    @ApiBinding
    public void setBlock(int x, int y, int z, int id) {
        setBlock(x, y, z, id, 0);
    }

    @ApiBinding
    public void setBlock(int x, int y, int z, BlockState state) {
        int runtimeId = state.getRuntimeId();
        if (runtimeId >= 0) {
            setBlockByRuntimeId(x, y, z, runtimeId, allowUpdate, updateType);
        } else {
            setBlock(x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }

    @ApiBinding
    public void setExtraBlock(int x, int y, int z, int id, int data) {
        setExtraBlock(x, y, z, id, data, allowUpdate, updateType);
    }

    @ApiBinding
    public void setExtraBlock(int x, int y, int z, int id) {
        setExtraBlock(x, y, z, id, 0);
    }

    @ApiBinding
    public void setExtraBlock(int x, int y, int z, BlockState state) {
        int runtimeId = state.getRuntimeId();
        if (runtimeId > 0) {
            setExtraBlockByRuntimeId(x, y, z, runtimeId, allowUpdate, updateType);
        } else {
            setExtraBlock(x, y, z, state.id, state.data, allowUpdate, updateType);
        }
    }

    public boolean isChunkLoaded(ChunkPos chunkPos) {
        return isChunkLoaded(chunkPos.x, chunkPos.z);
    }

    @ApiBinding
    public int getGrassColor(int x, int z) {
        return getGrassColor(x, 64, z);
    }

    @ApiBinding
    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay, int unknown) {
        addToTickingQueue(x, y, z, block.getRuntimeId(), delay, unknown);
    }

    @ApiBinding
    public void addToTickingQueue(int x, int y, int z, BlockState block, int delay) {
        addToTickingQueue(x, y, z, block, delay, 0);
    }

    @ApiBinding
    public void addToTickingQueue(int x, int y, int z, int delay, int unknown) {
        addToTickingQueue(x, y, z, -1, delay, unknown);
    }

    @ApiBinding
    public void addToTickingQueue(int x, int y, int z, int delay) {
        addToTickingQueue(x, y, z, -1, delay, 0);
    }

    @ApiBinding
    public void destroyBlock(int x, int y, int z, boolean drop) {
        destroyBlock(x, y, z, drop, updateType, destroyParticles);
    }

    @ApiBinding
    public void destroyBlock(int x, int y, int z) {
        destroyBlock(x, y, z, false);
    }

    @ApiBinding
    public void breakBlock(int x, int y, int z, boolean isDropAllowed, long actor, ItemInstance item) {
        GenericScriptPos3 coords = new GenericScriptPos3(x, y, z);
        BlockState block = getBlock(x, y, z);
        Callback.invokeAPICallback("BreakBlock", this, coords, block, isDropAllowed, actor, item);
        if (!NativeAPI.isDefaultPrevented() && StaticEntity.exists(actor) && StaticEntity.getDimension(actor) == getDimension()) {
            Callback.invokeAPICallback("DestroyBlock", coords, block, actor);
        }
        destroyBlock(x, y, z, isDropAllowed);
    }

    @ApiBinding
    public void breakBlock(int x, int y, int z, boolean isDropAllowed, long actor) {
        breakBlock(x, y, z, isDropAllowed, actor, StaticEntity.getCarriedItem(actor));
    }

    @ApiBinding
    public void breakBlock(int x, int y, int z, boolean isDropAllowed, ItemInstance item) {
        breakBlock(x, y, z, isDropAllowed, -1, item);
    }

    @ApiBinding
    public void breakBlock(int x, int y, int z, boolean isDropAllowed) {
        breakBlock(x, y, z, isDropAllowed, -1, new ItemInstance());
    }

    @ApiBinding
    public BlockBreakResult breakBlockForResult(int x, int y, int z, long actor, ItemInstance item) {
        synchronized (breakBlockSyncLock) {
            NativeCallback.startOverrideBlockBreakResult();
            breakBlock(x, y, z, true, actor, item);
            return NativeCallback.endOverrideBlockBreakResult();
        }
    }

    @ApiBinding
    public BlockBreakResult breakBlockForResult(int x, int y, int z, long actor) {
        return breakBlockForResult(x, y, z, actor, StaticEntity.getCarriedItem(actor));
    }

    @ApiBinding
    public BlockBreakResult breakBlockForResult(int x, int y, int z, ItemInstance item) {
        return breakBlockForResult(x, y, z, -1, item);
    }

    @ApiBinding
    public BlockBreakResult breakBlockForResult(int x, int y, int z) {
        return breakBlockForResult(x, y, z, -1, new ItemInstance());
    }

    @ApiBinding
    public ScriptableObject breakBlockForJsResult(int x, int y, int z, long actor, ScriptableObject item) {
        return breakBlockForResult(x, y, z, actor, new ItemInstance(item)).asScriptable();
    }

    @ApiBinding
    public ScriptableObject breakBlockForJsResult(int x, int y, int z, long actor) {
        return breakBlockForResult(x, y, z, actor).asScriptable();
    }

    @ApiBinding
    public ScriptableObject breakBlockForJsResult(int x, int y, int z, ScriptableObject item) {
        return breakBlockForResult(x, y, z, new ItemInstance(item)).asScriptable();
    }

    @ApiBinding
    public ScriptableObject breakBlockForJsResult(int x, int y, int z) {
        return breakBlockForResult(x, y, z).asScriptable();
    }

    @ApiBinding
    public long clip(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] output) {
        return clip(level, x1, y1, z1, x2, y2, z2, mode, output);
    }

    @ApiBinding
    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag) {
        return fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2, backCompEntityType, flag);
    }

    @ApiBinding
    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false);
    }

    @ApiBinding
    public long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false);
    }

    @ApiBinding
    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String type) {
        return fetchEntitiesOfTypeInAABB(level, x1, y1, z1, x2, y2, z2, namespace, type);
    }

    @ApiBinding
    public long[] fetchEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String fullType) {
        String namespace = "minecraft";
        int sep = fullType.indexOf(':');
        if (sep != -1) {
            namespace = fullType.substring(0, sep);
            fullType = fullType.substring(sep + 1);
        }
        return fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, namespace, fullType);
    }

    @ApiBinding
    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag) {
        return ScriptableObjectHelper.longToScriptableArray(fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2, backCompEntityType, flag));
    }

    @ApiBinding
    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType) {
        return ScriptableObjectHelper.longToScriptableArray(fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, backCompEntityType, false));
    }

    @ApiBinding
    public NativeArray listEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        return ScriptableObjectHelper.longToScriptableArray(fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, 256, false));
    }

    @ApiBinding
    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String type) {
        return ScriptableObjectHelper.longToScriptableArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, namespace, type));
    }

    @ApiBinding
    public NativeArray listEntitiesOfTypeInAABB(float x1, float y1, float z1, float x2, float y2, float z2, String fullType) {
        return ScriptableObjectHelper.longToScriptableArray(fetchEntitiesOfTypeInAABB(x1, y1, z1, x2, y2, z2, fullType));
    }

    @ApiBinding
    public long spawnEntity(float x, float y, float z, String str1, String str2, String str3) {
        return spawnNamespacedEntity(x, y, z, str1, str2, str3);
    }

    @ApiBinding
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

    @ApiBinding
    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data, ItemInstanceExtra extra) {
        EntityItem item = level.dropAndGetItem(new Vector3(x, y, z), ItemUtils.get(id, count, data, extra));
        if (item == null) {
            return 0;
        }
        return item.getId();
    }

    @ApiBinding
    public long spawnDroppedItem(float x, float y, float z, int id, int count, int data) {
        return spawnDroppedItem(x, y, z, id, count, data, null);
    }

    @ApiBinding
    public long spawnDroppedItem(float x, float y, float z, int id, int count) {
        return spawnDroppedItem(x, y, z, id, count, 0, null);
    }

    @ApiBinding
    public long spawnDroppedItem(float x, float y, float z, int id) {
        return spawnDroppedItem(x, y, z, id, 1, 0, null);
    }

    public void playSound(float x, float y, float z, String sound, float volume, float pitch, long[] players) {
        playSound(level, x, y, z, sound, volume, pitch, players);
    }

    @ApiBinding
    public void playSound(float x, float y, float z, String sound, float volume, float pitch, NativeArray players) {
        playSound(level, x, y, z, sound, volume, pitch, ScriptableObjectHelper.scriptableToLongArray(players));
    }

    @ApiBinding
    public void playSoundAtEntity(long entity, String sound, float volume, float pitch) {
        float[] position = new float[3];
        NativeAPI.getPosition(entity, position);
        playSound(position[0], position[1], position[2], sound, volume, pitch);
    }

    @ApiBinding
    public void playSoundAtEntity(long entity, String sound, float volume) {
        playSoundAtEntity(entity, sound, volume, 1.0f);
    }

    @ApiBinding
    public void playSoundAtEntity(long entity, String sound) {
        playSoundAtEntity(entity, sound, 1.0f, 1.0f);
    }

    public void playSoundAtEntity(long entity, String sound, float volume, float pitch, long[] players) {
        float[] position = new float[3];
        NativeAPI.getPosition(entity, position);
        playSound(position[0], position[1], position[2], sound, volume, pitch, players);
    }

    @ApiBinding
    public void playSoundAtEntity(long entity, String sound, float volume, float pitch, NativeArray players) {
        playSoundAtEntity(entity, sound, volume, pitch, ScriptableObjectHelper.scriptableToLongArray(players));
    }

    public void stopSound(String sound, long[] players) {
        stopSound(level, sound, players);
    }

    @ApiBinding
    public void stopSound(String sound, NativeArray players) {
        stopSound(level, sound, ScriptableObjectHelper.scriptableToLongArray(players));
    }

    private static final Int2ObjectMap<String> TILES_TYPES = new Int2ObjectOpenHashMap<>();

    static {
        TILES_TYPES.put(BlockID.CHEST, BlockEntity.CHEST);
        TILES_TYPES.put(BlockID.TRAPPED_CHEST, BlockEntity.CHEST);
        TILES_TYPES.put(BlockID.ENDER_CHEST, BlockEntity.ENDER_CHEST);
        TILES_TYPES.put(BlockID.FURNACE, BlockEntity.FURNACE);
        TILES_TYPES.put(BlockID.BLAST_FURNACE, BlockEntity.BLAST_FURNACE);
        TILES_TYPES.put(BlockID.SMOKER, BlockEntity.SMOKER);
        TILES_TYPES.put(BlockID.MONSTER_SPAWNER, BlockEntity.MOB_SPAWNER);
        TILES_TYPES.put(BlockID.ENCHANT_TABLE, BlockEntity.ENCHANT_TABLE);
        TILES_TYPES.put(BlockID.ITEM_FRAME_BLOCK, BlockEntity.ITEM_FRAME);
        TILES_TYPES.put(BlockID.HOPPER_BLOCK, BlockEntity.HOPPER);
        TILES_TYPES.put(BlockID.DROPPER, BlockEntity.DROPPER);
    }


    @ApiBinding("getBlockEntity") // compatibility - scripts expect this to return only native block entities
    public NativeTileEntity getNativeBlockEntity(int x, int y, int z) {
        BlockEntity entity = level.getBlockEntity(new BlockVector3(x, y, z));

        // TODO: Некоторые моды просто не знают, что блок может быть, а BlockEntity может и не быть(например StorageInterface)
        if(entity == null){
            final String type = TILES_TYPES.get(level.getBlockIdAt(x, y, z));
            if(type != null){
                entity = BlockEntity.createBlockEntity(type, new Position(x, y, z).setLevel(level), new CompoundTag(type)
                        .putString("id", type)
                        .putInt("x", x)
                        .putInt("y", y)
                        .putInt("z", z));
            }
        }

        return entity != null ? new NativeTileEntity(entity) : null;
    }

    public BlockEntityComponent getBlockEntity(BlockPos pos) {
        BlockEntityComponent blockEntityFromScene = getScene().queryAtBlock(pos, BlockEntityComponent.class).one();
        if (blockEntityFromScene != null)
            return blockEntityFromScene;
        return getNativeBlockEntity(pos.x, pos.y, pos.z);
    }

    public BlockEntityComponent getBlockEntity(int x, int y, int z) {
        return getBlockEntity(new BlockPos(x, y, z));
    }

    public void addToTickingQueue(int x, int y, int z, int runtimeId, int delay, int unknown_int) {
        InnerCoreServer.useNotCurrentSupport("BlockSourceMethods.addToTickingQueue");
    }

    @ApiBinding
    public void playSound(float x, float y, float z, String sound, float volume, float pitch) {
        PlaySoundPacket packet = new PlaySoundPacket();

        packet.name = sound;
        packet.volume = volume;
        packet.pitch = pitch;
        packet.x = (int) x;
        packet.y = (int) y;
        packet.z = (int) z;

        level.addChunkPacket(packet.x >> 4, packet.z >> 4, packet);
    }

    @ApiBinding
    public void stopSound(String sound) {
        StopSoundPacket packet = new StopSoundPacket();

        packet.name = sound;

        Server.broadcastPacket(level.getPlayers().values(), packet);
    }

    @ApiBinding("getBlockEntityAny")
    public Object getBlockEntityForScript(int x, int y, int z) {
        Object blockEntity = getBlockEntity(x, y, z);
        if (blockEntity instanceof ScriptObjectComponent)
            blockEntity = ((ScriptObjectComponent) blockEntity).getScriptObject();
        return blockEntity;
    }

    @ApiBinding("getBlockEntityNotNative")
    public Object getBlockEntityNotNativeForScript(int x, int y, int z) {
        Object blockEntity = getBlockEntity(x, y, z);
        if (blockEntity instanceof NativeTileEntity)
            return null;
        if (blockEntity instanceof ScriptObjectComponent)
            blockEntity = ((ScriptObjectComponent) blockEntity).getScriptObject();
        return blockEntity;
    }

    public BlockEntityComponent getOrAddBlockEntity(BlockPos pos) {
        BlockEntityComponent existing = getBlockEntity(pos);
        if (existing != null || !isServerSide())
            return existing;
        return BlockEntityRegistry.createForBlockAndPlaceAtScene(getScene(), getBlockID(pos.x, pos.y, pos.z), pos);
    }

    public BlockEntityComponent getOrAddBlockEntity(int x, int y, int z) {
        return getOrAddBlockEntity(new BlockPos(x, y, z));
    }

    @ApiBinding("getOrAddBlockEntity")
    public Object getOrAddBlockEntityForScript(int x, int y, int z) {
        Object blockEntity = getOrAddBlockEntity(x, y, z);
        if (blockEntity instanceof ScriptObjectComponent)
            blockEntity = ((ScriptObjectComponent) blockEntity).getScriptObject();
        return blockEntity;
    }

    public void destroyBlockEntity(BlockPos pos, @Nullable DestroyBlockContext ctx) {
        if (!isServerSide()) {
            throw new IllegalStateException("destroyBlockEntity is only allowed on server side");
        }
        if (ctx != null) {
            ctx.pos = pos;
        }
        BlockEntityComponent blockEntity = getScene().queryAtBlock(pos, BlockEntityComponent.class).one();
        if (blockEntity == null) {
            return;
        }
        if (ctx != null) {
            ComponentUtils.safeCallBlockDestroyedFor(blockEntity, this, pos, ctx);
        }
    }

    private static long clip(Level level, float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] joutput) {
        InnerCoreServer.useNotCurrentSupport("BlockSourceMethods.clip(level, x1, y1, z1, x2, y2, z2, mode, joutput)");
        return 0;
    }

    private static void playSound(Level level, float x, float y, float z, String sound, float volume, float pitch, long[] players) {

    }

    private static void stopSound(Level level, String sound, long[] players) {

    }

    private static Entity[] fetchEntitiesInAABB(Level level, float x1, float y1, float z1, float x2, float y2, float z2) {
        return level.getCollidingEntities(new SimpleAxisAlignedBB(x1, y1, z1, x2, y2, z2));
    }

    private static long[] fetchEntitiesInAABB(Level level, float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean blacklist) {
        return Arrays.stream(fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2))
                .filter(entity -> {
                    if (backCompEntityType == 256) {
                        return true;
                    }
                    int entityType = EntityMethod.getEntityTypeDirect(entity);
                    return blacklist != (entityType == backCompEntityType);
                })
                .mapMultiToLong((entity, consumer) -> consumer.accept(EntityMethod.getIdForEntity(entity)))
                .toArray();
    }

    private static long[] fetchEntitiesOfTypeInAABB(Level level, float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String name) {
        return Arrays.stream(fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2))
                .filter(entity -> entity.getName().equalsIgnoreCase(name))
                .mapMultiToLong((entity, consumer) -> consumer.accept(EntityMethod.getIdForEntity(entity)))
                .toArray();
    }
}
