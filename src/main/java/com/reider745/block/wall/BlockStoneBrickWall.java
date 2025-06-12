package com.reider745.block.wall;

public class BlockStoneBrickWall extends BlockWall {
    public BlockStoneBrickWall() {
        this(0);
    }

    public BlockStoneBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.STONE_BRICK;
    }
}
