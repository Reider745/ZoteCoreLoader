package com.zhekasmirnov.apparatus.mcpe;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;

public class NativeWorkbench {
    private static native void addShapedRecipe(int resultId, int resultCount, int resultData, long resultExtraPtr, String[] pattern, int[] ingredients);

    public static void addShapedRecipe(ItemStack result, String[] pattern, int[] ingredients) {
        addShapedRecipe(result.id, result.count, result.data, result.getExtraPtr(), pattern, ingredients);
    }

    private static native void addShapelessRecipe(int resultId, int resultCount, int resultData, long resultExtraPtr, int[] ingredients);

    public static void addShapelessRecipe(ItemStack result, int[] ingredients) {
        addShapelessRecipe(result.id, result.count, result.data, result.getExtraPtr(), ingredients);
    }

    public static native void removeRecipeByResult(int resultId, int resultCount, int resultData);
}
