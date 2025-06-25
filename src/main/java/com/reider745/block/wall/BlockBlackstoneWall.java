package com.reider745.block.wall;

public class BlockBlackstoneWall extends BlockWall {
    public BlockBlackstoneWall() {
        this(0);
    }

    public BlockBlackstoneWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.BLACKSTONE;
    }
}
