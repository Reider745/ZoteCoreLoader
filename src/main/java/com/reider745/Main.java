package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.network.Network;
import com.reider745.api.hooks.JarEditor;
import com.reider745.hooks.*;
import com.reider745.network.InnerCorePacket;


public class Main {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();

    public static class LoadingStages {
        public static void registerPacket(Network network){
            network.registerPacket(InnerCorePacket.NETWORK_ID, InnerCorePacket.class);
        }

        public static void preRegister(Server server) throws Exception {
            innerCoreServer.preLoad(server);
        }

        public static void postRegister(Server server){
            innerCoreServer.postLoad();
        }

        public static void start(Server server)  {
            innerCoreServer.start();
        }

        public static void stop(Server server) throws Exception {
            try{
                innerCoreServer.left();
            }catch (Exception e){
                e.printStackTrace();
            }
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

        loader.registerHooksInitializationForClass(NetworkHooks.class);
        loader.registerHooksInitializationForClass(GlobalBlockPalette.class);
        loader.registerHooksInitializationForClass(ServerHooks.class);
        loader.registerHooksInitializationForClass(Other.class);
        loader.registerHooksInitializationForClass(PlayerHooks.class);
        loader.registerHooksInitializationForClass(CallbackHooks.class);
        loader.registerHooksInitializationForClass(RuntimeItemsHooks.class);
        loader.registerHooksInitializationForClass(BlocksHooks.class);
        loader.registerHooksInitializationForClass(LevelHooks.class);
        loader.registerHooksInitializationForClass(ItemUtils.class);

        loader.init();
        loader.run("cn.nukkit.Nukkit", args);


    }
}