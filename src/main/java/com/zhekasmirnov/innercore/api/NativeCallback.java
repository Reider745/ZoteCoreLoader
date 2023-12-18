package com.zhekasmirnov.innercore.api;

import android.util.Pair;
import cn.nukkit.level.Level;

import com.reider745.InnerCoreServer;
import com.reider745.entity.EntityMethod;
import com.reider745.event.EventListener;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.adapter.innercore.game.Minecraft;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockBreakResult;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.ecs.ECS;
import com.zhekasmirnov.apparatus.ecs.types.LocalTicking;
import com.zhekasmirnov.apparatus.ecs.types.ServerTicking;
import com.zhekasmirnov.apparatus.mcpe.NativePlayer;
import com.zhekasmirnov.apparatus.mcpe.NativeWorkbenchContainer;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.ecs.core.EntitySystem;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.commontypes.FullBlock;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ScriptableParams;
import com.zhekasmirnov.innercore.api.entities.NativePathNavigation;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI.IDRegistry;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipe;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeRegistry;
import com.zhekasmirnov.innercore.api.mod.ui.window.WindowProvider;
import com.zhekasmirnov.innercore.api.mod.util.InventorySource;
import com.zhekasmirnov.innercore.api.runtime.*;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.other.WorldGen;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;
import com.zhekasmirnov.innercore.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by zheka on 25.06.2017.
 */

public class NativeCallback {
    public static final String LOGGER_TAG = "INNERCORE-CALLBACK";

    private static final Object[] EMPTY_ARGS = new Object[0];

    /* FALLBACK */

    public static void onCallbackExceptionOccurred(String callback, Throwable error) {
        error.printStackTrace();
        ICLog.e(LOGGER_TAG, "uncaught error occurred in callback " + callback, error);
    }

    public static String getStringParam(String name) {
        return InnerCoreServer.getStringParam(name);
    }

    /* TECHNICAL CALLBACKS */

    public static void onNativeGuiLoaded() {
        // MinecraftActivity.onNativeGuiLoaded();
    }

    public static void onCopyrightCheck() {
        InnerCoreServer.server.getLogger()
                .info("Inner Core is developed fully and only by zheka_smirnov (zheka2304), all rights are reserved.");
    }

    public static void onToastRequested() {
        Logger.debug("Native Toast: " + getStringParam("toast"));
    }

    public static void onDialogRequested() {
        Logger.debug(getStringParam("dialog_title"), getStringParam("dialog_text"));
    }

    public static void onDebugLog() {
        ICLog.d("NATIVE-DEBUG", "" + getStringParam("_log"));
        ICLog.flush();
    }

    // used for legacy method, that will return all entities
    private static final Set<Long> allEntities = new HashSet<>(256);

    public static Collection<Long> getAllEntities() {
        return allEntities;
    }

    // called before minecraft final initialization
    public static void onFinalInitStarted() {
        EngineConfig.reload();
        if (EngineConfig.isDeveloperMode()) {
            ICLog.i("NativeProfiling",
                    "developer mode is enabled - turning on native callback profiling and signal handling");
            // Profiler.setCallbackProfilingEnabled(true);
            // Profiler.setExtremeSignalHandlingEnabled(true);
        } else {
            ICLog.i("NativeProfiling",
                    "developer mode is disabled - turning off native callback profiling and signal handling");
            // Profiler.setCallbackProfilingEnabled(false);
            // Profiler.setExtremeSignalHandlingEnabled(false);
        }

        // NativeAPI.setDebugDumpDirectory(InnerCoreConfig.getBool("create_debug_dump")
        // ? FileTools.DIR_HORIZON + "logs/" : null);

        NameTranslation.loadBuiltinTranslations();
    }

    // called when minecraft initialization is complete
    public static void onFinalInitComplete() {
        UIUtils.initialize(UIUtils.getContext());

        AsyncModLauncher modLauncher = new AsyncModLauncher();
        modLauncher.launchModsInCurrentThread();
    }

    public static void onMinecraftAppSuspended() {
        Callback.invokeAPICallback("AppSuspended");
    }

    public static void onKeyEventDispatched(int key, int state) {
        Callback.invokeAPICallback("SystemKeyEventDispatched", key, state);
        if (key == 0 && state == 1) {
            Callback.invokeAPICallback("NavigationBackPressed");
            if (!NativeAPI.isDefaultPrevented()) {
                WindowProvider.instance.onBackPressed();
            }
        }
    }

    /* LEVEL CALLBACKS */

