package com.reider745.hooks;

import cn.nukkit.block.Block;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.world.BiomesMethods;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

@Hooks(className = "cn.nukkit.level.biome.Biome")
public class BiomesHooks implements HookClass {
    private static final int MAX_ID = 512;

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if (name.equals("biomes")) {
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);

        }
    }

    public static void init() {
        ReflectHelper.setField(Biome.class, "biomes", new Biome[MAX_ID]);
    }

    @Inject(className = "cn.nukkit.network.protocol.BiomeDefinitionListPacket", type = TypeHook.BEFORE_REPLACE)
    public static void encode(BiomeDefinitionListPacket self){
        BiomesMethods.encode(self);
    }
}
