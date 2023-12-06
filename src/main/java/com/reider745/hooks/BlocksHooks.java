package com.reider745.hooks;

import cn.nukkit.block.Block;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.block.BlockMethods;
import javassist.*;

@Hooks(className = "cn.nukkit.block.Block")
public class BlocksHooks implements HookClass {
    private static final int MAX_ID = 16000;

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if (name.equals("MAX_BLOCK_ID") || name.equals("usesFakeWater"))
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
    }

    @Inject
    public static void init() {
        ReflectHelper.setField(Block.class, "MAX_BLOCK_ID", 16000);
        ReflectHelper.setField(Block.class, "usesFakeWater", new boolean[MAX_ID]);
    }

    @Inject
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
    }
}
