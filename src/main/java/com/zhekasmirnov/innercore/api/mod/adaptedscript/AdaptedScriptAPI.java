package com.zhekasmirnov.innercore.api.mod.adaptedscript;

import android.util.Pair;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.item.CustomItem;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.EntityActor;
import com.zhekasmirnov.apparatus.api.player.armor.ActorArmorHandler;
import com.zhekasmirnov.apparatus.ecs.core.*;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.mcpe.NativePlayer;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import com.zhekasmirnov.innercore.api.*;
import com.zhekasmirnov.innercore.api.annotations.*;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ScriptableParams;
//import com.zhekasmirnov.innercore.api.dimensions.CustomDimensionGenerator;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimensionGenerator;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.recipes.RecipeRegistry;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.mod.ui.window.*;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;
import com.zhekasmirnov.innercore.api.nbt.NativeListTag;
import com.zhekasmirnov.innercore.api.nbt.NbtDataType;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
import com.zhekasmirnov.innercore.api.runtime.saver.world.ScriptableSaverScope;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.api.unlimited.SpecialType;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.api.particles.ParticleRegistry;

import org.json.JSONException;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by zheka on 28.07.2017.
 */
public class AdaptedScriptAPI extends API {

    @Override
    public String getName() {
        return "AdaptedScript";
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onModLoaded(Mod mod) {

    }

    @Override
    public void onCallback(String name, Object[] args) {

    }

    @Override
    public void setupCallbacks(Executable executable) {

    }

    @Override
    public void prepareExecutable(Executable executable) {
        super.prepareExecutable(executable);
    }

    @JSStaticFunction
    public static void logDeprecation(String functionName){
        ICLog.d("WARNING", "using deprecated or unimplemented method " + functionName + "()");
    }

    @APIStaticModule
    public static class ICRender extends NativeICRender {

    }

    @APIStaticModule
    public static class IDRegistry extends com.zhekasmirnov.innercore.api.unlimited.IDRegistry {
        @JSStaticFunction
        @Deprecated
        public static void __placeholder() {
            logDeprecation("IDRegistry.__placeholder");
        }

        @JSStaticFunction
        @Deprecated
        public static String getIdInfo(int id) {
            return NativeAPI.getStringIdAndTypeForIntegerId(id);
        }
    }

    @JSStaticFunction
    public static void preventDefault() {
        NativeAPI.preventDefault();
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("MOD", str);
    }

    @JSStaticFunction
    public static void print(final String str) {
        ICLog.d("MOD-PRINT", str);
    }

    @JSStaticFunction
    public static String getRoot(){
        return FileTools.DIR_ROOT;
    }



    @JSStaticFunction
    public static void setTile(int x, int y, int z, int id, int data) {
        NativeAPI.setTile(x, y, z, id, data);
    }

    @JSStaticFunction
    public static int getTile(int x, int y, int z) {
        return NativeAPI.getTile(x, y, z);
    }

    @JSStaticFunction
    public static int getTileAndData(int x, int y, int z) {
        return NativeAPI.getTileAndData(x, y, z);
    }

    @JSStaticFunction
    public static long getPlayerEnt() {
        return NativeAPI.getPlayer();
    }

    @JSStaticFunction
    public static void clientMessage(String message) {
    }

    @JSStaticFunction
    public static void tipMessage(String message) {
    }

    @JSStaticFunction
    public static void explode(double x, double y, double z, double power, boolean onFire) {
        NativeAPI.explode((float) x, (float) y, (float) z, (float) power, onFire);
    }


    @APIStaticModule
    public static class Logger {
        @JSStaticFunction
        public static void Log(String message, String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                prefix = "MOD";
            }
            ICLog.d(prefix, message);
        }

        @JSStaticFunction
        public static void LogError(Object error) {
            try {
                Throwable throwable = (Throwable) Context.jsToJava(error, Throwable.class);
                ICLog.e("ERROR", "STACK TRACE:", throwable);
            } catch (Throwable e) {
            }
        }

        @JSStaticFunction
        public static void Flush() {
            ICLog.flush();
        }


        // new log methods

        @JSStaticFunction
        public static void debug(String tag, String message) {
            ICLog.d(tag, message);
        }

        @JSStaticFunction
        public static void info(String tag, String message) {
            ICLog.i(tag, message);
        }

        @JSStaticFunction
        public static void error(String tag, String message, Object error) {
            try {
                Throwable throwable = (Throwable) Context.jsToJava(error, Throwable.class);
                ICLog.e(tag, message, throwable);
            } catch (Throwable e) {
                ICLog.e("ERROR", "error occurred while logging mod error (" + error + ", " + error.getClass() + "):", e);
            }
        }
    }



    @APIStaticModule
    public static class Level {
        @JSStaticFunction
        public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
            NativeAPI.setBlockChangeCallbackEnabled(id, enabled);
        }

        // tile

        @JSStaticFunction
        public static void setTile(int x, int y, int z, int id, int data) {
            NativeAPI.setTile(x, y, z, id, data);
        }

        @JSStaticFunction
        public static int getTile(int x, int y, int z) {
            return NativeAPI.getTile(x, y, z);
        }

        @JSStaticFunction
        public static int getData(int x, int y, int z) {
            return NativeAPI.getData(x, y, z);
        }

        @JSStaticFunction
        public static int getTileAndData(int x, int y, int z) {
            return NativeAPI.getTileAndData(x, y, z);
        }

        @JSStaticFunction
        public static int getBrightness(int x, int y, int z) {
            return NativeAPI.getBrightness(x, y, z);
        }

        @JSStaticFunction
        public static boolean isChunkLoaded(int x, int z) {
            return NativeAPI.isChunkLoaded(x, z);
        }

        @JSStaticFunction
        public static boolean isChunkLoadedAt(int x, int y, int z) {
            return NativeAPI.isChunkLoaded((int) Math.floor(x / 16.0), (int) Math.floor(z / 16.0));
        }

        @JSStaticFunction
        public static int getChunkState(int x, int z) {
            return NativeAPI.getChunkState(x, z);
        }

        @JSStaticFunction
        public static int getChunkStateAt(int x, int y, int z) {
            return NativeAPI.getChunkState((int) Math.floor(x / 16.0), (int) Math.floor(z / 16.0));
        }

        // biome

        @JSStaticFunction
        public static int getBiome(int x, int z) {
            return NativeAPI.getBiome(x, z);
        }

        @JSStaticFunction
        public static void setBiome(int x, int z, int id) {
            NativeAPI.setBiome(x, z, id);
        }

        @JSStaticFunction
        public static int getBiomeMap(int x, int z) {
            return NativeAPI.getBiomeMap(x, z);
        }

        @JSStaticFunction
        public static void setBiomeMap(int x, int z, int id) {
            NativeAPI.setBiomeMap(x, z, id);
        }

        @JSStaticFunction
        public static String biomeIdToName(int id) {
            return NativeAPI.getBiomeName(id);
        }

        @JSStaticFunction
        public static float getTemperature(int x, int y, int z) {
            return NativeAPI.getBiomeTemperatureAt(x, y, z);
        }

        @JSStaticFunction
        public static int getGrassColor(int x, int z) {
            return NativeAPI.getGrassColor(x, z);
        }

        @JSStaticFunction
        public static void setGrassColor(int x, int z, int color) {
            NativeAPI.setGrassColor(x, z, color);
        }

        @JSStaticFunction
        public static void destroyBlock(int x, int y, int z, boolean drop) {
            if (NativeAPI.getTile(x, y, z) > 0) {
                NativeAPI.destroyBlock(x, y, z, drop);
            }
        }

        // entity

        @JSStaticFunction
        public static void addParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {

        }

        @JSStaticFunction
        public static void addFarParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {

        }

        @JSStaticFunction
        public static long spawnMob(double x, double y, double z, int id, String skin) {
            long entity = NativeAPI.spawnEntity(id, (float) x, (float) y, (float) z);
            if (skin != null && skin.length() > 0 && !skin.equals("undefined")) {
                NativeAPI.setSkin(entity, skin);
            }
            return entity;
        }

        @JSStaticFunction
        public static void spawnExpOrbs(double x, double y, double z, int amount) {
            NativeAPI.spawnExpOrbs((float) x, (float) y, (float) z, amount);
        }

