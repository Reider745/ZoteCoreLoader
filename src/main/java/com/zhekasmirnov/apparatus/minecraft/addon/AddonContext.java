package com.zhekasmirnov.apparatus.minecraft.addon;

import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;

/* This class should allow access to all addon contents */
public class AddonContext {
    private static final AddonContext instance = MinecraftVersions.getCurrent().createAddonContext();

    public static AddonContext getInstance() {
        return instance;
    }


    private final MinecraftVersion version;
    private final AddonRecipeParser recipeParser;

    public AddonContext(MinecraftVersion version, AddonRecipeParser recipeParser) {
        this.version = version;
        this.recipeParser = recipeParser;
    }

    public MinecraftVersion getVersion() {
        return version;
    }

    public AddonRecipeParser getRecipeParser() {
        return recipeParser;
    }
}
