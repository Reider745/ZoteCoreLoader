package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.network.Network;

import java.util.Arrays;

import com.reider745.api.hooks.JarEditor;
import com.reider745.event.DispenseBehaviorOverrides;
import com.reider745.hooks.*;
import com.reider745.hooks.bugfix.SaplingFix;
import com.reider745.network.CraftingTransactionPacket;
import com.reider745.network.InnerCorePacket;

public class Main {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();
    public static boolean block_not_vanilla = false;

    public static class LoadingStages {
        public static void preload(Server server) throws Exception {
            innerCoreServer.preload(server);
        }

        public static void afterload(Server server) {
            innerCoreServer.afterload();
        }

        public static void start(Server server) {
            innerCoreServer.start();
        }
    }

    public static void main(String[] args) throws Throwable {
        // Отключает блокировку входа с ванильного майнкрафта, полезно при разработки фич не связанные с inner core
        // Внимание ломает вход с inner core!
        block_not_vanilla = Arrays.stream(args).anyMatch("--vanilla_not_block"::equals);
        JarEditor loader = new JarEditor();

        if(!block_not_vanilla) {
            loader.registerHooksInitializationForClass(GlobalBlockPalette.class);
            loader.registerHooksInitializationForClass(NetworkHooks.class);
            loader.registerHooksInitializationForClass(PlayerHooks.class);
        }

        loader.registerHooksInitializationForClass(ServerHooks.class);
        loader.registerHooksInitializationForClass(CraftingTransactionPacket.class);
        loader.registerHooksInitializationForClass(RhinoOverrides.class);

        loader.registerHooksInitializationForClass(RuntimeItemsHooks.class);
        loader.registerHooksInitializationForClass(BlocksHooks.class);
        loader.registerHooksInitializationForClass(LevelHooks.class);
        loader.registerHooksInitializationForClass(ItemUtils.class);
        loader.registerHooksInitializationForClass(EntityOverrides.class);
        loader.registerHooksInitializationForClass(DispenseBehaviorOverrides.class);
        loader.registerHooksInitializationForClass(AndroidHooks.class);

        if (Arrays.stream(args).anyMatch("--snowfall-everywhere"::equals)) {
            loader.registerHooksInitializationForClass(SnowfallEverywhere.class);
        }

        //loader.registerHooksInitializationForClass(BiomesHooks.class);
        loader.registerHooksInitializationForClass(GlobalBanList.class);

        // bug fix
        loader.registerHooksInitializationForClass(SaplingFix.class);

        loader.init(Arrays.stream(args).anyMatch("--debug-hooks"::equals));
        loader.run("cn.nukkit.Nukkit", args);
    }
}
