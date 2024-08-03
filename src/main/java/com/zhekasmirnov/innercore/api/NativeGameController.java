package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import org.mozilla.javascript.annotations.JSStaticFunction;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;


public class NativeGameController {
    @JSStaticFunction// Can break a block that breaks instantly
    public static boolean startDestroyBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.startDestroyBlock");
        return false;
    }
    @JSStaticFunction
    public static boolean continueDestroyBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.continueDestroyBlock");
        return false;
    }
    @JSStaticFunction// Interrupts the destruction animation
    public static void stopDestroyBlock(int x, int y, int z){
        InnerCoreServer.useClientMethod("NativeGameController.stopDestroyBlock");
    }



    @JSStaticFunction
    public static void startBuildBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.startBuildBlock");
    }
    @JSStaticFunction
    public static void continueBuildBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.continueBuildBlock");
    }
    @JSStaticFunction
    public static void stopBuildBlock(){
        InnerCoreServer.useClientMethod("NativeGameController.stopBuildBlock");
    }



    // Methods for interaction between the player and the world
    @JSStaticFunction
    public static void destroyBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.destroyBlock");
    }
    @JSStaticFunction
    public static void buildBlock(int x, int y, int z, int side){
        InnerCoreServer.useClientMethod("NativeGameController.buildBlock");
    }
    public static void useItemOn(long itemStack, float x, float y, float z, int side, long regionPtr){
        InnerCoreServer.useClientMethod("NativeGameController.useItemOn");
    }
    public static void attack(long entity){
        InnerCoreServer.useClientMethod("NativeGameController.attack");
    }
    public static void interact(long entity, float x, float y, float z){
        InnerCoreServer.useClientMethod("NativeGameController.interact");
    }
    @JSStaticFunction
    public static void releaseUsingItem(){
        InnerCoreServer.useClientMethod("NativeGameController.releaseUsingItem");
    }

    @JSStaticFunction
    public static void onItemUse(double x, double y, double z, int side){
        useItemOn(0, (float) x, (float) y, (float) z, side, 0);
    }

}
