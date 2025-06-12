package com.reider745.block.wall;

public class BlockCobblestoneWall extends BlockWall {
    public BlockCobblestoneWall() {
        this(0);
    }

    public BlockCobblestoneWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.COBBLESTONE;
    }
}
