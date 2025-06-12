package com.reider745.block.wall;

public class BlockBrickWall extends BlockWall {
    public BlockBrickWall() {
        this(0);
    }

    public BlockBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.BRICK;
    }
}
