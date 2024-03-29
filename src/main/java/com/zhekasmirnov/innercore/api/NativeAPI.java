package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.biome.Biome;

import java.lang.reflect.Field;

import com.reider745.InnerCoreServer;
import com.reider745.api.CallbackHelper;
import com.reider745.entity.EntityMethod;
import com.reider745.hooks.BiomesHooks;
import com.reider745.item.ItemMethod;
import com.reider745.world.BiomesMethods;
import com.reider745.world.WorldMethod;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.constants.PlayerAbility;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.mod.resource.ResourceStorage;

public class NativeAPI {

    public static boolean isLocalServerRunning() {
        return true;
    }

    public static String getStringIdAndTypeForIntegerId(int id) {
        return ItemMethod.getStringIdAndTypeForIntegerId(id);
    }

    public static String executeCommand(String command, int x, int y, int z, long blockSource) {
        if (Server.getInstance().getCommandMap().dispatch(Server.getInstance().getConsoleSender(), command)) {
            return "TODO";
        }
        return null;
    }

    @Deprecated(since = "Zote")
    public static void setDebugDumpDirectory(String path) {
        InnerCoreServer.useClientMethod("NativeAPI.setDebugDumpDirectory(path)");
    }

    @Deprecated(since = "Zote")
    public static void addTextureToLoad(String path) {
        ResourceStorage.addTextureToLoad(path);
    }

    @Deprecated(since = "Zote")
    public static void getAtlasTextureCoords(String name, int data, float[] result) {
        InnerCoreServer.useClientMethod("NativeAPI.getAtlasTextureCoords(name, data, result)");
    }

    public static String getGameLanguage() {
        return InnerCoreServer.getGameLanguage();
    }

    @Deprecated(since = "Zote")
    public static void preventPendingAppEvent(int event, int timeout) {
        InnerCoreServer.useClientMethod("NativeAPI.preventPendingAppEvent(event, timeout)");
    }

    @Deprecated(since = "Zote")
    public static void preventPendingKeyEvent(int event, int timeout) {
        InnerCoreServer.useClientMethod("NativeAPI.preventPendingKeyEvent(event, timeout)");
    }

