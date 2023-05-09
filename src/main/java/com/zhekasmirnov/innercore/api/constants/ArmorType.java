package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class ArmorType {
    public static int boots = GameEnums.getInt(GameEnums.getSingleton().getEnum("armor_type", "boots"));
    public static int chestplate = GameEnums.getInt(GameEnums.getSingleton().getEnum("armor_type", "chestplate"));
    public static int helmet = GameEnums.getInt(GameEnums.getSingleton().getEnum("armor_type", "helmet"));
    public static int leggings = GameEnums.getInt(GameEnums.getSingleton().getEnum("armor_type", "leggings"));
}
