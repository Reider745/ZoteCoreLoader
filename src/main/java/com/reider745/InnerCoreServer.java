package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginDescription;
import cn.nukkit.plugin.PluginManager;

import com.google.common.base.Preconditions;
import com.reider745.api.CallbackHelper;
import com.reider745.block.CustomBlock;
import com.reider745.commands.CommandsHelper;
import com.reider745.event.EventListener;
import com.reider745.event.InnerCorePlugin;
import com.reider745.hooks.BiomesHooks;
import com.reider745.hooks.GlobalBlockPalette;
import com.reider745.hooks.RuntimeItemsHooks;
import com.reider745.hooks.SnowfallEverywhere;
import com.reider745.hooks.bugfix.DimensionsFix;
import com.reider745.item.CustomEnchantMethods;
import com.reider745.item.CustomItem;

import com.reider745.item.ItemMethod;
import com.reider745.item.NukkitIdConvertor;
import com.reider745.network.InnerCorePacket;
import com.reider745.world.dimensions.DimensionsMethods;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkConfig;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;

import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerCoreServer {
    public enum MethodHandling {
        NONE, DEBUG, WARNING, RAISE;
    }

    public static final int PROTOCOL = 422;
    public static final int EXIT_CODE_NO_INTERNAL_PACKAGE = 32;
    public static final int EXIT_CODE_NO_MODPACK = 33;

    public static String dataPath;
    public static InnerCoreServer singleton;
    public static InnerCorePlugin plugin;

    public InnerCoreServer() {
        Preconditions.checkState(singleton == null, "Already initialized!");
        singleton = this;
    }

    public static String getStringParam(String name) {
        return switch (name) {
            case "world_dir", "world_name" -> "world";
            case "path_for_world" -> "worlds";
            default -> null;
        };
    }

    public static String getGameLanguage() {
        final String langName = Server.getInstance().getLanguage().getLang();

        final StringBuilder targetLang = new StringBuilder();
        for (int i = 0; i < langName.length() - 1; i++) {
            targetLang.append(langName.charAt(i));
        }
        return targetLang.toString();
    }

    public void left() {
        NativeCallback.onMinecraftAppSuspended();
        NativeCallback.onGameStopped(true);
        singleton = null;
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
            Server.getInstance().getLogger().warning("Failed to unpack '" + targetPath + "' or it was not found");
            Server.getInstance().getLogger().debug("Failed to unpack '" + targetPath + "' or it was not found", e);
        }
        return null;
    }

    public void preload(Server server) throws Exception {
        final long startupMillis = System.currentTimeMillis();

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        server.getLogger().info("Initiating target directory '" + server.getDataPath() + "'");
        BiomesHooks.init();

        dataPath = server.getDataPath();
        final File dataFolderFile = new File(dataPath);

        CallbackHelper.init();
        NukkitIdConvertor.init();
        DimensionsMethods.init();

        HashMap<String, Object> description = new HashMap<>();
        description.put("name", "ZoteCore");
        description.put("version", "SNAPSHOT");
        description.put("main", InnerCorePlugin.class.getName());
        description.put("api", new ArrayList<String>());

        plugin = new InnerCorePlugin();
        plugin.init(null, server, new PluginDescription(description), dataFolderFile, dataFolderFile);
        plugin.saveDefaultConfig();
        plugin.onLoad();
        plugin.setEnabled();

        NetworkConfig config = Network.getSingleton().getConfig();
        config.setDefaultPort(getPropertyInt("socket-port", config.getDefaultPort()));
        config.setSocketConnectionAllowed(
                getPropertyBoolean("socket-server-enable", config.isSocketConnectionAllowed()));

        JSONObject json = new JSONObject();
        json.put("server", true);
        json.put("socket_port", config.getDefaultPort());

        InnerCorePacket packet = new InnerCorePacket();
        packet.name = "system.server_detection";
        packet.format_id = 0;

        byte[] bytes = json.toString().getBytes();
        packet.bytes_length = bytes.length;
        packet.bytes = bytes;
        packet.encode();
        InnerCorePacket.sendInfo = packet;

        final File innerCoreDirectory = new File(dataPath, "innercore");
        if (!innerCoreDirectory.exists()) {
            server.getLogger().info("Extracting internal package...");
            try {
                File innercoreFile = unpackExistingResources("/innercore.zip", dataPath);
                try (final ZipFile zipFile = new ZipFile(innercoreFile)) {
                    unzip(zipFile, dataPath);
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
                    }
                    throw new RuntimeException("EXIT_CODE_NO_INTERNAL_PACKAGE");
                }
            }
        }

        unpackResources("/innercore_default_config.json", dataPath);

        // Required to be called before modpack instantiation
        FileTools.init();

        ModPackContext.getInstance().setCurrentModPack(getModpack());

        for (ModPackDirectory directoryWithMods : ModPackContext.getInstance().getCurrentModPack()
                .getDirectoriesOfType(ModPackDirectory.DirectoryType.MODS)) {
            directoryWithMods.assureDirectoryRoot();
            for (File potentialIcmodFile : directoryWithMods.getLocation().listFiles()) {
                if (potentialIcmodFile.isFile() && potentialIcmodFile.getName().endsWith(".icmod")) {
                    ExtractionHelper.extractICModFile(potentialIcmodFile, str -> server.getLogger().debug(str), null);
                    potentialIcmodFile.deleteOnExit();
                }
            }
        }

        InnerCore innerCore = new InnerCore(dataFolderFile);
        innerCore.load();
        innerCore.build();
        LoadingStage.setStage(LoadingStage.STAGE_MCPE_STARTING);
        LoadingStage.setStage(LoadingStage.STAGE_MCPE_INITIALIZING);
        LoadingUI.setTextAndProgressBar("Initializing Minecraft...", 0.55f);
        NativeCallback.onFinalInitStarted();
        NativeCallback.onFinalInitComplete();

        JSONObject object = new JSONObject();
        object.put("fix", getPropertyBoolean("use-legacy-inventory", true));
        Network.getSingleton().addServerInitializationPacket("server_fixed.inventory", (client) -> object, (v, v1) -> {
            // legacy inner core for mod ServerFixed
        });

        NativeCallback.onLocalServerStarted();
        WorldDataSaverHandler.getInstance().fetchParamsFromConfig();

        Logger.info("INNERCORE", "preloaded in " + (System.currentTimeMillis() - startupMillis) + "ms");
    }

    public void afterload() {
        CustomEnchantMethods.postInit();
        Server.getInstance().getLogger().info("Registering Nukkit-MOT containment...");
        PluginManager pluginManager = Server.getInstance().getPluginManager();
        pluginManager.getPlugins().put(plugin.getDescription().getName(), plugin);

        Logger.info("Registering ZoteCore events...");
        pluginManager.registerEvents(new EventListener(), plugin);
        if (SnowfallEverywhere.isActive) {
            pluginManager.registerEvents(new SnowfallEverywhere(), plugin);
        }

        DimensionsFix.init();
        GlobalBlockPalette.init();
        RuntimeItemsHooks.register();
        CustomBlock.init();
        CustomItem.init();
        CustomItem.addCreativeItemsBuild();
        CustomItem.addCreativeItemsWeapons();
        CustomItem.addCreativeItems();
        CustomItem.addCreativeItemsNature();
        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CommandsHelper.init();

        ItemMethod.isPostLoad = true;

        NativeCallback.onLevelCreated();
    }

    public void reload() {
        Logger.info("Reload ZoteCore events...");
        plugin.setEnabled();
        PluginManager pluginManager = Server.getInstance().getPluginManager();
        pluginManager.getPlugins().put(plugin.getDescription().getName(), plugin);
        pluginManager.registerEvents(new EventListener(), plugin);
        if (SnowfallEverywhere.isActive) {
            pluginManager.registerEvents(new SnowfallEverywhere(), plugin);
        }
    }

    public void start() {
    }

    public static Object getProperty(String variable) {
        return getProperty(variable, (Object) null);
    }

    public static Object getProperty(String variable, Object defaultValue) {
        return plugin != null && plugin.getConfig().exists(variable) ? plugin.getConfig().get(variable) : defaultValue;
    }

    public static String getPropertyString(String key) {
        return getPropertyString(key, (String) null);
    }

    public static String getPropertyString(String key, String defaultValue) {
        return plugin != null && plugin.getConfig().exists(key) ? String.valueOf(plugin.getConfig().get(key))
                : defaultValue;
    }

    public static int getPropertyInt(String variable) {
        return getPropertyInt(variable, (Integer) null);
    }

    public static int getPropertyInt(String variable, Integer defaultValue) {
        return plugin != null && plugin.getConfig().exists(variable) ? (!plugin.getConfig().get(variable).equals("")
                ? Integer.parseInt(String.valueOf(plugin.getConfig().get(variable)))
                : defaultValue) : defaultValue;
    }

    public static boolean getPropertyBoolean(String variable) {
        return getPropertyBoolean(variable, (Object) null);
    }

    public static boolean getPropertyBoolean(String variable, Object defaultValue) {
        Object value = plugin != null && plugin.getConfig().exists(variable) ? plugin.getConfig().get(variable)
                : defaultValue;
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return switch (String.valueOf(value)) {
                case "on", "true", "1", "yes" -> true;
                default -> false;
            };
        }
    }

    private ModPack getModpack() {
        String modpackRequirement = getPropertyString("modpack");
        ModPackStorage storage = ModPackContext.getInstance().getStorage();
        storage.rebuildModPackList();

        if (modpackRequirement == null) {
            return storage.getDefaultModPack();
        }

        List<ModPack> modpacks = storage.getAllModPacks();
        for (ModPack modpack : modpacks) {
            if (modpackRequirement.equals(modpack.getRootDirectory().getAbsolutePath())) {
                return modpack;
            }
            if (modpackRequirement.equals(modpack.getRootDirectory().getName())) {
                return modpack;
            }
        }
        for (ModPack modpack : modpacks) {
            if (modpackRequirement.equals(modpack.getManifest().getPackName())) {
                return modpack;
            }
            if (modpackRequirement.equals(modpack.getManifest().getDisplayedName())) {
                return modpack;
            }
        }

        Logger.critical(
                "Unable to find specified modpack, please enter modpack name, folder name or relative path instead. " +
                        "It is possible that your hosting provider dynamically updates paths, preventing you from specifying absolute one.");
        if (modpacks.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Available modpacks: ");
            for (ModPack modpack : modpacks) {
                builder.append(modpack.getRootDirectory().getName());
            }
            builder.append('.');
            Logger.critical(builder.toString());
        }

        try {
            System.exit(EXIT_CODE_NO_MODPACK);
        } catch (SecurityException e) {
        }
        throw new RuntimeException("EXIT_CODE_NO_MODPACK");
    }

    public static boolean isLegacyWorkbench() {
        return getPropertyBoolean("use-legacy-workbench-override", true);
    }

    public static boolean isUnsafeScriptingAllowed() {
        return getPropertyBoolean("allow-unsafe-scripting", true);
    }

    public static boolean isDebugInnerCoreNetwork() {
        return getPropertyBoolean("network-debug", false);
    }

    public static boolean isDeveloperMode() {
        return getPropertyBoolean("developer-mode", false);
    }

    public static int getAutoSavePeriod() {
        return getPropertyInt("auto-save-period", 60);
    }

    public static boolean canAutoSaveWorld() {
        return getPropertyBoolean("auto-save-world", true);
    }

    public static boolean isUnsupportedOptionsAllowed() {
        return getPropertyBoolean("allow-unsupported-options", false);
    }

    public static MethodHandling getUnsupportedMethodHandling() {
        try {
            return MethodHandling
                    .valueOf(getPropertyString("unsupported-method-handling", "debug").toUpperCase());
        } catch (IllegalArgumentException exc) {
            return MethodHandling.DEBUG;
        }
    }

    public static String getName() {
        return getPropertyString("pack", "Inner Core Test");
    }

    public static String getVersionName() {
        return getPropertyString("pack-version", "2.3.1b115 test");
    }

    public static int getVersionCode() {
        return getPropertyInt("pack-version-code", 152);
    }

    private static void handleUnsupportedMethod(String message) {
        MethodHandling handling = getUnsupportedMethodHandling();
        if (handling != MethodHandling.NONE) {
            switch (handling) {
                case DEBUG -> Logger.message(message);
                case WARNING -> Logger.warning(message);
                case RAISE -> throw new RuntimeException(message);
                default -> throw new UnsupportedOperationException();
            }
        }
    }

    public static void useNotSupport(String name) {
        handleUnsupportedMethod("Usage of unavailable multiplayer method " + name);
    }

    public static void useClientMethod(String name) {
        handleUnsupportedMethod("Usage of client method " + name);
    }

    public static void useNotCurrentSupport(String name) {
        handleUnsupportedMethod("Usage of method " + name + " currently is not supported");
    }

    public static void useIncomprehensibleMethod(String name) {
        handleUnsupportedMethod("I don't really understand what this method does (" + name
                + "), which is why you're reading this right now");
    }
}
