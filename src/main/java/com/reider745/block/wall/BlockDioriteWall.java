package com.reider745.block.wall;

public class BlockDioriteWall extends BlockWall {
    public BlockDioriteWall() {
        this(0);
    }

    public BlockDioriteWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.DIORITE;
    }
}
