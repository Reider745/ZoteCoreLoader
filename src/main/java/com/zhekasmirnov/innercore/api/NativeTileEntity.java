package com.zhekasmirnov.innercore.api;

import java.util.Map;
import java.util.HashMap;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.reider745.world.BlockSourceMethods;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityContainer;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeTileEntity {
    public static final boolean isContainer = false;

    private BlockEntity blockEntity;

    protected int type;
    protected int size;
    protected int x, y, z;

    @Deprecated(since = "Zote")
    public NativeTileEntity(long ptr) {
        throw new UnsupportedOperationException("NativeTileEntity(ptr)");
    }

    public NativeTileEntity(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.type = getType(blockEntity);
        this.size = getSize(blockEntity);
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
        Item item = getSlot(blockEntity, slot);
        return item != null ? new NativeItemInstance(item) : null;
    }

    public void setSlot(int slot, int id, int count, int data, Object extra) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(blockEntity, slot, id, count, data, NativeItemInstanceExtra.unwrapObject(extra));
    }

    public void setSlot(int slot, int id, int count, int data) {
        if (slot < 0 || slot >= size)
            return;
        setSlot(blockEntity, slot, id, count, data, (NativeItemInstanceExtra) null);
    }

    public void setSlot(int slot, NativeItemInstance item) {
        if (slot < 0 || slot >= size)
            return;
        setSlot2(blockEntity, slot, item.getPointer());
    }

    public NativeCompoundTag getCompoundTag() {
        return new NativeCompoundTag(getCompoundTag(blockEntity));
    }

    public void setCompoundTag(NativeCompoundTag tag) {
        if (tag != null) {
            setCompoundTag(blockEntity, tag.tag);
        }
    }

    @Deprecated(since = "Zote")
    public static NativeTileEntity getTileEntity(int x, int y, int z) {
        return NativeBlockSource.getCurrentWorldGenRegion().getBlockEntity(x, y, z);
    }

    /*
     * native part
     */

    private static final Map<String, String> nukkitToEnumMappings;

    static {
        nukkitToEnumMappings = new HashMap<>(); // Keep in Nukkit order please
        nukkitToEnumMappings.put(BlockEntity.CHEST, "chest");
        nukkitToEnumMappings.put(BlockEntity.ENDER_CHEST, "ender_chest");
        nukkitToEnumMappings.put(BlockEntity.FURNACE, "furnace");
        nukkitToEnumMappings.put(BlockEntity.SIGN, "sign");
        nukkitToEnumMappings.put(BlockEntity.MOB_SPAWNER, "mob_spawner");
        nukkitToEnumMappings.put(BlockEntity.ENCHANT_TABLE, "enchant_table");
        nukkitToEnumMappings.put(BlockEntity.SKULL, "skull");
        nukkitToEnumMappings.put(BlockEntity.FLOWER_POT, "flower_pot");
        nukkitToEnumMappings.put(BlockEntity.BREWING_STAND, "brewing_stand");
        nukkitToEnumMappings.put(BlockEntity.DAYLIGHT_DETECTOR, "daylight_detector");
        nukkitToEnumMappings.put(BlockEntity.MUSIC, "music");
        nukkitToEnumMappings.put(BlockEntity.ITEM_FRAME, "item_frame");
        nukkitToEnumMappings.put(BlockEntity.CAULDRON, "cauldron");
        nukkitToEnumMappings.put(BlockEntity.BEACON, "beacon");
        nukkitToEnumMappings.put(BlockEntity.PISTON_ARM, "piston_arm");
        nukkitToEnumMappings.put(BlockEntity.MOVING_BLOCK, "moving_block");
        nukkitToEnumMappings.put(BlockEntity.COMPARATOR, "comparator");
        nukkitToEnumMappings.put(BlockEntity.HOPPER, "hopper");
        nukkitToEnumMappings.put(BlockEntity.BED, "bed");
        nukkitToEnumMappings.put(BlockEntity.JUKEBOX, "jukebox");
        nukkitToEnumMappings.put(BlockEntity.SHULKER_BOX, "shulker_box");
        nukkitToEnumMappings.put(BlockEntity.BANNER, "banner");
        nukkitToEnumMappings.put(BlockEntity.LECTERN, "lectern");
        nukkitToEnumMappings.put(BlockEntity.BEEHIVE, "beehive");
        nukkitToEnumMappings.put(BlockEntity.DROPPER, "dropper");
        nukkitToEnumMappings.put(BlockEntity.DISPENSER, "dispenser");
        nukkitToEnumMappings.put(BlockEntity.BARREL, "barrel");
        nukkitToEnumMappings.put(BlockEntity.CAMPFIRE, "campfire");
        nukkitToEnumMappings.put(BlockEntity.BELL, "bell");
        nukkitToEnumMappings.put(BlockEntity.END_GATEWAY, "end_gateway");
    }

    public static int getType(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "none"));
        }
        String tileEntityName = nukkitToEnumMappings.getOrDefault(blockEntity.getName(), null);
        if (tileEntityName == null) {
            Logger.error("NativeTileEntity",
                    "Could not locate tile " + blockEntity.getClass().getSimpleName() + ", it was not implemented");
            tileEntityName = "none";
        }
        return GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", tileEntityName));
    }

    public static int getSize(BlockEntity blockEntity) {
        if (blockEntity instanceof InventoryHolder holder) {
            return holder.getInventory().getSize();
        } else if (blockEntity instanceof BlockEntityContainer container) {
            return container.getSize();
        }
        return 0;
    }

    public static Item getSlot(BlockEntity blockEntity, int slot) {
        if (blockEntity instanceof InventoryHolder holder) {
            return holder.getInventory().getItem(slot);
        } else if (blockEntity instanceof BlockEntityContainer container) {
            return container.getItem(slot);
        }
        return null;
    }

    public static void setSlot(BlockEntity blockEntity, int slot, int id, int count, int data, Item extra) {
        setSlot(blockEntity, slot, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setSlot(BlockEntity blockEntity, int slot, int id, int count, int data, NativeItemInstanceExtra extra) {
        Item item = ItemUtils.get(id, count, data, extra);
        if (blockEntity instanceof InventoryHolder holder) {
            holder.getInventory().setItem(slot, item);
        } else if (blockEntity instanceof BlockEntityContainer container) {
            container.setItem(slot, item);
        }
    }

    public static void setSlot2(BlockEntity blockEntity, int slot, Item itemInstance) {
        if (blockEntity instanceof InventoryHolder holder) {
            holder.getInventory().setItem(slot, itemInstance);
        } else if (blockEntity instanceof BlockEntityContainer container) {
            container.setItem(slot, itemInstance);
        }
    }

    public static CompoundTag getCompoundTag(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return null;
        }
        blockEntity.saveNBT();
        return blockEntity.namedTag;
    }

    public static void setCompoundTag(BlockEntity blockEntity, CompoundTag tag) {
        if (blockEntity == null) {
            return;
        }
        blockEntity.x = (double) tag.getInt("x");
        blockEntity.y = (double) tag.getInt("y");
        blockEntity.z = (double) tag.getInt("z");
        blockEntity.movable = tag.getBoolean("isMovable", true);
        blockEntity.namedTag = tag;
    }

    @Deprecated(since = "Zote")
    public static BlockEntity getInWorld(int x, int y, int z) {
        return BlockSourceMethods.getBlockEntity(NativeBlockSource.getCurrentWorldGenRegion().getPointer(), x, y, z);
    }

    @Deprecated(since = "Zote")
    public static void nativeFinalize(BlockEntity pointer) {
        InnerCoreServer.useNotSupport("NativeTileEntity.nativeFinalize(pointer)");
    }

    @Deprecated(since = "Zote")
    public static void setSlot(BlockEntity pointer, int slot, int id, int count, int data, long extra) {
        InnerCoreServer.useNotSupport("NativeTileEntity.setSlot(pointer, slot, id, count, data, extra)");
    }

    @Deprecated(since = "Zote")
    public static void setSlot2(BlockEntity pointer, int slot, long itemInstance) {
        InnerCoreServer.useNotSupport("NativeTileEntity.setSlot2(pointer, slot, itemInstance)");
    }

    @Deprecated(since = "Zote")
    public static void setCompoundTag(BlockEntity pointer, long tag) {
        InnerCoreServer.useNotSupport("NativeTileEntity.setCompoundTag(pointer, tag)");
    }
}
