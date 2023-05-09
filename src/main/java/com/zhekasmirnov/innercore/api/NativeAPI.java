package com.zhekasmirnov.innercore.api;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
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

    public static long getEntityCarriedItem(long entity){
        Entity ent = getEntityToLong(entity);
        return 0;
    }

    public static long getEntityOffhandItem(long entity){
        Entity ent = getEntityToLong(entity);
        return 0;
    }
    public static long getEntityArmor(long entity, int armor){
        Entity ent = getEntityToLong(entity);
        return 0;
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, long extra){

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

    public static long getPlayerArmor(int player){
        return 0;
    }

    public static String getStringIdAndTypeForIntegerId(int id){
        return "";
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

    public static void addTextureToLoad(String name){

    }

    public static String convertNameId(String name){
        return "";
    }

    public static boolean isGlintItemInstance(int id, int data, long extra){
        return false;
    }

    public static void setItemRequiresIconOverride(int id, boolean enable){

    }

    public static void overrideItemIcon(String name, int index){

    }

    public static long getInventorySlot(int index){
        return 0;
    }

    public static void setInventorySlot(int index, int id, int count, int data, long extra){

    }
}
