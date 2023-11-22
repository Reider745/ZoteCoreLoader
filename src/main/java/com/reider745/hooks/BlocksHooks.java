package com.reider745.hooks;

import cn.nukkit.block.Block;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import javassist.*;

@Hooks(class_name = "cn.nukkit.block.Block")
public class BlocksHooks implements HookClass {
    private static final int MAX_ID = 16000;

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if(name.equals("MAX_BLOCK_ID") || name.equals("usesFakeWater"))
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
    }

    @Inject
    public static void init(){
        ReflectHelper.setField(Block.class, "MAX_BLOCK_ID", 16000);
        ReflectHelper.setField(Block.class, "usesFakeWater", new boolean[MAX_ID]);
    }
}
