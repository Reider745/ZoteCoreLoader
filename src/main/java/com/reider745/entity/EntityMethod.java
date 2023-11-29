package com.reider745.entity;

import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntityThrownTrident;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;

public class EntityMethod {
    public static Entity getEntityToLong(long ent){
        for (Level level : InnerCoreServer.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == ent)
                    return entity;
        return null;
    }

    public static EntityHuman getEntityHumanToLong(long ent){
        for (Level level : InnerCoreServer.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == ent && entity instanceof EntityHuman)
                    return (EntityHuman) entity;
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

    public static void setPosition(long entity, float x, float y, float z){
        Entity ent = getEntityToLong(entity);
        if(ent == null) return;
        ent.setPosition(new Vector3(x, y, z));
    }

    public static void setPositionAxis(long entity, float axis, float val){
        Entity ent = getEntityToLong(entity);
        if(ent == null) return;

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
        EntityHuman ent = getEntityHumanToLong(entity);
        if(ent == null)
            return Item.get(0).clone();
        return validItem(ent.getInventory().getItemInHand());
    }

    public static Item getEntityOffhandItem(long entity){
        EntityHuman ent = getEntityHumanToLong(entity);
        if(ent == null) return Item.get(0).clone();
        return ent.getOffhandInventory().getItem(0);
    }
    public static Item getEntityArmor(long entity, int armor){
        EntityHuman ent = getEntityHumanToLong(entity);
        if(ent == null) return Item.get(0).clone();
        return ent.getInventory().getArmorItem(armor);
    }

    public static void setEntityArmor(long entity, int slot, int id, int count, int data, long extra){
        EntityHuman ent = getEntityHumanToLong(entity);
        if(ent == null) return;
        ent.getInventory().setArmorItem(slot, ItemUtils.get(id, count, data, extra));
    }

    public static boolean isValidEntity(long ent){
        for (Level level : InnerCoreServer.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == ent)
                    return true;
        return false;
    }

    public static void setHealth(long unwrapEntity, int health) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            ent.setHealth(health);
    }

    public static void setMaxHealth(long unwrapEntity, int health) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            ent.setMaxHealth(health);
    }

    public static int getFireTicks(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            return 0;
        return 0;
    }

    public static void setFireTicks(long unwrapEntity, int ticks, boolean force) {
    }

    public static boolean isImmobile(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            return ent.isImmobile();
        return false;
    }

    public static void setImmobile(long unwrapEntity, boolean val) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            ent.setImmobile(val);
    }

    public static boolean isSneaking(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            return ent.isSneaking();
        return false;
    }

    public static void setSneaking(long unwrapEntity, boolean val) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            ent.setSneaking(val);
    }

    public static String getNameTag(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            return ent.getNameTag();
        return "";
    }

    public static void setNameTag(long unwrapEntity, String tag) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent != null)
            ent.setNameTag(tag);
    }

    public static Item getItemFromDrop(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent instanceof EntityItem entItem)
            return entItem.getItem();
        return Item.get(0).clone();
    }

    public static Item getItemFromProjectile(long unwrapEntity) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent instanceof EntityThrownTrident entItem)
            return entItem.getItem();
        return Item.get(0).clone();
    }

    public static void setItemToDrop(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent instanceof EntityThrownTrident entItem)
            entItem.setItem(ItemUtils.get(id, count, data, unwrapValue));
    }

    public static void setEntityCarriedItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null)
            ent.getInventory().setItemInHand(ItemUtils.get(id, count, data, unwrapValue));
    }

    public static void setEntityOffhandItem(long unwrapEntity, int id, int count, int data, long unwrapValue) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null)
            ent.getOffhandInventory().setItem(0, ItemUtils.get(id, count, data, unwrapValue));
    }

    public static void removeEntity(long unwrapEntity) {
        for (Level level : InnerCoreServer.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == unwrapEntity)
                    level.removeEntity(entity);
    }

    public static void addEffect(long unwrapEntity, int effect, int duration, int level, boolean b1, boolean b2, boolean effectAnimation) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null) {
            Effect effectInstance = Effect.getEffect(effect).clone();
            effectInstance.setDuration(duration);
            effectInstance.setAmplifier(level);
            effectInstance.setAmbient(b1);
            effectInstance.setVisible(b2);
            ent.addEffect(effectInstance);
        }
    }

    public static int getEffectLevel(long unwrapEntity, int effect) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null) {
            Effect effectInstance = ent.getEffect(effect);
            if(effectInstance != null)
                return effectInstance.getAmplifier();
        }
        return 0;
    }

    public static int getEffectDuration(long unwrapEntity, int effect) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null) {
            Effect effectInstance = ent.getEffect(effect);
            if(effectInstance != null)
                return effectInstance.getDuration();
        }
        return 0;
    }

    public static void removeEffect(long unwrapEntity, int effect) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null)
            ent.removeEffect(effect);
    }

    public static void removeAllEffects(long unwrapEntity) {
        EntityHuman ent = getEntityHumanToLong(unwrapEntity);
        if(ent != null)
            ent.removeAllEffects();
    }

    public static void rideAnimal(long unwrapEntity, long unwrapEntity1) {
        Entity ent = getEntityToLong(unwrapEntity);
        if(ent instanceof EntityRideable rideable)
            rideable.mountEntity(getEntityToLong(unwrapEntity1));
    }

    public static long getRider(long unwrapEntity) {
        return 0;
    }

    public static long getRiding(long unwrapEntity) {
        return 0;
    }

    public static long getTarget(long unwrapEntity) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity instanceof BaseEntity base)
            return base.getTarget().getId();
        return 0;
    }

    public static void setTarget(long unwrapEntity, long unwrapEntity1) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity instanceof BaseEntity base)
            base.setTarget(getEntityToLong(unwrapEntity1));
    }

    public static int getEntityType(long unwrapEntity) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity != null)
            return 1;
        return 0;
    }

    public static CompoundTag getEntityCompoundTag(long unwrapEntity) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity != null)
            return entity.namedTag;
        return null;
    }

    public static void setEntityCompoundTag(long unwrapEntity, CompoundTag pointer) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity != null)
            entity.namedTag = pointer;
    }

    public static void getRotation(long unwrapEntity, float[] pos) {
        Entity entity = getEntityToLong(unwrapEntity);
        if(entity != null){
            pos[0] = (float) entity.getYaw();
            pos[1] = (float) entity.getPitch();
        }
    }
}
