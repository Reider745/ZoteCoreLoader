package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class TileEntityType {
    public static int NONE = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "none"));
    public static int BEACON = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "beacon"));
    public static int BREWING_STAND = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "brewing_stand"));
    public static int CAULDRON = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "cauldron"));
    public static int CHEST = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "chest"));
    public static int DISPENSER = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "dispenser"));
    public static int FURNACE = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "furnace"));
    public static int HOPPER = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "hopper"));
    public static int JUKEBOX = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "jukebox"));
    public static int LECTERN = GameEnums.getInt(GameEnums.getSingleton().getEnum("tile_entity_type", "lectern"));
}
