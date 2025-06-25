package com.reider745.block.wall;

public class BlockPolishedBlackstoneWall extends BlockWall {
    public BlockPolishedBlackstoneWall() {
        this(0);
    }

    public BlockPolishedBlackstoneWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.POLISHED_BLACKSTONE;
    }
}
