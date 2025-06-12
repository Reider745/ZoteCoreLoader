package com.reider745.block.wall;

public class BlockMossyCobblestoneWall extends BlockWall {
    public BlockMossyCobblestoneWall() {
        this(0);
    }

    public BlockMossyCobblestoneWall(int meta) {
        super(meta);
    }

    @Override
    public BlockWall.WallType getWallType() {
        return BlockWall.WallType.MOSSY_COBBLESTONE;
    }
}