        @JSStaticFunction
        public static long dropItem(double x, double y, double z, int placeholder, int id, int count, int data, Object extra) {
            if (id == 0) {
                return -1;
            }
            return NativeAPI.spawnDroppedItem((float) x, (float) y, (float) z, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static NativeTileEntity getTileEntity(int x, int y, int z) {
            return NativeTileEntity.getTileEntity(x, y, z);
        }

        // global

        @JSStaticFunction
        public static long getTime() {
            return NativeAPI.getTime();
        }

        @JSStaticFunction
        public static void setTime(int time) {
            NativeAPI.setTime(time);
        }

        @JSStaticFunction
        public static int getGameMode() {
            return NativeAPI.getGameMode();
        }

        @JSStaticFunction
        public static void setGameMode(int mode) {
            NativeAPI.setGameMode(mode);
        }

        @JSStaticFunction
        public static int getDifficulty() {
            return NativeAPI.getDifficulty();
        }

        @JSStaticFunction
        public static void setDifficulty(int val) {
            NativeAPI.setDifficulty(val);
        }

        @JSStaticFunction
        public static double getRainLevel() {
            return NativeAPI.getRainLevel();
        }

        @JSStaticFunction
        public static void setRainLevel(double val) {
            NativeAPI.setRainLevel((float) val);
        }

        @JSStaticFunction
        public static double getLightningLevel() {
            return NativeAPI.getLightningLevel();
        }

        @JSStaticFunction
        public static void setLightningLevel(double val) {
            NativeAPI.setLightningLevel((float) val);
        }

        // other

        @JSStaticFunction
        @Placeholder
        public static void playSound(double x, double y, double z,String name, double f1, double f2) {
            NativeAPI.playSound(name, (float) x, (float) y, (float) z, (float) f1, (float) f2);
        }

        @JSStaticFunction
        @Placeholder
        public static void playSoundEnt(Object ent, String name, double f1, double f2) {
            NativeAPI.playSoundEnt(name, Entity.unwrapEntity(ent), (float) f1, (float) f2);
        }

        @JSStaticFunction
        public static void explode(double x, double y, double z, double power, boolean fire) {
            NativeAPI.explode((float) x, (float) y, (float) z, (float) power, fire);
        }

        @JSStaticFunction
        public static void setNightMode(boolean val) {
           NativeAPI.setNightMode(val);
        }

        @JSStaticFunction
        public static void setRespawnCoords(int x, int y, int z) {
            NativeAPI.setRespawnCoords(x, y, z);
        }

        @JSStaticFunction
        @DeprecatedAPIMethod
        public static void setSpawn(int x, int y, int z) {
            NativeAPI.setRespawnCoords(x, y, z);
        }

        @JSStaticFunction
        public static long getSeed() {
            return NativeAPI.getSeed();
        }

        @JSStaticFunction
        public static String getWorldName() {
            return LevelInfo.getLevelName();
        }

        @JSStaticFunction
        public static String getWorldDir() {
            return LevelInfo.getLevelDir();
        }



        @JSStaticFunction
        public static ScriptableObject clip(double x1, double y1, double z1, double x2, double y2, double z2, int mode) {
            float[] clip = new float[4];
            long actor = NativeAPI.clipWorld((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, mode, clip);
            int side = (int) clip[3];

            int normalX = 0, normalY = 0, normalZ = 0;

            switch(side){
                case 0:
                    normalY--;
                    break;
                case 1:
                    normalY++;
                    break;
                case 2:
                    normalZ--;
                    break;
                case 3:
                    normalZ++;
                    break;
                case 4:
                    normalX--;
                    break;
                case 5:
                    normalX++;
                    break;
            }

            double dis = Math.sqrt(Math.pow(clip[0] - x1, 2) + Math.pow(clip[1] - y1, 2) + Math.pow(clip[2] - z1, 2));
            double lim = 0.99;

            boolean collision = !(Math.abs(clip[0] - x2) < .0001 && Math.abs(clip[1] - y2) < .0001 && Math.abs(clip[2] - z2) < .0001);

            return new ScriptableParams(
                    new Pair<String, Object>("entity", actor),
                    new Pair<String, Object>("side", (double) side),
                    new Pair<String, Object>("collision", collision),
                    new Pair<String, Object>("pos", new ScriptableParams(
                            new Pair<String, Object>("x", (double) clip[0]),
                            new Pair<String, Object>("y", (double) clip[1]),
                            new Pair<String, Object>("z", (double) clip[2])
                    )),
                    new Pair<String, Object>("pos_limit", new ScriptableParams(
                            new Pair<String, Object>("x", collision ? clip[0] * lim + x1 * (1 - lim) : clip[0]),
                            new Pair<String, Object>("y", collision ? clip[1] * lim + y1 * (1 - lim) : clip[1]),
                            new Pair<String, Object>("z", collision ? clip[2] * lim + z1 * (1 - lim) : clip[2])
                    )),
                    new Pair<String, Object>("normal", new ScriptableParams(
                            new Pair<String, Object>("x", (double) normalX),
                            new Pair<String, Object>("y", (double) normalY),
                            new Pair<String, Object>("z", (double) normalZ)
                    ))
            );
        }



        @JSStaticFunction
        public static void setSkyColor(double r, double g, double b) {
            NativeAPI.setSkyColor((float) r, (float) g, (float) b);
        }

        @JSStaticFunction
        public static void resetSkyColor() {
            NativeAPI.resetSkyColor();
        }

        @JSStaticFunction
        public static void setCloudColor(double r, double g, double b) {
            NativeAPI.setCloudColor((float) r, (float) g, (float) b);
        }

        @JSStaticFunction
        public static void resetCloudColor() {
            NativeAPI.resetCloudColor();
        }

        @JSStaticFunction
        public static void setSunsetColor(double r, double g, double b) {
            NativeAPI.setSunsetColor((float) r, (float) g, (float) b);
        }

        @JSStaticFunction
        public static void resetSunsetColor() {
            NativeAPI.resetSunsetColor();
        }

        @JSStaticFunction
        public static void setFogColor(double r, double g, double b) {
            NativeAPI.setFogColor((float) r, (float) g, (float) b);
        }

        @JSStaticFunction
        public static void resetFogColor() {
            NativeAPI.resetFogColor();
        }

        @JSStaticFunction
        public static void setFogDistance(double start, double end) {
            NativeAPI.setFogDistance((float) start, (float) end);
        }

        @JSStaticFunction
        public static void resetFogDistance() {
            NativeAPI.resetFogDistance();
        }

        @JSStaticFunction
        public static void setUnderwaterFogColor(double r, double g, double b) {
            NativeAPI.setUnderwaterFogColor((float) r, (float) g, (float) b);
        }

        @JSStaticFunction
        public static void resetUnderwaterFogColor() {
            NativeAPI.resetUnderwaterFogColor();
        }

        @JSStaticFunction
        public static void setUnderwaterFogDistance(double start, double end) {
            NativeAPI.setUnderwaterFogDistance((float) start, (float) end);
        }

        @JSStaticFunction
        public static void resetUnderwaterFogDistance() {
            NativeAPI.resetUnderwaterFogDistance();
        }
    }



    @APIStaticModule
    public static class Entity {

        static long unwrapEntity(Object ent) {
            /*if (!NativeAPI.isValidEntity(entity)) {
                throw new IllegalArgumentException("invalid entity passed to api method: " + entity);
            }*/
            return (long) (ent instanceof Wrapper ? ((Wrapper) ent).unwrap() : ((Number) ent).longValue());
        }

        @JSStaticFunction
        public static boolean isValid(Object entity) {
            return NativeAPI.isValidEntity(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static int getDimension(Object entity) {
            return NativeAPI.getEntityDimension(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static ArrayList<Long> getAllArrayList() {
            return new ArrayList<>(NativeCallback.getAllEntities());
        }

        @JSStaticFunction
        public static NativeArray getAll() {
            return new NativeArray(NativeCallback.getAllEntities().toArray());
        }

        @JSStaticFunction
        public static float[] getPosition(Object entity) {
            float[] pos = new float[3];
            NativeAPI.getPosition(unwrapEntity(entity), pos);
            return pos;
        }

        @JSStaticFunction
        public static double getX(Object entity) {
            return getPosition(entity)[0];
        }

        @JSStaticFunction
        public static double getY(Object entity) {
            return getPosition(entity)[1];
        }

        @JSStaticFunction
        public static double getZ(Object entity) {
            return getPosition(entity)[2];
        }

        @JSStaticFunction
        public static void setPosition(Object entity, double x, double y, double z) {
            NativeAPI.setPosition(unwrapEntity(entity), (float) x, (float) y, (float) z);
        }

        @JSStaticFunction
        public static void setPositionAxis(Object entity, int axis, double val) {
            NativeAPI.setPositionAxis(unwrapEntity(entity), axis, (float) val);
        }

        @JSStaticFunction
        public static float[] getVelocity(Object entity) {
            float[] pos = new float[3];
            NativeAPI.getVelocity(unwrapEntity(entity), pos);
            return pos;
        }

        @JSStaticFunction
        public static double getVelX(Object entity) {
            return getVelocity(entity)[0];
        }

        @JSStaticFunction
        public static double getVelY(Object entity) {
            return getVelocity(entity)[1];
        }

        @JSStaticFunction
        public static double getVelZ(Object entity) {
            return getVelocity(entity)[2];
        }

        @JSStaticFunction
        public static void setVelocity(Object entity, double x, double y, double z) {
            NativeAPI.setVelocity(unwrapEntity(entity), (float) x, (float) y, (float) z);
        }

        @JSStaticFunction
        public static void setVelocityAxis(Object entity, int axis, double val) {
            NativeAPI.setVelocityAxis(unwrapEntity(entity), axis, (float) val);
        }

        @JSStaticFunction
        public static float[] getRotation(Object entity) {
            float[] pos = new float[2];
            NativeAPI.getRotation(unwrapEntity(entity), pos);
            return pos;
        }

        @JSStaticFunction
        public static double getYaw(Object entity) {
            return getRotation(entity)[0];
        }

        @JSStaticFunction
        public static double getPitch(Object entity) {
            return getRotation(entity)[1];
        }

        @JSStaticFunction
        public static void setRot(Object entity, double x, double y) {
            setRotation(entity, x, y);
        }

        @JSStaticFunction
        public static void setRotation(Object entity, double x, double y) {
            NativeAPI.setRotation(unwrapEntity(entity), (float) x, (float) y);
        }

        @JSStaticFunction
        public static void setRotationAxis(Object entity, int axis, double val) {
            NativeAPI.setRotationAxis(unwrapEntity(entity), axis, (float) val);
        }

        // properties

        @JSStaticFunction
        public static int getHealth(Object entity) {
            return NativeAPI.getHealth(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setHealth(Object entity, int health) {
            NativeAPI.setHealth(unwrapEntity(entity), health);
        }

        @JSStaticFunction
        public static int getMaxHealth(Object entity) {
            return NativeAPI.getMaxHealth(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setMaxHealth(Object entity, int health) {
            NativeAPI.setMaxHealth(unwrapEntity(entity), health);
        }

        @JSStaticFunction
        public static int getAnimalAge(Object entity) {
            return NativeAPI.getAge(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setAnimalAge(Object entity, int age) {
            NativeAPI.setAge(unwrapEntity(entity), age);
            logDeprecation("Entity.setAnimalAge");
        }

        @JSStaticFunction
        public static int getFireTicks(Object entity) {
            return NativeAPI.getFireTicks(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setFireTicks(Object entity, int ticks, boolean force) {
            NativeAPI.setFireTicks(unwrapEntity(entity), ticks, force);
        }

        @JSStaticFunction
        public static boolean isImmobile(Object entity) {
            return NativeAPI.isImmobile(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setImmobile(Object entity, boolean val) {
            NativeAPI.setImmobile(unwrapEntity(entity), val);
        }

        @JSStaticFunction
        public static boolean isSneaking(Object entity) {
            return NativeAPI.isSneaking(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setSneaking(Object entity, boolean val) {
            NativeAPI.setSneaking(unwrapEntity(entity), val);
        }

        @JSStaticFunction
        public static String getNameTag(Object entity) {
            return NativeAPI.getNameTag(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setNameTag(Object entity, String tag) {
            NativeAPI.setNameTag(unwrapEntity(entity), tag);
        }

        @JSStaticFunction
        public static int getRenderType(Object entity) {
            return NativeAPI.getRenderType(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setRenderType(Object entity, int type) {
            NativeAPI.setRenderType(unwrapEntity(entity), type);
        }

        @JSStaticFunction
        @Placeholder
        public static String getSkin(Object entity) {
            return "missing_texture.png";
        }

        @JSStaticFunction
        public static void setSkin(Object entity, String skin) {
            NativeAPI.setSkin(unwrapEntity(entity), skin);
        }

        @JSStaticFunction
        @Placeholder
        public static String getMobSkin(Object entity) {
            return "missing_texture.png";
        }

        @JSStaticFunction
        public static void setMobSkin(Object entity, String skin) {
            NativeAPI.setSkin(unwrapEntity(entity), skin);
        }

        // inventory

        @JSStaticFunction
        public static ItemInstance getDroppedItem(Object entity) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getItemFromDrop(unwrapEntity(entity))));
        }

        @JSStaticFunction
        public static ItemInstance getProjectileItem(Object entity) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getItemFromProjectile(unwrapEntity(entity))));
        }

        @JSStaticFunction
        @Indev
        public static void setDroppedItem(Object entity, int id, int count, int data, Object extra) {
            NativeAPI.setItemToDrop(unwrapEntity(entity), id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static ItemInstance getCarriedItem(Object entity) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityCarriedItem(unwrapEntity(entity))));
        }

        @JSStaticFunction
        public static ItemInstance getOffhandItem(Object entity) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityOffhandItem(unwrapEntity(entity))));
        }

        @JSStaticFunction
        public static void setCarriedItem(Object entity, int id, int count, int data, Object extra) {
            NativeAPI.setEntityCarriedItem(unwrapEntity(entity), id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static void setOffhandItem(Object entity, int id, int count, int data, Object extra) {
            NativeAPI.setEntityOffhandItem(unwrapEntity(entity), id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static ItemInstance getArmor(Object entity, int slot) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityArmor(unwrapEntity(entity), slot)));
        }

        @JSStaticFunction
        public static void setArmor(Object entity, int slot, int id, int count, int data, Object extra) {
            NativeAPI.setEntityArmor(unwrapEntity(entity), slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static ItemInstance getArmorSlot(Object entity, int slot) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityArmor(unwrapEntity(entity), slot)));
        }

        @JSStaticFunction
        public static void setArmorSlot(Object entity, int slot, int id, int count, int data, Object extra) {
            NativeAPI.setEntityArmor(unwrapEntity(entity), slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        // other

        @JSStaticFunction
        public static long getPlayerEnt() {
            return NativeAPI.getPlayer();
        }

        @JSStaticFunction
        public static void remove(Object entity) {
            NativeAPI.removeEntity(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void addEffect(Object entity, int effect, int duration, int level, boolean b1, boolean b2, boolean effectAnimation) {
            NativeAPI.addEffect(unwrapEntity(entity), effect, duration, level, b1, b2, effectAnimation);
        }

        @JSStaticFunction
        public static boolean hasEffect(Object entity, int effect) {
            return NativeAPI.getEffectLevel(unwrapEntity(entity), effect) != 0;
        }

        @JSStaticFunction
        public static int getEffectLevel(Object entity, int effect) {
            return NativeAPI.getEffectLevel(unwrapEntity(entity), effect);
        }

        @JSStaticFunction
        public static int getEffectDuration(Object entity, int effect) {
            return NativeAPI.getEffectDuration(unwrapEntity(entity), effect);
        }

        @JSStaticFunction
        public static void removeEffect(Object entity, int effect) {
            NativeAPI.removeEffect(unwrapEntity(entity), effect);
        }

        @JSStaticFunction
        public static void removeAllEffects(Object entity) {
            NativeAPI.removeAllEffects(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void rideAnimal(Object entity, Object rider) {
            NativeAPI.rideAnimal(unwrapEntity(entity), unwrapEntity(rider));
        }

        @JSStaticFunction
        public static long getRider(Object entity) {
            return NativeAPI.getRider(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static long getRiding(Object entity) {
            return NativeAPI.getRiding(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static long getTarget(Object entity) {
            return NativeAPI.getTarget(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void setTarget(Object entity, Object target) {
            NativeAPI.setTarget(unwrapEntity(entity), unwrapEntity(target));
        }

        @JSStaticFunction
        @DeprecatedAPIMethod
        public static int getEntityTypeId(Object entity) {
            return NativeAPI.getEntityType(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static int getType(Object entity) {
            return NativeAPI.getEntityType(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static String getTypeName(Object entity) {
            return NativeAPI.getEntityTypeName(unwrapEntity(entity));
        }

        @JSStaticFunction
        public static NativeCompoundTag getCompoundTag(Object entity) {
            CompoundTag ptr = NativeAPI.getEntityCompoundTag(unwrapEntity(entity));
            return ptr != null ? new NativeCompoundTag(ptr) : null;
        }

        @JSStaticFunction
        public static void setCompoundTag(Object entity, Object _tag) {
            NativeCompoundTag tag = (NativeCompoundTag) Context.jsToJava(_tag, NativeCompoundTag.class);
            if (tag != null) {
                NativeAPI.setEntityCompoundTag(unwrapEntity(entity), tag.pointer);
            }
        }

        @JSStaticFunction
        public static void setCollisionSize(Object entity, double w, double h) {
            NativeAPI.setCollisionSize(unwrapEntity(entity), (float) w, (float) h);
        }

        @JSStaticFunction
        public static void dealDamage(Object entity, int damage, int cause, ScriptableObject additionalParams) {
            if (additionalParams == null) {
                additionalParams = ScriptableObjectHelper.createEmpty();
            }

            Object attacker = ScriptableObjectHelper.getProperty(additionalParams, "attacker", null);
            boolean b1 = ScriptableObjectHelper.getBooleanProperty(additionalParams, "bool1", false);
            boolean b2 = ScriptableObjectHelper.getBooleanProperty(additionalParams, "bool2", false);

            NativeAPI.dealDamage(unwrapEntity(entity), damage, cause, attacker == null ? -1 : unwrapEntity(attacker), b1, b2);
        }

        /*@JSStaticFunction
        public static NativeAttributeInstance getAttribute(Object entity, String attribute){
            return new NativeAttributeInstance(unwrapEntity(entity), attribute);
        }

        @JSStaticFunction
        public static NativePathNavigation getPathNavigation(Object entity){
            return NativePathNavigation.getNavigation(unwrapEntity(entity));
        }*/

        @JSStaticFunction
        public static NativeArray getEntitiesInsideBox(double x1, double y1, double z1, double x2, double y2, double z2, int type, boolean flag) {
            long[] ents = NativeAPI.fetchEntitiesInAABB((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, type, flag);
            Object[] result = new Object[ents.length];
            int i = 0;
            for (long ent : ents) {
                result[i++] = ent;
            }
            return ScriptableObjectHelper.createArray(result);
        }
    }



    @APIStaticModule
    public static class Player {
        @JSStaticFunction
        public static long get() {
            return NativeAPI.getPlayer();
        }

        @JSStaticFunction
        public static long getServer() { return NativeAPI.getServerPlayer(); }

        @JSStaticFunction
        public static long getLocal() { return NativeAPI.getLocalPlayer(); }

        @JSStaticFunction
        public static boolean isPlayer(Object entity) {
            return NativeAPI.getEntityType(Entity.unwrapEntity(entity)) == 1;
        }

        @JSStaticFunction
        public static ScriptableObject getPointed() {
            int[] pos = new int[4];
            float[] vec = new float[3];

            long entity = NativeAPI.getPointedData(pos, vec);

            return new ScriptableParams(
                    new Pair<String, Object>("pos", new ScriptableParams(
                            new Pair<String, Object>("x", pos[0]),
                            new Pair<String, Object>("y", pos[1]),
                            new Pair<String, Object>("z", pos[2]),
                            new Pair<String, Object>("side", pos[3])
                    )),

                    new Pair<String, Object>("vec", new ScriptableParams(
                            new Pair<String, Object>("x", vec[0]),
                            new Pair<String, Object>("y", vec[1]),
                            new Pair<String, Object>("z", vec[2])
                    )),

                    new Pair<String, Object>("entity", entity)
            );
        }

        // inventory

        @JSStaticFunction
        public static void addItemInventory(int id, int count, int data, boolean preventDropThatLeft, Object extra) {
            NativeAPI.addItemToInventory(id, count, data, NativeItemInstanceExtra.unwrapValue(extra), !preventDropThatLeft);
        }

        @JSStaticFunction
        public static void addItemCreativeInv(int id, int count, int data, Object extra) {
            NativeItem.addToCreative(id, count, data, extra);
        }

        @JSStaticFunction
        public static ItemInstance getInventorySlot(int slot) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getInventorySlot(slot)));
        }

        @JSStaticFunction
        public static void setInventorySlot(int slot, int id, int count, int data, Object extra) {
            NativeAPI.setInventorySlot(slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static ItemInstance getArmorSlot(int slot) {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getPlayerArmor(slot)));
        }

        @JSStaticFunction
        public static void setArmorSlot(int slot, int id, int count, int data, Object extra) {
            NativeAPI.setPlayerArmor(slot, id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static ItemInstance getCarriedItem() {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityCarriedItem(get())));
        }

        @JSStaticFunction
        public static ItemInstance getOffhandItem() {
            return new ItemInstance(new NativeItemInstance(NativeAPI.getEntityOffhandItem(get())));
        }

        @JSStaticFunction
        public static void setCarriedItem(int id, int count, int data, Object extra) {
            NativeAPI.setEntityCarriedItem(get(), id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static void setOffhandItem(int id, int count, int data, Object extra) {
            NativeAPI.setEntityOffhandItem(get(), id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static int getSelectedSlotId() {
            return NativeAPI.getPlayerSelectedSlot();
        }

        @JSStaticFunction
        public static void setSelectedSlotId(int slot) {
            NativeAPI.setPlayerSelectedSlot(slot);
        }

        // properties

        @JSStaticFunction
        public static double getHunger() {
            return NativeAPI.getPlayerHunger();
        }

        @JSStaticFunction
        public static void setHunger(double val) {
            NativeAPI.setPlayerHunger((float) val);
        }

        @JSStaticFunction
        public static double getSaturation() {
            return NativeAPI.getPlayerSaturation();
        }

        @JSStaticFunction
        public static void setSaturation(double val) {
            NativeAPI.setPlayerSaturation((float) val);
        }

        @JSStaticFunction
        public static double getExhaustion() {
            return NativeAPI.getPlayerExhaustion();
        }

        @JSStaticFunction
        public static void setExhaustion(double val) {
            NativeAPI.setPlayerExhaustion((float) val);
        }

        @JSStaticFunction
        public static void addExperience(int val) {
            NativeAPI.addPlayerExperience(val);
        }

        @JSStaticFunction
        public static void addExp(int val) {
            NativeAPI.addPlayerExperience(val);
        }

        @JSStaticFunction
        public static double getExperience() {
            return NativeAPI.getPlayerExperience();
        }

        @JSStaticFunction
        public static double getExp() {
            return NativeAPI.getPlayerExperience();
        }

        @JSStaticFunction
        public static void setExperience(double val) {
            NativeAPI.setPlayerExperience((float) val);
        }

        @JSStaticFunction
        public static void setExp(double val) {
            NativeAPI.setPlayerExperience((float) val);
        }

        @JSStaticFunction
        public static double getLevel() {
            return NativeAPI.getPlayerLevel();
        }

        @JSStaticFunction
        public static void setLevel(double val) {
            NativeAPI.setPlayerLevel((float) val);
        }

        @JSStaticFunction
        public static int getScore() {
            return NativeAPI.getPlayerScore();
        }

        @JSStaticFunction
        public static boolean isFlying() {
            return NativeAPI.isPlayerFlying();
        }

        @JSStaticFunction
        public static void setFlying(boolean val) {
            NativeAPI.setPlayerFlying(val);
        }

        @JSStaticFunction
        public static boolean canFly() {
            return NativeAPI.canPlayerFly();
        }

        @JSStaticFunction
        public static void setCanFly(boolean val) {
            NativeAPI.setPlayerCanFly(val);
        }

        @JSStaticFunction
        public static double getX() {
            return Entity.getX(get());
        }

        @JSStaticFunction
        public static double getY() {
            return Entity.getY(get());
        }

        @JSStaticFunction
        public static double getZ() {
            return Entity.getY(get());
        }

        @JSStaticFunction
        public static float[] getPosition() {
            return Entity.getPosition(get());
        }

        @JSStaticFunction
        public static int getDimension() {
            return NativeAPI.getDimension();
        }

        @JSStaticFunction
        public static void setFov(double fov) {
            NativeAPI.nativeSetFov((float) fov);
        }

        @JSStaticFunction
        public static void resetFov() {
            NativeAPI.nativeSetFov(-1);
        }

        @JSStaticFunction
        public static void setCameraEntity(Object entity) {
            NativeAPI.nativeSetCameraEntity(Entity.unwrapEntity(entity));
        }

        @JSStaticFunction
        public static void resetCamera() {
            NativeAPI.nativeSetCameraEntity(0);
        }

        @JSStaticFunction
        public static void setAbility(String ability, Object value){
            if(!NativeAPI.isValidAbility(ability)){
                throw new IllegalArgumentException("Invalid ability name: " + ability);
            }

            if(value instanceof Number){
                NativeAPI.setPlayerFloatAbility(ability, ((Number) value).floatValue());
            } else {
                NativeAPI.setPlayerBooleanAbility(ability, (Boolean) value);
            }
        }

        @JSStaticFunction
        public static float getFloatAbility(String ability){
            if(NativeAPI.isValidAbility(ability)){
                return NativeAPI.getPlayerFloatAbility(ability);
            } else throw new IllegalArgumentException("Invalid ability name: " + ability);
        }

        @JSStaticFunction
        public static boolean getBooleanAbility(String ability){
            if(NativeAPI.isValidAbility(ability)){
                return NativeAPI.getPlayerBooleanAbility(ability);
            } else throw new IllegalArgumentException("Invalid ability name: " + ability);
        }
    }






    @APIStaticModule
    public static class Item extends NativeItem {
        protected Item(int id, Object ptr, String nameId, String nameToDisplay) {
            super(id, CustomItem.getItemManager(id), nameId, nameToDisplay);
        }

        @JSStaticFunction
        public static NativeItem createFoodItem(int id, String nameId, String name, String iconName, int iconIndex, int food) {
            /*NativeItem item = createItem(id, nameId, name, iconName, iconIndex);
            String props = "{\"use_animation\":\"eat\",\"use_duration\": 32,\"food\":{\"nutrition\":" + food 
                    + ",\"saturation_modifier\": \"normal\",\"is_meat\": false}, \"components\": {\"minecraft:food\": {\"nutrition\": " 
                    + food + ", \"saturation_modifier\": \"normal\"}}}";
            item.setProperties(props);
            item.setUseAnimation(1);
            item.setMaxUseDuration(32);
            return item;*/
            NativeItem item = createFoodItem(id, nameId, name, food);
            //item.setProperties(null);
            item.setUseAnimation(1);
            item.setMaxUseDuration(32);
            return item;
        }

        @JSStaticFunction
        public static String getName(int id, int data, Object extra) {
            return getNameForId(id, data, NativeItemInstanceExtra.unwrapValue(extra));
        }

        @JSStaticFunction
        public static int getMaxDamage(int id) {
            return getMaxDamageForId(id, 0);
        }

        @JSStaticFunction
        public static int getMaxStackSize(int id, int data) {
            return getMaxStackForId(id, data);
        }

        @JSStaticFunction
        public static void setRequiresIconOverride(int id, boolean enabled) {
            NativeItem.setItemRequiresIconOverride(id, enabled);
        }

        @JSStaticFunction
        public static void overrideCurrentIcon(String name, int index) {
            NativeItem.overrideItemIcon(name, index);
        }

        @JSStaticFunction
        public static void overrideCurrentName(String name) {
            NativeAPI.overrideItemName(name);
        }

        @JSStaticFunction
        public static void invokeItemUseOn(int id, int count, int data, Object extra, int x, int y, int z, int side, double vx, double vy, double vz, Object entity) {
            NativeAPI.invokeUseItemOn(id, count, data, NativeItemInstanceExtra.unwrapValue(extra), x, y, z, side, (float) vx, (float) vy, (float) vz, Entity.unwrapEntity(entity));
        } 

        public static void invokeItemUseNoTarget(int id, int count, int data, Object extra) {
            NativeAPI.invokeUseItemNoTarget(id, count, data, NativeItemInstanceExtra.unwrapValue(extra));
        }
    }


    // keep old methods, but now inherit from apparatus api
    public static class Armor extends ActorArmorHandler {
        private Armor(EntityActor actor) {
            super(actor, false);
        }

        @JSStaticFunction
        public static void registerCallbacks(int id, ScriptableObject obj) {
            ArmorRegistry.registerArmor(id, obj);
        }

        @JSStaticFunction
        public static void preventDamaging(int id) {
            ArmorRegistry.preventArmorDamaging(id);
        }
    }



    public static class ItemExtraData extends NativeItemInstanceExtra {
        public ItemExtraData(long extra) {
            super(extra != 0 ? constructClone(extra) : 0);
        }

        public ItemExtraData(NativeItemInstanceExtra extra) {
            super(extra);
        }

        public ItemExtraData() {
            super();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }


    @APIStaticModule
    public static class NBT extends NbtDataType {
        public static class CompoundTag extends NativeCompoundTag {
            public CompoundTag() {
                super();
            }

            public CompoundTag(NativeCompoundTag tag) {
                super(tag);
            }
        }

        public static class ListTag extends NativeListTag {
            public ListTag() {
                super();
            }

            public ListTag(NativeListTag tag) {
                super(tag);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        }
    }



    @APIStaticModule
    public static class Recipes extends RecipeRegistry {
        @JSStaticFunction
        @Deprecated
        public static void __placeholder() {
            logDeprecation("Recipes.__placeholder");
        }
    }


    @APIStaticModule
    public static class Block {
        private static int anonymousSpecialTypeIndex = 0;

        private static SpecialType parseSpecialType(Object type) {
            SpecialType specialType = SpecialType.DEFAULT;
            if(type instanceof SpecialType){
                specialType = (SpecialType) type;
            } else if(type instanceof String){
                specialType = SpecialType.getSpecialType((String) type);
            } else if(type instanceof ScriptableObject) {
                specialType = SpecialType.createSpecialType("anonymous_type_" + (anonymousSpecialTypeIndex++));
                specialType.setupProperties((ScriptableObject) type);
            }
            return specialType;
        }

        @JSStaticFunction
        public static void createBlock(int uid, String nameId, ScriptableObject variantsScriptable, Object type) {
            BlockRegistry.createBlock(uid, nameId, variantsScriptable, parseSpecialType(type));
        }

        @JSStaticFunction
        public static void createLiquidBlock(int id1, String nameId1, int id2, String nameId2, ScriptableObject variantsScriptable, Object type, int tickDelay, boolean isRenewable) {
            BlockRegistry.createLiquidBlockPair(id1, nameId1, id2, nameId2, variantsScriptable, parseSpecialType(type), tickDelay, isRenewable);
        }

        @JSStaticFunction
        public static String createSpecialType(String name, ScriptableObject props) {
            SpecialType type = SpecialType.createSpecialType(name);
            type.setupProperties(props);
            return type.name;
        }

        @JSStaticFunction
        public static void setShape(int id, double x1, double y1, double z1, double x2, double y2, double z2, Object _data) {
            if (_data instanceof Number) {
                int data = ((Number) _data).intValue();
                BlockRegistry.setShape(id, data, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
            }
            else {
                BlockRegistry.setShape(id, -1, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
            }
        }

        @JSStaticFunction
        public static int getMaterial(int id) {
            return NativeBlock.getMaterial(id);
        }

        @JSStaticFunction
        public static boolean isSolid(int id) {
            return NativeBlock.isSolid(id);
        }

        @JSStaticFunction
        public static boolean canContainLiquid(int id) {
            return NativeBlock.canContainLiquid(id);
        }

        @JSStaticFunction
        public static boolean canBeExtraBlock(int id) {
            return NativeBlock.canBeExtraBlock(id);
        }

        @JSStaticFunction
        public static double getDestroyTime(int id) {
            return NativeBlock.getDestroyTimeForId(id);
        }

        @JSStaticFunction
        public static double getExplosionResistance(int id) {
            return NativeBlock.getExplosionResistance(id);
        }

        @JSStaticFunction
        public static double getFriction(int id) {
            return NativeBlock.getFriction(id);
        }

        @JSStaticFunction
        public static double getTranslucency(int id) {
            return NativeBlock.getTranslucency(id);
        }

        @JSStaticFunction
        public static int getLightLevel(int id) {
            return NativeBlock.getLightLevel(id);
        }

        @JSStaticFunction
        public static int getLightOpacity(int id) {
            return NativeBlock.getLightOpacity(id);
        }

        @JSStaticFunction
        public static int getRenderLayer(int id) {
            return NativeBlock.getRenderLayer(id);
        }

        @JSStaticFunction
        public static int getRenderType(int id) {
            return NativeBlock.getRenderType(id);
        }

        @JSStaticFunction
        public static void setDestroyTime(int id, double time) {
            NativeBlock.setDestroyTimeForId(id, (float) time);
        }

        @JSStaticFunction
        public static void setTempDestroyTime(int id, double time) {
            NativeBlock.setTempDestroyTimeForId(id, (float) time);
        }

        @JSStaticFunction
        public static int getMapColor(int id) {
            return NativeBlock.getMapColor(id);
        }

        @JSStaticFunction
        public static void setRedstoneTile(int id, Object data, final boolean redstone) {
            NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
                @Override
                public void onIdDataIterated(int id, int data) {
                    NativeBlock.setRedstoneTileNative(id, data, redstone);
                }
            });
        }

        @JSStaticFunction
        public static void setRedstoneEmitter(int id, Object data, final boolean redstone) {
            NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
                @Override
                public void onIdDataIterated(int id, int data) {
                    NativeBlock.setRedstoneEmitterNative(id, data, redstone);
                }
            });
        }

        @JSStaticFunction
        public static void setRedstoneConnector(int id, Object data, final boolean redstone) {
            NativeIdMapping.iterateMetadata(id, data, new NativeIdMapping.IIdIterator() {
                @Override
                public void onIdDataIterated(int id, int data) {
                    NativeBlock.setRedstoneConnectorNative(id, data, redstone);
                }
            });
        }

        @JSStaticFunction
        public static void setRandomTickCallback(int id, Function callback) {
            NativeBlock.setRandomTickCallback(id, callback);
        }

        @JSStaticFunction
        public static void setAnimateTickCallback(int id, Function callback) {
            NativeBlock.setAnimateTickCallback(id, callback);
        }
        @JSStaticFunction
        public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
            NativeAPI.setBlockChangeCallbackEnabled(id, enabled);
        }

        @JSStaticFunction
        public static void setEntityInsideCallbackEnabled(int id, boolean enabled) {
            NativeBlock.setReceivingEntityInsideEvent(id, enabled);
        }

        @JSStaticFunction
        public static void setEntityStepOnCallbackEnabled(int id, boolean enabled) {
            NativeBlock.setReceivingEntityStepOnEvent(id, enabled);
        }

        @JSStaticFunction
        public static void setNeighbourChangeCallbackEnabled(int id, boolean enabled) {
            NativeBlock.setReceivingNeighbourChangeEvent(id, enabled);
        }

        @JSStaticFunction
        public static ScriptableObject getBlockAtlasTextureCoords(String name, int id) {
            float[] coords = new float[4];
            NativeAPI.getAtlasTextureCoords(name, id, coords);
            ScriptableObject result = ScriptableObjectHelper.createEmpty();
            result.put("u1", result, coords[0]);
            result.put("v1", result, coords[1]);
            result.put("u2", result, coords[2]);
            result.put("v2", result, coords[3]);
            return result;
        }
    }



    public static class RenderMesh extends NativeRenderMesh {
        public RenderMesh() {
            super();
        }

        public RenderMesh(String file, String type, Scriptable params) {
            this();
            importFromFile(file, type, params);
        }

        public RenderMesh(String file, String type) {
            this(file, type, null);
        }

        public RenderMesh(String file) {
            this(file, "obj");
        }
    }


    @APIStaticModule
    public static class ItemModel extends NativeItemModel {
        
    }


    @APIStaticModule
    public static class CustomEnchant extends NativeCustomEnchant {

    }


    @APIStaticModule
    public static class BlockRenderer extends NativeBlockRenderer {
        public static class Model extends NativeBlockModel {
            public Model() {
                super();
            }

            public Model(NativeRenderMesh mesh) {
                super(mesh);
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, ScriptableObject obj) {
                super();
                addBox(x1, y1, z1, x2, y2, z2, obj);
            }

            public Model(ScriptableObject obj) {
                this(0, 0, 0, 1, 1, 1, obj);
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, String texName, int texId) {
                super();
                addBox(x1, y1, z1, x2, y2, z2, texName, texId);
            }

            public Model(String texName, int texId) {
                this(0, 0, 0, 1, 1, 1, texName, texId);
            }

            public Model(float x1, float y1, float z1, float x2, float y2, float z2, int id, int data) {
                super();
                addBox(x1, y1,z1, x2, y2, z2, id, data);
            }

            public Model(int id, int data) {
                this(0, 0, 0, 1, 1, 1, id, data);
            }
        }

        @JSStaticFunction
        public static NativeBlockModel createModel() {
            return new NativeBlockModel();
        }

        @JSStaticFunction
        public static NativeBlockModel createTexturedBox(double x1, double y1, double z1, double x2, double y2, double z2, ScriptableObject tex) {
            return NativeBlockModel.createTexturedBox((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, tex);
        }

        @JSStaticFunction
        public static NativeBlockModel createTexturedBlock(ScriptableObject tex) {
            return NativeBlockModel.createTexturedBlock(tex);
        }

        @JSStaticFunction
        public static void addRenderCallback(int id, Function callback) {
            NativeBlockRenderer._addRenderCallback(id, callback);
        }

        @JSStaticFunction
        public static void forceRenderRebuild(int x, int y, int z, int mode) {

        }
    }





    @APIStaticModule
    public static class Renderer extends NativeRenderer {
        @JSStaticFunction
        public static NativeRenderer.Renderer getItemModel(int id, int count, int data, double scale, double rX, double rY, double rZ, boolean randomize) {
            return null;
        }
    }



    @APIStaticModule
    public static class StaticRenderer {
        @JSStaticFunction
        public static NativeStaticRenderer createStaticRenderer(int renderer, double x, double y, double z) {
            try {
                if (renderer == -1) {
                    return NativeStaticRenderer.createStaticRenderer(null, (float) x, (float) y, (float) z);
                } else {
                    return NativeStaticRenderer.createStaticRenderer(NativeRenderer.getRendererById(renderer), (float) x, (float) y, (float) z);
                }
            }
            catch (NullPointerException e) {
                throw new IllegalArgumentException("invalid renderer id " + renderer + ", id must belong only to custom renderer");
            }
        }
    }



    public static class ActorRenderer extends NativeActorRenderer {
        public ActorRenderer() {
            super();
        }

        public ActorRenderer(String template) {
            super(template);
        }
    }



    public static class AttachableRender extends NativeAttachable {
        public AttachableRender(long actorUid) {
            super(actorUid);
        }

        @JSStaticFunction
        public static void attachRendererToItem(int id, Object renderer, String texture, String material) {
            NativeAttachable.attachRendererToItem(id, (NativeActorRenderer) Context.jsToJava(renderer, NativeActorRenderer.class), texture != null ? texture : "", material != null ? material : "");
        }

        @JSStaticFunction
        public static void detachRendererFromItem(int id) {
            NativeAttachable.detachRendererFromItem(id);
        }
    }



    @APIStaticModule
    public static class Callback {
        @JSStaticFunction
        public static void addCallback(String name, Function func, int priority) {
            com.zhekasmirnov.innercore.api.runtime.Callback.addCallback(name, func, priority);
        }

        @JSStaticFunction
        public static void invokeCallback(String name, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10) {
            com.zhekasmirnov.innercore.api.runtime.Callback.invokeCallback(name, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
        }
    }



    @APIStaticModule
    public static class Updatable {
        @JSStaticFunction
        public static void addUpdatable(ScriptableObject obj) {
            com.zhekasmirnov.innercore.api.runtime.Updatable.getForServer().addUpdatable(obj);
        }
        
        @JSStaticFunction
        public static void addLocalUpdatable(ScriptableObject obj) {
            addAnimator(obj);
        }
        
        @JSStaticFunction
        public static void addAnimator(ScriptableObject obj) {
            com.zhekasmirnov.innercore.api.runtime.Updatable.getForClient().addUpdatable(obj);
        }

        @JSStaticFunction
        public static List<ScriptableObject> getAll() {
            return com.zhekasmirnov.innercore.api.runtime.Updatable.getForServer().getAllUpdatableObjects();
        }

        @JSStaticFunction
        public static int getSyncTime() {
            return NativeCallback.getGlobalServerTickCounter();
        }
    }


    @APIStaticModule
    public static class Saver {
        @APIIgnore
        public static interface IScopeSaver {
            void read(Object scope);
            Object save();
        }

        @APIIgnore
        public static interface IObjectSaver {
            Object read(ScriptableObject input);
            ScriptableObject save(Object input);
        }

        @JSStaticFunction
        public static void registerScopeSaver(final String name, Object scopeSaver) {
            final IScopeSaver saver = (IScopeSaver) Context.jsToJava(scopeSaver, IScopeSaver.class);
            WorldDataScopeRegistry.getInstance().addScope(name, new ScriptableSaverScope() {
                @Override
                public Object save() {
                    return saver.save();
                }

                @Override
                public void read(Object scope) {
                    if (scope == null) {
                        scope = ScriptableObjectHelper.createEmpty();
                    }
                    saver.read(scope);
                }
            });
        }

        @JSStaticFunction
        public static int registerObjectSaver(final String name, Object scopeSaver) {
            final IObjectSaver saver = (IObjectSaver) Context.jsToJava(scopeSaver, IObjectSaver.class);
            return ObjectSaverRegistry.registerSaver(name, new ObjectSaver() {
                @Override
                public Object read(ScriptableObject input) {
                    return saver.read(input);
                }

                @Override
                public ScriptableObject save(Object input) {
                    return saver.save(input);
                }
            });
        }

        @JSStaticFunction
        public static String serializeToString(Object param) {
            return ScriptableSerializer.jsonToString(ScriptableSerializer.scriptableToJson(param, null));
        }

        @JSStaticFunction
        public static Object deserializeFromString(String param) {
            try {
                return ScriptableSerializer.scriptableFromJson(ScriptableSerializer.stringToJson(param));
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @JSStaticFunction
        public static void registerObject(Object obj, int saverId) {
            ObjectSaverRegistry.registerObject(obj, saverId);
        }

        @JSStaticFunction
        public static void setObjectIgnored(ScriptableObject obj, boolean ignore) {
            ObjectSaverRegistry.setObjectIgnored(obj, ignore);
        }

        @JSStaticFunction
        public static int getObjectSaverId(Object obj) {
            ObjectSaver saver = ObjectSaverRegistry.getSaverFor(obj);
            return saver != null ? saver.getSaverId() : -1;
        }
    }



    public static class Config extends com.zhekasmirnov.innercore.mod.build.Config {
        public Config(File file) {
            super(file);
        }

        public Config(CharSequence path) {
            super(new File(path.toString()));
        }
    }



    @APIStaticModule
    public static class Resources {
        @JSStaticFunction
        public static String getItemTextureName(String name, int index) {
            return ResourcePackManager.getItemTextureName(name, index);
        }

        @JSStaticFunction
        public static String getBlockTextureName(String name, int index) {
            return ResourcePackManager.getBlockTextureName(name, index);
        }

        @JSStaticFunction
        public static InputStream getInputStream(String name) {
            return FileTools.getAssetInputStream(name);
        }

        @JSStaticFunction
        public static byte[] getBytes(String name) {
            return FileTools.getAssetBytes(name);
        }
    }



    // TODO: create module
    @APIStaticModule
    public static class Translation {
        @JSStaticFunction
        public static void addTranslation(String name, ScriptableObject localization) {
            NameTranslation.addTranslation(name, localization);
        }

        @JSStaticFunction
        public static String translate(String str) {
            return NameTranslation.translate(str);
        }

        @JSStaticFunction
        public static String getLanguage() {
            return NameTranslation.getLanguage();
        }
    }



    @APIStaticModule
    public static class Particles extends ParticleRegistry {

    }

    @APIStaticModule
    public static class WorldRenderer {
        @JSStaticFunction
        public static Object getGlobalUniformSet() {
            return new NativeShaderUniformSet(0);
        }
    }

    @APIStaticModule
    public static class UI {
        public static class Container extends com.zhekasmirnov.innercore.api.mod.ui.container.Container {
            public Container() {
                super();
            }

            public Container(Object parent) {
                super(parent);
            }
        }

        public static class Window extends UIWindow {
            public Window(UIWindowLocation location) {
                super(location);
            }

            public Window(ScriptableObject content) {
                super(content);
            }

            public Window() {
                super(ScriptableObjectHelper.createEmpty());
            }
        }

        public static class WindowGroup extends UIWindowGroup {
            public WindowGroup() {
                super();
            }
        }

        // legacy
        public static class StandartWindow extends UIWindowStandard {
            public StandartWindow(ScriptableObject content) {
                super(content);
            }

            public StandartWindow() {
                super(ScriptableObjectHelper.createEmpty());
            }

            protected boolean isLegacyFormat() {
                return true;
            }
        }

        // new one
        public static class StandardWindow extends UIWindowStandard {
            public StandardWindow(ScriptableObject content) {
                super(content);
            }

            public StandardWindow() {
                super(ScriptableObjectHelper.createEmpty());
            }

            @Override
            protected boolean isLegacyFormat() {
                return false;
            }
        }

        public static class AdaptiveWindow extends UIAdaptiveWindow {
            public AdaptiveWindow(ScriptableObject content) {
            }

            public AdaptiveWindow() {
            }
        }

        public static class TabbedWindow extends UITabbedWindow {
            public TabbedWindow(UIWindowLocation location) {
            }

            public TabbedWindow(ScriptableObject content) {
            }

            public TabbedWindow() {
            }
        }

        public static class WindowLocation extends UIWindowLocation {
            public WindowLocation() {
                super();
            }

            public WindowLocation(ScriptableObject obj) {
                super(obj);
            }
        }

        public static class Font extends com.zhekasmirnov.innercore.api.mod.ui.types.Font {
            public Font(int color, float size, float shadow) {
                super(color, size, shadow);
            }

            public Font(ScriptableObject obj) {
                super(obj);
            }
        }

        public static class ConfigVisualizer {
            public ConfigVisualizer(com.zhekasmirnov.innercore.mod.build.Config config, String prefix) {

            }

            public ConfigVisualizer(com.zhekasmirnov.innercore.mod.build.Config config) {

            }
        }

        @APIStaticModule
        public static class FrameTextureSource {
            @JSStaticFunction
            public static Object get(String name) {
                return null;
            }
        }

        @APIStaticModule
        public static class TextureSource {
            @JSStaticFunction
            public static Object get(String name) {
                return com.zhekasmirnov.innercore.api.mod.ui.TextureSource.instance.getSafe(name);
            }

            @JSStaticFunction
            public static Object getNullable(String name) {
                return com.zhekasmirnov.innercore.api.mod.ui.TextureSource.instance.get(name);
            }

            @JSStaticFunction
            public static void put(String name, Object bmp) {
                com.zhekasmirnov.innercore.api.mod.ui.TextureSource.instance.put(name, null);
            }
        }

        @JSStaticFunction
        public static float getMinecraftUiScale() {
            return 1;
        }

        @JSStaticFunction
        public static float getRelMinecraftUiScale() {
            return 1;
        }

        @JSStaticFunction
        public static float getScreenRelativeHeight() {
            return 1;
        }

        @JSStaticFunction
        public static float getScreenHeight() {
            return getScreenRelativeHeight();
        }

        @JSStaticFunction
        public static Object getContext() {
            return null;
        }

    }


    @APIStaticModule
    public static class GenerationUtils extends NativeGenerationUtils {
        
    }

    public static class CustomBiome extends com.zhekasmirnov.innercore.api.biomes.CustomBiome {
        public CustomBiome(String name) {
            super(name);
        }
    }

    @APIStaticModule
    public static class Dimensions {
        public static class CustomDimension extends com.zhekasmirnov.innercore.api.dimensions.CustomDimension {
            public CustomDimension(String name, int preferredId) {
                super(name, preferredId);
            }
        }

        public static class CustomGenerator extends CustomDimensionGenerator {
            public CustomGenerator(int type) {
                super(type);
            }

            public CustomGenerator(String type) {
                super(type);
            }
        }

        public static class MonoBiomeTerrainGenerator extends com.zhekasmirnov.innercore.api.dimensions.MonoBiomeTerrainGenerator {
            public MonoBiomeTerrainGenerator() {
                super();
            }
        }

        public static class NoiseConversion extends com.zhekasmirnov.innercore.api.dimensions.NoiseConversion {
            public NoiseConversion() {
                super();
            }
        }

        public static class NoiseOctave extends com.zhekasmirnov.innercore.api.dimensions.NoiseOctave {
            public NoiseOctave(int type) {
                super(type);
            }

            public NoiseOctave(String type) {
                super(type);
            }

            public NoiseOctave() {
                super();
            }
        }

        public static class NoiseLayer extends com.zhekasmirnov.innercore.api.dimensions.NoiseLayer {
            public NoiseLayer() {
                super();
            }
        }

        public static class NoiseGenerator extends com.zhekasmirnov.innercore.api.dimensions.NoiseGenerator {
            public NoiseGenerator() {
                super();
            }
        }


        @JSStaticFunction
        public static void overrideGeneratorForVanillaDimension(int id, Object generator) {
            if (generator instanceof Wrapper) {
                generator = ((Wrapper) generator).unwrap();
            }
            com.zhekasmirnov.innercore.api.dimensions.CustomDimension.setCustomGeneratorForVanillaDimension(id, (CustomDimensionGenerator) generator);
        }
    
        @JSStaticFunction
        public static com.zhekasmirnov.innercore.api.dimensions.CustomDimension getDimensionByName(String name) {
            return com.zhekasmirnov.innercore.api.dimensions.CustomDimension.getDimensionByName(name);
        }
    
        @JSStaticFunction
        public static com.zhekasmirnov.innercore.api.dimensions.CustomDimension getDimensionById(int id) {
            return com.zhekasmirnov.innercore.api.dimensions.CustomDimension.getDimensionById(id);
        }
        
        @JSStaticFunction
        public static boolean isLimboId(int id) {
            return com.zhekasmirnov.innercore.api.dimensions.CustomDimension.isLimboId(id);
        }
        
        
        @JSStaticFunction
        public static void transfer(Object entity, int dimension) {
            NativeAPI.transferToDimension(Entity.unwrapEntity(entity), dimension);
        }

        @JSStaticFunction
        public static ScriptableObject getAllRegisteredCustomBiomes() {
            return ScriptableObjectHelper.createFromMap(CustomBiome.getAllCustomBiomes());
        }
    }


    @JSStaticFunction
    public static void runOnMainThread(Object _action) {
        Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
        MainThreadQueue.serverThread.enqueue(action);
    }

    @JSStaticFunction
    public static void runOnClientThread(Object _action) {
        Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
        MainThreadQueue.localThread.enqueue(action);
    }

    @APIStaticModule
    public static class MCSystem {
        @JSStaticFunction
        public static NetworkJsAdapter getNetwork() {
            return new NetworkJsAdapter(Network.getSingleton());
        }




        @JSStaticFunction
        public static void debugStr(String s) {

        }


        @JSStaticFunction
        public static String getMinecraftVersion() {
            return MinecraftVersions.getCurrent().getName();
        }

        @JSStaticFunction
        public static Object getInnerCoreVersion() {
            return Version.INNER_CORE_VERSION.name;
        }

        @JSStaticFunction
        public static void throwException(String msg) {
            throw new RuntimeException(msg);
        }

        @JSStaticFunction
        public static void debugAPILookUp() {
            API.debugLookUpClass(AdaptedScriptAPI.class);
        }

        @JSStaticFunction
        public static void runOnMainThread(Object _action) {
            Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
            MainThreadQueue.serverThread.enqueue(action);
        }

        @JSStaticFunction
        public static void runOnClientThread(Object _action) {
            Runnable action = (Runnable) Context.jsToJava(_action, Runnable.class);
            MainThreadQueue.localThread.enqueue(action);
        }

        @JSStaticFunction
        public static void setLoadingTip(String tip) {
            LoadingUI.setTip(tip);
        }

        @JSStaticFunction
        public static void setNativeThreadPriority(int p) {
            logDeprecation("setNativeThreadPriority");
        }

        @JSStaticFunction
        public static void forceNativeCrash() {

        }
        
        @JSStaticFunction
        public static boolean isDefaultPrevented() {
            return NativeAPI.isDefaultPrevented();
        }

        @JSStaticFunction
        public static boolean isMainThreadStopped() {
            return NativeCallback.isServerTickDisabledDueToError();
        }

        @JSStaticFunction
        public static Object evalInScope(String script, Scriptable scope, String name) {
            return Compiler.assureContextForCurrentThread().evaluateString(scope, script, name, 0, null);
        }

        @JSStaticFunction
        public static String addRuntimePack(String typeStr, String name) {
            ModLoader.MinecraftPackType type = ModLoader.MinecraftPackType.fromString(typeStr);
            return ModLoader.instance.addRuntimePack(type, name).getAbsolutePath();
        }

        @JSStaticFunction
        public static void setCustomFatalErrorCallback(Object callback) {

        }

        @JSStaticFunction
        public static void setCustomNonFatalErrorCallback(Object callback) {

        }

        @JSStaticFunction
        public static void setCustomStartupErrorCallback(Object callback) {

        }

        @JSStaticFunction
        public static String getCurrentThreadType() {
            return ThreadTypeMarker.getSingleton().getCurrentThreadMark().toString();
        }
    }


    @APIStaticModule
    public static class FileUtil {
        @JSStaticFunction
        public static String readFileText(String path) {
            try {
                return FileTools.readFileText(new File(path));
            } catch (IOException exception) {
                ICLog.e("FileUtil", "error in reading file " + path, exception);
                return null;
            }
        }

        @JSStaticFunction
        public static void writeFileText(String path, String text) {
            try {
                FileTools.writeFileText(new File(path), text);
            } catch (IOException exception) {
                ICLog.e("FileUtil", "error in writing file " + path, exception);
            }
        }
    }

    
    @APIStaticModule
    public static class Commands {
        @JSStaticFunction
        public static String exec(String command, Object player0, Object blockSource0) {
            NativeBlockSource blockSource = null;
            try {
                blockSource = (NativeBlockSource) Context.jsToJava(blockSource0, NativeBlockSource.class);
            } catch (Exception ignore) { }
            return NativeAPI.executeCommand(command, 0, 0, 0, blockSource != null ? blockSource.getPointer() : null);
        }

        @JSStaticFunction
        public static String execAt(String command, int x, int y, int z, Object blockSource0) {
            NativeBlockSource blockSource = null;
            try {
                blockSource = (NativeBlockSource) Context.jsToJava(blockSource0, NativeBlockSource.class);
            } catch (Exception ignore) { }
            return NativeAPI.executeCommand(command, x, y, z, blockSource != null ? blockSource.getPointer() : null);
        }
    }

    
    @APIStaticModule
    public static class TagRegistry extends com.zhekasmirnov.innercore.api.mod.TagRegistry {

        private static List<String> toTagList(NativeArray arr) {
            List<String> result = new ArrayList<>();
            if (arr != null) {
                for (Object obj : arr.toArray()) {
                    if (obj != null) {
                        result.add(obj.toString());
                    }
                }
            }
            return result;
        }

        private static String[] toTagArray(NativeArray arr) {
            List<String> tags = toTagList(arr);
            String[] result = new String[tags.size()];
            tags.toArray(result);
            return result;
        }

        @JSStaticFunction
        public static void addTagFactory(String group, Object factory) {
            getOrCreateGroup(group).addTagFactory((TagFactory) Context.jsToJava(factory, TagFactory.class));
        }

        @JSStaticFunction
        public static void addCommonObject(String group, Object obj, NativeArray tags) {
            getOrCreateGroup(group).addCommonObject(obj, toTagArray(tags));
        }

        @JSStaticFunction
        public static void removeCommonObject(String group, Object obj) {
            getOrCreateGroup(group).removeCommonObject(obj);
        }

        @JSStaticFunction
        public static void addTagsFor(String group, Object obj, NativeArray tags, boolean noAdd) {
            if (noAdd) {
                getOrCreateGroup(group).addTagsFor(obj, toTagArray(tags));
            } else {
                getOrCreateGroup(group).addCommonObject(obj, toTagArray(tags));
            }
        }

        @JSStaticFunction
        public static void addTagFor(String group, Object obj, String tag, boolean noAdd) {
            if (noAdd) {
                getOrCreateGroup(group).addTagsFor(obj, tag);
            } else {
                getOrCreateGroup(group).addCommonObject(obj, tag);
            }
        }

        @JSStaticFunction
        public static void removeTagsFor(String group, Object obj, NativeArray tags) {
            getOrCreateGroup(group).removeTagsFor(obj, toTagArray(tags));
        }

        @JSStaticFunction
        public static NativeArray getTagsFor(String group, Object obj) {
            Collection<String> tags = getOrCreateGroup(group).getTags(obj);
            return ScriptableObjectHelper.createArray(tags.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWith(String group, Object _predicate) {
            TagPredicate predicate = (TagPredicate) Context.jsToJava(_predicate, TagPredicate.class);
            Collection<Object> objects = getOrCreateGroup(group).getAllWhere(predicate);
            return ScriptableObjectHelper.createArray(objects.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWithTags(String group, NativeArray tags) {
            Collection<Object> objects = getOrCreateGroup(group).getAllWithTags(toTagList(tags));
            return ScriptableObjectHelper.createArray(objects.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllWithTag(String group, String tag) {
            Collection<Object> objects = getOrCreateGroup(group).getAllWithTag(tag);
            return ScriptableObjectHelper.createArray(objects.toArray());
        }
    }


    public static class NetworkConnectedClientList extends ConnectedClientList {
        public NetworkConnectedClientList(boolean addToGlobalRefreshList) {
            super(addToGlobalRefreshList);
        }

        public NetworkConnectedClientList() {
            super();
        }
    }

    public static class NetworkEntity extends com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity {

        public NetworkEntity(com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType type, Object target, String name) {
            super(type, target, name);
        }

        public NetworkEntity(com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType type, Object target) {
            super(type, target);
        }

        public NetworkEntity(com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType type) {
            super(type);
        }
    }

    public static class NetworkEntityType extends com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType {
        public NetworkEntityType(String typeName) {
            super(typeName);
        }
    }

    public static class SyncedNetworkData extends com.zhekasmirnov.apparatus.multiplayer.util.entity.SyncedNetworkData {
        public SyncedNetworkData(String name) {
            super(name);
        }

        public SyncedNetworkData() {
            super();
        }
    }

    public static class ItemContainer extends com.zhekasmirnov.apparatus.api.container.ItemContainer {
        public ItemContainer() {
            super();
        }

        public ItemContainer(Container container){
            super(container);
        }
    }

    public static class BlockSource extends NativeBlockSource {
        public BlockSource(int dimension, boolean b1, boolean b2) {
            super(dimension, b1, b2);
        }

        public BlockSource(int dimension) {
            super(dimension);
        }
    }

    public static class BlockState extends com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState {
        public BlockState(int id, int data) {
            super(id, data);
        }

        public BlockState(int id, Object scriptable) {
            super(id, (Scriptable) scriptable);
        }
    }

    public static class PlayerActor extends NativePlayer {
        public PlayerActor(long entity) {
            super(entity);
        }
    }

    public static class IntFlatMap extends com.zhekasmirnov.apparatus.ecs.core.collection.IntFlatMap {
        IntFlatMap(int emptyKey) {
            super(emptyKey);
        }

        IntFlatMap() {
            super(0);
        }
    }

    @APIStaticModule
    public static class ECS {
        @JSStaticFunction
        public static Object getEntityManager() {
            return Context.javaToJS(com.zhekasmirnov.apparatus.ecs.ECS.getEntityManager(), ScriptableObjectHelper.getDefaultScope());
        }

        @JSStaticFunction
        public static String getTypeName(int typeId) {
            return TypeIndexMap.getTypeName(typeId);
        }

        @JSStaticFunction
        public static int getTypeId(String name) {
            return TypeIndexMap.getTypeIndex(name);
        }

        @JSStaticFunction
        public static int getInvalidEntity() {
            return EntityManager.INVALID_ENTITY;
        }

        @JSStaticFunction
        public static Object getTagComponentObject() {
            return EntityManager.TAG;
        }
    }

    public static class EcsAddComponents extends ComponentCollection {
        EcsAddComponents() {
            super();
        }
    }

    public static class EcsRemoveComponents extends ComponentCollection {
        EcsRemoveComponents() {
            super();
        }
    }

    public static class EcsQuery extends Query {
        public EcsQuery(Object... componentTypes) {
            super(componentTypes);
        }
    }

    public static class EcsActionQueue extends DelayedActionQueue {
        public EcsActionQueue() {
            super(com.zhekasmirnov.apparatus.ecs.ECS.getEntityManager());
        }
    }
    
    @JSStaticFunction
    public static Function requireMethodFromNativeAPI(String _className, final String methodName, final boolean denyConversion) {
        if (!_className.startsWith("com.zhekasmirnov.innercore.")) {
            _className = "com.zhekasmirnov.innercore." + _className;
        }

        final String className = _className;

        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }

        Method[] methods = clazz.getMethods();
        Method _method = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                _method = m;
            }
        }

        if (_method == null) {
            throw new RuntimeException("method cannot be found class=" + className + " method=" + methodName);
        }

        final Method method = _method;
        final Class[] types = method.getParameterTypes();
        final Object[] javaParams = new Object[types.length];
        final boolean[] isNumberParam = new boolean[types.length];
        final int[] numberParamType = new int[types.length];

        boolean _onlyNumbers = true;
        for (int i = 0; i < types.length; i++) {
            isNumberParam[i] = int.class.isAssignableFrom(types[i]) || double.class.isAssignableFrom(types[i]) || float.class.isAssignableFrom(types[i]);

            if (!isNumberParam[i]) {
                _onlyNumbers = false;
            }
            else {
                if (int.class.isAssignableFrom(types[i])) {
                    numberParamType[i] = 0;
                }
                else if (float.class.isAssignableFrom(types[i])) {
                    numberParamType[i] = 1;
                }
                else if (double.class.isAssignableFrom(types[i])) {
                    numberParamType[i] = 1;
                }
            }
        }

        final boolean onlyNumbers = _onlyNumbers;

        return new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                Object _params;
                if (denyConversion) {
                    _params = params;
                }
                else {
                    if (onlyNumbers) {
                        for (int i = 0; i < types.length; i++) {
                            Number param = 0;
                            if (i < params.length) {
                                Object _param = params[i];
                                param = _param instanceof Number ? (Number) _param : 0;
                            }
                            switch (numberParamType[i]) {
                                case 0:
                                    javaParams[i] = param.intValue();
                                    break;
                                case 1:
                                    javaParams[i] = param.floatValue();
                                    break;
                                case 2:
                                    javaParams[i] = param.doubleValue();
                                    break;
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < types.length; i++) {
                            Object param = i >= params.length ? null : params[i];

                            if (param == null && isNumberParam[i]){
                                param = 0.0;
                            }

                            javaParams[i] = Context.jsToJava(param, types[i]);
                        }
                    }
                    _params = javaParams;
                }

                try {
                    return Context.javaToJS(method.invoke(null, (Object[]) _params), parent);
                } catch (IllegalAccessException e) {
                    ICLog.i("ERROR", "failed to call required java method class=" + className + " method=" + methodName);
                    throw new RuntimeException(e.toString());
                } catch (InvocationTargetException e) {
                    ICLog.i("ERROR", "failed to call required java method class=" + className + " method=" + methodName);
                    throw new RuntimeException(e.toString());
                } catch(Exception e) {
                    ICLog.i("ERROR", "failed to call required java method class=" + className + " method=" + methodName);
                    throw e;
                }
            }
        };
    }
}
