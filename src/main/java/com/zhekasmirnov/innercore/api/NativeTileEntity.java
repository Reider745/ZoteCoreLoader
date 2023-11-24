package com.zhekasmirnov.innercore.api;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnableContainer;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeTileEntity {
    public static final boolean isContainer = false;

    private BlockEntity pointer;

    protected int type;
    protected int size;
    protected int x, y, z;

    public NativeTileEntity(BlockEntity ptr) {
        this.pointer = ptr;
        this.type = getType(ptr);
        this.size = getSize(ptr);
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public NativeItemInstance getSlot(int slot) {
        if (slot < 0 || slot >= size)
            return null;
        Item itemPtr = getSlot(pointer, slot);
        if (itemPtr == null)
            return null;
        return new NativeItemInstance(itemPtr);
    }

    public void setSlot(int slot, int id, int count, int data, Object extra) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(pointer, slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
    }

    public void setSlot(int slot, int id, int count, int data) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(pointer, slot, id, count, data, 0);
    }

    public void setSlot(int slot, NativeItemInstance item) {
        if (slot < 0 || slot >= size)
            return;
        setSlot2(pointer, slot, item.item);
    }

    public NativeCompoundTag getCompoundTag() {
        return new NativeCompoundTag(getCompoundTag(pointer));
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        if (tag != null) {
            setCompoundTag(pointer, tag.pointer);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        nativeFinalize(pointer);
    }

    public static NativeTileEntity getTileEntity(int x, int y, int z) {
        BlockEntity ptr = getInWorld(x, y, z);
        if (ptr == null) {
            return null;
        }
        else {
            return new NativeTileEntity(ptr);
        }
    }

    /*
     * native part
     */

    public static BlockEntity getInWorld(int x, int y, int z){
        InnerCoreServer.useNotSupport("getInWorld");
        return null;
    }

    public static void nativeFinalize(BlockEntity pointer){

    }

    public static int getType(BlockEntity pointer){
        String id = pointer.getName();

        switch (id){
            case BlockEntity.BED -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "bed"));
            }
            case BlockEntity.BARREL -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "barrel"));
            }
            case BlockEntity.BEACON -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "beacon"));
            }
            case BlockEntity.BELL -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "bell"));
            }
            case BlockEntity.CHEST, BlockEntity.SHULKER_BOX -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "chest"));
            }
            case BlockEntity.SIGN -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "sign"));
            }
            case BlockEntity.MOB_SPAWNER -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "mob_spawner"));
            }
            case BlockEntity.ENCHANT_TABLE -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "enchanting_table"));
            }
            case BlockEntity.SKULL  -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "skull"));
            }
            case BlockEntity.FLOWER_POT  -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "flower_pot"));
            }
            case BlockEntity.BREWING_STAND  -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "brewing_stand"));
            }
            case BlockEntity.DAYLIGHT_DETECTOR  -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "daylight_detector"));
            }
            case BlockEntity.MUSIC -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "music_block"));
            }
            case BlockEntity.ITEM_FRAME -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "item_frame"));
            }
            case BlockEntity.CAULDRON -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "cauldron"));
            }
            case BlockEntity.PISTON_ARM -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "piston"));
            }
            case BlockEntity.COMPARATOR -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "comparator"));
            }
            case BlockEntity.HOPPER -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "hopper"));
            }
            case BlockEntity.JUKEBOX -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "jukebox"));
            }
            case BlockEntity.LECTERN -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "lectern"));
            }
            case BlockEntity.CAMPFIRE -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "campfire"));
            }
            case BlockEntity.END_GATEWAY -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "end_gateway"));
            }
            default -> {
                return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "none"));
            }
        }
        //throw new RuntimeException("Not convert "+id);
    }

    public static int getSize(BlockEntity pointer){
        if(pointer instanceof BlockEntitySpawnableContainer blockEntity)
            return blockEntity.getSize();
        return 0;
    }

    public static Item getSlot(BlockEntity pointer, int slot){
        if(pointer instanceof BlockEntitySpawnableContainer blockEntity)
            return blockEntity.getInventory().getItem(slot);
        return null;
    }

    public static void setSlot(BlockEntity pointer, int slot, int id, int count, int data, long extra){
        if(pointer instanceof BlockEntitySpawnableContainer blockEntity)
            blockEntity.getInventory().setItem(slot, ItemUtils.get(id, count, data, extra));
    }

    public static void setSlot2(BlockEntity pointer, int slot, Item itemInstance){
        if(pointer instanceof BlockEntitySpawnableContainer blockEntity)
            blockEntity.getInventory().setItem(slot, itemInstance);
    }

    public static CompoundTag getCompoundTag(BlockEntity pointer){
        return pointer.namedTag;
    }

    public static void setCompoundTag(BlockEntity pointer, CompoundTag tag){
        pointer.namedTag = tag;
    }
}
