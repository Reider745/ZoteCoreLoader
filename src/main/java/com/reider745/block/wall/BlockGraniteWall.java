package com.reider745.block.wall;

public class BlockGraniteWall extends BlockWall {
    public BlockGraniteWall() {
        this(0);
    }

    public BlockGraniteWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.GRANITE;
    }
}
