package com.reider745.hooks.block;

import cn.nukkit.block.Block;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.block.BlockMethods;

@Hooks(className = "cn.nukkit.block.Block")
public class BlocksHooks {
    @Inject
    public static void init() {
        ReflectHelper.setField(Block.class, "MAX_BLOCK_ID", 16000);
        // ReflectHelper.setField(Block.class, "usesFakeWater", new boolean[MAX_ID]);
    }

    // TODO: не поддерживается переопределение свойств накита

    /*@Inject
    public static boolean isSolid(Block self) {
        return BlockMethods.isSolid(self.getId());
    }

    @Inject
    public static double getHardness(Block self) {
        return BlockMethods.getDestroyTime(self.getId());
    }

    @Inject
    public static double getResistance(Block self) {
        return BlockMethods.getExplosionResistance(self.getId());
    }

    @Inject
    public static double getFrictionFactor(Block self) {
        return BlockMethods.getFriction(self.getId());
    }

    @Inject
    public static int getLightLevel(Block self) {
        return BlockMethods.getLightLevel(self.getId());
    }*/
}