    // called before entering the world, when directory is known, but nothing was
    // loaded yet
    public static void onLocalServerStarted() {
        // reset server thread
        isFirstServerTick = true;

        // cleanup and rebuild id conversion maps (network and local)
        // NativeIdPlaceholderGenerator.clearAll();
        // NativeIdConversionMap.clearAll();
        IdConversionMap.getSingleton().clearLocalIdMap();
        VanillaIdConversionMap.getSingleton().reloadFromAssets();
        IDRegistry.rebuildNetworkIdMap();

        // get world name and directory
        String worldName = getStringParam("world_name");
        String worldDir = getStringParam("world_dir");
        // File worldDirFile = new File(FileTools.DIR_PACK, "worlds/" + worldDir);
        File worldDirFile = new File(getStringParam("path_for_world"));
        LevelInfo.onEnter(worldName, worldDir);

        // add resource packs to world
        // TODO: ModLoader.instance.addResourceAndBehaviorPacksInWorld(worldDirFile);

        // initialize new world data saver instance
        WorldDataSaverHandler.getInstance().onLevelSelected(worldDirFile);

        // clear queues and updatables
        MainThreadQueue.localThread.clearQueue();
        MainThreadQueue.serverThread.clearQueue();
        Updatable.cleanUpAll();

        // refresh translations
        NameTranslation.refresh(false);
        NativeCustomEnchant.updateAllEnchantsDescriptions();

        // invoke callbacks
        Minecraft.onLevelSelected();
        Callback.invokeAPICallback("LevelSelected", worldName, worldDir);
    }

    private static boolean isFirstServerTick = true;
    private static boolean isServerTickDisabledDueToError = false;
    private static int globalServerTickCounter = 0;

    private static int currentPlayerDimension = 0;

    // this is called, when ServerLevel is constructed
    public static void onLevelCreated() {
        // reset server tick
        isFirstServerTick = true;

        // invoke callback
        Callback.invokeAPICallback("LevelCreated");
    }

    private static boolean isLevelDisplayed = false;

    public static boolean isLevelDisplayed() {
        return isLevelDisplayed;
    }

    @Deprecated // this is called, when level renderer is set up
    public static void onLevelDisplayed() {
        isLevelDisplayed = true;
        Callback.invokeAPICallback("LevelDisplayed");

        // TODO: remove this filth
        Minecraft.onLevelDisplayed();
    }

    // read config and setup thread priorities
    private static void setupThreadPriorityFromConfig() {
        TickExecutor.getInstance()
                .setAdditionalThreadCount(InnerCoreConfig.getInt("threading.additional_thread_count", 0));
        // we divide by 4 to fit into standard 1-40 priority range
        TickExecutor.getInstance()
                .setAdditionalThreadPriority(InnerCoreConfig.getInt("threading.additional_thread_priority", 12) / 4);
        if (InnerCoreConfig.getBool("threading.advanced")) {
            NativeAPI.setNativeThreadPriorityParams(
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.priority_low", 1)),
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.priority_high", 40)),
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.threshold_fps", 45)));
        } else {
            NativeAPI.setNativeThreadPriorityParams(
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.priority_simple", 1)),
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.priority_simple", 1) + 10),
                    InnerCoreConfig.convertThreadPriority(InnerCoreConfig.getInt("threading.threshold_fps", 45)));
        }
    }

    // called before first server tick for local world and when connected to remote
    // world
    private static void onLevelPostLoaded(boolean isServer) {
        // clear queues
        MainThreadQueue.localThread.clearQueue();
        MainThreadQueue.serverThread.clearQueue();

        // run pre-loaded callback
        Callback.invokeAPICallback("LevelPreLoaded", isServer);
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelPreLoaded");
        } else {
            Callback.invokeAPICallback("RemoteLevelPreLoaded");
        }

        // initialize data saver
        WorldDataSaverHandler.getInstance().onLevelLoading();

        // if this is local world, start server
        if (isServer) {
            Network.getSingleton().startLanServer();
        }

        // setup name overrides (mostly translations)
        NameTranslation.refresh(true);

        // change level info state
        LevelInfo.onLoaded();

        // run level loaded callbacks
        Callback.invokeAPICallback("LevelLoaded", isServer);
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelLoaded");
        } else {
            Callback.invokeAPICallback("RemoteLevelLoaded");
        }

