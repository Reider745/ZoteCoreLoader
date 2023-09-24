package com.reider745;

import cn.nukkit.Server;
import com.reider745.api.pointers.PointersStorage;
import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.innercore.api.NativeCallback;

public class InnerCoreServer {
    public void loadMods(){

    }

    public void main(){
        new PointersStorage("items");
    }

    public void left(){
        System.out.println("CallbackLeft");
        NativeCallback.onGameStopped(true);
        NativeCallback.onMinecraftAppSuspended();
        NativeCallback.onLocalServerStarted();
    }

    public void preLoad(Server self) throws Exception {
        Apparatus.init(self);
    }

    public void postLoad(){
        Apparatus.postInit();
        NativeCallback.onLevelCreated();
    }

    public void onPlayerEat(int food, float radio, long player){
        NativeCallback.onPlayerEat(food, radio, player);
    }

    public void tick(){
        NativeCallback.onTick();
    }
}
