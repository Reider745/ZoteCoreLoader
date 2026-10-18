package com.reider745.behavior;

import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPackSourcesTest {
    @TempDir Path root;

    @Test
    void preservesStandalonePacksAndLoadsModSourcesInPlace() throws Exception {
        Path standalone = root.resolve("server/behavior_packs/standalone");
        Path mod = root.resolve("mod/behavior");
        Files.createDirectories(standalone);
        Files.createDirectories(mod);
        Files.writeString(standalone.resolve("keep.txt"), "installed by server owner");
        Files.writeString(mod.resolve("manifest.json"), """
                {"format_version":2,"header":{"name":"Mod","uuid":"mod-pack","version":[1,0,0]}}
                """);
        String previousHorizon = FileTools.DIR_HORIZON;
        ModLoader previousLoader = ModLoader.instance;
        try {
            FileTools.DIR_HORIZON = root.resolve("server").toString();
            ModLoader loader = new ModLoader();
            ModLoader.instance = loader;
            ModLoader.addMinecraftBehaviorPack(mod.toFile());
            loader.loadResourceAndBehaviorPacks();
            assertEquals("installed by server owner", Files.readString(standalone.resolve("keep.txt")));
            assertTrue(loader.getBehaviorPackDirectories().contains(mod.toFile()));
            assertTrue(loader.getBehaviorPackDirectories().contains(standalone.toFile()));
            assertFalse(Files.exists(root.resolve("server/behavior_packs/behavior")));
        } finally {
            FileTools.DIR_HORIZON = previousHorizon;
            ModLoader.instance = previousLoader;
        }
    }
}
