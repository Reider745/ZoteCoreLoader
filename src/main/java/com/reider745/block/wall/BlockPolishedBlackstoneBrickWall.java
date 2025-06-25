package com.reider745.block.wall;

public class BlockPolishedBlackstoneBrickWall extends BlockWall {
    public BlockPolishedBlackstoneBrickWall() {
        this(0);
    }

    public BlockPolishedBlackstoneBrickWall(int meta) {
        super(meta);
    }

    @Override
    public BlockWall.WallType getWallType() {
        return WallType.POLISHED_BLACKSTONE_BRICK;
    }
}
