package com.reider745.block.wall;

public class BlockSandstoneWall extends BlockWall {
    public BlockSandstoneWall() {
        this(0);
    }

    public BlockSandstoneWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.SANDSTONE;
    }
}
