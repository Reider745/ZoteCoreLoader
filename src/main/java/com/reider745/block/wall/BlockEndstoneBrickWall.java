package com.reider745.block.wall;

public class BlockEndstoneBrickWall extends BlockWall {
    public BlockEndstoneBrickWall() {
        this(0);
    }

    public BlockEndstoneBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.END_BRICK;
    }
}
