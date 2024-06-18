package com.reider745.hooks.bugfix;

import cn.nukkit.block.BlockSapling;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

@Hooks(className = "cn.nukkit.block.BlockSapling")
public class SaplingFix implements HookClass {
    @Inject
    public static boolean isAged(BlockSapling self) {
        return true;
    }
}
