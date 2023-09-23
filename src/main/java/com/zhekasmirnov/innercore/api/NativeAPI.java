package com.zhekasmirnov.innercore.api;

import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import com.reider745.item.ItemMethod;
import com.zhekasmirnov.apparatus.Apparatus;

public class NativeAPI {
    
    public static Entity getEntityToLong(long ent){
        for (Level level : Apparatus.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == ent)
                    return entity;
        return null;
    }
    public static int getEntityDimension(long entity){
        Entity ent = getEntityToLong(entity);
        if(ent != null)
            return ent.getLevel().getDimension();
        return -1;
    }

    public static void getPosition(long entity, float[] pos){
        Entity ent = getEntityToLong(entity);
        if(ent == null) return;
        Position position = ent.getPosition();
        pos[0] = (float) position.x;
        pos[1] = (float) position.y;
        pos[2] = (float) position.z;
    }

    public static int getHealth(long entity){
        Entity ent = getEntityToLong(entity);
        if(ent != null)
            return (int) ent.getHealth();
        return 0;
    }

    public static int getMaxHealth(long entity){
        Entity ent = getEntityToLong(entity);
        if(ent != null)
            return ent.getMaxHealth();
        return 0;
    }

    private static Item validItem(Item item){
        if(item == null)
            return Item.get(0).clone();
        return item;
    }

    public static Item getEntityCarriedItem(long entity){
        Entity ent = getEntityToLong(entity);
        if(ent == null) return Item.get(0).clone();
        return validItem(ent.getCarriedItem());
    }

    public static Item getEntityOffhandItem(long entity){
        Entity ent = getEntityToLong(entity);
        if(ent == null) return Item.get(0).clone();
        return ent.getOffhandItem();
    }
    public static Item getEntityArmor(long entity, int armor){
        Entity ent = getEntityToLong(entity);
        return null;
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, long extra){
        Entity ent = getEntityToLong(entity);
    }

    public static void setPlayerArmor(int slot, int id, int count, int data, long extra){

    }

    public static String getGameLanguage(){
        return "en";
    }

    public static void sendCachedItemNameOverride(int id, int data, String name){

    }

    public static boolean isValidEntity(long entity){
        return true;
    }

    public static long getPlayer(){
        return 0;
    }

    public static Item getPlayerArmor(int player){
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
        return 0;
    }

    public static int getTileAndData(int x, int y, int z) {
        return 0;
    }
    public static void setTile(int x, int y, int z, int id, int data){

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
        return null;
    }

    public static void setInventorySlot(int index, int id, int count, int data, long extra){

    }

    public static void preventDefault() {
    }

    public static void explode(float x, float y, float z, float power, boolean onFire) {
    }

    public static void addParticle(){

    }

    public static void addFarParticle(){

    }

    public static void setBlockChangeCallbackEnabled(int id, boolean enabled) {
    }

    public static int getData(int x, int y, int z) {
        return 0;
    }

    public static int getBrightness(int x, int y, int z) {
        return 0;
    }

    public static boolean isChunkLoaded(int x, int z) {
        return false;
    }

    public static int getChunkState(int x, int z) {
        return 0;
    }

    public static int getBiome(int x, int z) {
        return 0;
    }

    public static void setBiome(int x, int z, int id) {
    }

    public static int getBiomeMap(int x, int z) {
        return 0;
    }

    public static void setBiomeMap(int x, int z, int id) {
    }

    public static String getBiomeName(int id) {
        return "";
    }

    public static float getBiomeTemperatureAt(int x, int y, int z) {
        return 0;
    }

    public static int getGrassColor(int x, int z) {
        return 0;
    }

    public static void setGrassColor(int x, int z, int color) {
    }

    public static void destroyBlock(int x, int y, int z, boolean drop) {
    }

    public static long spawnEntity(int id, float x, float y, float z) {
        return 0;
    }

    public static void setSkin(long entity, String skin) {
    }

    public static void spawnExpOrbs(float x, float y, float z, int amount) {
    }

    public static long spawnDroppedItem(float x, float y, float z, int id, int count, int data, long unwrapValue) {
        return 0;
    }

    public static long getTime() {
        return 0;
    }

    public static void setTileUpdateType(){

    }

    public static void setTileUpdateAllowed(){

    }

    public static void setTime(int time) {
    }

    public static int getGameMode() {
        return 0;
    }

    public static void setGameMode(int mode) {
    }

    public static int getDifficulty() {
        return 0;
    }

    public static void setDifficulty(int val) {
    }

    public static double getRainLevel() {
        return 0;
    }

    public static void setRainLevel(float val) {
    }

    public static double getLightningLevel() {
        return 0;
    }

    public static void setLightningLevel(float val) {
    }

    public static void playSound(String name, float x, float y, float z, float f1, float f2) {
    }

    public static void playSoundEnt(String name, long unwrapEntity, float f1, float f2) {
    }

    public static void setNightMode(boolean val) {
    }

    public static void setRespawnCoords(int x, int y, int z) {
    }

    public static long clipWorld(float x1, float y1, float z1, float x2, float y2, float z2, int mode, float[] clip) {
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
    }

    public static void setMaxHealth(long unwrapEntity, int health) {
    }

    public static int getAge(long unwrapEntity) {
        return 0;
    }

    public static void setAge(long unwrapEntity, int age) {
    }

    public static int getFireTicks(long unwrapEntity) {
        return 0;
    }

    public static void setFireTicks(long unwrapEntity, int ticks, boolean force) {
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
        return null;
    }

    public static Item getItemFromProjectile(long unwrapEntity) {
        return null;
    }

    public static void setItemToDrop(long unwrapEntity, int id, int count, int data, long unwrapValue) {
    }

    public static void setEntityCarriedItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
    }

    public static void setEntityOffhandItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
    }

    public static void removeEntity(long unwrapEntity) {
    }

    public static void addEffect(long unwrapEntity, int effect, int duration, int level, boolean b1, boolean b2, boolean effectAnimation) {
    }

    public static int getEffectLevel(long unwrapEntity, int effect) {
        return 0;
    }

    public static int getEffectDuration(long unwrapEntity, int effect) {
        return 0;
    }

    public static void removeEffect(long unwrapEntity, int effect) {
    }

    public static void removeAllEffects(long unwrapEntity) {
    }

    public static void rideAnimal(long unwrapEntity, long unwrapEntity1) {
    }

    public static long getRider(long unwrapEntity) {
        return 0;
    }

    public static long getRiding(long unwrapEntity) {
        return 0;
    }

    public static long getTarget(long unwrapEntity) {
        return 0;
    }

    public static void setTarget(long unwrapEntity, long unwrapEntity1) {
    }

    public static int getEntityType(long unwrapEntity) {
        return 0;
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
