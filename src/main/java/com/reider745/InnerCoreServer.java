package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginDescription;
import com.reider745.api.CallbackHelper;
import com.reider745.block.CustomBlock;
import com.reider745.commands.CommandsHelper;
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
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerCoreServer {
    public static final int PROTOCOL = 422;

    public static InnerCorePlugin plugin;

    public static String PATH;
    public static Server server;
    public static InnerCoreServer ic_server;

    public static String getStringParam(String name) {
        return switch (name) {
            case "world_dir", "world_name" -> "world";
            case "path_for_world" -> "worlds";
            default -> null;
        };
    }

    public void loadMods() {
    }

    public void left() {
        NativeCallback.onGameStopped(true);
        NativeCallback.onMinecraftAppSuspended();
    }

    private static final ClassLoader classLoader = InnerCoreServer.class.getClassLoader();

    public static File cloneFile(String name) {
        return cloneFile(name, name);
    }

    public static File cloneFile(String name, String path) {
        try {
            final File file = new File(name);
            if (!file.exists()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
                BufferedInputStream bis = new BufferedInputStream(classLoader.getResourceAsStream(name));

                bos.write(bis.readAllBytes());
                bis.close();
                bos.close();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void processFile(ZipFile file, String uncompressedDirectory, ZipEntry entry) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(file.getInputStream(entry));
        final BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(uncompressedDirectory + entry.getName()));
        bos.write(bis.readAllBytes());

        bos.close();
        bis.close();
    }

    public boolean isLegacyWorkbench() {
        return true;
    }

    private static void processDirectory(String uncompressedDirectory, ZipEntry entry) {
        final File directory = new File(uncompressedDirectory + entry.getName());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @SuppressWarnings("unchecked")
    public static void unzip(final ZipFile file, final String uncompressedDirectory) {
        try {
            final Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    processDirectory(uncompressedDirectory, entry);
                } else {
                    processFile(file, uncompressedDirectory, entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzipMods(final String path) throws IOException {
        final File directory_mods = new File(path);
        final File[] files = directory_mods.listFiles();
        if (files == null)
            return;

        for (final File file : files) {
            final String name = file.getName();
            if (file.isFile() && name.endsWith(".icmod")) {
                server.getLogger().info("Unzip mod " + name);

                final ZipFile zipFile = new ZipFile(file);
                if (zipFile.getEntry("build.config") == null)
                    unzip(zipFile, path + "/");
                else {
                    final String nameFolder = name.replace(".icmod", "");
                    new File(path + "/" + nameFolder).mkdir();
                    unzip(zipFile, path + "/" + nameFolder + "/");
                }
            }
        }
    }

    static void getFolderRootFromUri(URI uri, String targetPath, Consumer<? super Path> action) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (final FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            action.accept(fileSystem.getPath(targetPath));
        } catch (IllegalArgumentException | IOException e) {
            action.accept(Paths.get(uri));
        }
    }

    static void traverseResourcesFileSystem(String targetPath, Consumer<? super Path> action)
            throws URISyntaxException {
        final URI uri = InnerCoreServer.class.getResource(targetPath).toURI();
        getFolderRootFromUri(uri, targetPath, folderRoot -> {
            try (final Stream<Path> walk = Files.walk(folderRoot)) {
                walk.forEach(childPath -> {
                    if (Files.isRegularFile(childPath)) {
                        action.accept(folderRoot.relativize(childPath));
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void unpackResources(String targetPath, String outputPath) throws URISyntaxException {
        traverseResourcesFileSystem(targetPath, childPath -> {
            try (final InputStream inputStream = InnerCoreServer.class
                    .getResourceAsStream(targetPath + "/" + childPath.toString())) {
                File outputFile = new File(outputPath, childPath.toString());
                outputFile.getParentFile().mkdirs();
                try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    FileTools.inStreamToOutStream(inputStream, outputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void preload(Server server) throws Exception {
        final long startupMillis = System.currentTimeMillis();
        server.getLogger().info("Initiating target directory '" + server.getDataPath() + "'");

        PATH = server.getDataPath();
        InnerCoreServer.server = server;
        InnerCoreServer.ic_server = this;
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

        final File innercoreDirectory = new File(PATH, "innercore");
        if (!innercoreDirectory.exists()) {
            server.getLogger().info("Extracting internal package...");
            final File zipFile = cloneFile("innercore.zip");//но у нас нет в ресурсах innercore.zip :D
            if (zipFile != null) {
                unzip(new ZipFile(zipFile), PATH);
                try {
                    zipFile.delete();
                } catch (SecurityException e) {
                }
            } else {
                unpackResources("/innercore", innercoreDirectory.getPath());
            }
        }

        cloneFile("innercore_default_config.json");
        unzipMods(PATH + "/innercore/mods");

        FileTools.init();

        MultiplayerModList.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        NetworkEntity.loadClass();
        IdConversionMap.loadClass();

        JSONObject object = new JSONObject();
        object.put("fix", server.getPropertyBoolean("inner_core.legacy_inventory", true));
        Network.getSingleton().addServerInitializationPacket("server_fixed.inventory", (client) -> object, (v, v1) -> {
            // legacy inner core for mod ServerFixed
        });
        Network.getSingleton().addServerInitializationPacket("system.dedicated_server", (client) -> object, (v, v1) -> {
            // for new inner core
        });

        RuntimeIdDataPacketSender.loadClass();
        NetworkJsAdapter.instance = new NetworkJsAdapter(Network.getSingleton());
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        API.loadAllAPIs();
        ModLoader.initialize();
        ModPackContext.getInstance()
                .setCurrentModPack(ModPackFactory.getInstance().createFromDirectory(innercoreDirectory));

        InnerCoreConfig.set("gameplay.use_legacy_workbench_override", isLegacyWorkbench());

        ModLoader.loadModsAndSetupEnvViaNewModLoader();
        ModLoader.prepareResourcesViaNewModLoader();
        new AsyncModLauncher().launchModsInCurrentThread();

        Updatable.init();
        NativeCallback.onLocalServerStarted();

        ItemContainer.loadClass();

        Logger.info("INNERCORE", "preloaded in " + (System.currentTimeMillis() - startupMillis) + "ms");
        Logger.info("INNERCORE", PackInfo.toInfo());
    }

    public void afterload() {
        server.getLogger().info("Registering Nukkit-MOT containment...");
        server.getPluginManager().registerEvents(new EventListener(), plugin);

        CustomBlock.init();
        CustomItem.init();
        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CommandsHelper.init();
        CustomItem.initCreativeItems();

        NativeCallback.onLevelCreated();
    }

    public void start() {
    }


    public static int getVersionCode() {
        return server.getPropertyInt("inner-core-version", 152);
    }

    public static String getVersionName() {
        return server.getPropertyString("inner-core-version-name", "2.3.1b115 test");
    }

    public static String getName() {
        return server.getPropertyString("inner-core-pack-name", "Inner Core Test");
    }

    public static void useNotSupport(String name) {
        throw new RuntimeException("Use not support multiplayer method " + name);
    }

    public static void useClientMethod(String name) {
        throw new RuntimeException("Use client method " + name);
    }

    public static void useNotCurrentSupport(String name) {
        throw new RuntimeException("The " + name + " method is currently not supported");
    }

    public static void useIncomprehensibleMethod(String name) {
        throw new RuntimeException("I don't really understand what this method does (" + name
                + "), which is why you're reading this right now");
    }
}
