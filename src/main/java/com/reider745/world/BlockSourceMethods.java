package com.reider745.world;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.block.BlockExplosionPrimeEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.PlaySoundPacket;

import com.reider745.InnerCoreServer;
import com.reider745.entity.EntityMethod;
import com.reider745.event.EventListener;
import com.reider745.hooks.GlobalBlockPalette;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

import java.util.Arrays;
import java.util.Map;

public class BlockSourceMethods {

    private static void defDestroy(Level level, Block target) {
        BlockEntity blockEntity = level.getBlockEntity(target);
        if (blockEntity != null) {
            blockEntity.onBreak();
            blockEntity.close();
            level.updateComparatorOutputLevel(target);
        }
    }

    public static Level getLevelForDimension(int dimension) {
        /*return */
        //FakeDimensions.getFakeIdForLevel()
        return FakeDimensions.getLevelForFakeId(dimension);
    }

    public static Level getLevelForName(String name) {
        return Server.getInstance().getLevelByName(name);
    }

    public static Level getDefaultLevel() {
        return Server.getInstance().getDefaultLevel();
    }

    public static void destroyBlock(Level level, int x, int y, int z, boolean drop, int updateType,
            boolean destroyParticles) {
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

    public static int getBlockId(Level level, int x, int y, int z) {
        return level.getBlock(x, y, z).getId();
    }

    public static boolean isChunkLoaded(Level level, int x, int z) {
        return level.isChunkLoaded(x, z);
    }

    public static long spawnDroppedItem(Level level, float x, float y, float z, int id, int count, int data, NativeItemInstanceExtra extra) {
        EntityItem item = level.dropAndGetItem(new Vector3(x, y, z), ItemUtils.get(id, count, data, extra));
        if (item == null)
            return 0;
        return item.getId();
    }

    public static boolean canSeeSky(Level level, int x, int y, int z) {
        return level.canBlockSeeSky(new Vector3(x, y, z));
    }

    public static int getBiome(Level level, int x, int y, int z) {
        return level.getBiomeId(x, z);
    }

    public static float getBiomeTemperatureAt(Level level, int x, int y, int z) {
        return 0;
        // return Biome.getBiome(level.getBiomeId(x, z)).get
    }

    public static float getBiomeDownfallAt(Level level, int x, int y, int z) {
        return 0;
    }

    public static int getBrightness(Level level, int x, int y, int z) {
        int time = level.getTime();
        ;
        if (!(time >= Level.TIME_NIGHT && time < Level.TIME_SUNRISE) && level.canBlockSeeSky(new Vector3(x, y, z)))
            return 15;
        return level.getBlockLightAt(x, y, z);
    }

    public static int getBlockData(Level level, int x, int y, int z) {
        return level.getBlockDataAt(x, y, z);
    }

    public static Block getBlockIdDataAndState(Level level, int x, int y, int z) {
        return level.getBlock(x, y, z);
    }

    public static Block getExtraBlockIdDataAndState(Level level, int x, int y, int z) {
        return level.getBlock(x, y, z);
    }

    public static void setBlock(Level level, int x, int y, int z, int id, int data, boolean allowUpdate,
            int updateType) {
        Block block = level.getBlock(x, y, z);
        defDestroy(level, block);
        level.setBlock(x, y, z, Block.get(id, data), false, allowUpdate);
    }

    public static void setBlockByRuntimeId(Level level, int x, int y, int z, int runtimeId, boolean allowUpdate,
            int updateType) {
        int legacyId = GlobalBlockPalette.getLegacyFullId(runtimeId);
        setBlock(level, x, y, z, legacyId >> 6, legacyId & 0x3F, allowUpdate, updateType);
    }

    public static void setExtraBlock(Level level, int x, int y, int z, int id, int data, boolean allowUpdate,
            int updateType) {
        level.setBlockExtraDataAt(x, y, z, id, data);
    }

    public static void setExtraBlockByRuntimeId(Level level, int x, int y, int z, int runtimeId, boolean allowUpdate,
            int updateType) {
        int legacyId = GlobalBlockPalette.getLegacyFullId(runtimeId);
        setExtraBlock(level, x, y, z, legacyId >> 6, legacyId & 0x3F, allowUpdate, updateType);
    }

    public static int getGrassColor(Level level, int x, int y, int z) {
        return 0;
    }

    public static BlockEntity getBlockEntity(Level level, int x, int y, int z) {
        return level.getBlockEntity(new BlockVector3(x, y, z));
    }

    public static int getDimension(Level level) {
        return FakeDimensions.getFakeIdForLevel(level);
    }

    public static void setBiome(Level level, int chunkX, int chunkZ, int id) {
        level.setBiomeId(chunkX, chunkZ, (byte) id);
    }

    public static void setRespawnCoords(Level level, int x, int y, int z) {
        level.setSpawnLocation(new Vector3(x, y, z));
    }

    public static int getChunkState(Level level, int chunkX, int chunkZ) {
        InnerCoreServer.useNotCurrentSupport("BlockSourceMethods.getChunkState(level, chunkX, chunkZ)");
        return 0;
    }

    public static void addToTickingQueue(Level level, int x, int y, int z, int runtimeId /* = -1 */, int delay,
            int unknown /* = 0 */) {
        InnerCoreServer.useNotCurrentSupport(
                "BlockSourceMethods.addToTickingQueue(level, x, y, z, runtimeId, delay, unknown)");
    }

    public static void explode(Level level, float x, float y, float z, float power, boolean fire) {
        final Block block = level.getBlock((int) x, (int) y, (int) z);
        final BlockExplosionPrimeEvent event = new BlockExplosionPrimeEvent(block, power, 0d);
        event.setIncendiary(fire);

        synchronized (EventListener.DEALING_LOCK) {
            EventListener.dealingEvent = event;
            Server.getInstance().getPluginManager().callEvent(event);
            EventListener.dealingEvent = null;
        }

        if (event.isCancelled()) {
            return;
        }

        final Position position = new Position(x, y, z);
        position.setLevel(level);
        final Explosion explosion = new Explosion(position, event.getForce(), block);
        if (event.isBlockBreaking()) {
            explosion.explodeA();
        }
        explosion.explodeB();
    }

    public static long clip(Level level, float x1, float y1, float z1, float x2, float y2, float z2, int mode,
            float[] joutput) {
        InnerCoreServer.useNotCurrentSupport("BlockSourceMethods.clip(level, x1, y1, z1, x2, y2, z2, mode, joutput)");
        return 0;
    }

    private static Entity[] fetchEntitiesInAABB(Level level, float x1, float y1, float z1, float x2, float y2,
            float z2) {
        return level.getCollidingEntities(
                new SimpleAxisAlignedBB((double) x1, (double) y1, (double) z1, (double) x2, (double) y2, (double) z2));
    }

    public static long[] fetchEntitiesInAABB(Level level, float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType, boolean blacklist) {
        return Arrays.stream(fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2))
                .filter(entity -> {
                    if (backCompEntityType == 256) {
                        return true;
                    }
                    int entityType = EntityMethod.getEntityTypeDirect(entity);
                    return !blacklist ? entityType == backCompEntityType : entityType != backCompEntityType;
                })
                .mapMultiToLong((entity, consumer) -> consumer.accept(entity.getId()))
                .toArray();
    }

