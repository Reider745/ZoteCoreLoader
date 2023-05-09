package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class DimensionId {
    public static final int END = GameEnums.getInt(GameEnums.getSingleton().getEnum("dimension", "end"));
    public static final int NETHER = GameEnums.getInt(GameEnums.getSingleton().getEnum("dimension", "nether"));
    public static final int NORMAL = GameEnums.getInt(GameEnums.getSingleton().getEnum("dimension", "normal"));
    public static final int OVERWORLD = GameEnums.getInt(GameEnums.getSingleton().getEnum("dimension", "overworld"));
}
