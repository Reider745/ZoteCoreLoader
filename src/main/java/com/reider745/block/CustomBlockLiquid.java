package com.reider745.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.block.BlockWater;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import com.reider745.api.CustomManager;
import com.zhekasmirnov.innercore.api.NativeCallback;
import org.jetbrains.annotations.NotNull;

public class CustomBlockLiquid extends BlockLiquid implements RandomTick {
    private CustomManager manager;
    private int id, tick_rate;
    private String name;

    private boolean TickingTile;
    private boolean NeighbourChange, isRenewable;

    protected CustomBlockLiquid(int id, int meta){
        this(id, meta, CustomBlock.getBlockManager(id));
    }

    protected CustomBlockLiquid(int id, int meta, CustomManager manager) {
        this(id, meta, manager, "blank");
    }

    protected CustomBlockLiquid(int id, int meta, CustomManager manager, String name) {
        super(meta);

        this.manager = manager;
        this.id = id;
        this.name = name;

        TickingTile = manager.get("TickingTile:"+meta, false);
        NeighbourChange = manager.get("NeighbourChange", false);
        tick_rate = manager.get("tick_rate", 5);
        isRenewable = manager.get("isRenewable", false);
    }

    @Override
    public int tickRate() {
        return tick_rate;
    }

    @Override
    public boolean usesWaterLogging() {
        return false;
    }

    @Override
    public int getToolTier() {
        return 0;
    }

    @Override
    public boolean canRandomTickBlocks() {
        return TickingTile;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{Item.get(id, this.getDamage(), 1)};
    }


    @Override
    public int onUpdate(int type) {
        return super.onUpdate(type);
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    public Block clone() {
        CustomBlockLiquid customBlock = (CustomBlockLiquid) super.clone();
        customBlock.id = id;
        customBlock.name = name;
        customBlock.manager = manager;
        customBlock.setDamage(getDamage());
        return customBlock;
    }

    @Override
    public BlockLiquid getBlock(int meta) {
        return (BlockLiquid) Block.get(id, meta);
    }
}