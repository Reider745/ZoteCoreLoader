package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class GameDifficulty {
    public static final int PEACEFUL = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_difficulty", "peaceful"));
    public static final int EASY = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_difficulty", "easy"));
    public static final int NORMAL = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_difficulty", "normal"));
    public static final int HARD = GameEnums.getInt(GameEnums.getSingleton().getEnum("game_difficulty", "hard"));
}
