package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.modpack.strategy.extract.*;
import com.zhekasmirnov.innercore.modpack.strategy.request.ConfigDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.ConfigInModDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DefaultDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.CacheDirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.DirectoryDeniedUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.JsonMergeDirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.ResourceDirectoryUpdateStrategy;

import java.io.File;

public class ModPackFactory {
    private final static ModPackFactory instance = new ModPackFactory();

    public static ModPackFactory getInstance() {
        return instance;
    }

    private ModPackFactory() {
    }

    public ModPack createFromDirectory(File root) {
        return new ModPack(root)
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.MODS, new File(root, "mods"), "mods",
                        new DefaultDirectoryRequestStrategy(), new ResourceDirectoryUpdateStrategy(),
                        new AllFilesDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.MOD_ASSETS,
                        new File(root, "mod_assets"), "mod_assets", new DefaultDirectoryRequestStrategy(),
                        new ResourceDirectoryUpdateStrategy(), new AllFilesDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.CONFIG, new File(root, "config"),
                        "config", new ConfigDirectoryRequestStrategy(), new JsonMergeDirectoryUpdateStrategy(),
                        new AllFilesDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.CACHE, new File(root, "cache"),
                        "cache", new DefaultDirectoryRequestStrategy(), new CacheDirectoryUpdateStrategy(),
                        new AllIgnoredDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.RESOURCE_PACKS,
                        new File(root, "resource_packs"), "resource_packs", new DefaultDirectoryRequestStrategy(),
                        new ResourceDirectoryUpdateStrategy(), new AllFilesDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.BEHAVIOR_PACKS,
                        new File(root, "behavior_packs"), "behavior_packs", new DefaultDirectoryRequestStrategy(),
                        new ResourceDirectoryUpdateStrategy(), new AllFilesDirectoryExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.TEXTURE_PACKS,
                        new File(root, "texture_packs"), "texture_packs", new DefaultDirectoryRequestStrategy(),
                        new ResourceDirectoryUpdateStrategy(), new AllFilesDirectoryExtractStrategy()));
    }

    /*
     * default structure:
     * innercore/ <- root
     * mods/ <- mods with configs
     * mod_assets/ <- new modpack-related directory
     * cache/
     * resource_packs/
     * behavior_packs/
     * resourcepacks/ <- texture packs outside directory
     */
    public ModPack createDefault(File root) {
        return new ModPack(root)
                .addDirectory(newDefaultPackDirectory(ModPackDirectory.DirectoryType.MODS, root, "mods",
                        new DefaultDirectoryRequestStrategy(), new ModsWithoutConfigExtractStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.CONFIG, new File(root, "mods"),
                        "config", new ConfigInModDirectoryRequestStrategy(), new DirectoryDeniedUpdateStrategy(),
                        new ConfigFromModsExtractStrategy()))
                .addDirectory(newDefaultPackDirectory(ModPackDirectory.DirectoryType.MOD_ASSETS, root, "mod_assets",
                        new DefaultDirectoryRequestStrategy()))
                .addDirectory(newDefaultPackDirectory(ModPackDirectory.DirectoryType.CACHE, root, "cache",
                        new DefaultDirectoryRequestStrategy(), new AllIgnoredDirectoryExtractStrategy()))
                .addDirectory(newDefaultPackDirectory(ModPackDirectory.DirectoryType.RESOURCE_PACKS, root,
                        "resource_packs", new DefaultDirectoryRequestStrategy()))
                .addDirectory(newDefaultPackDirectory(ModPackDirectory.DirectoryType.BEHAVIOR_PACKS, root,
                        "behavior_packs", new DefaultDirectoryRequestStrategy()))
                .addDirectory(new ModPackDirectory(ModPackDirectory.DirectoryType.TEXTURE_PACKS,
                        new File(root.getParentFile(), "resourcepacks"), "texture_packs",
                        new DefaultDirectoryRequestStrategy(), new DirectoryDeniedUpdateStrategy(),
                        new AllFilesDirectoryExtractStrategy()));
    }

    private ModPackDirectory newDefaultPackDirectory(ModPackDirectory.DirectoryType type, File root, String name,
            DirectoryRequestStrategy requestStrategy, DirectoryExtractStrategy extractStrategy) {
        return new ModPackDirectory(type, new File(root, name), name, requestStrategy,
                new DirectoryDeniedUpdateStrategy(), extractStrategy);
    }

    private ModPackDirectory newDefaultPackDirectory(ModPackDirectory.DirectoryType type, File root, String name,
            DirectoryRequestStrategy requestStrategy) {
        return newDefaultPackDirectory(type, root, name, requestStrategy, new AllFilesDirectoryExtractStrategy());
    }
}
