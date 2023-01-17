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
}
