package com.reider745.hooks.optimization;

import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

@Hooks(className = "cn.nukkit.level.GlobalBlockPalette")
public class Palette {
    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void init() {
    }
}
