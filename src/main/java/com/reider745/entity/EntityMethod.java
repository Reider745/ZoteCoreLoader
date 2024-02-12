package com.reider745.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Identifier;

import com.reider745.api.ReflectHelper;
import com.reider745.event.EventListener;
import com.reider745.hooks.ItemUtils;
import com.reider745.world.BlockSourceMethods;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.constants.EntityType;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class EntityMethod {

    public static Entity getEntityById(long entityUid) {
        Map<Integer, Level> levels = Server.getInstance().getLevels();
        for (Level level : levels.values()) {
            Entity entity = level.getEntity(entityUid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    public static Player getPlayerById(long entityUid) {
        Map<Integer, Level> levels = Server.getInstance().getLevels();
        for (Level level : levels.values()) {
            Map<Long, Player> players = level.getPlayers();
            Player player = players.containsKey(entityUid) ? players.get(entityUid) : null;
            if (player != null) {
                return player;
            }
        }
        return null;
    }

    public static boolean isValid(Entity entity) {
        return entity != null && entity.isValid() && !entity.isClosed();
    }

    private static <T> T validateThen(long entityUid, Function<Entity, T> then, T defaultValue) {
        Entity entity = getEntityById(entityUid);
        if (isValid(entity)) {
            return then.apply(entity);
        }
        return defaultValue;
    }

    private static void validateThen(long entityUid, Consumer<Entity> then) {
        Entity entity = getEntityById(entityUid);
        if (isValid(entity)) {
            then.accept(entity);
        }
    }

    public static int getEntityDimension(long entityUid) {
        return validateThen(entityUid, entity -> entity.getLevel().getDimension(), -1);
    }

    public static void getPosition(long entityUid, float[] position) {
        validateThen(entityUid, entity -> {
            Position entityPosition = entity.getPosition();
            position[0] = (float) entityPosition.x;
            position[1] = (float) entityPosition.y;
            position[2] = (float) entityPosition.z;
        });
    }

    public static void setPosition(long entityUid, float x, float y, float z) {
        validateThen(entityUid, entity -> entity.setPosition(new Vector3(x, y, z)));
    }

    public static void setPositionAxis(long entityUid, int axis, float val) {
        validateThen(entityUid, entity -> {
            switch (axis) {
                case 0 -> entity.setPosition(new Vector3(val, entity.y, entity.x));
                case 1 -> entity.setPosition(new Vector3(entity.x, val, entity.x));
                case 2 -> entity.setPosition(new Vector3(entity.x, entity.y, val));
            }
        });
    }

    public static int getHealth(long entityUid) {
        return validateThen(entityUid, entity -> (int) entity.getHealth(), -1);
    }

    public static int getMaxHealth(long entityUid) {
        return validateThen(entityUid, entity -> entity.getMaxHealth(), -1);
    }

    public static Item getEntityCarriedItem(long entityUid) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        Item item = validateThen(entityUid,
                entity -> entity instanceof EntityHuman human ? human.getInventory().getItemInHandFast() : null, null);
        return item != null ? item : Item.AIR_ITEM.clone();
    }

    public static Item getEntityOffhandItem(long entityUid) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        Item item = validateThen(entityUid,
                entity -> entity instanceof EntityHuman human ? human.getOffhandInventory().getItemFast(0) : null, null);
        return item != null ? item : Item.AIR_ITEM.clone();
    }

    public static Item getEntityArmor(long entityUid, int slot) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        Item item = validateThen(entityUid, entity -> {
            if (entity instanceof EntityHuman human) {
                PlayerInventory inventory = human.getInventory();
                return inventory.getItemFast(inventory.getSize() + slot);
            }
            return null;
        }, null);
        return item != null ? item : Item.AIR_ITEM.clone();
    }

    public static void setEntityCarriedItem(long entityUid, int id, int count, int data,
            Item extra) {
        setEntityCarriedItem(entityUid, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityCarriedItem(long entityUid, int id, int count, int data,
            NativeItemInstanceExtra extra) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        validateThen(entityUid, entity -> {
            if (entity instanceof EntityHuman human) {
                human.getInventory().setItemInHand(ItemUtils.get(id, count, data, extra));
            }
        });
    }

    public static void setEntityOffhandItem(long entityUid, int id, int count, int data,
            Item extra) {
        setEntityOffhandItem(entityUid, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityOffhandItem(long entityUid, int id, int count, int data,
            NativeItemInstanceExtra extra) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        validateThen(entityUid, entity -> {
            if (entity instanceof EntityHuman human) {
                human.getOffhandInventory().setItem(0, ItemUtils.get(id, count, data, extra));
            }
        });
    }

    public static void setEntityArmor(long entityUid, int slot, int id, int count, int data,
            Item extra) {
        setEntityArmor(entityUid, slot, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setEntityArmor(long entityUid, int slot, int id, int count, int data,
            NativeItemInstanceExtra extra) {
        // TODO: Human actually player, there is no way to check/replace
        // TODO: regular entity, properties hardcoded at spawn.
        validateThen(entityUid, entity -> {
            if (entity instanceof EntityHuman human) {
                human.getInventory().setArmorItem(slot, ItemUtils.get(id, count, data, extra));
            }
        });
    }

    public static void setHealth(long entityUid, int health) {
        validateThen(entityUid, entity -> entity.setHealth(health));
    }

    public static void setMaxHealth(long entityUid, int health) {
        validateThen(entityUid, entity -> entity.setMaxHealth(health));
    }

    public static int getFireTicks(long entityUid) {
        return validateThen(entityUid, entity -> entity.fireTicks, -1);
    }

    public static void setFireTicks(long entityUid, int ticks, boolean force) {
        validateThen(entityUid, entity -> {
            entity.fireTicks = ticks;
            if (ticks <= 0 && force) {
                entity.extinguish();
            }
        });
    }

    public static boolean isImmobile(long entityUid) {
        return validateThen(entityUid, entity -> entity.isImmobile(), false);
    }

    public static void setImmobile(long entityUid, boolean val) {
        validateThen(entityUid, entity -> entity.setImmobile(val));
    }

    public static boolean isSneaking(long entityUid) {
        return validateThen(entityUid, entity -> entity.isSneaking(), false);
    }

    public static void setSneaking(long entityUid, boolean val) {
        validateThen(entityUid, entity -> entity.setSneaking(val));
    }

    public static String getNameTag(long entityUid) {
        return validateThen(entityUid, entity -> entity.getNameTag(), "");
    }

    public static void setNameTag(long entityUid, String tag) {
        validateThen(entityUid, entity -> entity.setNameTag(tag));
    }

    public static Item getItemFromDrop(long entityUid) {
        Item item = validateThen(entityUid, entity -> entity instanceof EntityItem drop ? drop.getItem() : null, null);
        return item != null ? item : Item.AIR_ITEM.clone();
    }

    public static Item getItemFromProjectile(long entityUid) {
        Item item = validateThen(entityUid,
                entity -> entity instanceof EntityProjectile projectile
                        ? NBTIO.getItemHelper(projectile.namedTag.getCompound("ItemIc"))
                        : null,
                null);
        return item != null ? item : Item.AIR_ITEM.clone();
    }

    public static void setItemToDrop(long entityUid, int id, int count, int data, Item extra) {
        setItemToDrop(entityUid, id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra));
    }

    public static void setItemToDrop(long entityUid, int id, int count, int data, NativeItemInstanceExtra extra) {
        // TODO: It will actually doesn't update item, hardcode again.
        validateThen(entityUid, entity -> {
            if (entity instanceof EntityItem drop) {
                ReflectHelper.setField(drop, "item", ItemUtils.get(id, count, data, extra));
            }
        });
    }

    public static void removeEntity(long entityUid) {
        validateThen(entityUid, entity -> entity.getLevel().removeEntity(entity));
    }

    public static void addEffect(long entityUid, int effectId, int effectData, int effectTime, boolean ambient, boolean particles,
            boolean effectAnimation) {
        validateThen(entityUid, entity -> entity.addEffect(
                Effect.getEffect(effectId).setDuration(effectTime).setAmplifier(effectData).setAmbient(ambient).setVisible(particles)));
    }

    public static int getEffectLevel(long entityUid, int effectId) {
        return validateThen(entityUid, entity -> {
            Effect effect = entity.getEffect(effectId);
            return effect != null ? effect.getAmplifier() : 0;
        }, -1);
    }

    public static int getEffectDuration(long entityUid, int effectId) {
        return validateThen(entityUid, entity -> {
            Effect effect = entity.getEffect(effectId);
            return effect != null ? effect.getDuration() : 0;
        }, -1);
    }

    public static void removeEffect(long entityUid, int effectId) {
        validateThen(entityUid, entity -> entity.removeEffect(effectId));
    }

    public static void removeAllEffects(long entityUid) {
        validateThen(entityUid, entity -> entity.removeAllEffects());
    }

    public static void rideAnimal(long entityUid, long riderUid) {
        validateThen(entityUid, entity -> {
            if (entity instanceof EntityRideable rideable) {
                validateThen(riderUid, rider -> rideable.mountEntity(rider));
            }
        });
    }

    public static long getRider(long entityUid) {
        Entity passenger = validateThen(entityUid, entity -> entity.getPassenger(), null);
        return passenger != null ? passenger.getId() : -1;
    }

    public static long getRiding(long entityUid) {
        Entity riding = validateThen(entityUid, entity -> entity.getRiding(), null);
        return riding != null ? riding.getId() : -1;
    }

    public static long getTarget(long entityUid) {
        Entity target = validateThen(entityUid,
                entity -> entity instanceof BaseEntity attacker ? attacker.getTarget() : null, null);
        return target != null ? target.getId() : -1;
    }

    public static void setTarget(long entityUid, long targetUid) {
        validateThen(entityUid, entity -> {
            if (entity instanceof BaseEntity attacker) {
                validateThen(targetUid, target -> attacker.setTarget(target));
            }
        });
    }

    public static int getEntityTypeDirect(Entity entity) {
        int networkId = entity.getNetworkId();
        if (networkId == -1) {
            if (entity.isPlayer) {
                networkId = EntityType.PLAYER;
            }
            if (networkId == -1) {
                Identifier identifier = entity.getIdentifier();
                if (identifier != null) {
                    networkId = GameEnums.getInt(GameEnums.getSingleton().getEnum("entity_type", identifier.getPath()));
                }
            }
        }
        return networkId;
    }

    public static int getEntityType(long entityUid) {
        return validateThen(entityUid, entity -> getEntityTypeDirect(entity), -1);
    }

    public static String getEntityTypeName(long entityUid) {
        return validateThen(entityUid, entity -> {
            Identifier identifier = entity.getIdentifier();
            if (identifier == null && entity.isPlayer) {
                identifier = Identifier.of("minecraft", "player");
            }
            return identifier != null ? identifier + "<>" : null;
        }, null);
    }

    public static CompoundTag getEntityCompoundTag(long entityUid) {
        return validateThen(entityUid, entity -> {
            entity.saveNBT();
            return entity.namedTag;
        }, null);
    }

    public static void setEntityCompoundTag(long entityUid, CompoundTag tag) {
        // TODO: Update properties, which also not implemented in
        // TODO: Nukkit-MOT, use Entity.init(chunk, nbt) for example.
        validateThen(entityUid, entity -> entity.namedTag = tag);
    }

    public static void getRotation(long entityUid, float[] pos) {
        validateThen(entityUid, entity -> {
            pos[0] = (float) entity.getYaw();
            pos[1] = (float) entity.getPitch();
        });
    }

    public static void setRotation(long entityUid, float x, float y) {
        validateThen(entityUid, entity -> entity.setRotation(x, y));
    }

    public static void getVelocity(long entityUid, float[] velocity) {
        validateThen(entityUid, entity -> {
            final double[] entityOffset = EntityMotion.getVelocity(entity);
            velocity[0] = (float) entityOffset[0];
            velocity[1] = (float) entityOffset[1];
            velocity[2] = (float) entityOffset[2];
        });
    }

    public static void setVelocity(long entityUid, float x, float y, float z) {
        validateThen(entityUid, entity -> entity.setMotion(new Vector3(x, y, z)));
    }

    public static int getAge(long entityUid) {
        return validateThen(entityUid, entity -> entity.age, null);
    }

    public static void setAge(long entityUid, int age) {
        validateThen(entityUid, entity -> {
            entity.age = age;
            if (entity instanceof EntityAgeable ageable) {
                ageable.setBaby(age > 0);
            }
        });
    }

    public static void setCollisionSize(long entityUid, float w, float h) {
        // TODO: Nukkit-MOT hardcoded getWidth() and getHeight().
    }

    public static void dealDamage(long entityUid, int damage, int cause, long attackerUid, boolean b1, boolean b2) {
        validateThen(entityUid, entity -> {
            if (entity instanceof Player player && !(player.isSurvival() || player.isAdventure())) {
                return;
            }

            EntityDamageEvent event;
            Entity attacker = getEntityById(attackerUid);
            if (isValid(attacker)) {
                event = new EntityDamageByEntityEvent(attacker, entity, EventListener.convertEnumToDamageCause(cause),
                        damage / 2f);
            } else {
                event = new EntityDamageEvent(entity, EventListener.convertEnumToDamageCause(cause), damage / 2f);
            }

            synchronized (EventListener.DEALING_LOCK) {
                EventListener.dealingEvent = event;
                entity.attack(event);
                EventListener.dealingEvent = null;
            }
        });
    }

    public static void invokeUseItemOn(int id, int count, int data, Item extra, int x, int y, int z,
            int side, float vx, float vy, float vz, long entityUid) {
        invokeUseItemOn(id, count, data, NativeItemInstanceExtra.getExtraOrNull(extra), x, y, z, side, vx, vy, vz, entityUid);
    }

    public static void invokeUseItemOn(int id, int count, int data, NativeItemInstanceExtra extra, int x, int y, int z,
            int side, float vx, float vy, float vz, long entityUid) {
        validateThen(entityUid, entity -> {
            synchronized (EventListener.DEALING_LOCK) {
                EventListener.dealingEvent = true;
                entity.getLevel().useItemOn(new Vector3(x, y, z), ItemUtils.get(id, count, data, extra),
                        BlockFace.fromHorizontalIndex(side), vx, vy, vz, (Player) entity);
                EventListener.dealingEvent = false;
            }
        });
    }

    public static void transferToDimension(long entityUid, int dimension) {
        validateThen(entityUid, entity -> entity
                .setPosition(entity.getPosition().setLevel(BlockSourceMethods.getLevelForDimension(dimension))));
    }

    public static int getExperienceOrbValue(long entityUid) {
        return validateThen(entityUid, entity -> entity instanceof EntityXPOrb orb ? orb.getExp() : 0, -1);
    }
}
