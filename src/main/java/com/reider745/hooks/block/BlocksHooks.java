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

    public static Boolean isSolid(Block self) {
        return (Boolean) BlockMethods.getOrDefault(self.getId(), BlockMethods.SOLID, null);
    }

    public static Double getHardness(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.DESTROY_TIME, null);
    }

    public static Double getResistance(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.EXPLOSION_RESISTANCE, null);
    }

    public static Double getFrictionFactor(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.FRICTION, null);
    }

    public static Integer getLightLevel(Block self) {
        return (Integer) BlockMethods.getOrDefault(self.getId(), BlockMethods.LIGHT_LEVEL, null);
    }
}