    public static long[] fetchEntitiesOfTypeInAABB(Level level, float x1, float y1, float z1, float x2, float y2,
            float z2, String namespace, String name) {
        // TODO: Quite BETTER name to identifier conversion!
        return Arrays.stream(fetchEntitiesInAABB(level, x1, y1, z1, x2, y2, z2))
                .filter(entity -> entity.getName().equalsIgnoreCase(name))
                .mapMultiToLong((entity, consumer) -> consumer.accept(entity.getId()))
                .toArray();
    }

    public static long spawnEntity(Level level, int type, float x, float y, float z) {
        Entity entity = Entity.createEntity(type, new Position(x, y, z, level));
        if (entity != null) {
            entity.spawnToAll();
            return entity.getId();
        }
        return -1;
    }

    public static long spawnNamespacedEntity(Level level, float x, float y, float z, String str1, String str2,
            String str3) {
        if (str2 == null) {
            return -1;
        }
        // TODO: Quite BETTER name to identifier conversion!
        Entity entity = Entity.createEntity(str2.substring(0, 1).toUpperCase() + str2.substring(1).replace("_", ""),
                new Position(x, y, z, level));
        if (entity != null) {
            entity.spawnToAll();
            return entity.getId();
        }
        return -1;
    }

    public static long spawnExpOrbs(Level level, float x, float y, float z, int amount) {
        Vector3 source = new Vector3(x, y, z);
        CompoundTag nbt = Entity.getDefaultNBT(source, new Vector3(0, 0, 0));
        Entity entity = Entity.createEntity("XpOrb", new Position(x, y, z, level), nbt);
        if (entity != null) {
            entity.spawnToAll();
            return entity.getId();
        }
        return -1;
    }

    public static void playSound(Level level, String sound, int x, int y, int z, float volume, float pitch, Player[] players) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound;
        packet.volume = volume;
        packet.pitch = pitch;
        packet.x = x;
        packet.y = y;
        packet.z = z;

        if (players == null || players.length == 0) {
            level.addChunkPacket(x >> 4, z >> 4, packet);
        } else {
            Server.broadcastPacket(players, packet);
        }
    }
}