    public static void setNativeWorldsPathOverride(String path) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setNativeWorldsPathOverride(path)");
    }

    public static void setNativeResourcePacksPathOverride(String path) {
        InnerCoreServer.useNotSupport("NativeAPI.setNativeResourcePacksPathOverride(path)");
    }

    public static void setNativeBehaviorPacksPathOverride(String path) {
        InnerCoreServer.useNotSupport("NativeAPI.setNativeBehaviorPacksPathOverride(path)");
    }

    @Deprecated(since = "Zote")
    public static void sendCachedItemNameOverride(int id, int data, String overrideCache) {
    }

    @Deprecated(since = "Zote")
    public static void setItemNameOverrideCallbackForced(int id, boolean forces) {
    }

    public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setBlockChangeCallbackEnabled(id, enabled)");
    }

    private static String cleanupPathOverride(String path, boolean terminateWithSlash) {
        if (path != null) {
            path = path.replaceAll("\\\\", "/");
            if (terminateWithSlash) {
                if (!path.endsWith("/")) {
                    path += "/";
                }
            } else {
                while (path.endsWith("/")) {
                    path = path.substring(path.length() - 1);
                }
            }
        }
        return path;
    }

    public static void setWorldsPathOverride(String path) {
        path = cleanupPathOverride(path, true);
        setNativeWorldsPathOverride(path);
        LevelInfo.worldsPathOverride = path;
    }

    public static void setResourcePacksPathOverride(String path) {
        path = cleanupPathOverride(path, false);
        setNativeResourcePacksPathOverride(path);
    }

    public static void setBehaviorPacksPathOverride(String path) {
        path = cleanupPathOverride(path, false);
        setNativeBehaviorPacksPathOverride(path);
    }

    public static void invokeUseItemNoTarget(int id, int count, int data, Item extra) {
        invokeUseItemNoTarget(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void invokeUseItemNoTarget(int id, int count, int data, NativeItemInstanceExtra extra) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.invokeUseItemNoTarget(id, count, data, extra)");
    }

    public static void invokeUseItemOn(int id, int count, int data, Item extra, int x, int y, int z, int side, float vx,
            float vy, float vz, long entity) {
        invokeUseItemOn(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra), x, y, z, side, vx, vy, vz, entity);
    }

    public static void invokeUseItemOn(int id, int count, int data, NativeItemInstanceExtra extra, int x, int y, int z, int side, float vx,
            float vy, float vz, long entity) {
        EntityMethod.invokeUseItemOn(id, count, data, extra, x, y, z, side, vx, vy, vz, entity);
    }

    @Deprecated(since = "Zote")
    public static void playSound(String name, float x, float y, float z, float f1, float f2) {
        NativeBlockSource region = NativeBlockSource.getDefault();
        if (region != null)
            region.playSound(x, y, z, name, f1, f2);
        InnerCoreServer.useClientMethod("NativeAPI.playSound(name, x, y, z, f1, f2)");
    }

    public static void playSoundEnt(String name, long entity, float f1, float f2) {
        EntityMethod.playSoundEnt(entity, name, f1, f2);
    }

    public static void addEffect(long entity, int effect, int duration, int level, boolean b1, boolean b2,
            boolean effectAnimation) {
        EntityMethod.addEffect(entity, effect, duration, level, b1, b2, effectAnimation);
    }

    public static int getEffectLevel(long entity, int effect) {
        return EntityMethod.getEffectLevel(entity, effect);
    }

    public static int getEffectDuration(long unwrapEntity, int effect) {
        return EntityMethod.getEffectDuration(unwrapEntity, effect);
    }

    @Deprecated(since = "Zote")
    public static void addFarParticle(int i, double d, double d2, double d3, double d4, double d5, double d6, int i2) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.addFarParticle(i, d, d2, d3, d4, d5, d6, i2)");
    }

    @Deprecated(since = "Zote")
    public static void addItemToInventory(int id, int count, int data, long extra, boolean b) {
        InnerCoreServer.useClientMethod("NativeAPI.addItemToInventory(id, count, data, extra, b)");
    }

    @Deprecated(since = "Zote")
    public static void addParticle(int i, double d, double d2, double d3, double d4, double d5, double d6, int i2) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.addParticle(i, d, d2, d3, d4, d5, d6, i2)");
    }

    @Deprecated(since = "Zote")
    public static void addPlayerExperience(int experience) {
        InnerCoreServer.useClientMethod("NativeAPI.addPlayerExperience(experience)");
    }

    @Deprecated(since = "Zote")
    public static boolean canPlayerFly() {
        InnerCoreServer.useClientMethod("NativeAPI.canPlayerFly()");
        return false;
    }

    public static void clearAllFurnaceRecipes() {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.clearAllFurnaceRecipes()");
    }

    @Deprecated(since = "Zote")
    public static void clientMessage(String str) {
    }

    @Deprecated(since = "Zote")
    public static float getGuiScale() {
        InnerCoreServer.useClientMethod("NativeAPI.getGuiScale()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static int clipWorld(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] clip) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return (int) region.clip(x1, y1, z1, x2, y2, z2, mode, clip);
        return 0;
    }

    public static void dealDamage(long unwrapEntity, int damage, int cause, long l, boolean b1, boolean b2) {
        EntityMethod.dealDamage(unwrapEntity, damage, cause, l, b1, b2);
    }

    @Deprecated(since = "Zote")
    public static void destroyBlock(int x, int y, int z, boolean drop) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.destroyBlock(x, y, z, drop);
    }

    @Deprecated(since = "Zote")
    public static void explode(float x, float y, float z, float power, boolean onFire) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.explode(x, y, z, power, onFire);
    }

    public static void leaveGame() {
        Server.getInstance().shutdown();
    }

    public static void forceCrash() {
        Server.getInstance().forceShutdown();
    }

    public static void forceLevelSave() {
        Server.getInstance().doAutoSave();
    }

    @Deprecated(since = "Zote")
    public static void forceRenderRefreshUnsafe(int i, int i2, int i3, int i4) {
        InnerCoreServer.useClientMethod("NativeAPI.forceRenderRefreshUnsafe(i, i2, i3, i4)");
    }

    @Deprecated(since = "Zote")
    public static void clearAllStaticRenders() {
        InnerCoreServer.useClientMethod("NativeAPI.clearAllStaticRenders()");
    }

    @Deprecated(since = "Zote")
    public static void clearAllRenderMappings() {
        InnerCoreServer.useClientMethod("NativeAPI.clearAllRenderMappings()");
    }

    @Deprecated(since = "Zote")
    public static long getGlobalShaderUniformSet() {
        InnerCoreServer.useClientMethod("NativeAPI.getGlobalShaderUniformSet()");
        return 0;
    }

    public static int getAge(long entity) {
        return EntityMethod.getAge(entity);
    }

    public static int getBiomeMap(int x, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBiome(x, z);
        return 0;
    }

    public static void setBiomeMap(int x, int z, int id) {
        BiomesMethods.setBiomeMap(x, z, id);
    }

    @Deprecated(since = "Zote")
    public static int getBiome(int x, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBiome(x, z);
        return 0;
    }

    @Deprecated(since = "Zote")
    public static void setBiome(int x, int z, int id) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.setBiome(x, z, id);
    }

    public static String getBiomeName(int id) {
        Biome biome = Biome.biomes[id];
        return biome != null ? biome.getName() : null;
    }

    @Deprecated(since = "Zote")
    public static float getBiomeTemperatureAt(int x, int y, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBiomeTemperatureAt(x, y, z);
        return 0;
    }

    @Deprecated(since = "Zote")
    public static int getBrightness(int x, int y, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getLightLevel(x, y, z);
        return 0;
    }

    @Deprecated(since = "Zote")
    public static int getData(int x, int y, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBlockData(x, y, z);
        return 0;
    }

    public static int getDifficulty() {
        return WorldMethod.getDifficulty();
    }

    @Deprecated(since = "Zote")
    public static int getDimension() {
        InnerCoreServer.useClientMethod("NativeAPI.getDimension()");
        return 0;
    }

    public static int getEntityDimension(long entity) {
        return EntityMethod.getEntityDimension(entity);
    }

    public static long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int entityType,
            boolean flag) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.fetchEntitiesInAABB(x1, y1, z1, x2, y2, z2, entityType, flag)");
        return new long[0];
    }

    public static long getEntityArmor(long entity, int slot) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getEntityArmor(entity, slot)");
        // return EntityMethod.getEntityArmor(entity, slot);
        return 0;
    }

    public static long getEntityCarriedItem(long entity) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getEntityCarriedItem(entity)");
        // return EntityMethod.getEntityCarriedItem(entity);
        return 0;
    }

    public static long getEntityOffhandItem(long entity) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getEntityOffhandItem(entity)");
        // return EntityMethod.getEntityOffhandItem(entity);
        return 0;
    }

    public static int getEntityType(long entity) {
        return EntityMethod.getEntityType(entity);
    }

    public static String getEntityTypeName(long entity) {
        return EntityMethod.getEntityTypeName(entity);
    }

    public static long getEntityCompoundTag(long entity) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getEntityCompoundTag(entity)");
        // return EntityMethod.getEntityCompoundTag(entity);
        return 0;
    }

    public static void setEntityCompoundTag(long entity, long tag) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setEntityCompoundTag(entity, tag)");
        // EntityMethod.setEntityCompoundTag(entity, tag);
    }

    public static int getFireTicks(long entity) {
        return EntityMethod.getFireTicks(entity);
    }

    public static int getGameMode() {
        return WorldMethod.getGameMode();
    }

    @Deprecated(since = "Zote")
    public static int getGrassColor(int x, int z) {
        InnerCoreServer.useClientMethod("NativeAPI.getGrassColor(x, z)");
        return 0;
    }

    public static int getHealth(long entity) {
        return EntityMethod.getHealth(entity);
    }

    @Deprecated(since = "Zote")
    public static long getInventorySlot(int slot) {
        InnerCoreServer.useClientMethod("NativeAPI.getInventorySlot(slot)");
        return 0;
    }

    public static long getItemFromDrop(long entity) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getItemFromDrop(entity)");
        return 0;
    }

    public static long getItemFromProjectile(long entity) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getItemFromProjectile(entity)");
        // return EntityMethod.getItemFromProjectile(entity);
        return 0;
    }

    public static float getLightningLevel() {
        return (float) WorldMethod.getLightningLevel();
    }

    public static int getMaxHealth(long entity) {
        return EntityMethod.getMaxHealth(entity);
    }

    public static String getNameTag(long entity) {
        return EntityMethod.getNameTag(entity);
    }

    @Deprecated(since = "Zote")
    public static long getPlayer() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayer()");
        return 0;
    }

    @Deprecated(since = "Zote")
    public static long getServerPlayer() {
        InnerCoreServer.useNotSupport("NativeAPI.getServerPlayer()");
        return 0;
    }

    @Deprecated(since = "Zote")
    public static long getLocalPlayer() {
        InnerCoreServer.useClientMethod("NativeAPI.getLocalPlayer()");
        return 0;
    }

    @Deprecated(since = "Zote")
    public static long getPlayerArmor(int slot) {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerArmor(slot)");
        return 0;
    }

    @Deprecated
    public static float getPlayerExhaustion() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerExhaustion()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static float getPlayerExperience() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerExperience()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static float getPlayerHunger() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerHunger()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static float getPlayerLevel() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerLevel()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static float getPlayerSaturation() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerSaturation()");
        return 0.0f;
    }

    @Deprecated(since = "Zote")
    public static int getPlayerScore() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerScore()");
        return 0;
    }

    @Deprecated(since = "Zote")
    public static int getPlayerSelectedSlot() {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerSelectedSlot()");
        return 0;
    }

    @Deprecated(since = "Zote")
    public static long getPointedData(int[] iArr, float[] fArr) {
        InnerCoreServer.useClientMethod("NativeAPI.getPointedData(iArr, fArr)");
        return 0;
    }

    public static void getPosition(long entity, float[] position) {
        EntityMethod.getPosition(entity, position);
    }

    public static float getRainLevel() {
        return (float) WorldMethod.getRainLevel();
    }

    @Deprecated(since = "Zote")
    public static int getRenderType(long entity) {
        InnerCoreServer.useClientMethod("NativeAPI.getRenderType(entity)");
        return 0;
    }

    public static void getRotation(long entity, float[] rotation) {
        EntityMethod.getRotation(entity, rotation);
    }

    public static long getSeed() {
        return WorldMethod.getSeed();
    }

    @Deprecated(since = "Zote")
    public static int getTile(int x, int y, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBlockId(x, y, z);
        return 0;
    }

    @Deprecated(since = "Zote")
    public static int getTileAndData(int i, int i2, int i3) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.getTileAndData(i, i2, i3)");
        return 0;
    }

    public static long getTime() {
        return WorldMethod.getTime();
    }

    @Deprecated(since = "Zote")
    public static int getUiProfile() {
        return 0;
    }

    public static void getVelocity(long entity, float[] velocity) {
        EntityMethod.getVelocity(entity, velocity);
    }

    @Deprecated(since = "Zote")
    public static boolean isChunkLoaded(int x, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.isChunkLoaded(x, z);
        return false;
    }

    @Deprecated(since = "Zote")
    public static int getChunkState(int x, int z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getChunkStateAt(x, z);
        return 0;
    }

    public static boolean isDefaultPrevented() {
        return CallbackHelper.isPrevent();
    }

    public static boolean isGlintItemInstance(int id, int data) {
        return isGlintItemInstance(id, data, (NativeItemInstanceExtra) null);
    }

    public static boolean isGlintItemInstance(int id, int data, Item extra) {
        return isGlintItemInstance(id, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static boolean isGlintItemInstance(int id, int data, NativeItemInstanceExtra extra) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.isGlintItemInstance(id, data, extra)");
        return false;
    }

    public static boolean isImmobile(long entity) {
        return EntityMethod.isImmobile(entity);
    }

    @Deprecated(since = "Zote")
    public static boolean isPlayerFlying() {
        InnerCoreServer.useClientMethod("NativeAPI.isPlayerFlying()");
        return false;
    }

    public static boolean isSneaking(long entity) {
        return EntityMethod.isSneaking(entity);
    }

    @Deprecated(since = "Zote")
    public static boolean isTileUpdateAllowed() {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBlockUpdateAllowed();
        return false;
    }

    @Deprecated(since = "Zote")
    public static int getTileUpdateType() {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.getBlockUpdateType();
        return 0;
    }

    public static boolean isValidEntity(long entity) {
        return EntityMethod.isValid(EntityMethod.getEntityById(entity));
    }

    @Deprecated(since = "Zote")
    public static void nativeSetCameraEntity(long entity) {
        InnerCoreServer.useClientMethod("NativeAPI.nativeSetCameraEntity(entity)");
    }

    @Deprecated(since = "Zote")
    public static void nativeSetFov(float fov) {
        InnerCoreServer.useClientMethod("NativeAPI.nativeSetFov(fov)");
    }

    @Deprecated(since = "Zote")
    public static void nativeVibrate(int i) {
        InnerCoreServer.useClientMethod("NativeAPI.nativeVibrate(i)");
    }

    @Deprecated(since = "Zote")
    public static void overrideItemIcon(String name, int index) {
        InnerCoreServer.useClientMethod("NativeAPI.overrideItemIcon(name, index)");
    }

    @Deprecated(since = "Zote")
    public static void overrideItemModel(long model) {
        InnerCoreServer.useClientMethod("NativeAPI.overrideItemModel(model)");
    }

    @Deprecated(since = "Zote")
    public static void overrideItemName(String name) {
    }

    @Deprecated(since = "Zote")
    public static void preventBlockDrop(int i, int i2, int i3) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.preventBlockDrop(i, i2, i3)");
    }

    public static void preventDefault() {
        CallbackHelper.prevent();
    }

    public static void removeAllEffects(long entity) {
        EntityMethod.removeAllEffects(entity);
    }

    public static void removeEffect(long entity, int effect) {
        EntityMethod.removeEffect(entity, effect);
    }

    public static void removeEntity(long entity) {
        EntityMethod.removeEntity(entity);
    }

    public static void rideAnimal(long entity, long rider) {
        EntityMethod.rideAnimal(entity, rider);
    }

    public static long getRider(long entity) {
        return EntityMethod.getRider(entity);
    }

    public static long getRiding(long entity) {
        return EntityMethod.getRiding(entity);
    }

    @Deprecated(since = "Zote")
    public static void resetSkyColor() {
        InnerCoreServer.useClientMethod("NativeAPI.resetSkyColor()");
    }

    public static void setAge(long entity, int age) {
        EntityMethod.setAge(entity, age);
    }

    public static void setCollisionSize(long entity, float w, float h) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setCollisionSize(entity, w, h)");
        EntityMethod.setCollisionSize(entity, w, h);
    }

    public static void setDifficulty(int difficulty) {
        WorldMethod.setDifficulty(difficulty);
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, Item extra) {
        setEntityArmor(entity, slot, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, NativeItemInstanceExtra extra) {
        EntityMethod.setEntityArmor(entity, slot, id, count, data, extra);
    }

    public static void setEntityCarriedItem(long entity, int id, int count, int data, Item extra) {
        setEntityCarriedItem(entity, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityCarriedItem(long entity, int id, int count, int data, NativeItemInstanceExtra extra) {
        EntityMethod.setEntityCarriedItem(entity, id, count, data, extra);
    }

    public static void setEntityOffhandItem(long entity, int id, int count, int data, Item extra) {
        setEntityOffhandItem(entity, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityOffhandItem(long entity, int id, int count, int data, NativeItemInstanceExtra extra) {
        EntityMethod.setEntityOffhandItem(entity, id, count, data, extra);
    }

    public static void setFireTicks(long entity, int ticks, boolean force) {
        EntityMethod.setFireTicks(entity, ticks, force);
    }

    public static void setGameMode(int mode) {
        WorldMethod.setGameMode(mode);
    }

    @Deprecated(since = "Zote")
    public static void setGrassColor(int x, int z, int color) {
        InnerCoreServer.useClientMethod("NativeAPI.setGrassColor(x, z, color)");
    }

    public static void setHealth(long entity, int health) {
        EntityMethod.setHealth(entity, health);
    }

    public static void setImmobile(long entity, boolean mobile) {
        EntityMethod.setImmobile(entity, mobile);
    }

    public static void setInnerCoreVersion(String str) {
    }

    @Deprecated(since = "Zote")
    public static void setInventorySlot(int slot, int id, int data, int count, long extra) {
        InnerCoreServer.useClientMethod("NativeAPI.setInventorySlot(slot, id, data, count, extra)");
    }

    @Deprecated(since = "Zote")
    public static void setItemRequiresIconOverride(int id, boolean enabled) {
    }

    public static void setItemToDrop(long entity, int id, int count, int data, Item extra) {
        setItemToDrop(entity, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setItemToDrop(long entity, int id, int count, int data, NativeItemInstanceExtra extra) {
        EntityMethod.setItemToDrop(entity, id, count, data, extra);
    }

    public static void setLightningLevel(float level) {
        WorldMethod.setLightningLevel(level);
    }

    public static void setMaxHealth(long entity, int health) {
        EntityMethod.setMaxHealth(entity, health);
    }

    public static void setNameTag(long entity, String tag) {
        EntityMethod.setNameTag(entity, tag);
    }

    public static void setTicksPerSecond(float f) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setTicksPerSecond(f)");
    }

    public static void setNativeThreadPriorityParams(int low, int high, int threshold) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setNativeThreadPriorityParams(low, high, threshold)");
    }

    public static void setNightMode(boolean z) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setNightMode(z)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerArmor(int i, int i2, int i3, int i4, long extra) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerArmor(i, i2, i3, i4, extra)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerCanFly(boolean enabled) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerCanFly(enabled)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerExhaustion(float exhaustion) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerExhaustion(exhaustion)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerExperience(float experience) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerExperience(experience)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerFlying(boolean enabled) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerFlying(enabled)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerHunger(float hunger) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerHunger(hunger)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerLevel(float level) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerLevel(level)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerSaturation(float saturation) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerSaturation(saturation)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerSelectedSlot(int slot) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerSelectedSlot(slot)");
    }

    public static void setPosition(long entity, float x, float y, float z) {
        EntityMethod.setPosition(entity, x, y, z);
    }

    public static void setPositionAxis(long entity, int axis, float value) {
        EntityMethod.setPositionAxis(entity, axis, value);
    }

    public static void setRainLevel(float level) {
        WorldMethod.setRainLevel(level);
    }

    @Deprecated(since = "Zote")
    public static void setRenderType(long entity, int type) {
        InnerCoreServer.useClientMethod("NativeAPI.setRenderType(entity, type)");
    }

    public static void setRespawnCoords(int x, int y, int z) {
        NativeBlockSource region = NativeBlockSource.getDefault();
        if (region != null)
            region.setRespawnCoords(x, y, z);
    }

    public static void setRotation(long entity, float x, float y) {
        EntityMethod.setRotation(entity, x, y);
    }

    public static void setRotationAxis(long entity, int axis, float value) {
        EntityMethod.setRotationAxis(entity, axis, value);
    }

    @Deprecated(since = "Zote")
    public static void setSkin(long entity, String skin) {
        InnerCoreServer.useClientMethod("NativeAPI.setSkin(entity, skin)");
    }

    @Deprecated(since = "Zote")
    public static void setSkyColor(float r, float g, float b) {
        InnerCoreServer.useClientMethod("NativeAPI.setSkyColor(r, g, b)");
    }

    @Deprecated(since = "Zote")
    public static void setCloudColor(float r, float g, float b) {
        InnerCoreServer.useClientMethod("NativeAPI.setCloudColor(r, g, b)");
    }

    @Deprecated(since = "Zote")
    public static void resetCloudColor() {
        InnerCoreServer.useClientMethod("NativeAPI.resetCloudColor()");
    }

    @Deprecated(since = "Zote")
    public static void setSunsetColor(float r, float g, float b) {
        InnerCoreServer.useClientMethod("NativeAPI.setSunsetColor(r, g, b)");
    }

    @Deprecated(since = "Zote")
    public static void resetSunsetColor() {
        InnerCoreServer.useClientMethod("NativeAPI.resetSunsetColor()");
    }

    @Deprecated(since = "Zote")
    public static void setFogColor(float r, float g, float b) {
        InnerCoreServer.useClientMethod("NativeAPI.setFogColor(r, g, b)");
    }

    @Deprecated(since = "Zote")
    public static void resetFogColor() {
        InnerCoreServer.useClientMethod("NativeAPI.resetFogColor()");
    }

    @Deprecated(since = "Zote")
    public static void setFogDistance(float start, float end) {
        InnerCoreServer.useClientMethod("NativeAPI.setFogDistance(start, end)");
    }

    @Deprecated(since = "Zote")
    public static void resetFogDistance() {
        InnerCoreServer.useClientMethod("NativeAPI.resetFogDistance()");
    }

    @Deprecated(since = "Zote")
    public static void setUnderwaterFogColor(float r, float g, float b) {
        InnerCoreServer.useClientMethod("NativeAPI.setUnderwaterFogColor(r, g, b)");
    }

    @Deprecated(since = "Zote")
    public static void resetUnderwaterFogColor() {
        InnerCoreServer.useClientMethod("NativeAPI.resetUnderwaterFogColor()");
    }

    @Deprecated(since = "Zote")
    public static void setUnderwaterFogDistance(float start, float end) {
        InnerCoreServer.useClientMethod("NativeAPI.setUnderwaterFogDistance(start, end)");
    }

    @Deprecated(since = "Zote")
    public static void resetUnderwaterFogDistance() {
        InnerCoreServer.useClientMethod("NativeAPI.resetUnderwaterFogDistance()");
    }

    public static void setSneaking(long entity, boolean sneak) {
        EntityMethod.setSneaking(entity, sneak);
    }

    @Deprecated(since = "Zote")
    public static void setTile(int x, int y, int z, int id, int data) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.setBlock(x, y, z, id, data);
    }

    @Deprecated(since = "Zote")
    public static void setTileUpdateAllowed(boolean z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.setBlockUpdateAllowed(z);
    }

    @Deprecated(since = "Zote")
    public static void setTileUpdateType(int i) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.setBlockUpdateType(i);
    }

    public static void setTime(long time) {
        WorldMethod.setTime((int) (time & Integer.MAX_VALUE));
    }

    public static void setVelocity(long entity, float x, float y, float z) {
        EntityMethod.setVelocity(entity, x, y, z);
    }

    // TODO
    public static void setVelocityAxis(long entity, int axis, float value) {
        InnerCoreServer.useNotCurrentSupport("NativeAPI.setVelocityAxis(entity, axis, value)");
    }

    @Deprecated(since = "Zote")
    public static long spawnDroppedItem(float x, float y, float z, int id, int count, int data, Item extra) {
        return spawnDroppedItem(x, y, z, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    @Deprecated(since = "Zote")
    public static long spawnDroppedItem(float x, float y, float z, int id, int count, int data, NativeItemInstanceExtra extra) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.spawnDroppedItem(x, y, z, id, count, data, extra);
        return -1;
    }

    @Deprecated(since = "Zote")
    public static long spawnEntity(int id, float x, float y, float z) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            return region.spawnEntity(x, y, z, id);
        return -1;
    }

    @Deprecated(since = "Zote")
    public static void spawnExpOrbs(float x, float y, float z, int amount) {
        NativeBlockSource region = NativeBlockSource.getCurrentWorldGenRegion();
        if (region != null)
            region.spawnExpOrbs(x, y, z, amount);
    }

    public static void teleportTo(long entity, float x, float y, float z) {
        EntityMethod.setPosition(entity, x, y, z);
    }

    @Deprecated(since = "Zote")
    public static void tipMessage(String str) {
    }

    public static void transferToDimension(long entity, int id) {
        EntityMethod.transferToDimension(entity, id);
    }

    @Deprecated(since = "Zote")
    public static void forceRenderRefresh(final int x, final int y, final int z, final int mode) {
        NativeAPI.forceRenderRefreshUnsafe(x, y, z, mode);
    }

    public static void nativeLog(String message) {
        Logger.debug(message);
    }

    public static String convertNameId(String str) {
        StringBuilder builder = new StringBuilder();
        int upperCaseCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (upperCaseCount == 0 && builder.length() > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
                upperCaseCount++;
            } else {
                if (upperCaseCount > 1 && c != '_') {
                    builder.insert(builder.length() - 1, '_');
                }
                builder.append(c);
                upperCaseCount = 0;
            }
        }
        return builder.toString();
    }

    @Deprecated(since = "Zote")
    public static void setPlayerBooleanAbility(String ability, boolean value) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerBooleanAbility(ability, value)");
    }

    @Deprecated(since = "Zote")
    public static void setPlayerFloatAbility(String ability, float value) {
        InnerCoreServer.useClientMethod("NativeAPI.setPlayerFloatAbility(ability, value)");
    }

    @Deprecated(since = "Zote")
    public static boolean getPlayerBooleanAbility(String ability) {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerBooleanAbility(ability)");
        return false;
    }

    @Deprecated(since = "Zote")
    public static float getPlayerFloatAbility(String ability) {
        InnerCoreServer.useClientMethod("NativeAPI.getPlayerFloatAbility(ability)");
        return 0.0f;
    }

    public static boolean isValidAbility(String ability) {
        Field[] fields = PlayerAbility.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.get(null).equals(ability)) {
                    return true;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return false;
    }

    public static long getTarget(long entity) {
        return EntityMethod.getTarget(entity);
    }

    public static void setTarget(long entity, long target) {
        EntityMethod.setTarget(entity, target);
    }
}
