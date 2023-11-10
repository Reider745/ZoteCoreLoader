package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.reider745.api.hooks.HookClassLoader;
import com.reider745.api.hooks.JarFileLoader;
import com.reider745.hooks.*;
import com.reider745.network.InnerCorePacket;
import com.reider745.network.ItemStackRequestPacket;

import java.io.File;

public class Main {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();

    public static class LoadingStages {
        public static void registerPacket(Network network){
            network.registerPacket(InnerCorePacket.NETWORK_ID, InnerCorePacket.class);
            network.registerPacket(ProtocolInfo.ITEM_STACK_REQUEST_PACKET, ItemStackRequestPacket.class);
        }

        public static void start(Server server) throws Exception {
            innerCoreServer.preLoad(server);
        }
    }

    public static void main(String[] args) throws Throwable {
        String jarname = "Nukkit-MOT-SNAPSHOT.jar";

        for(String arg : args)
            if(arg.startsWith("PATH_ASSETS="))
                HookClassLoader.pathAssets = System.getProperty("user.dir") + "/" + arg.replace("PATH_ASSETS=", "");
            else if(arg.startsWith("JAR_NAME="))
                jarname = arg.replace("JAR_NAME=", "");


        JarFileLoader loader = new JarFileLoader(new File(jarname));

        loader.registerHooksForClass(NetworkHooks.class);
        loader.registerHooksForClass(GlobalBlockPalette.class);
        loader.registerHooksForClass(LoadingStage.class);
        loader.registerHooksForClass(Other.class);
        loader.registerHooksForClass(PlayerHooks.class);


        loader.run("cn.nukkit.Nukkit", args);


    }
}