        // setup server tick priority
        setupThreadPriorityFromConfig();
    }

    // this is local callback!
    public static void onDimensionChanged(int current, int last) {
        if (current != last) {
            Callback.invokeAPICallback("DimensionUnloaded", last);
        }
        currentPlayerDimension = current;
        Callback.invokeAPICallback("DimensionLoaded", current, last);
        ICLog.d(LOGGER_TAG, "player entered dimension " + current + " from " + last);
    }

    public static void onGameStopped(boolean isServer) {
        // reset ticking
        if (isServer) {
            // reset server tick
            globalServerTickCounter = 0;
            isFirstServerTick = true;
            isServerTickDisabledDueToError = false;
        } else {
            // reset local tick
            isFirstLocalTick = true;
            isLocalTickDisabledDueToError = false;
            isLevelDisplayed = false;
        }

        // check, if we are required to call legacy callback: if we are the server, call
        // it for server leave game, otherwise call for client
        boolean callLegacyCallback = isServer == (Minecraft.getLastWorldState() == Minecraft.GameState.HOST_WORLD);

        // log this event
        ICLog.i(LOGGER_TAG, "Shutting down " + (isServer ? "server" : "client") + " level, world state: "
                + Minecraft.getLastWorldState() + (callLegacyCallback ? " (legacy callback will be called here)" : ""));

        // legacy callback for server pre-left
        if (callLegacyCallback) {
            Callback.invokeAPICallback("LevelPreLeft", isServer);
        }

        // new pre-left callback
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelPreLeft");
        } else {
            Callback.invokeAPICallback("LocalLevelPreLeft");
        }

        // reset networking
        if (isServer) {
            Network.getSingleton().shutdownServer();
        } else {
            Network.getSingleton().shutdownClient();
        }
        NativeNetworking.onLevelLeft(isServer);
        NetworkPlayerRegistry.getSingleton().onGameLeft(isServer);
        // NativeIdPlaceholderGenerator.clearAll();

        // on server side save all data
        if (isServer) {
            WorldDataSaverHandler.getInstance().onLevelLeft();
        }

        // legacy callback for server left
        if (callLegacyCallback) {
            Callback.invokeAPICallback("LevelLeft", isServer);
            Callback.invokeAPICallback("GameLeft", isServer);
        }

        // main callback
        if (isServer) {
            // new callback for server left
            Callback.invokeAPICallback("ServerLevelLeft");
        } else {
            // DimensionLoaded and DimensionUnloaded are client callbacks
            Callback.invokeAPICallback("DimensionUnloaded", currentPlayerDimension);
            // new callback for server left
            Callback.invokeAPICallback("LocalLevelLeft");
        }

        // change state to non-world
        // this will also call LevelInfo.onLeft() to reset it on right thread
        Minecraft.onGameStopped(isServer);

        // cleanup updatables
        if (isServer) {
            Updatable.getForServer().cleanUp();
        } else {
            Updatable.getForClient().cleanUp();
        }

        // clear legacy entity array
        if (isServer) {
            allEntities.clear();
        }

        // reset more server side native modules
        if (isServer) {
            // path navigation module for entities
            NativePathNavigation.cleanup();

            // block source by dimension cache for server ticking thread
            NativeBlockSource.resetDefaultBlockSources();

            // id conversion map - it will be cleared on next level load or connection
            // anyway, so clear it only on server side to not fuck up last server tick
            // NativeIdConversionMap.clearAll();
            IdConversionMap.getSingleton().clearLocalIdMap();
        }
    }

    static {
        new TPSMeter("main-thread", 20, 2000);
        new TPSMeter("mod-thread", 20, 2000);
    }

    private static void setupWorld() {
        int updatableMode = (boolean) InnerCoreConfig.get("performance.time_based_limit") ? Updatable.MODE_TIME_BASED
                : Updatable.MODE_COUNT_BASED;
        Updatable.setPreferences(updatableMode,
                updatableMode == Updatable.MODE_COUNT_BASED ? InnerCoreConfig.getInt("performance.max_update_count")
                        : InnerCoreConfig.getInt("performance.max_update_time"));
    }

    private static boolean isLocalTickDisabledDueToError = false;
    private static boolean isFirstLocalTick = true;

    public static boolean isLocalTickDisabledDueToError() {
        return isLocalTickDisabledDueToError;
    }

    private static final EntitySystem localTickingSystem = new EntitySystem.Of1<LocalTicking>(LocalTicking.class,
            "tag:world") {
        public void accept(int entity, LocalTicking c1) {
            c1.tick(queue);
        }
    };

    public static void onLocalTick() {
        // run for first local tick
        if (isFirstLocalTick) {
            isFirstLocalTick = false;
            // setup thread check
            ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.CLIENT);

            // set local player uid for network client
            Network.getSingleton().getClient().setPlayerUid(NativeAPI.getLocalPlayer());

            // invoke callback
            Callback.invokeAPICallback("LocalLevelLoaded");

            // call dimension change on first open
            int dimension = NativeAPI.getEntityDimension(NativeAPI.getLocalPlayer());
            onDimensionChanged(dimension, dimension);
        } else {
            // check for dimension change
            int dimension = NativeAPI.getEntityDimension(NativeAPI.getLocalPlayer());
            if (dimension != currentPlayerDimension) {
                onDimensionChanged(dimension, currentPlayerDimension);
            }
        }

        // update inventory for UI
        // if (!NativeAPI.isLocalServerRunning()) {
        // TODO: InventorySource.tick();
        // }

        // update local player handler
        NetworkPlayerRegistry.getSingleton().onLocalTick();

        if (!isLocalTickDisabledDueToError) {
            // call normal tick
            try {
                // callback
                Callback.invokeAPICallbackUnsafe("LocalTick", EMPTY_ARGS);

                // updatables
                Updatable.getForClient().onTickSingleThreaded();

                // ecs
                localTickingSystem.run(ECS.getEntityManager());
            } catch (Throwable e) {
                // report error if needed
                ICLog.e("INNERCORE-CALLBACK", "error occurred in local tick callback", e);
                isLocalTickDisabledDueToError = true;
            }
        }

        // execute local tick queue
        MainThreadQueue.localThread.executeQueue();
    }

    public static boolean isServerTickDisabledDueToError() {
        return isServerTickDisabledDueToError;
    }

    public static int getGlobalServerTickCounter() {
        return globalServerTickCounter;
    }

    private static final EntitySystem serverTickingSystem = new EntitySystem.Of1<ServerTicking>(ServerTicking.class,
            "tag:world") {
        public void accept(int entity, ServerTicking c1) {
            c1.tick(queue);
        }
    };

    public static void onTick() {
        // for the first tick
        if (isFirstServerTick) {
            globalServerTickCounter = 0;
            isFirstServerTick = false;

            // setup thread check
            ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);

            // run post loaded callback and actions
            onLevelPostLoaded(true);

            // final setup
            setupWorld();
        }

        // call mod tick
        if (!isServerTickDisabledDueToError) {
            try {
                // tick internal systems
                InventorySource.tick();
                // ArmorRegistry.onTick();
                WorldDataSaverHandler.getInstance().onTick();

                // run callback and updatable
                TickExecutor executor = TickExecutor.getInstance();
                if (executor.isAvailable()) {
                    executor.execute(Callback.getCallbackAsRunnableList("tick", EMPTY_ARGS));
                } else {
                    Callback.invokeAPICallbackUnsafe("tick", EMPTY_ARGS);
                }
                Updatable.getForServer().onTick();
                executor.blockUntilExecuted();
                Updatable.getForServer().onPostTick();

                // run ecs
                serverTickingSystem.run(ECS.getEntityManager());
            } catch (Throwable e) {
                isServerTickDisabledDueToError = true;
                ICLog.e("INNERCORE-CALLBACK", "error occurred in server tick callback", e);
            }
        }

        // call server player handlers
        NetworkPlayerRegistry.getSingleton().onTick();

        // execute all stuff posted on server thread
        MainThreadQueue.serverThread.executeQueue();

        // increment tick counter
        globalServerTickCounter++;
    }

    @Deprecated
    public static void onConnectToHost(int port) {
        InnerCoreServer.useNotSupport("NativeCallback.onConnectToHost(port)");
    }

    private static boolean isDestroyBlockCallbackInProgress = false;

    public static void onBlockDestroyed(int x, int y, int z, int side, long player) {
        // guard
        if (isDestroyBlockCallbackInProgress) {
            return;
        }
        isDestroyBlockCallbackInProgress = true;

        // check, if we can get block source
        NativeBlockSource blockSource = NativeBlockSource.getDefaultForActor(player);
        if (blockSource == null) {
            return;
        }

        // BreakBlock callback is called when block is destroyed by player or by block
        // source method, or by any other source in the future maybe
        // it will receive parameters:
        // - BlockSource where the block was destroyed
        // - x, y, z object with optional side parameter
        // - block state of the breaking block
        // - actor - uid of breaking actor or -1 if no actor is passed
        // - item instance of the item used to destroy the block or null if no item is
        // passed
        BlockState block = blockSource.getBlock(x, y, z);
        Callback.invokeAPICallback("BreakBlock",
                blockSource,
                new Coords(x, y, z, side),
                block,
                EngineConfig.isDeveloperMode() || new NativePlayer(player).getGameMode() != 1,
                player,
                new ItemInstance(EntityMethod.getEntityCarriedItem(player)));

        // DestroyBlock callback is called, when player breaks the block, this is
        // considered legacy callback
        if (!NativeAPI.isDefaultPrevented()) {
            Callback.invokeAPICallback("DestroyBlock", new Coords(x, y, z, side), block, player);
        }

        if (!NativeAPI.isDefaultPrevented()) {
            // if it was not vanilla block, destroy it without any drop
            int id = blockSource.getBlockId(x, y, z);
            if (id >= 2048) {
                blockSource.destroyBlock(x, y, z, false);
            }
        }

        // if block was already destroyed, prevent default action
        if (blockSource.getBlockId(x, y, z) == 0) {
            NativeAPI.preventDefault();
        }

        // guard end
        isDestroyBlockCallbackInProgress = false;
    }

    @Deprecated
    public static void onBlockDestroyStarted(int x, int y, int z, int side) {
        InnerCoreServer.useNotSupport("NativeCallback.onBlockDestroyStarted(x, y, z, side)");
    }

    @Deprecated // fix callback
    public static void _onBlockDestroyStarted(int x, int y, int z, int side) {
        InnerCoreServer.useNotSupport("NativeCallback._onBlockDestroyStarted(x, y, z, side)");
    }

    @Deprecated
    public static void onBlockDestroyContinued(int x, int y, int z, int side, float progress) {
        InnerCoreServer.useNotSupport("NativeCallback.onBlockDestroyContinued(x, y, z, side, progress)");
    }

    public static void onBlockBuild(int x, int y, int z, int side, long player) {
        Callback.invokeAPICallback("BuildBlock", new Coords(x, y, z, side), new FullBlock(player, x, y, z), player);
    }

    @Deprecated
    public static void onBlockChanged(int x, int y, int z, int id1, int data1, int id2, int data2, int i1, int i2,
            long region) {
        InnerCoreServer.useNotSupport("NativeCallback.onBlockChanged(x, y, z, id1, data1, id2, data2, i1, i2, region)");
    }

    public static void onBlockChanged(int x, int y, int z, int id1, int data1, int id2, int data2, int i1, int i2,
            Level level) {
        Callback.invokeAPICallback("BlockChanged", new Coords(x, y, z), new FullBlock(id1, data1),
                new FullBlock(id2, data2), i1, i2, NativeBlockSource.getFromServerCallbackPointer(level));
    }

    public static void onItemUsed(int x, int y, int z, int side, float fx, float fy, float fz, boolean isServer,
            boolean isExternal, long player) {
        // initialize coordinates
        Coords coords = new Coords(x, y, z, side);
        coords.put("vec", coords, new Coords(fx, fy, fz));

        if (isServer) {
            // call server callback
            ItemInstance item = new ItemInstance(EntityMethod.getEntityCarriedItem(player));
            FullBlock block = new FullBlock(player, x, y, z);
            Callback.invokeAPICallback("ItemUse", coords, item, block, isExternal, player);
            Callback.invokeAPICallback("ItemUseServer", coords, item, block, player);
        } else {
            // call client callback
            ItemInstance item = new ItemInstance(EntityMethod.getEntityCarriedItem(player));
            FullBlock block = new FullBlock(NativeBlockSource.getCurrentClientRegion(), x, y, z);
            Callback.invokeAPICallback("ItemUseLocal", coords, item, block, player);
            Callback.invokeAPICallback("ItemUseLocalServer", coords, item, block, false, player);
        }
    }

    public static void onExplode(float x, float y, float z, float power, long entity, boolean b1, boolean b2,
            float anotherFloat) {
        Callback.invokeAPICallback("Explosion", new Coords(x, y, z),
                new ScriptableParams(
                        new Pair<String, Object>("power", power),
                        new Pair<String, Object>("entity", entity),
                        new Pair<String, Object>("onFire", b1),
                        new Pair<String, Object>("someBool", b2),
                        new Pair<String, Object>("someFloat", anotherFloat)));
    }

    /* PLAYER CALLBACKS */

    public static void onPlayerEat(int food, float ratio, long player) {
        Callback.invokeAPICallback("FoodEaten", food, ratio, player);
        NetworkPlayerRegistry.getSingleton().onPlayerEat(player, food, ratio);
    }

    public static void onPlayerExpAdded(int exp, long player) {
        Callback.invokeAPICallback("ExpAdd", exp, player);
    }

    public static void onPlayerLevelAdded(int level, long player) {
        Callback.invokeAPICallback("ExpLevelAdd", level, player);
    }

    @Deprecated
    public static void onCommandExec() {
        String command = getStringParam("command");
        Callback.invokeAPICallback("NativeCommand", command == null ? null : command.trim());
    }

    /* ENTITY CALLBACKS */

    public static void onEntityAttacked(long entity, long attacker) {
        Callback.invokeAPICallback("PlayerAttack", attacker, entity);
    }

    public static void onInteractWithEntity(long entity, long player, float x, float y, float z) {
        Callback.invokeAPICallback("EntityInteract", entity, player, new Coords(x, y, z));
    }

    // handling legacy block breaking logic, that manually drops items and
    // experience into world by intercepting
    // new dropped items & orbs and instantly removing them, adding them to drop
    // result
    // note: this is not thread safe, so it needs to be synchronized from
    // startOverrideBlockBreakResult to endOverrideBlockBreakResult

    private static BlockBreakResult currentBlockBreakResultOverride = null;
    private static final int legacyBreakResultOverrideDroppedItemEntityId = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("entity_type", "item"));
    private static final int legacyBreakResultOverrideExpOrbEntityId = GameEnums
            .getInt(GameEnums.getSingleton().getEnum("entity_type", "experience_orb"));

    public static void startOverrideBlockBreakResult() {
        currentBlockBreakResultOverride = new BlockBreakResult();
    }

    public static BlockBreakResult endOverrideBlockBreakResult() {
        BlockBreakResult result = currentBlockBreakResultOverride;
        currentBlockBreakResultOverride = null;
        return result;
    }

    public static void onEntityAdded(long entity) {
        // when breaking block with legacy mod logic, when getting dropped items, add
        // them to result and then remove immediately
        if (currentBlockBreakResultOverride != null) {
            int type = StaticEntity.getType(entity);
            if (type == legacyBreakResultOverrideDroppedItemEntityId) {
                currentBlockBreakResultOverride.getItems().add(StaticEntity.getDroppedItem(entity));
                NativeAPI.removeEntity(entity);
                return;
            } else if (type == legacyBreakResultOverrideExpOrbEntityId) {
                currentBlockBreakResultOverride.addExperience(StaticEntity.getExperienceOrbValue(entity));
                NativeAPI.removeEntity(entity);
                return;
            }
        }

        // add entity to allEntities set
        allEntities.add(entity);

        // invoke js callback
        Callback.invokeAPICallback("EntityAdded", entity);
    }

    @Deprecated
    public static void onLocalEntityAdded(long entity) {
        Callback.invokeAPICallback("EntityAddedLocal", entity);
    }

    public static void onEntityRemoved(long entity) {
        // when handling breaking block with legacy mod logic, ignore all dropped items
        // and experience orbs
        if (currentBlockBreakResultOverride != null) {
            int type = StaticEntity.getType(entity);
            if (type == legacyBreakResultOverrideDroppedItemEntityId
                    || type == legacyBreakResultOverrideExpOrbEntityId) {
                return;
            }
        }

        // invoke js callback
        Callback.invokeAPICallback("EntityRemoved", entity);

        // remove from allEntities set
        allEntities.remove(entity);
    }

    @Deprecated
    public static void onLocalEntityRemoved(long entity) {
        Callback.invokeAPICallback("EntityRemovedLocal", entity);
    }

    public static void onEntityPickUpDrop(long entity, long dropEntity, int count) {
        ItemInstance dropStack = new ItemInstance(new NativeItemInstance(EntityMethod.getItemFromDrop(dropEntity)));
        Callback.invokeAPICallback("EntityPickUpDrop", entity, dropEntity, dropStack, count);
    }

    @Deprecated
    public static void onExpOrbsSpawned(long region, int amount, float x, float y, float z, long player) {
        InnerCoreServer.useNotSupport("NativeCallback.onExpOrbsSpawned(region, amount, x, y, z, player)");
    }

    public static void onExpOrbsSpawned(Level level, int amount, float x, float y, float z, long player) {
        Callback.invokeAPICallback("ExpOrbsSpawned", NativeBlockSource.getFromServerCallbackPointer(level), amount,
                new Coords(x, y, z), player);
    }

    public static void onEntityDied(long entity, long attacker, int damageType) {
        Callback.invokeAPICallback("EntityDeath", entity, attacker, damageType);
    }

    public static void onEntityHurt(long entity, long attacker, int damageType, int damageValue, boolean someBool1,
            boolean someBool2) {
        Callback.invokeAPICallback("EntityHurt", attacker, entity, damageValue, damageType, someBool1, someBool2);

        NetworkPlayerRegistry.getSingleton().onEntityHurt(entity, attacker, damageType, damageValue, someBool1,
                someBool2);

        // if (entity == NativeAPI.getPlayer()) {
        // ArmorRegistry.onHurt(attacker, damageValue, damageType, someBool1,
        // someBool2);
        // }
    }

    public static void onThrowableHit(long projectile, float hitX, float hitY, float hitZ, long entity, int blockX,
            int blockY, int blockZ, int blockSide, int itemId, int itemCount, int itemData, long itemExtra) {
        Callback.invokeAPICallback("ProjectileHit", projectile,
                new ItemInstance(itemId, itemCount, itemData, NativeItemInstanceExtra.getExtraOrNull(itemExtra)),
                new ScriptableParams(
                        new Pair<String, Object>("x", hitX),
                        new Pair<String, Object>("y", hitY),
                        new Pair<String, Object>("z", hitZ),
                        new Pair<String, Object>("entity", entity),
                        new Pair<String, Object>("coords",
                                blockX == 0 && blockY == 0 && blockZ == 0 && blockSide == 0 ? null
                                        : new Coords(blockX, blockY, blockZ, blockSide))));
    }

    public static void onPathNavigationDone(long entity, int result) {
        NativePathNavigation.onNavigationResult(entity, result);
    }

    /* BLOCK CALLBACKS */

    @Deprecated
    public static void onRedstoneSignalChange(int x, int y, int z, int signal, boolean isLoadingChange, long region) {
        InnerCoreServer
                .useNotSupport("NativeCallback.onRedstoneSignalChange(x, y, z, signal, isLoadingChange, region)");
    }

    public static void onRedstoneSignalChange(int x, int y, int z, int signal, boolean isLoadingChange, Level level) {
        NativeBlockSource blockSource = NativeBlockSource.getFromServerCallbackPointer(level);
        Callback.invokeAPICallback("RedstoneSignal", new Coords(x, y, z), new ScriptableParams(
                new Pair<String, Object>("power", signal),
                new Pair<String, Object>("signal", signal),
                new Pair<String, Object>("onLoad", isLoadingChange)), new FullBlock(blockSource, x, y, z), blockSource);
    }

    @Deprecated
    public static void onRandomBlockTick(int x, int y, int z, int id, int data, long region) {
        InnerCoreServer.useNotSupport("NativeCallback.onRandomBlockTick(x, y, z, id, data, region)");
    }

    public static void onRandomBlockTick(int x, int y, int z, int id, int data, Level level) {
        NativeBlock.onRandomTickCallback(x, y, z, id, data, NativeBlockSource.getFromServerCallbackPointer(level));
    }

    public static void onAnimateBlockTick(int x, int y, int z, int id, int data) {
        NativeBlock.onAnimateTickCallback(x, y, z, id, data);
    }

    @Deprecated
    public static void onBlockSpawnResources(int x, int y, int z, int id, int data, float f, int i, long region) {
        InnerCoreServer.useNotSupport("NativeCallback.onBlockSpawnResources(x, y, z, id, data, f, i, region)");
    }

    public static void onBlockSpawnResources(int x, int y, int z, int id, int data, float f, int i, Level level) {
        Callback.invokeAPICallback("PopBlockResources", new Coords(x, y, z), new FullBlock(id, data), (double) f, i,
                NativeBlockSource.getFromServerCallbackPointer(level));
    }

    public static void onBlockEventEntityInside(int x, int y, int z, long entity) {
        Callback.invokeAPICallback("BlockEventEntityInside", new Coords(x, y, z), new FullBlock(entity, x, y, z),
                entity);
    }

    public static void onBlockEventEntityStepOn(int x, int y, int z, long entity) {
        Callback.invokeAPICallback("BlockEventEntityStepOn", new Coords(x, y, z), new FullBlock(entity, x, y, z),
                entity);
    }

    @Deprecated
    public static void onBlockEventNeighbourChange(int x, int y, int z, int changedX, int changedY, int changedZ,
            long region) {
        InnerCoreServer.useNotSupport(
                "NativeCallback.onBlockEventNeighbourChange(x, y, z, changedX, changedY, changedZ, region)");
    }

    public static void onBlockEventNeighbourChange(int x, int y, int z, int changedX, int changedY, int changedZ,
            Level level) {
        NativeBlockSource blockSource = NativeBlockSource.getFromServerCallbackPointer(level);
        Callback.invokeAPICallback("BlockEventNeighbourChange", new Coords(x, y, z),
                new FullBlock(blockSource, x, y, z), new Coords(changedX, changedY, changedZ), blockSource);
    }

    public static void onCustomTessellation(long tessellator, int x, int y, int z, int id, int data, boolean b) {
        NativeBlockRenderer.onRenderCall(new NativeBlockRenderer.RenderAPI(tessellator), new Coords(x, y, z),
                new FullBlock(id, data), b);
    }

    /* ITEM CALLBACKS */

    @Deprecated
    public static void onItemIconOverride(int id, int count, int data, int extra) {
        synchronized (NativeItem.DYNAMIC_ICON_LOCK) {
            Callback.invokeAPICallback("ItemIconOverride",
                    new ItemInstance(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra)), false);
        }
    }

    @Deprecated
    public static void onItemModelOverride(long modelPtr, int id, int count, int data, long extra) {
        NativeItemModel model = NativeItemModel.getByPointer(modelPtr);
        if (model != null) {
            try {
                NativeItemModel override = model.getModelForItemInstance(id, count, data,
                        NativeItemInstanceExtra.getExtraOrNull(extra));
                if (override != model) {
                    NativeAPI.overrideItemModel(override != null ? override.pointer : 0);
                }
            } catch (Throwable err) {
                ICLog.e("INNERCORE-CALLBACK", "error occurred in model override callback", err);
            }
        }
    }

    @Deprecated
    public static void onItemNameOverride(int id, int count, int data, int extra) {
        String name = getStringParam("name");
        String translated = NameTranslation.translate(name);
        synchronized (NativeItem.DYNAMIC_NAME_LOCK) {
            NativeAPI.overrideItemName(translated);
            Callback.invokeAPICallback("ItemNameOverride",
                    new ItemInstance(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra)), translated, name);
        }
    }

    public static void onItemUsedNoTarget(long player) {
        Callback.invokeAPICallback("ItemUseNoTarget", new ItemInstance(EntityMethod.getEntityCarriedItem(player)),
                player);
    }

    public static void onItemUseReleased(int ticks, long player) {
        Callback.invokeAPICallback("ItemUsingReleased", new ItemInstance(EntityMethod.getEntityCarriedItem(player)),
                ticks, player);
    }

    public static void onItemUseComplete(long player) {
        Callback.invokeAPICallback("ItemUsingComplete", new ItemInstance(EntityMethod.getEntityCarriedItem(player)),
                player);
    }

    @Deprecated
    public static void onItemDispensed(float x, float y, float z, int side, int id, int count, int data, long extra,
            long region, int slot) {
        InnerCoreServer
                .useNotSupport("NativeCallback.onItemDispensed(x, y, z, side, id, count, data, extra, region, slot)");
    }

    public static void onItemDispensed(float x, float y, float z, int side, int id, int count, int data, long extra,
            Level level, int slot) {
        int rx = (int) Math.floor(x);
        int ry = (int) Math.floor(y);
        int rz = (int) Math.floor(z);
        int ix = rx, iy = ry, iz = rz;
        switch (side) {
            case 0:
                iy++;
                break;
            case 1:
                iy--;
                break;
            case 2:
                iz++;
                break;
            case 3:
                iz--;
                break;
            case 4:
                ix++;
                break;
            case 5:
                ix--;
                break;
        }
        Coords coords = new Coords(ix, iy, iz);
        coords.put("relative", coords, new Coords(rx, ry, rz));
        coords.put("vec", coords, new Coords(x, y, z));
        coords.setSide(side);
        Callback.invokeAPICallback("ItemDispensed", coords,
                new ItemInstance(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra)),
                NativeBlockSource.getFromServerCallbackPointer(level), slot);
    }

    /* ENCHANT CALLBACKS */

    public static void onEnchantPostAttack(int enchantId, int damage, long actor1, long actor2) {
        ItemStack item = new ItemStack();
        Callback.invokeAPICallback("EnchantPostAttack", enchantId, item, damage, actor1, actor2);
        if (!NativeAPI.isDefaultPrevented()) {
            NativeCustomEnchant enchant = NativeCustomEnchant.getEnchantById(enchantId);
            NativeCustomEnchant.DoPostAttackListener listener = enchant.getDoPostAttackListener();
            if (listener != null) {
                listener.doPostAttack(item, damage, actor1, actor2);
            }
        }
    }

    public static void onEnchantPostHurt(int enchantId, int itemId, int itemCount, int itemData, long itemExtraPtr,
            int damage, long actor1, long actor2) {
        NativeItemInstanceExtra itemExtra = new NativeItemInstanceExtra(itemExtraPtr);
        ItemStack item = new ItemStack(itemId, itemCount, itemData, itemExtra);
        Callback.invokeAPICallback("EnchantPostHurt", enchantId, item, damage, actor1, actor2);
        if (!NativeAPI.isDefaultPrevented()) {
            NativeCustomEnchant enchant = NativeCustomEnchant.getEnchantById(enchantId);
            NativeCustomEnchant.DoPostAttackListener listener = enchant.getDoPostAttackListener();
            if (listener != null) {
                listener.doPostAttack(item, damage, actor1, actor2);
            }
        }
    }

    public static void onEnchantGetDamageBonus(int enchantId, int damage, long actor) {
        Callback.invokeAPICallback("EnchantGetDamageBonus", enchantId, damage, actor);
        if (!NativeAPI.isDefaultPrevented()) {
            NativeCustomEnchant enchant = NativeCustomEnchant.getEnchantById(enchantId);
            NativeCustomEnchant.AttackDamageBonusProvider provider = enchant.getAttackDamageBonusProvider();
            if (provider != null) {
                float bonus = provider.getDamageBonus(damage, actor);
                NativeCustomEnchant.passCurrentDamageBonus(bonus);
            }
        }
    }

    public static void onEnchantGetProtectionBonus(int enchantId, int damage, int cause, long attackerActor) {
        Callback.invokeAPICallback("EnchantGetProtectionBonus", enchantId, damage, cause, attackerActor);
        if (!NativeAPI.isDefaultPrevented()) {
            NativeCustomEnchant enchant = NativeCustomEnchant.getEnchantById(enchantId);
            NativeCustomEnchant.ProtectionBonusProvider provider = enchant.getProtectionBonusProvider();
            if (provider != null) {
                float bonus = provider.getProtectionBonus(damage, cause, attackerActor);
                NativeCustomEnchant.passCurrentProtectionBonus(bonus);
            }
        }
    }

    /* WORKBENCH CALLBACKS */

    public static void onWorkbenchCraft(long containerPtr, long player, int size) {
        NativeWorkbenchContainer container = new NativeWorkbenchContainer(containerPtr, size, player);

        // if some changes are done to container in this callback, it must call apply,
        // because it is not guaranteed that apply will be called
        Callback.invokeAPICallback("WorkbenchCraft", container);
        WorkbenchRecipe recipe = WorkbenchRecipeRegistry.getRecipeFromField(container, "");

        // if this is inner core recipe, and it was not prevented by callback above,
        // provide it
        if (!NativeAPI.isDefaultPrevented() && recipe != null) {
            // legacy callback before craft
            Callback.invokeCallback("VanillaWorkbenchCraft", recipe.getResult(), container, player, recipe);
            if (!recipe.isVanilla()) {
                // handle modded recipe: replace vanilla craft logic with modded one
                NativeAPI.preventDefault();
                ItemInstance result = recipe.provideRecipe(container);
                if (result != null && result.getId() != 0 && result.getCount() > 0) {
                    new NativePlayer(player).addItemToInventory(result.getId(), result.getCount(), result.getData(),
                            result.getExtra(), true);
                    Callback.invokeCallback("VanillaWorkbenchPostCraft", result, container, player, recipe);
                }
                container.apply();
            } else {
                // handle vanilla recipe: keep vanilla logic after this callback
                Network.getSingleton().getServerThreadJobExecutor().add(() -> {
                    Callback.invokeCallback("VanillaWorkbenchPostCraft", recipe.getResult(), container, player, recipe);
                });
            }
        }
    }

    /* OTHER CALLBACKS */

    @Deprecated
    public static void onScreenChanged(boolean isPushEvent) {
        // get screen name
        String name = getStringParam("screen_name");
        String lastName = getStringParam("last_screen_name");
        Logger.debug("screen changed: " + lastName + " -> " + name + (isPushEvent ? " (pushed)" : " (popped)"));

        // auto-save if this is pause screen
        if (name.equals("leave_level_screen") || name.equals("pause_screen")
                || name.startsWith("world_loading_progress_screen")) {
            WorldDataSaverHandler.getInstance().onPauseScreenOpened();
        }

        // call js callback
        Callback.invokeAPICallback("NativeGuiChanged", name, lastName, isPushEvent);
    }

    /* WORLD GENERATION CALLBACKS */

    public static void onPreChunkPostProcessed(int x, int z) {
        WorldGen.generateChunk(x, z, WorldGen.LEVEL_PRE_VANILLA);
    }

    public static void onChunkPostProcessed(int x, int z) {
        WorldGen.generateChunk(x, z, WorldGen.LEVEL_POST_VANILLA);
    }

    public static void onBiomeMapGenerated(int dimension, int x, int z) {
        WorldGen.onBiomeMapGenerated(dimension, x >> 4, z >> 4);
    }

    public static void onCustomDimensionTransfer(long entity, int from, int to) {
        Callback.invokeAPICallback("CustomDimensionTransfer", entity, from, to);
    }

    /* NETWORK CALLBACKS */

    public static void onModdedServerPacketReceived(int formatId) {
        String name = getStringParam("name");
        String client = getStringParam("client");
        try {
            NativeNetworking.onServerPacketReceived(client, name, formatId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onModdedClientPacketReceived(int formatId) {
        String name = getStringParam("name");
        try {
            NativeNetworking.onClientPacketReceived(name, formatId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
