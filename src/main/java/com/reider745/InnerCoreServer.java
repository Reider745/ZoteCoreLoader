package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.item.CustomItem;
import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerPackVersionChecker;
import com.zhekasmirnov.apparatus.multiplayer.mod.RuntimeIdDataPacketSender;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

public class InnerCoreServer {
    public static final int PROTOCOL = 422;

    public static String PATH;
    public static Server server;

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

    public void preLoad(Server server) throws Exception {
        long start = System.currentTimeMillis();
        server.getLogger().info("start load inner core "+server.getDataPath());;
        PATH = server.getDataPath();


        InnerCoreServer.server = server;
        ICLog.server = server;
        com.zhekasmirnov.horizon.runtime.logger.Logger.server = server;
        FileTools.init();

        MultiplayerModList.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        NetworkEntity.loadClass();
        IdConversionMap.loadClass();





        RuntimeIdDataPacketSender.loadClass();
        Network.getSingleton().startLanServer();
        NetworkJsAdapter.instance = new NetworkJsAdapter(Network.getSingleton());
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        // API.loadAllAPIs();
        // ModLoader.initialize();

        //  ModPackContext.getInstance().setCurrentModPack(ModPackFactory.getInstance().createFromDirectory(new File(server.getDataPath()+"innercore")));

        // ModLoader.loadModsAndSetupEnvViaNewModLoader();
        // ModLoader.prepareResourcesViaNewModLoader();
        /// new AsyncModLauncher().launchModsInCurrentThread();
        //IDRegistry.rebuildNetworkIdMap();

        // CustomBlock.init();

        Logger.info("INNERCORE", "end load, time: "+(System.currentTimeMillis()-start));
        Logger.info("INNERCORE", PackInfo.toInfo());
    }

    public void postLoad(){
        Logger.debug("Post loaded innercore...");

        Item.clearCreativeItems();

        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CustomItem.initCreativeItems();

        //Item.initCreativeItems();
        NativeCallback.onLevelCreated();
    }

    public void onPlayerEat(int food, float radio, long player){
        NativeCallback.onPlayerEat(food, radio, player);
    }

    public void tick(){
        NativeCallback.onTick();
    }

    public static String getVersion(){
        return "2.3.1b115";
    }

    public static String getName(){
        return "Inner Core";
    }
}
