package com.zhekasmirnov.innercore.api;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.level.Level;

import com.reider745.InnerCoreServer;
import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.adapter.innercore.game.Minecraft;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockBreakResult;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.ecs.ECS;
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
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
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
import com.zhekasmirnov.innercore.api.mod.util.InventorySource;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.*;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.other.WorldGen;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;
import com.zhekasmirnov.innercore.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

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

    public static void onCopyrightCheck() {
        Server.getInstance().getLogger()
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

    /* LEVEL CALLBACKS */

    // called before entering the world, when directory is known,
    // but nothing was loaded yet
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

    // this is called, when ServerLevel is constructed
    public static void onLevelCreated() {
        // reset server tick
        isFirstServerTick = true;

        // invoke callback
        Callback.invokeAPICallback("LevelCreated");
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

    // called before first server tick for local world
    // and when connected to remote world
    private static void onLevelPostLoaded(boolean isServer) {
        if (!isServer) {
            InnerCoreServer.useClientMethod("NativeCallback.onLevelPostLoaded(isServer)");
            return;
        }

        // clear queues
        MainThreadQueue.localThread.clearQueue();
        MainThreadQueue.serverThread.clearQueue();

        // run pre-loaded callback
        Callback.invokeAPICallback("LevelPreLoaded", isServer);
        Callback.invokeAPICallback("ServerLevelPreLoaded");

        // initialize data saver
        WorldDataSaverHandler.getInstance().onLevelLoading();

        // if this is local world, start server
        Network.getSingleton().startLanServer();

        // setup name overrides (mostly translations)
        NameTranslation.refresh(true);

        // change level info state
        LevelInfo.onLoaded();

        // run level loaded callbacks
        Callback.invokeAPICallback("LevelLoaded", isServer);
        Callback.invokeAPICallback("ServerLevelLoaded");

        // setup server tick priority
        setupThreadPriorityFromConfig();
    }

    public static void onGameStopped(boolean isServer) {
        if (!isServer) {
            // reset local tick
            InnerCoreServer.useClientMethod("NativeCallback.onGameStopped(isServer)");
            return;
        }

        // reset server ticking
        globalServerTickCounter = 0;
        isFirstServerTick = true;
        isServerTickDisabledDueToError = false;

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
        Callback.invokeAPICallback("ServerLevelPreLeft");

        // reset networking
        Network.getSingleton().shutdownServer();
        NativeNetworking.onLevelLeft(isServer);
        NetworkPlayerRegistry.getSingleton().onGameLeft(isServer);
        // NativeIdPlaceholderGenerator.clearAll();

        // on server side save all data
        WorldDataSaverHandler.getInstance().onLevelLeft();

        // legacy callback for server left
        if (callLegacyCallback) {
            Callback.invokeAPICallback("LevelLeft", isServer);
            Callback.invokeAPICallback("GameLeft", isServer);
        }

        // new callback for server left
        Callback.invokeAPICallback("ServerLevelLeft");

        // change state to non-world
        // this will also call LevelInfo.onLeft() to reset it on right thread
        Minecraft.onGameStopped(isServer);

        // cleanup updatables
        Updatable.getForServer().cleanUp();

        // clear legacy entity array
        allEntities.clear();

        // reset more server side native modules
        // path navigation module for entities
        NativePathNavigation.cleanup();

        // block source by dimension cache for server ticking thread
        NativeBlockSource.resetDefaultBlockSources();

        // id conversion map - it will be cleared on next level load or connection
        // anyway, so clear it only on server side to not fuck up last server tick
        // TODO: NativeIdConversionMap.clearAll();
        IdConversionMap.getSingleton().clearLocalIdMap();
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
        if (!isServer) {
            // call client callback
            InnerCoreServer.useClientMethod(
                    "NativeCallback.onItemUsed(x, y, z, side, fx, fy, fz, isServer, isExternal, player)");
            return;
        }

        // initialize coordinates
        Coords coords = new Coords(x, y, z, side);
        coords.put("vec", coords, new Coords(fx, fy, fz));

        // call server callback
        ItemInstance item = new ItemInstance(EntityMethod.getEntityCarriedItem(player));
        FullBlock block = new FullBlock(player, x, y, z);
        Callback.invokeAPICallback("ItemUse", coords, item, block, isExternal, player);
        Callback.invokeAPICallback("ItemUseServer", coords, item, block, player);
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

    public static void onServerCommand(String command, float x, float y, float z, long entity) {
        Callback.invokeAPICallback("ServerCommand", command, new Coords(x, y, z), entity);
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
        InnerCoreServer.useClientMethod("NativeCallback.onCommandExec()");
    }

    public static void onPlayerLogin(ConnectedClient client, String username, Consumer<String> acceptor) {
        Callback.invokeAPICallback("ServerPlayerLogin", client, username, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                acceptor.accept(args.length > 0 ? (String) args[0] : null);
                return null;
            }
        });
    }

    /* ENTITY CALLBACKS */

    public static void onEntityAttacked(long entity, long attacker) {
        Callback.invokeAPICallback("PlayerAttack", attacker, entity);
    }

    public static void onInteractWithEntity(long entity, long player, float x, float y, float z) {
        Callback.invokeAPICallback("EntityInteract", entity, player, new Coords(x, y, z));
    }

    // handling legacy block breaking logic, that manually drops items and
    // experience into world by intercepting new dropped items & orbs and
    // instantly removing them, adding them to drop result
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
            int blockY, int blockZ, int blockSide, int itemId, int itemCount, int itemData, NativeItemInstanceExtra itemExtra) {
        Callback.invokeAPICallback("ProjectileHit", projectile,
                new ItemInstance(itemId, itemCount, itemData, itemExtra),
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

    /* ITEM CALLBACKS */

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

    public static void onItemDispensed(float x, float y, float z, int side, int id, int count, int data, NativeItemInstanceExtra extra,
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
                new ItemInstance(id, count, data, extra),
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

    public static void onEnchantPostHurt(int enchantId, int itemId, int itemCount, int itemData, NativeItemInstanceExtra itemExtra,
            int damage, long actor1, long actor2) {
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

    public static void onWorkbenchCraft(CraftingGrid grid, long player, int size) {
        NativeWorkbenchContainer container = new NativeWorkbenchContainer(grid, size, player);

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

    /* WORLD GENERATION CALLBACKS */

    public static void onPreChunkPostProcessed(int x, int z) {
        WorldGen.generateChunk(x, z, WorldGen.LEVEL_PRE_VANILLA);
    }

    public static void onChunkPostProcessed(int x, int z) {
        WorldGen.generateChunk(x, z, WorldGen.LEVEL_POST_VANILLA);
    }

    public static void onBiomeMapGenerated(int dimension, int x, int z) {
        WorldGen.onBiomeMapGenerated(dimension, x, z);
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

    /* LOCAL CALLBACKS, WHICH IS DISABLED ON SERVER */

    @Deprecated
    public static void onNativeGuiLoaded() {
        InnerCoreServer.useClientMethod("NativeCallback.onNativeGuiLoaded()");
    }

    @Deprecated
    public static void onKeyEventDispatched(int key, int state) {
        InnerCoreServer.useClientMethod("NativeCallback.onKeyEventDispatched(key, state)");
    }

    @Deprecated
    public static boolean isLevelDisplayed() {
        InnerCoreServer.useClientMethod("NativeCallback.isLevelDisplayed()");
        return false;
    }

    @Deprecated // this is called, when level renderer is set up
    public static void onLevelDisplayed() {
        InnerCoreServer.useClientMethod("NativeCallback.onLevelDisplayed()");
    }

    @Deprecated // this is local callback!
    public static void onDimensionChanged(int current, int last) {
        InnerCoreServer.useClientMethod("NativeCallback.onDimensionChanged(current, last)");
    }

    @Deprecated
    public static boolean isLocalTickDisabledDueToError() {
        InnerCoreServer.useClientMethod("NativeCallback.isLocalTickDisabledDueToError()");
        return false;
    }

    @Deprecated
    public static void onLocalTick() {
        InnerCoreServer.useClientMethod("NativeCallback.onLocalTick()");
    }

    @Deprecated
    public static void onConnectToHost(int port) {
        InnerCoreServer.useNotSupport("NativeCallback.onConnectToHost(port)");
    }

    @Deprecated
    public static void onBlockDestroyStarted(int x, int y, int z, int side) {
        InnerCoreServer.useClientMethod("NativeCallback.onBlockDestroyStarted(x, y, z, side)");
    }

    @Deprecated // fix callback
    public static void _onBlockDestroyStarted(int x, int y, int z, int side) {
        InnerCoreServer.useClientMethod("NativeCallback._onBlockDestroyStarted(x, y, z, side)");
    }

    @Deprecated
    public static void onBlockDestroyContinued(int x, int y, int z, int side, float progress) {
        InnerCoreServer.useClientMethod("NativeCallback.onBlockDestroyContinued(x, y, z, side, progress)");
    }

    @Deprecated
    public static void onLocalEntityAdded(long entity) {
        Callback.invokeAPICallback("EntityAddedLocal", entity);
    }

    @Deprecated
    public static void onLocalEntityRemoved(long entity) {
        Callback.invokeAPICallback("EntityRemovedLocal", entity);
    }

    @Deprecated
    public static void onCustomTessellation(long tessellator, int x, int y, int z, int id, int data, boolean b) {
        InnerCoreServer.useClientMethod("NativeCallback.onCustomTessellation(tessellator, x, y, z, id, data, b)");
    }

    @Deprecated
    public static void onItemIconOverride(int id, int count, int data, int extra) {
        InnerCoreServer.useClientMethod("NativeCallback.onItemIconOverride(id, count, data, extra)");
    }

    @Deprecated
    public static void onItemModelOverride(long modelPtr, int id, int count, int data, long extra) {
        InnerCoreServer.useClientMethod("NativeCallback.onItemModelOverride(modelPtr, id, count, data, extra)");
    }

    @Deprecated
    public static void onItemNameOverride(int id, int count, int data, int extra) {
        InnerCoreServer.useClientMethod("NativeCallback.onItemNameOverride(id, count, data, extra)");
    }

    @Deprecated
    public static void onScreenChanged(boolean isPushEvent) {
        InnerCoreServer.useClientMethod("NativeCallback.onScreenChanged(isPushEvent)");
    }
}
