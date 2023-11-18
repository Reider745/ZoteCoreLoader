package com.zhekasmirnov.innercore.api;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import com.reider745.InnerCoreServer;
import com.reider745.entity.EntityMethod;
import com.reider745.item.ItemMethod;
import com.reider745.world.WorldMethod;

public class NativeAPI {


    public static int getEntityDimension(long entity){
        return EntityMethod.getEntityDimension(entity);
    }

    public static void getPosition(long entity, float[] pos){
        EntityMethod.getPosition(entity, pos);
    }

    public static int getHealth(long entity){
        return EntityMethod.getHealth(entity);
    }

    public static int getMaxHealth(long entity){
        return EntityMethod.getMaxHealth(entity);
    }

    public static Item getEntityCarriedItem(long entity){
        return EntityMethod.getEntityCarriedItem(entity);
    }

    public static Item getEntityOffhandItem(long entity){
        return EntityMethod.getEntityOffhandItem(entity);
    }
    public static Item getEntityArmor(long entity, int armor){
        return EntityMethod.getEntityArmor(entity, armor);
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, long extra){
        EntityMethod.setEntityArmor(entity, slot, id, count, data, extra);
    }

    public static void setPlayerArmor(int slot, int id, int count, int data, long extra){
        InnerCoreServer.useNotSupport("setPlayerArmor");
    }

    public static String getGameLanguage(){
        return "en";
    }

    public static void sendCachedItemNameOverride(int id, int data, String name){

    }

    public static boolean isValidEntity(long entity){
        return EntityMethod.isValidEntity(entity);
    }

    public static long getPlayer(){
        InnerCoreServer.useNotSupport("getPlayer");
        return 0;
    }

    public static Item getPlayerArmor(int player){
        InnerCoreServer.useNotSupport("getPlayerArmor");
        return null;
    }

    public static String getStringIdAndTypeForIntegerId(int id){
        return ItemMethod.getStringIdAndTypeForIntegerId(id);
    }

    public static int getSeed(){
        return 0;
    }
    public static void forceLevelSave(){

    }

    public static boolean isDefaultPrevented(){
        return false;
    }

    public static int getTile(int x, int y, int z){
        InnerCoreServer.useNotSupport("getTile");
        return 0;
    }

    public static int getTileAndData(int x, int y, int z) {
        InnerCoreServer.useNotSupport("getTileAndData");
        return 0;
    }
    public static void setTile(int x, int y, int z, int id, int data){
        InnerCoreServer.useNotSupport("setTile");
    }

    public static void addTextureToLoad(String name){

    }

    public static String convertNameId(String name){
        return name.replace(" ", "_").toLowerCase();
    }

    public static boolean isGlintItemInstance(int id, int data, long extra){
        return false;
    }

    public static void setItemRequiresIconOverride(int id, boolean enable){

    }

    public static void overrideItemIcon(String name, int index){

    }

    public static Item getInventorySlot(int index){
        InnerCoreServer.useNotSupport("getInventorySlot");
        return null;
    }

    public static void setInventorySlot(int index, int id, int count, int data, long extra){
        InnerCoreServer.useNotSupport("setInventorySlot");
    }

    public static void preventDefault() {
    }

    public static void explode(float x, float y, float z, float power, boolean onFire) {
        InnerCoreServer.useNotSupport("explode");
    }

    public static void addParticle(){

    }

    public static void addFarParticle(){

    }

