package com.reider745.block.wall;

public class BlockRedNetherBrickWall extends BlockWall {
    public BlockRedNetherBrickWall() {
        this(0);
    }

    public BlockRedNetherBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.RED_NETHER_BRICK;
    }
}
