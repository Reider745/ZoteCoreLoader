package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class GameMode {
    public static final int SURVIVAL = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_mode", "survival"));
    public static final int CREATIVE = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_mode", "creative"));
    public static final int ADVENTURE = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_mode", "adventure"));
    public static final int SPECTATOR = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_mode", "spectator"));
}
