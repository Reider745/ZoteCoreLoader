package com.reider745.block.wall;

public class BlockNetherBrickWall extends BlockWall {
    public BlockNetherBrickWall() {
        this(0);
    }

    public BlockNetherBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.NETHER_BRICK;
    }
}
