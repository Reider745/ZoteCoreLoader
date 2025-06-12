package com.reider745.block.wall;

public class BlockMossyStoneBrickWall extends BlockWall {
    public BlockMossyStoneBrickWall() {
        this(0);
    }

    public BlockMossyStoneBrickWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.MOSSY_STONE_BRICK;
    }
}
