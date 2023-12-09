package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class PlayerAbility {
    public static final String INVULNERABLE = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "invulnerable"));
    public static final String FLYING = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "flying"));
    public static final String INSTABUILD = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "instabuild"));
    public static final String LIGHTNING = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "lightning"));
    public static final String FLYSPEED = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "flyspeed"));
    public static final String WALKSPEED = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "walkspeed"));
    public static final String NOCLIP = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "noclip"));
    public static final String MAYFLY = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "mayfly"));
    public static final String WORLDBUILDER = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "worldbuilder"));
    public static final String MUTED = GameEnums.getString(GameEnums.getSingleton().getEnum("player_ability", "muted"));
    public static final String BUILD = GameEnums.getString(GameEnums.getSingleton().getEnum("player_ability", "build"));
    public static final String MINE = GameEnums.getString(GameEnums.getSingleton().getEnum("player_ability", "mine"));
    public static final String DOORS_AND_SWITCHES = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "doors_and_switches"));
    public static final String OPEN_CONTAINERS = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "open_containers"));
    public static final String ATTACK_PLAYERS = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "attack_players"));
    public static final String ATTACK_MOBS = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "attack_mobs"));
    public static final String OPERATOR_COMMANDS = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "operator_commands"));
    // public static final String INVISIBLE =
    // GameEnums.getString(GameEnums.getSingleton().getEnum("player_ability",
    // "invisible")); // crash
    public static final String TELEPORT = GameEnums
            .getString(GameEnums.getSingleton().getEnum("player_ability", "teleport"));
}
