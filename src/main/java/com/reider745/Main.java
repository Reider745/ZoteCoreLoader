package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.network.Network;

import java.util.Arrays;

import com.reider745.api.hooks.JarEditor;
import com.reider745.event.DispenseBehaviorOverrides;
import com.reider745.hooks.*;
import com.reider745.hooks.bugfix.DimensionsFix;
import com.reider745.hooks.bugfix.EntityItemHooks;
import com.reider745.network.CraftingTransactionPacket;
import com.reider745.network.InnerCorePacket;

public class Main {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();

    public static class LoadingStages {
        public static void registerPacket(Network network) {
            network.registerPacket(InnerCorePacket.NETWORK_ID, InnerCorePacket.class);
        }

        public static void preload(Server server) throws Exception {
            innerCoreServer.preload(server);
        }

        public static void afterload(Server server) {
            innerCoreServer.afterload();
        }

        public static void start(Server server) {
            innerCoreServer.start();
        }

        public static void stop(Server server) throws Exception {
            try {
                innerCoreServer.left();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        JarEditor loader = new JarEditor();

        loader.registerHooksInitializationForClass(NetworkHooks.class);
        loader.registerHooksInitializationForClass(GlobalBlockPalette.class);
        loader.registerHooksInitializationForClass(ServerHooks.class);
        loader.registerHooksInitializationForClass(CraftingTransactionPacket.class);
        loader.registerHooksInitializationForClass(RhinoOverrides.class);
        loader.registerHooksInitializationForClass(PlayerHooks.class);
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

         loader.registerHooksInitializationForClass(BiomesHooks.class);
        loader.registerHooksInitializationForClass(GlobalBanList.class);

        // bug fix
        loader.registerHooksInitializationForClass(EntityItemHooks.class);
        loader.registerHooksInitializationForClass(DimensionsFix.class);

        loader.init();
        loader.run("cn.nukkit.Nukkit", args);
    }
}
