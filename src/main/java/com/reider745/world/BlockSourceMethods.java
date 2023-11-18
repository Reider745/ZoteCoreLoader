package com.reider745.world;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.reider745.InnerCoreServer;

public class BlockSourceMethods {
    public static Level getLevelForDimension(int dimension){
        if(dimension >= 0 && dimension <= 2)
            dimension++;
        return InnerCoreServer.server.getLevel(dimension);
    }

    public static void destroyBlock(Level pointer, int x, int y, int z, boolean drop, int updateType, boolean destroyParticles){
        pointer.setBlock(x, y, z, Block.get(0), false, updateType == 1);
    }

    public static int getBlockId(Level pointer, int x, int y, int z){
        return pointer.getBlock(x, y, z).getId();
    }

    public static boolean isChunkLoaded(Level level, int x, int z){
        return level.isChunkLoaded(x, z);
    }

    public static long spawnDroppedItem(Level pointer, float x, float y, float z, int id, int count, int data, long extra){
        return pointer.dropAndGetItem(new Vector3(x, y, z), Item.get(id, data, count)).getId();
    }
}
