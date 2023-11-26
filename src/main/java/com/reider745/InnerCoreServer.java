package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginDescription;
import com.reider745.api.CallbackHelper;
import com.reider745.block.CustomBlock;
import com.reider745.event.EventListener;
import com.reider745.event.InnerCorePlugin;
import com.reider745.item.CustomItem;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.api.container.ItemContainer;
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
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.runtime.AsyncModLauncher;
import com.zhekasmirnov.innercore.api.runtime.Updatable;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONObject;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerCoreServer {
    public static final int PROTOCOL = 422;

    public static InnerCorePlugin plugin;

    public static String PATH;
    public static Server server;

    public static String getStringParam(String name) {
        switch (name){
            case "world_dir", "world_name" -> {
                return "world";
            }
            case "path_for_world" -> {
                return "worlds";
            }
        }
        return null;
    }

    public void loadMods(){

    }

    public void left(){
        NativeCallback.onGameStopped(true);
        NativeCallback.onMinecraftAppSuspended();
    }
    private final ClassLoader classLoader = InnerCoreServer.class.getClassLoader();;
    private File cloneFile(String name){
        try {
            final File file = new File(name);
            if (!file.exists()){
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(name));
                BufferedInputStream bis = new BufferedInputStream(classLoader.getResourceAsStream(name));

                bos.write(bis.readAllBytes());
                bis.close();
                bos.close();
            }
            return file;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static void processFile(ZipFile file, String uncompressedDirectory, ZipEntry entry) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(file.getInputStream(entry));
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(uncompressedDirectory + entry.getName()));
        bos.write(bis.readAllBytes());

        bos.close();
        bis.close();
    }

    private static void processDirectory(String uncompressedDirectory, ZipEntry entry) {
        final File directory = new File(uncompressedDirectory + entry.getName());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void unzip(final File zipFile, final String uncompressedDirectory){
        try {
            final ZipFile file = new ZipFile(zipFile);
            final Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) processDirectory(uncompressedDirectory, entry);
                else processFile(file, uncompressedDirectory, entry);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void preLoad(Server server) throws Exception {
        long start = System.currentTimeMillis();
        server.getLogger().info("start load inner core "+server.getDataPath());;
        PATH = server.getDataPath();
        InnerCoreServer.server = server;
        ICLog.server = server;
        com.zhekasmirnov.horizon.runtime.logger.Logger.server = server;

        CallbackHelper.init();

        plugin = new InnerCorePlugin();
        plugin.setEnabled(true);

        HashMap<String, Object> configs = new HashMap<>();
        configs.put("name", "InnerCore");
        configs.put("version", getVersionName());
        configs.put("main", "cn.nukkit.plugin.InternalPlugin");
        configs.put("api", null);

        plugin.init(null, server, new PluginDescription(configs), null, null);

        if(!(new File(PATH+"/innercore").exists())){
            final File zipFile = cloneFile("innercore.zip");
            if(zipFile != null){
                unzip(zipFile, PATH);
                zipFile.deleteOnExit();
            }
        }

        cloneFile("innercore_default_config.json");

        FileTools.init();

        MultiplayerModList.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        NetworkEntity.loadClass();
        IdConversionMap.loadClass();


        JSONObject object = new JSONObject();
        object.put("fix", server.getPropertyBoolean("inner_core.legacy_inventory", true));
        Network.getSingleton().addServerInitializationPacket("server_fixed.inventory", (client) -> object, (v ,v1) -> {});//legacy inner core for mod ServerFixed
        Network.getSingleton().addServerInitializationPacket("system.dedicated_server", (client) -> object, (v ,v1) -> {});//for new inner core

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

        Updatable.init();
        NativeCallback.onLocalServerStarted();

        ItemContainer.loadClass();

        Logger.info("INNERCORE", "end load, time: "+(System.currentTimeMillis()-start));
        Logger.info("INNERCORE", PackInfo.toInfo());
    }

    public void postLoad(){
        Logger.debug("Post loaded innercore...");
        server.getPluginManager().registerEvents(new EventListener(), plugin);

        CustomBlock.init();
        CustomItem.init();
        CustomItem.initCreativeItems();
        NativeWorkbench.init();
        NativeFurnaceRegistry.init();


        NativeCallback.onLevelCreated();
    }

    public void start(){

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

    public static void useNotCurrentSupport(String name){
        throw new RuntimeException("The "+name+" method is currently not supported");
    }

    public static void useHzMethod(String name){
        throw new RuntimeException("В душе не ебу что делает данный метод, поэтому ты это сейчас читаешь "+name);
    }
}
