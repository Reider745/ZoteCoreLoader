package com.reider745.world;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.event.entity.EntityExplosionPrimeEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.format.anvil.palette.BiomePalette;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.GlobalBlockPalette;
import com.reider745.hooks.ItemUtils;

import java.util.Map;


public class BlockSourceMethods {
    private static void defDestroy(Level level, Block target){
        BlockEntity blockEntity = level.getBlockEntity(target);
        if (blockEntity != null) {
            blockEntity.onBreak();
            blockEntity.close();

            level.updateComparatorOutputLevel(target);
        }
    }
    public static Level getLevelForDimension(int dimension){
        if(dimension >= 0 && dimension <= 2)
            dimension++;
        return InnerCoreServer.server.getLevel(dimension);
    }

    public static void destroyBlock(Level pointer, int x, int y, int z, boolean drop, int updateType, boolean destroyParticles){
        boolean update = updateType == 3;
        Block block = pointer.getBlock(x, y, z);
        pointer.setBlock(x, y, z, Block.get(0), update, update);
        if(destroyParticles) {
            Map<Integer, Player> players = pointer.getChunkPlayers((int) x >> 4, (int) z >> 4);
            pointer.addParticle(new DestroyBlockParticle(block.add(0.5), block), players.values());
        }
        if(drop){
            Item[] drops = block.getDrops(Item.get(0));

        }
        defDestroy(pointer, block);
    }

    public static int getBlockId(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z).getId();
    }

    public static boolean isChunkLoaded(Level level, int x, int z){
        return level.isChunkLoaded(x, z);
    }

    public static long spawnDroppedItem(Level pointer, float x, float y, float z, int id, int count, int data, long extra){
        return pointer.dropAndGetItem(new Vector3(x, y, z), ItemUtils.get(id, count, data, extra)).getId();
    }

    public static Long nativeGetForClientSide(){
        return null;
    }
    public static void nativeFinalize(long pointer){

    }

    public static boolean canSeeSky(Level pointer, int x, int y, int z){
        return pointer.canBlockSeeSky(new Vector3(x, y, z));
    }
    public static int getBiome(Level pointer, int x, int y, int z){
        return pointer.getBiomeId(x, z);
    }
    public static float getBiomeTemperatureAt(Level pointer, int x, int y, int z){
        return 0;
        // return Biome.getBiome(pointer.getBiomeId(x, z)).get
    }
    public static float getBiomeDownfallAt(Level pointer, int x, int y, int z){
        return 0;
    }
    public static int getBrightness(Level Level, int x, int y, int z){
        return 0;
    }

    public static int getBlockData(Level pointer, int x, int y, int z){
        return pointer.getBlockDataAt(x, y, z);
    }
    public static Block getBlockIdDataAndState(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z);
    }
    public static Block getExtraBlockIdDataAndState(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z);
    }
    public static void setBlock(Level pointer, int x, int y, int z, int id, int data, boolean allowUpdate, int updateType){
        Block block = pointer.getBlock(x, y, z);
        pointer.setBlock(x, y, z, Block.get(id, data).clone(), allowUpdate, allowUpdate);
        defDestroy(pointer, block);
    }
    public static void setBlockByRuntimeId(Level pointer, int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType){
        Block block = pointer.getBlock(x, y, z);
        int legacyId = GlobalBlockPalette.getLegacyFullId(runtimeId);
        setBlock(pointer, x, y, z, legacyId >> 6, legacyId & 0x3F, allowUpdate, updateType);
        defDestroy(pointer, block);
    }
    public static void setExtraBlock(Level pointer, int x, int y, int z, int id, int data, boolean allowUpdate, int updateType){
        pointer.setBlockExtraDataAt(x, y, z, id, data);
    }
    public static void setExtraBlockByRuntimeId(Level pointer, int x, int y, int z, int runtimeId, boolean allowUpdate, int updateType){
        int legacyId = GlobalBlockPalette.getLegacyFullId(runtimeId);
        setExtraBlock(pointer, x, y, z, legacyId >> 6, legacyId & 0x3F, allowUpdate, updateType);
    }
    public static int getGrassColor(Level pointer, int x, int y, int z){
        return 0;
    }
    public static BlockEntity getBlockEntity(Level pointer, int x, int y, int z){
        return pointer.getBlockEntity(new BlockVector3(x, y, z));
    }
    public static int getDimension(Level pointer){
        return pointer.getDimension();
    }

    public static void setBiome(Level pointer, int chunkX, int chunkZ, int id){
        pointer.setBiomeId(chunkX, chunkZ, (byte) id);
    }
    public static int getChunkState(Level pointer, int chunkX, int chunkZ){
        return 0;
    }

    public static void addToTickingQueue(Level pointer, int x, int y, int z, int runtimeId /* = -1 */, int delay, int unknown /* = 0 */){

    }

    public static void explode(Level pointer, float x, float y, float z, float power, boolean fire){
        EntityExplosionPrimeEvent event = new EntityExplosionPrimeEvent(null, 4);
        InnerCoreServer.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Explosion explosion = new Explosion(new Position(x, y, z), event.getForce(), (Entity) null);
        if (event.isBlockBreaking()) {
            explosion.explodeA();
        }
        explosion.explodeB();
    }

    public static long clip(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] joutput){
        return 0;
    }

    public static native long[] fetchEntitiesInAABB(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, int backCompEntityType, boolean flag);
    public static native long[] fetchEntitiesOfTypeInAABB(Level pointer, float x1, float y1, float z1, float x2, float y2, float z2, String namespace, String name);
    public static long spawnEntity(Level pointer, int type, float x, float y, float z){
        Entity entity = Entity.createEntity(type, new Position(x, y, z));
        pointer.addEntity(entity);
        return entity.getId();
    }
    public static native long spawnNamespacedEntity(Level pointer, float x, float y, float z, String str1, String str2, String str3);
    public static long spawnExpOrbs(Level pointer, float x, float y, float z, int amount){
        Vector3 source = new Vector3(x, y, z);
        CompoundTag nbt = Entity.getDefaultNBT(source, new Vector3(0, 0, 0));
        Entity entity = Entity.createEntity("XpOrb", pointer.getChunk(source.getChunkX(), source.getChunkZ()), nbt);
        entity.spawnToAll();
        return entity.getId();
    }
}
