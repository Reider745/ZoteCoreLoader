package com.reider745.hooks.network;

import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.network.protocol.CraftingDataPacket;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.ShadowGetField;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;

import java.util.List;

@Hooks(className = "cn.nukkit.network.protocol.CraftingDataPacket")
public class CraftingDataPacketHooks {
    @ShadowGetField(field = "entries")
    public static List<Recipe> getEntries(CraftingDataPacket self) {return null;}

    @Inject
    public static void addShapelessRecipe(CraftingDataPacket self, ShapelessRecipe[] recipes) {
        final List<Recipe> entries = getEntries(self);

        for(ShapelessRecipe recipe : recipes)
            if(!NativeWorkbench.canRemoveRecipe(recipe.getId()))
                entries.add(recipe);
    }

    @Inject
    public static void addShapedRecipe(CraftingDataPacket self, ShapedRecipe[] recipes) {
        final List<Recipe> entries = getEntries(self);

        for(ShapedRecipe recipe : recipes)
            if(!NativeWorkbench.canRemoveRecipe(recipe.getId()))
                entries.add(recipe);
    }
}
