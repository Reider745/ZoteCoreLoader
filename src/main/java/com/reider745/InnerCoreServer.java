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
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;

import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerCoreServer {
    public static final int PROTOCOL = 422;
    public static final int EXIT_CODE_NO_INTERNAL_PACKAGE = 32;

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

    public boolean isLegacyWorkbench() {
        return true;
    }

    private static void processFile(ZipFile file, String uncompressedDirectory, ZipEntry entry) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(file.getInputStream(entry));
        final BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(uncompressedDirectory + entry.getName()));
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

    static void getFolderRootFromUri(URI uri, String targetPath, Consumer<? super Path> action) {
        final Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (final FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            action.accept(fileSystem.getPath(targetPath));
        } catch (IllegalArgumentException | IOException e) {
            action.accept(Paths.get(uri));
        }
    }

    static void traverseResourcesFileSystem(String targetPath, Consumer<? super Path> action)
            throws IOException {
        try {
            final URI uri = InnerCoreServer.class.getResource(targetPath).toURI();
            getFolderRootFromUri(uri, targetPath, folderRoot -> {
                try (final Stream<Path> walk = Files.walk(folderRoot)) {
                    walk.forEach(childPath -> {
                        if (Files.isRegularFile(childPath)) {
                            action.accept(childPath.equals(folderRoot) ? childPath.getFileName()
                                    : folderRoot.relativize(childPath));
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException | URISyntaxException e) {
            throw new IOException(e);
        }
    }

    static File unpackExistingResources(String targetPath, String outputPath) throws IOException {
        final AtomicReference<File> result = new AtomicReference<>(new File(outputPath));
        traverseResourcesFileSystem(targetPath, childPath -> {
            final File outputFile = new File(outputPath, childPath.toString());
            if (!outputFile.exists()) {
                final boolean isSingleFile = targetPath.equals("/" + childPath.toString());
                outputFile.getParentFile().mkdirs();
                try (final InputStream inputStream = InnerCoreServer.class
                        .getResourceAsStream(isSingleFile ? targetPath
                                : targetPath + "/" + childPath.toString())) {
                    try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        FileTools.inStreamToOutStream(inputStream, outputStream);
                        if (isSingleFile) {
                            result.set(outputFile);
                        }
                    }
                } catch (IOException | NullPointerException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return result.get();
    }

    static File unpackResources(String targetPath, String outputPath) {
        try {
            return unpackExistingResources(targetPath, outputPath);
        } catch (IOException | SecurityException e) {
            server.getLogger().warning("Failed to unpack '" + targetPath + "' or it was not found");
            server.getLogger().debug("Failed to unpack '" + targetPath + "' or it was not found", e);
        }
        return null;
    }

    public void preload(Server server) throws Exception {
        final long startupMillis = System.currentTimeMillis();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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
            try {
                File innercoreFile = unpackExistingResources("/innercore.zip", PATH);
                try (final ZipFile zipFile = new ZipFile(innercoreFile)) {
                    unzip(zipFile, PATH);
                }
            } catch (IOException | SecurityException fileExc) {
                try {
                    unpackExistingResources("/innercore", innercoreDirectory.getPath());
                } catch (IOException | SecurityException directoryExc) {
                    server.getLogger().debug("Failed to unpack 'innercore.zip' or it was not found", fileExc);
                    server.getLogger().debug("Failed to unpack 'innercore' or it was not found", directoryExc);
                    server.getLogger().critical(
                            "Failed to extract 'innercore.zip' or 'innercore' folder containing pack with necessary files. "
                                    + "Please check your ZoteCore installation, or try enabling debug mode to obtain more information.");
                    try {
                        System.exit(EXIT_CODE_NO_INTERNAL_PACKAGE);
                    } catch (SecurityException e) {
                        throw new RuntimeException("EXIT_CODE_NO_INTERNAL_PACKAGE");
                    }
                }
            }
        }

        unpackResources("/innercore_default_config.json", PATH);

        // Required to be called before modpack instantiation
        FileTools.init();

        ModPack innercoreModPack = ModPackFactory.getInstance().createFromDirectory(innercoreDirectory);
        ModPackContext.getInstance().setCurrentModPack(innercoreModPack);

        for (ModPackDirectory directoryWithMods : innercoreModPack
                .getDirectoriesOfType(ModPackDirectory.DirectoryType.MODS)) {
            directoryWithMods.assureDirectoryRoot();
            for (File potentialIcmodFile : directoryWithMods.getLocation().listFiles()) {
                if (potentialIcmodFile.isFile() && potentialIcmodFile.getName().endsWith(".icmod")) {
                    ExtractionHelper.extractICModFile(potentialIcmodFile, str -> server.getLogger().debug(str), null);
                    potentialIcmodFile.deleteOnExit();
                }
            }
        }

        // Rhino JavaMembers.reflect patches
        ColorsPatch.init();
        ReflectionPatch.init();

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
        InnerCoreConfig.set("gameplay.use_legacy_workbench_override", isLegacyWorkbench());

        API.loadAllAPIs();
        ModLoader.initialize();
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

    public static boolean isRuntimeException(){
        return server.getPropertyBoolean("inner_core.runtime_exception", false);
    }

    public static void useNotSupport(String name) {
        if(isRuntimeException())
            throw new RuntimeException("Use not support multiplayer method " + name);
    }

    public static void useClientMethod(String name) {
        if(isRuntimeException())
            throw new RuntimeException("Use client method " + name);
    }

    public static void useNotCurrentSupport(String name) {
        if(isRuntimeException())
            throw new RuntimeException("The " + name + " method is currently not supported");
    }

    public static void useIncomprehensibleMethod(String name) {
        if(isRuntimeException())
            throw new RuntimeException("I don't really understand what this method does (" + name
                + "), which is why you're reading this right now");
    }
}
