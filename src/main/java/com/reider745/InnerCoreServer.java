package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginDescription;

import com.reider745.api.CallbackHelper;
import com.reider745.block.CustomBlock;
import com.reider745.commands.CommandsHelper;
import com.reider745.event.EventListener;
import com.reider745.event.InnerCorePlugin;
import com.reider745.item.CustomItem;

import com.reider745.item.ItemMethod;
import com.reider745.item.NukkitIdConvertor;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;

import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
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

    public static String getGameLanguage() {
        final String lang = server.getLanguage().getLang();

        final StringBuilder icLang = new StringBuilder();
        for (int i = 0; i < lang.length() - 1; i++)
            icLang.append(lang.charAt(i));
        return icLang.toString();
    }

    public void left() {
        NativeCallback.onGameStopped(true);
        NativeCallback.onMinecraftAppSuspended();
    }

    public boolean isLegacyWorkbench() {
        return server.getPropertyBoolean("legacy.workbench");
    }

    public static boolean canEvalEnable() {
        return server.getPropertyBoolean("eval-enable", true);
    }

    public static boolean isDevelopMode() {
        return server.getPropertyBoolean("develop-mode", false);
    }

    public static boolean isRuntimeException() {
        return server.getPropertyBoolean("inner_core.runtime_exception", false);
    }

    private static void processFile(ZipFile file, String uncompressedDirectory, ZipEntry entry) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(file.getInputStream(entry));

        final File out = new File(uncompressedDirectory + entry.getName());
        out.getParentFile().mkdirs();

        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
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
        Logger.server = server;

        CallbackHelper.init();
        NukkitIdConvertor.init();

        plugin = new InnerCorePlugin();
        plugin.setEnabled(true);

        HashMap<String, Object> configs = new HashMap<>();
        configs.put("name", "InnerCore");
        configs.put("version", getVersionName());
        configs.put("main", "cn.nukkit.plugin.InternalPlugin");
        configs.put("api", null);

        plugin.init(null, server, new PluginDescription(configs), null, null);

        final File innerCoreDirectory = new File(PATH, "innercore");
        if (!innerCoreDirectory.exists()) {
            server.getLogger().info("Extracting internal package...");
            try {
                File innercoreFile = unpackExistingResources("/innercore.zip", PATH);
                try (final ZipFile zipFile = new ZipFile(innercoreFile)) {
                    unzip(zipFile, PATH);
                }
            } catch (IOException | SecurityException fileExc) {
                try {
                    unpackExistingResources("/innercore", innerCoreDirectory.getPath());
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

        ModPack innerCoreModPack = ModPackFactory.getInstance().createFromDirectory(innerCoreDirectory);
        ModPackContext.getInstance().setCurrentModPack(innerCoreModPack);

        for (ModPackDirectory directoryWithMods : innerCoreModPack
                .getDirectoriesOfType(ModPackDirectory.DirectoryType.MODS)) {
            directoryWithMods.assureDirectoryRoot();
            for (File potentialIcmodFile : directoryWithMods.getLocation().listFiles()) {
                if (potentialIcmodFile.isFile() && potentialIcmodFile.getName().endsWith(".icmod")) {
                    ExtractionHelper.extractICModFile(potentialIcmodFile, str -> server.getLogger().debug(str), null);
                    potentialIcmodFile.deleteOnExit();
                }
            }
        }

        InnerCore innerCore = new InnerCore(new File(PATH));
        innerCore.load();
        innerCore.build();
        LoadingStage.setStage(LoadingStage.STAGE_MCPE_STARTING);
        InnerCoreConfig.set("gameplay.use_legacy_workbench_override", isLegacyWorkbench());
        LoadingStage.setStage(LoadingStage.STAGE_MCPE_INITIALIZING);
        LoadingUI.setTextAndProgressBar("Initializing Minecraft...", 0.55f);
        NativeCallback.onFinalInitStarted();
        NativeCallback.onFinalInitComplete();

        JSONObject object = new JSONObject();
        object.put("fix", server.getPropertyBoolean("inner_core.legacy_inventory", true));
        Network.getSingleton().addServerInitializationPacket("server_fixed.inventory", (client) -> object, (v, v1) -> {
            // legacy inner core for mod ServerFixed
        });
        Network.getSingleton().addServerInitializationPacket("system.dedicated_server", (client) -> object, (v, v1) -> {
            // for new inner core
        });

        NativeCallback.onLocalServerStarted();

        Logger.info("INNERCORE", "preloaded in " + (System.currentTimeMillis() - startupMillis) + "ms");
    }

    public void afterload() {
        server.getLogger().info("Registering Nukkit-MOT containment...");

        Logger.info("Register events ZotCoreLoader");
        InnerCoreServer.server.getPluginManager().registerEvents(new EventListener(), InnerCoreServer.plugin);

        CustomBlock.init();
        CustomItem.postInit();
        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CommandsHelper.init();

        ItemMethod.isPostLoad = true;

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
        if (!isDevelopMode())
            return;
        String message = "Use not support multiplayer method " + name;
        if (isRuntimeException()) {
            throw new RuntimeException(message);
        } else {
            Logger.warning(message);
        }
    }

    public static void useClientMethod(String name) {
        if (!isDevelopMode())
            return;
        String message = "Use client method " + name;
        if (isRuntimeException()) {
            throw new RuntimeException(message);
        } else {
            Logger.warning(message);
        }
    }

    public static void useNotCurrentSupport(String name) {
        if (!isDevelopMode())
            return;
        String message = "The " + name + " method is currently not supported";
        if (isRuntimeException()) {
            throw new RuntimeException(message);
        } else {
            Logger.warning(message);
        }
    }

    public static void useIncomprehensibleMethod(String name) {
        if (!isDevelopMode())
            return;
        String message = "I don't really understand what this method does (" + name
                + "), which is why you're reading this right now";
        if (isRuntimeException()) {
            throw new RuntimeException(message);
        } else {
            Logger.warning(message);
        }
    }
}
