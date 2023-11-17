package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
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
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.runtime.AsyncModLauncher;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

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

        ClassLoader classLoader = InnerCoreServer.class.getClassLoader();

        try {
            Path resourcePath = Paths.get(classLoader.getResource("innercore").toURI());
            Path targetPath = Paths.get(PATH + "/innercore");

            File targetPathFile = targetPath.toFile();
            if (!targetPathFile.exists()) {

                String sourceDirectory = resourcePath.toString();

                Files.walk(Paths.get(sourceDirectory))
                        .forEach(source -> {
                            Path destination = Paths.get(targetPath.toString(), source.toString()
                                    .substring(sourceDirectory.length()));
                            try {
                                Files.copy(source, destination);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                System.out.println("Directory innercore craete");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            final String config = "innercore_default_config.json";
            final File file = new File(config);
            if(!file.exists())
                FileUtils.writeFileText(file, new String(classLoader.getResourceAsStream(config).readAllBytes()));
        }catch (Exception e){
            e.printStackTrace();
        }

        InnerCoreServer.server = server;
        ICLog.server = server;
        com.zhekasmirnov.horizon.runtime.logger.Logger.server = server;
        FileTools.init();

        MultiplayerModList.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        NetworkEntity.loadClass();
        IdConversionMap.loadClass();

        JSONObject object = new JSONObject();
        object.put("fix", server.getPropertyBoolean("inner_core.legacy_inventory", true));
        Network.getSingleton().addServerInitializationPacket("server_fixed.inventory", (client) -> object, (v ,v1) -> {});

        RuntimeIdDataPacketSender.loadClass();
        Network.getSingleton().startLanServer();
        NetworkJsAdapter.instance = new NetworkJsAdapter(Network.getSingleton());
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        API.loadAllAPIs();
        ModLoader.initialize();
        ModPackContext.getInstance().setCurrentModPack(ModPackFactory.getInstance().createFromDirectory(new File(PATH+"innercore")));

        ModLoader.loadModsAndSetupEnvViaNewModLoader();
        ModLoader.prepareResourcesViaNewModLoader();
        new AsyncModLauncher().launchModsInCurrentThread();
        IDRegistry.rebuildNetworkIdMap();

        CustomBlock.init();

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

    public static int getVersionCode(){
        return server.getPropertyInt("inner-core-version", 152);
        //return 152;
    }

    public static String getVersionName(){
        return server.getPropertyString("inner-core-version-name", "2.3.1b115 test");
    }

    public static String getName(){
        return server.getPropertyString("inner-core-pack-name", "Inner Core Test");
        //return "Inner Core Test";
    }

    public static void useNotSupport(String name){
        throw new RuntimeException("Use not support multiplayer method "+name);
    }

    public static void useClientMethod(String name){
        throw new RuntimeException("Use client method "+name);
    }

    public static void useHzMethod(String name){
        throw new RuntimeException("В душе не ебу что делает данный метод, поэтому ты это сейчас читаешь "+name);
    }
}
