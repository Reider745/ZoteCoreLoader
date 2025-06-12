package com.reider745.block.wall;

public class BlockPrismarineWall extends BlockWall {
    public BlockPrismarineWall() {
        this(0);
    }

    public BlockPrismarineWall(int meta) {
        super(meta);
    }

    @Override
    public WallType getWallType() {
        return WallType.PRISMARINE;
    }
}
