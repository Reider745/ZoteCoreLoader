package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class BlockFace {
    public static final int DOWN = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "down"));
    public static final int EAST = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "east"));
    public static final int NORTH = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "north"));
    public static final int SOUTH = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "south"));
    public static final int UP = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "up"));
    public static final int WEST = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_side", "west"));
}
