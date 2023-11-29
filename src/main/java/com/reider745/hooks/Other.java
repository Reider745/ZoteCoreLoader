package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import com.reider745.api.hooks.ArgumentTypes;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.workbench.Workbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Hooks
public class Other implements HookClass {
    @Inject(class_name = "org.mozilla.javascript.ScriptRuntime")
    public static String[] getTopPackageNames(){
        return new String[] {"java", "javax", "org", "com", "edu", "net", "android"};
    }
}
