package com.reider745.block.wall;

public class BlockAndesiteWall extends BlockWall {
    public BlockAndesiteWall() {
        this(0);
    }

    public BlockAndesiteWall(int meta) {
        super(meta);
    }

    @Override
    public BlockWall.WallType getWallType() {
        return BlockWall.WallType.ANDESITE;
    }
}
