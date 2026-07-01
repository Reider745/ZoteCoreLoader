package com.zhekasmirnov.apparatus.adapter.innercore.game.entity;

import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;
import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.apparatus.adapter.innercore.game.common.Vector3;
import com.zhekasmirnov.innercore.api.NativeAPI;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.inventory.ItemInstanceExtra;

public class StaticEntity {

    public static int getDimension(long entity) {
        return NativeAPI.getEntityDimension(entity);
    }

    public static Vector3 getPosition(long entity) {
        float[] position = new float[3];
        NativeAPI.getPosition(entity, position);
        return new Vector3(position);
    }

    public static boolean exists(long entity) {
        return NativeAPI.isValidEntity(entity);
    }

    public static ItemInstance getCarriedItem(long entity) {
        return ItemInstance.makeFrom(EntityMethod.getEntityCarriedItem(entity));
    }

    public static int getType(long entity) {
        return NativeAPI.getEntityType(entity);
    }

    public static Vector3 getVelocity(long entity) {
        float[] position = new float[3];
        NativeAPI.getVelocity(entity, position);
        return new Vector3(position);
    }

    public static ItemInstance getDroppedItem(long entity) {
        return ItemInstance.makeFrom(EntityMethod.getItemFromDrop(entity));
    }

    public static int getExperienceOrbValue(long entity) {
        return EntityMethod.getExperienceOrbValue(entity);
    }

    @ClientOnly
    public static EntityActor newDroppedItem(float x, float y, float z, int id, int count, int data,
                                             ItemInstanceExtra extra) {
        InnerCoreServer.useNotSupport("StaticEntity.newDroppedItem");
        return new EntityActor(NativeAPI.spawnDroppedItem(x, y, z, id, count, data, extra));
    }
}
