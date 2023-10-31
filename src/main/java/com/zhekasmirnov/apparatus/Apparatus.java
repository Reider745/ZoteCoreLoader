package com.zhekasmirnov.apparatus;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
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
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.runtime.AsyncModLauncher;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.*;
import java.net.URL;
import java.util.HashMap;

public class Apparatus {
    public static String PATH;
    public static Server server;

    private static class EntryDump {
        int newId;
        int data;
        HashMap<String, Integer> states;

        Long dbgHash;
    }


    public static void init(Server server) throws Exception {
        long start = System.currentTimeMillis();
        server.getLogger().info("start load inner core "+server.getDataPath());
        URL url = Server.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(url.toURI());
        PATH = server.getDataPath();


        Apparatus.server = server;
        ICLog.server = server;
        Logger.server = server;
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

        API.loadAllAPIs();
        ModLoader.initialize();

        ModPackContext.getInstance().setCurrentModPack(ModPackFactory.getInstance().createFromDirectory(new File(server.getDataPath()+"innercore")));

        ModLoader.loadModsAndSetupEnvViaNewModLoader();
        ModLoader.prepareResourcesViaNewModLoader();
        new AsyncModLauncher().launchModsInCurrentThread();
        IDRegistry.rebuildNetworkIdMap();

        CustomBlock.init();

        Logger.info("INNERCORE", "end load, time: "+(System.currentTimeMillis()-start));
    }

    public static void initCreativeItems(){

    }

    public static void postInit(){
        Logger.debug("Post loaded innercore...");

        Item.clearCreativeItems();

        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CustomItem.initCreativeItems();

        Item.initCreativeItems();
    }

    public static int getVersionCode() {
        return 1;
    }

    public static boolean isDevelop() {
        return true;
    }
}
