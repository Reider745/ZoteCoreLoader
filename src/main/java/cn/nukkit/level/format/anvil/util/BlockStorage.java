package cn.nukkit.level.format.anvil.util;

import cn.nukkit.Server;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.util.PalettedBlockStorage;
import cn.nukkit.utils.BinaryStream;
import com.google.common.base.Preconditions;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.io.IOException;
import java.util.Arrays;

public class BlockStorage {
    private static final int SECTION_SIZE = 4096;
    private final int[] blockIds;
    private final NibbleArray blockData;

    public BlockStorage() {
        blockIds = new int[SECTION_SIZE];
        blockData = new NibbleArray(SECTION_SIZE);
    }

    private BlockStorage(int[] blockIds, NibbleArray blockData) {
        this.blockIds = blockIds;
        this.blockData = blockData;
    }

    private static int getIndex(int x, int y, int z) {
        int index = (x << 8) + (z << 4) + y; // XZY = Bedrock format
        Preconditions.checkArgument(index >= 0 && index < SECTION_SIZE, "Invalid index");
        return index;
    }

    public int getBlockData(int x, int y, int z) {
        return blockData.get(getIndex(x, y, z)) & 0xf;
    }

    public int getBlockId(int x, int y, int z) {
        return blockIds[getIndex(x, y, z)];
    }

    public static void er() {
        try {
            throw new IOException("Уебан");
        }catch (IOException e){
            Server.getInstance().getLogger().info("Et,fy", e);
        }
    }

    public void setBlockId(int x, int y, int z, int id) {
        if(id == -11){
            er();
        }
        blockIds[getIndex(x, y, z)] = id;
    }

    public void setBlockData(int x, int y, int z, int data) {
        blockData.set(getIndex(x, y, z), (byte) data);
    }

    public int getFullBlock(int x, int y, int z) {
        return getFullBlock(getIndex(x, y, z));
    }

    public void setFullBlock(int x, int y, int z, int value) {
        if(value == ((-11 << 4) | 0) || value == ((-82 << 4) | 2) || value == ((-82 << 4) | 10)){
            Server.getInstance().getLogger().info("Уебан setFullBlock");
        }
        this.setFullBlock(getIndex(x, y, z), value);
    }

    public int getAndSetFullBlock(int x, int y, int z, int value) {
        return getAndSetFullBlock(getIndex(x, y, z), value);
    }

    private int getAndSetFullBlock(int index, int value) {
        //Preconditions.checkArgument(value < 0xfff, "Invalid full block");
        int oldBlock = blockIds[index];
        byte oldData = blockData.get(index);
        int newBlock = value >> 4;
        byte newData = (byte) (value & 0xf);

        if(newBlock == -11 || newBlock == -82){
            Server.getInstance().getLogger().info("Уебан getAndSetFullBlock "+newBlock+":"+newData+((newBlock << 4) | newData));
        }

        if (oldBlock != newBlock) {
            blockIds[index] = newBlock;
        }
        if (oldData != newData) {
            blockData.set(index, newData);
        }
        return (oldBlock << 4) | oldData;
    }

    private int getFullBlock(int index) {
        int block = blockIds[index];
        byte data = blockData.get(index);
        if(block == -11 || block == -82){
            Server.getInstance().getLogger().info(block+":"+data+":"+((block << 4) | data));
        }
        return (block << 4) | data;
    }

    private void setFullBlock(int index, int value) {
        //Preconditions.checkArgument(value < 0xfff, "Invalid full block");
        int block = (value >> 4);
        byte data = (byte) (value & 0xf);

        if(block == -11 || block == -82){
            Server.getInstance().getLogger().info("Уебан setFullBlock " + block+":"+data+((block << 4) | data));
        }

        blockIds[index] = block;
        blockData.set(index, data);
    }

    public int[] getBlockIds() {
        return Arrays.copyOf(blockIds, blockIds.length);
    }

    public byte[] getBlockData() {
        return blockData.getData();
    }

    public void writeTo(BinaryStream stream) {
        PalettedBlockStorage storage = new PalettedBlockStorage();
        for (int i = 0; i < SECTION_SIZE; i++) {
            storage.setBlock(i, GlobalBlockPalette.getOrCreateRuntimeId(blockIds[i], blockData.get(i)));
        }
        storage.writeTo(stream);
    }

    public BlockStorage copy() {
        return new BlockStorage(blockIds.clone(), blockData.copy());
    }
}