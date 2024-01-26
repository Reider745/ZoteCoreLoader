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
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import com.reider745.InnerCoreServer;
import com.reider745.entity.EntityMethod;
import com.reider745.hooks.GlobalBlockPalette;
import com.reider745.hooks.ItemUtils;

import java.util.ArrayList;
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

    private static void sendBlock(Level level, int x, int y, int z, int layer) {
        level.sendBlocks(level.getChunkPlayers(x / 16, z / 16).values().toArray(new Player[0]),
                new Vector3[] { new Vector3(x, y, z) }, 0, layer);
    }

    public static Level getLevelForDimension(int dimension) {
        return Server.getInstance().getLevels().values().stream().filter(level -> level.getDimension() == dimension)
                .findFirst().orElse(null);
    }

    public static Level getLevelForName(String name) {
        return Server.getInstance().getLevelByName(name);
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
        sendBlock(level, x, y, z, 0);
    }

    public static int getBlockId(Level level, int x, int y, int z) {
        return level.getBlock(x, y, z).getId();
    }

    public static boolean isChunkLoaded(Level level, int x, int z) {
        return level.isChunkLoaded(x, z);
    }

    public static long spawnDroppedItem(Level level, float x, float y, float z, int id, int count, int data,
            long extra) {
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
        level.setBlock(x, y, z, Block.get(id, data).clone(), false, allowUpdate);
        sendBlock(level, x, y, z, 0);
    }

    public static void setBlockByRuntimeId(Level level, int x, int y, int z, int runtimeId, boolean allowUpdate,
            int updateType) {
        int legacyId = GlobalBlockPalette.getLegacyFullId(runtimeId);
        setBlock(level, x, y, z, legacyId >> 6, legacyId & 0x3F, allowUpdate, updateType);
    }

    public static void setExtraBlock(Level level, int x, int y, int z, int id, int data, boolean allowUpdate,
            int updateType) {
        level.setBlockExtraDataAt(x, y, z, id, data);
        sendBlock(level, x, y, z, 1);
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
        return level.getDimension();
    }

    public static void setBiome(Level level, int chunkX, int chunkZ, int id) {
        level.setBiomeId(chunkX, chunkZ, (byte) id);
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
        Server.getInstance().getPluginManager().callEvent(event);

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

    private static long[] convert(ArrayList<Long> list) {
        final int size = list.size();
        if (size == 0)
            return new long[] {};

        long[] result = new long[size];
        for (int i = 0; i < size; i++)
            result[i] = list.get(i);

        return result;
    }

    public static long[] fetchEntitiesInAABB(Level level, float x1, float y1, float z1, float x2, float y2, float z2,
            int backCompEntityType, boolean flag) {
        Entity[] entities = level.getEntities();
        ArrayList<Long> list = new ArrayList<>();

        for (Entity entity : entities) {
            Position position = entity.getPosition();
            if ((flag == (EntityMethod.getEntityTypeDirect(entity) != backCompEntityType))
                    && (x1 >= position.x && position.x <= x2) && (y1 >= position.y && position.y <= y2)
                    && (z1 >= position.z && position.z <= z2))
                list.add(entity.getId());
        }

        return convert(list);
    }

    public static long[] fetchEntitiesOfTypeInAABB(Level level, float x1, float y1, float z1, float x2, float y2,
            float z2, String namespace, String name) {
        Entity[] entities = level.getEntities();
        ArrayList<Long> list = new ArrayList<>();

        for (Entity entity : entities) {
            Position position = entity.getPosition();
            // TODO: Quite BETTER name to identifier conversion!
            if (entity.getName().equalsIgnoreCase(name)
                    && (x1 >= position.x && position.x <= x2) && (y1 >= position.y && position.y <= y2)
                    && (z1 >= position.z && position.z <= z2))
                list.add(entity.getId());
        }

        return convert(list);
    }

    public static long spawnEntity(Level level, int type, float x, float y, float z) {
        Entity entity = Entity.createEntity(type, new Position(x, y, z));
        level.addEntity(entity);
        return entity.getId();
    }

    public static long spawnNamespacedEntity(Level level, float x, float y, float z, String str1, String str2,
            String str3) {
        if (str2 == null) {
            return -1;
        }
        // TODO: Quite BETTER name to identifier conversion!
        Entity entity = Entity.createEntity(str2.substring(0, 1).toUpperCase() + str2.substring(1).replace("_", ""),
                new Position(x, y, z));
        level.addEntity(entity);
        return entity.getId();
    }

    public static long spawnExpOrbs(Level level, float x, float y, float z, int amount) {
        Vector3 source = new Vector3(x, y, z);
        CompoundTag nbt = Entity.getDefaultNBT(source, new Vector3(0, 0, 0));
        Entity entity = Entity.createEntity("XpOrb", level.getChunk(source.getChunkX(), source.getChunkZ()), nbt);
        entity.spawnToAll();
        return entity.getId();
    }
}
