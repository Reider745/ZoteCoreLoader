package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class ItemCategory {
    // old
    public static final int DECORATION = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("item_category", "decoration"));
    public static final int FOOD = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "food"));
    public static final int INTERNAL = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "internal"));
    public static final int MATERIAL = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "material"));
    public static final int TOOL = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "tool"));

    // new
    public static final int CONSTRUCTION = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("item_category", "construction"));
    public static final int NATURE = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "nature"));
    public static final int ITEMS = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_category", "items"));
    public static final int EQUIPMENT = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("item_category", "equipment"));
    public static final int COMMAND_ONLY = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("item_category", "command_only"));

    private ItemCategory() {
    }
}
