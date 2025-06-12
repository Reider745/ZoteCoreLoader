package com.reider745.block.wall;

public class BlockRedSandstoneWall extends BlockWall {
    public BlockRedSandstoneWall() {
        this(0);
    }

    public BlockRedSandstoneWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.RED_SANDSTONE;
    }
}
