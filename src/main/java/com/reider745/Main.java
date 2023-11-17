package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.network.Network;
import com.reider745.api.hooks.JarEditor;
import com.reider745.hooks.*;
import com.reider745.network.InnerCorePacket;

import java.io.File;

public class Main {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();

    public static class LoadingStages {
        public static void registerPacket(Network network){
            network.registerPacket(InnerCorePacket.NETWORK_ID, InnerCorePacket.class);
        }

        public static void start(Server server) throws Exception {
            innerCoreServer.preLoad(server);
        }
    }

    /*@Hooks(class_name = "org.example.Perlin2D")
    public static class TestHook {
        @Inject(controller = false)
        public static float lerp(Object perlin, float a, float b, float t){
            return a + (b - a) * t;
            *//*Arguments arguments = controller.getArguments();
            float a = arguments.arg("a");
            float b = arguments.arg("b");
            float t = arguments.arg("t");*//*
        }
        *//*@Inject(arguments_map = true)
        public static float lerp(HookController controller){
            Arguments arguments = controller.getArguments();
            float a = arguments.arg("a");
            float b = arguments.arg("b");
            float t = arguments.arg("t");
            return a + (b - a) * t;
        }*//*
    }*/

    public static void main(String[] args) throws Throwable {
        /*JarFileLoader loader = new JarFileLoader("C:\\vs\\untitled1\\build\\libs\\untitled1-1.0-SNAPSHOT.jar");
        loader.registerHooksForClass(TestHook.class);
        loader.run("org.example.Main", args);*/

        JarEditor loader = new JarEditor();

        loader.registerHooksForClass(NetworkHooks.class);
        loader.registerHooksForClass(GlobalBlockPalette.class);
        loader.registerHooksForClass(ServerHooks.class);
        loader.registerHooksForClass(Other.class);
        loader.registerHooksForClass(PlayerHooks.class);


        loader.run("cn.nukkit.Nukkit", args);


    }
}