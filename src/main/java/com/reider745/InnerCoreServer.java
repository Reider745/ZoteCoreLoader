package com.reider745;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginDescription;
import cn.nukkit.plugin.PluginManager;

import com.google.common.base.Preconditions;
import com.reider745.api.CallbackHelper;
import com.reider745.behavior.BehaviorPack;
import com.reider745.commands.CommandsHelper;
import com.reider745.event.EventListener;
import com.reider745.event.InnerCorePlugin;
import com.reider745.hooks.ZoteClassLoaderPool;
import com.reider745.hooks.item.RuntimeItemsHooks;
import com.reider745.hooks.SnowfallEverywhere;
import com.reider745.item.CustomItem;

import com.reider745.item.NukkitIdConverter;
import com.reider745.world.FakeDimensions;
import com.reider745.world.dimensions.DimensionsMethods;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbench;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InnerCoreServer {
    public enum MethodHandling {
        NONE, DEBUG, WARNING, TRACE, RAISE;
    }

    public static final int PROTOCOL = 422;
    public static final int RECIPES_PROTOCOL = 419;
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
        String levelName = "world";
        try {
            final cn.nukkit.utils.Config properties = Server.getInstance().getProperties();
            if (properties.exists("level-name") && !properties.get("level-name").toString().isEmpty()) {
                levelName = properties.get("level-name").toString();
            }
        } catch (Throwable e) {
            levelName = "world";
        }
        return switch (name) {
            case "world_dir", "world_name" -> levelName;
            case "path_for_world" -> new File(dataPath, "worlds/" + levelName).getPath();
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

        org.mozilla.javascript.Logger.DEBUG = Logger::debug;
        Class.forName("org.mozilla.javascript.Context");
        ScriptRuntime.GET_MESSAGES = () -> FileTools.getAssetInputStream("rhino/Messages.properties");
        Context.BUILD_LOADER = ZoteClassLoaderPool::new;

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        server.getLogger().info("Initiating target directory '" + server.getDataPath() + "'");
        // BiomesHooks.init();

        dataPath = server.getDataPath();
        NativeAPI.setWorldsPathOverride(new File(dataPath, "worlds").getPath());
        final File dataFolderFile = new File(dataPath);

        CallbackHelper.init();
        NukkitIdConverter.init();
        DimensionsMethods.init();
        FakeDimensions.init();

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

        Logger.initConfig();

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
        {
            InputStream inputStream = InnerCoreServer.class.getClassLoader().getResourceAsStream("innercore/coreengine/core-engine.dev.js");
            File file = new File(dataPath+"/innercore/coreengine/core-engine.dev.js");
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);

            outputStream.write(inputStream.readAllBytes());

            inputStream.close();
            outputStream.close();
        }

        // Required to be called before modpack instantiation
        FileTools.init();

        Network.getSingleton().getConfig().updateFromEngineConfig();
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

        NativeCallback.onLocalServerStarted();
        WorldDataSaverHandler.getInstance().fetchParamsFromConfig();

        Logger.info("INNERCORE", "preloaded in " + (System.currentTimeMillis() - startupMillis) + "ms");
    }

    private static boolean endLoad = false;

    public static boolean canEndLoadMods(){
        return endLoad;
    }

    public void afterload() {
        Server.getInstance().getLogger().info("Registering Nukkit-MOT containment...");
        PluginManager pluginManager = Server.getInstance().getPluginManager();
        pluginManager.getPlugins().put(plugin.getDescription().getName(), plugin);

        Logger.info("Registering ZoteCore events...");
        pluginManager.registerEvents(new EventListener(), plugin);
        if (SnowfallEverywhere.isActive) {
            pluginManager.registerEvents(new SnowfallEverywhere(), plugin);
        }

        List<BehaviorPack> behaviorPacks = BehaviorPack.loadDefinitionsFromDirectories(
                ModLoader.instance.getBehaviorPackDirectories());
        IDRegistry.rebuildNetworkIdMap();
        CustomItem.init();
        RuntimeItemsHooks.register();
        CustomItem.addCreativeItemsBuild();
        CustomItem.addCreativeItemsWeapons();
        CustomItem.addCreativeItems();
        CustomItem.addCreativeItemsNature();

        for (var factory : com.reider745.behavior.entities.EntityContentFactory.factoryMap.values()) {
            if (factory.description.isSpawnable) {
                cn.nukkit.item.Item.addCreativeItem(407, cn.nukkit.item.Item.get(cn.nukkit.item.Item.SPAWN_EGG,
                        factory.getEntityDefinition().getRuntimeId(), 1));
            }
        }

        for (BehaviorPack pack : behaviorPacks) pack.loadRecipes();
        com.reider745.behavior.spawnrules.MobSpawner.start();

        NativeWorkbench.init();
        NativeFurnaceRegistry.init();
        CommandsHelper.init();

        endLoad = true;

        NativeCallback.onLevelCreated();

        if(isSocketEnabled())
            Logger.warning("Socket connections are enabled, it is possible that the connection is not stable");
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

        Logger.critical("Unable to find specified modpack, please enter modpack name, folder name or relative path instead. " +
                        "It is possible that your hosting provider dynamically updates paths, preventing you from specifying absolute one.");
        if (modpacks.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Available modpacks: ");
            boolean requiresComma = false;
            for (ModPack modpack : modpacks) {
                if (requiresComma) {
                    builder.append(", ");
                }
                builder.append(modpack.getRootDirectory().getName());
                requiresComma = true;
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
        return getPropertyBoolean("use-legacy-workbench-override", false);
    }

    public static boolean isUnsafeScriptingAllowed() {
        return getPropertyBoolean("allow-unsafe-scripting", true);
    }

    public static boolean isDebugInnerCoreNetwork() {
        return getPropertyBoolean("network-debug", false);
    }
    public static boolean isDebugLogger() {
        return getPropertyBoolean("debug-logger", false);
    }
    public static boolean isInfoLogger() {
        return getPropertyBoolean("info-logger", false);
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

    public static int getMaxPacketSending(){
        return getPropertyInt("max-packet-sending", 200);
    }

    public static int getAdditionalThreadCount() {
        return getPropertyInt("threading_additional_thread_count", 0);
    }

    public static boolean isUnsupportedOptionsAllowed() {
        return getPropertyBoolean("allow-unsupported-options", false);
    }

    public static MethodHandling getUnsupportedMethodHandling() {
        try {
            return MethodHandling.valueOf(getPropertyString("unsupported-method-handling", "debug").toUpperCase());
        } catch (IllegalArgumentException exc) {
            return MethodHandling.DEBUG;
        }
    }

    public static boolean shouldStoppedOnCriticalTickingError() {
        return getPropertyBoolean("stop-on-critical-ticking-error", true);
    }

    public static boolean shouldPlayersReceiveExceptionForms() {
        return getPropertyBoolean("display-ticking-errors-to-players", true);
    }

    public static boolean canReconvertJava() {
        return getPropertyBoolean("reconvert-java", false);
    }

    public static boolean canOverridePlayerUUID(){
        return getPropertyBoolean("override-player-uuid", true);
    }

    public static String getName() {
        return getPropertyString("pack", "Inner Core");
    }

    public static boolean isSocketEnabled() {
        return InnerCoreServer.getPropertyBoolean("socket-server-enabled", false);
    }

    private static class RemotePackInfo {
        private final String httpRequest;

        private Long lastTimeRequest;
        private int version = 0;

        public RemotePackInfo(String httpRequest) {
            this.httpRequest = httpRequest;
        }

        public int getVersion() {
            if (lastTimeRequest == null || System.currentTimeMillis() - lastTimeRequest >= 1000 * 60 * 15) {
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(httpRequest).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try (InputStream is = connection.getInputStream()) {
                            final JSONObject manifest = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                            version = manifest.optInt("packVersionCode", version);
                        }
                    }
                } catch (Exception ignore) {
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                if (version != 0) {
                    lastTimeRequest = System.currentTimeMillis();
                }
            }
            return version;
        }
    }

    private static Map<String, RemotePackInfo> REMOTE_PACKS = Map.of(
        "latest", new RemotePackInfo("https://gitlab.com/zhekasmirnov/horizon-cloud-config/-/raw/master/innercore64/manifest.json?ref_type=heads"),
        "latest-arm32", new RemotePackInfo("https://gitlab.com/zhekasmirnov/horizon-cloud-config/-/raw/master/innercore/manifest.json?ref_type=heads"),
        "latest-legacy", new RemotePackInfo("https://gitlab.com/zhekasmirnov/horizon-cloud-config/-/raw/master/innercore-legacy/manifest.json?ref_type=heads")
    );

    public static String getVersionName() {
        return getPropertyString("pack-version", "2.4.0b122 or higher");
    }

    public static int getVersionCode() {
        return 163; // please make sure that it is supported on most platforms
    }

    public static boolean canConnectionFor(int clientVersion) {
        List<Object> specs = getVersionCodeSpecs();
        if (specs.size() == 0 || specs.contains(clientVersion)) {
            return true;
        }
        if (specs.contains(-clientVersion)) {
            return false;
        }
        for (Object spec : specs) {
            if (spec instanceof int[] range) {
                if (range[0] == 0) {
                    if (clientVersion < range[1]) {
                        return true;
                    }
                } else if (range[1] == 0) {
                    if (clientVersion >= range[0]) {
                        return true;
                    }
                } else if (clientVersion >= range[0] && clientVersion <= range[1]) {
                    return true;
                }
            } else if (spec instanceof String revision) {
                RemotePackInfo info = REMOTE_PACKS.get(revision);
                if (info != null && clientVersion == info.getVersion()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Object> getVersionCodeSpecs() {
        String specs = getPropertyString("pack-version-code", ">" + getVersionCode());
        String[] supportedRanges = specs.replaceAll("\\s+", "").split(",");

        List<Object> versions = new ArrayList<>();
        if (Arrays.asList(supportedRanges).contains("any")) {
            return versions;
        }

        for (String spec : supportedRanges) {
            if (spec.length() == 0) {
                continue;
            }
            try {
                switch (spec.charAt(0)) {
                    case '<':
                    case '>':
                    case '!':
                        int code = Integer.parseInt(spec.substring(1));
                        if (spec.charAt(0) == '!') {
                            versions.add(-code);
                        } else {
                            versions.add(
                                spec.charAt(0) == '>'
                                    ? new int[] { code, 0 }
                                    : new int[] { 0, code }
                            );
                        }
                        continue;
                }
                if (spec.contains("-")) {
                    String[] range = spec.split("-", 2);
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    versions.add(new int[] { min, max });
                    continue;
                }
                versions.add(Integer.parseInt(spec));
            } catch (NumberFormatException ex) {
                if (!spec.contains("-") && spec.matches("[a-zA-Z0-9_-]+")) {
                    versions.add(spec);
                } else {
                    Logger.error("Invalid `pack-version-code` format spec: " + spec + " (" + ex.getMessage() + ")");
                }
            }
        }
        return versions;
    }

    private static void handleUnsupportedMethod(String message) {
        MethodHandling handling = getUnsupportedMethodHandling();
        if (handling != MethodHandling.NONE) {
            switch (handling) {
                case DEBUG -> Logger.message(message);
                case WARNING -> Logger.warning(message);
                case RAISE -> throw new RuntimeException(message);
                case TRACE -> printStackTrace(message);
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

    public static void warning(String message) {
        handleUnsupportedMethod("Warning: " + message);
    }

    public static void printStackTrace(String message) {
        try {
            throw Context.reportRuntimeError(message);
        } catch (Exception e) {
            System.out.println(ICLog.getStackTrace(e));
        }
    }

    public static void printStackTrace() {
        printStackTrace("Trace");
    }
}