    public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
    }

    public static int getData(int x, int y, int z) {
        InnerCoreServer.useNotSupport("getData");
        return 0;
    }

    public static int getBrightness(int x, int y, int z) {
        InnerCoreServer.useNotSupport("getBrightness");
        return 0;
    }

    public static boolean isChunkLoaded(int x, int z) {
        InnerCoreServer.useNotSupport("isChunkLoaded");
        return false;
    }

    public static int getChunkState(int x, int z) {
        InnerCoreServer.useNotSupport("getChunkState");
        return 0;
    }

    public static int getBiome(int x, int z) {
        InnerCoreServer.useNotSupport("getBiome");
        return 0;
    }

    public static void setBiome(int x, int z, int id) {
        InnerCoreServer.useNotSupport("setBiome");
    }

    public static int getBiomeMap(int x, int z) {
        InnerCoreServer.useNotSupport("getBiomeMap");
        return 0;
    }

    public static void setBiomeMap(int x, int z, int id) {
        InnerCoreServer.useNotSupport("setBiomeMap");
    }

    public static String getBiomeName(int id) {
        InnerCoreServer.useNotSupport("getBiomeName");
        return "";
    }

    public static float getBiomeTemperatureAt(int x, int y, int z) {
        InnerCoreServer.useNotSupport("getBiomeTemperatureAt");
        return 0;
    }

    public static int getGrassColor(int x, int z) {
        InnerCoreServer.useNotSupport("getGrassColor");
        return 0;
    }

    public static void setGrassColor(int x, int z, int color) {
        InnerCoreServer.useNotSupport("setGrassColor");
    }

    public static void destroyBlock(int x, int y, int z, boolean drop) {
        InnerCoreServer.useNotSupport("destroyBlock");
    }

    public static long spawnEntity(int id, float x, float y, float z) {
        InnerCoreServer.useNotSupport("spawnEntity");
        return 0;
    }

    public static void setSkin(long entity, String skin) {
        InnerCoreServer.useNotSupport("setSkin");
    }

    public static void spawnExpOrbs(float x, float y, float z, int amount) {
        InnerCoreServer.useNotSupport("spawnExpOrbs");
    }

    public static long spawnDroppedItem(float x, float y, float z, int id, int count, int data, long unwrapValue) {
        InnerCoreServer.useNotSupport("spawnDroppedItem");
        return 0;
    }

    public static long getTime() {
        return WorldMethod.getTime();
    }

    public static void setTileUpdateType(){
        InnerCoreServer.useNotSupport("setTileUpdateType");
    }

    public static void setTileUpdateAllowed(){
        InnerCoreServer.useNotSupport("setTileUpdateAllowed");
    }

    public static void setTime(int time) {
        WorldMethod.setTime(time);
    }

    public static int getGameMode() {
        return WorldMethod.getGameMode();
    }

    public static void setGameMode(int mode) {
        WorldMethod.setGameMode(mode);
    }

    public static int getDifficulty() {
        return WorldMethod.getDifficulty();
    }

    public static void setDifficulty(int val) {
        WorldMethod.setDifficulty(val);
    }

    public static double getRainLevel() {
        return WorldMethod.getRainLevel();
    }

    public static void setRainLevel(float val) {
        WorldMethod.setRainLevel(val);
    }

    public static double getLightningLevel() {
        return WorldMethod.getLightningLevel();
    }

    public static void setLightningLevel(float val) {
        WorldMethod.setLightningLevel(val);
    }

    public static void playSound(String name, float x, float y, float z, float f1, float f2) {
        //InnerCoreServer.useClientMethod("playSound");
    }

    public static void playSoundEnt(String name, long unwrapEntity, float f1, float f2) {
        //InnerCoreServer.useClientMethod("playSound");
    }

    public static void setNightMode(boolean val) {
        InnerCoreServer.useClientMethod("playSound");
    }

    public static void setRespawnCoords(int x, int y, int z) {
        InnerCoreServer.useNotSupport("setRespawnCoords");
    }

    public static long clipWorld(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] clip) {
        InnerCoreServer.useHzMethod("clipWorld");
        return 0;
    }

    public static void setSkyColor(float r, float g, float b) {
    }

    public static void resetSkyColor() {
    }

    public static void setCloudColor(float r, float g, float b) {
    }

    public static void resetCloudColor() {

    }

    public static void setSunsetColor(float r, float g, float b) {
    }

    public static void resetSunsetColor() {
    }

    public static void setFogColor(float r, float g, float b) {
    }

    public static void resetFogColor() {
    }

    public static void setFogDistance(float start, float end) {
    }

    public static void resetFogDistance() {
    }

    public static void setUnderwaterFogColor(float r, float g, float b) {
    }

    public static void resetUnderwaterFogColor() {
    }

    public static void setUnderwaterFogDistance(float start, float end) {
    }

    public static void resetUnderwaterFogDistance() {
    }

    public static void setItemNameOverrideCallbackForced(int id){

    }

    public static void setPosition(long unwrapEntity, float x, float y, float z) {
        EntityMethod.setPosition(unwrapEntity, x, y, z);
    }

    public static void setPositionAxis(long unwrapEntity, int axis, float val) {
    }

    public static void getVelocity(long unwrapEntity, float[] pos) {
    }

    public static void setVelocity(long unwrapEntity, float x, float y, float z) {
    }

    public static void setVelocityAxis(long unwrapEntity, int axis, float val) {
    }

    public static void getRotation(long unwrapEntity, float[] pos) {
    }

    public static void setRotation(long unwrapEntity, float x, float y) {
    }

    public static void setRotationAxis(long unwrapEntity, int axis, float val) {
    }

    public static void setHealth(long unwrapEntity, int health) {
        EntityMethod.setHealth(unwrapEntity, health);
    }

    public static void setMaxHealth(long unwrapEntity, int health) {
        EntityMethod.setMaxHealth(unwrapEntity, health);
    }

    public static int getAge(long unwrapEntity) {
        return 0;
    }

    public static void setAge(long unwrapEntity, int age) {
    }

    public static int getFireTicks(long unwrapEntity) {
        return EntityMethod.getFireTicks(unwrapEntity);
    }

    public static void setFireTicks(long unwrapEntity, int ticks, boolean force) {
        EntityMethod.setFireTicks(unwrapEntity, ticks, force);
    }

    public static boolean isImmobile(long unwrapEntity) {
        return false;
    }

    public static void setImmobile(long unwrapEntity, boolean val) {
    }

    public static boolean isSneaking(long unwrapEntity) {
        return false;
    }

    public static void setSneaking(long unwrapEntity, boolean val) {
    }

    public static String getNameTag(long unwrapEntity) {
        return "";
    }

    public static void setNameTag(long unwrapEntity, String tag) {
    }

    public static int getRenderType(long unwrapEntity) {
        return 0;
    }

    public static void setRenderType(long unwrapEntity, int type) {
    }

    public static Item getItemFromDrop(long unwrapEntity) {
        return EntityMethod.getItemFromDrop(unwrapEntity);
    }

    public static Item getItemFromProjectile(long unwrapEntity) {
        return EntityMethod.getItemFromProjectile(unwrapEntity);
    }

    public static void setItemToDrop(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        EntityMethod.setItemToDrop(unwrapEntity, id, count, data, unwrapValue);
    }

    public static void setEntityCarriedItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        EntityMethod.setEntityCarriedItem(unwrapEntity, id, count, data, unwrapValue);
    }

    public static void setEntityOffhandItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        EntityMethod.setEntityOffhandItem(unwrapEntity, id, count, data, unwrapValue);
    }

    public static void removeEntity(long unwrapEntity) {
        EntityMethod.removeEntity(unwrapEntity);
    }

    public static void addEffect(long unwrapEntity, int effect, int duration, int level, boolean b1, boolean b2, boolean effectAnimation) {
        EntityMethod.addEffect(unwrapEntity, effect, duration, level, b1, b2, effectAnimation);
    }

    public static int getEffectLevel(long unwrapEntity, int effect) {
        return EntityMethod.getEffectLevel(unwrapEntity, effect);
    }

    public static int getEffectDuration(long unwrapEntity, int effect) {
        return EntityMethod.getEffectDuration(unwrapEntity, effect);
    }

    public static void removeEffect(long unwrapEntity, int effect) {
        EntityMethod.removeEffect(unwrapEntity, effect);
    }

    public static void removeAllEffects(long unwrapEntity) {
        EntityMethod.removeAllEffects(unwrapEntity);
    }

    public static void rideAnimal(long unwrapEntity, long unwrapEntity1) {
        EntityMethod.rideAnimal(unwrapEntity, unwrapEntity1);
    }

    public static long getRider(long unwrapEntity) {
        return EntityMethod.getRider(unwrapEntity);
    }

    public static long getRiding(long unwrapEntity) {
        return EntityMethod.getRiding(unwrapEntity);
    }

    public static long getTarget(long unwrapEntity) {
        return EntityMethod.getTarget(unwrapEntity);
    }

    public static void setTarget(long unwrapEntity, long unwrapEntity1) {
        EntityMethod.setTarget(unwrapEntity, unwrapEntity1);
    }

    public static int getEntityType(long unwrapEntity) {
        return EntityMethod.getEntityType(unwrapEntity);
    }

    public static String getEntityTypeName(long unwrapEntity) {
        return "";
    }

    public static long getEntityCompoundTag(long unwrapEntity) {
        return 0;
    }

    public static void setEntityCompoundTag(long unwrapEntity, long pointer) {
    }

    public static void setCollisionSize(long unwrapEntity, float w, float h) {
    }

    public static void dealDamage(long unwrapEntity, int damage, int cause, long l, boolean b1, boolean b2) {
    }

    public static long[] fetchEntitiesInAABB(float x1, float y1, float z1, float x2, float y2, float z2, int type, boolean flag) {
        return new long[] {};
    }

    public static long getServerPlayer() {
        return 0;
    }

    public static long getLocalPlayer() {
        return 0;
    }

    public static long getPointedData(int[] pos, float[] vec) {
        return 0;
    }

    public static void addItemToInventory(int id, int count, int data, long unwrapValue, boolean b) {
    }

    public static int getPlayerSelectedSlot() {
        return 0;
    }

    public static void setPlayerSelectedSlot(int slot) {
    }

    public static double getPlayerHunger() {
        return 0;
    }

    public static void setPlayerHunger(float val) {
    }

    public static double getPlayerSaturation() {
        return 0;
    }

    public static void setPlayerSaturation(float val) {
    }

    public static double getPlayerExhaustion() {
        return 0;
    }

    public static void setPlayerExhaustion(float val) {
    }

    public static void addPlayerExperience(int val) {
    }

    public static double getPlayerExperience() {
        return 0;
    }

    public static void setPlayerExperience(float val) {
    }

    public static double getPlayerLevel() {
        return 0;
    }

    public static void setPlayerLevel(float val) {
    }

    public static int getPlayerScore() {
        return 0;
    }

    public static boolean isPlayerFlying() {
        return false;
    }

    public static void setPlayerFlying(boolean val) {
    }

    public static boolean canPlayerFly() {
        return false;
    }

    public static void setPlayerCanFly(boolean val) {
    }

    public static int getDimension() {
        return 0;
    }

    public static void nativeSetFov(float fov) {
    }

    public static void nativeSetCameraEntity(long unwrapEntity) {
    }

    public static boolean isValidAbility(String ability) {
        return true;
    }

    public static void setPlayerFloatAbility(String ability, float floatValue) {
    }

    public static void setPlayerBooleanAbility(String ability, Boolean value) {
    }

    public static float getPlayerFloatAbility(String ability) {
        return 0;
    }

    public static boolean getPlayerBooleanAbility(String ability) {
        return false;
    }

    public static void overrideItemName(String name) {
    }

    public static void invokeUseItemOn(int id, int count, int data, long unwrapValue, int x, int y, int z, int side, float vx, float vy, float vz, long unwrapEntity) {
    }

    public static void invokeUseItemNoTarget(int id, int count, int data, long unwrapValue) {
    }

    public static void getAtlasTextureCoords(String name, int id, float[] coords) {
    }

    public static String executeCommand(String command, int i, int i1, int i2, Level l) {
        return "";
    }

    public static void transferToDimension(long unwrapEntity, int dimension) {
    }

    public static boolean isTileUpdateAllowed() {
        return false;
    }

    public static void forceRenderRefresh(int x, int y, int z, int i) {
    }
}
