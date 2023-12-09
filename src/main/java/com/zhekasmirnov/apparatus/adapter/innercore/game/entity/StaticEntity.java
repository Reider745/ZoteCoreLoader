package com.zhekasmirnov.apparatus.adapter.innercore.game.entity;

import cn.nukkit.nbt.tag.CompoundTag;

import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.apparatus.adapter.innercore.game.common.Vector3;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;

/*import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.nbt.NativeCompoundTag;*/

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

    public static ItemStack getCarriedItem(long entity) {
        return new ItemStack(new NativeItemInstance(EntityMethod.getEntityCarriedItem(entity)));
    }

    public static int getType(long entity) {
        return NativeAPI.getEntityType(entity);
    }

    public static Vector3 getVelocity(long entity) {
        float[] position = new float[3];
        NativeAPI.getVelocity(entity, position);
        return new Vector3(position);
    }


    public static ItemStack getDroppedItem(long entity) {
        return new ItemStack(new NativeItemInstance(EntityMethod.getItemFromDrop(entity)));
    }

    public static int getExperienceOrbValue(long entity) {
        CompoundTag tag = EntityMethod.getEntityCompoundTag(entity);
        return tag != null ? new NativeCompoundTag(tag).getInt("experience value") : 0;
    }


    public static EntityActor newDroppedItem(float x, float y, float z, int id, int count, int data, NativeItemInstanceExtra extra) {
        return new EntityActor(NativeAPI.spawnDroppedItem(x, y, z, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra)));
    }
}
