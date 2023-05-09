package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class UseAnimation {
    public static int bow = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_animation", "bow"));
    public static int normal = GameEnums.getInt(GameEnums.getSingleton().getEnum("item_animation", "normal"));
}
