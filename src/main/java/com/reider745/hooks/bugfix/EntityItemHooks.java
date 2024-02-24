package com.reider745.hooks.bugfix;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLava;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.entity.ItemDespawnEvent;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.EntityEventPacket;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

@Hooks(className = "cn.nukkit.entity.item.EntityItem")
public class EntityItemHooks implements HookClass {
    private static boolean isInsideOfLava(EntityItem self) {
        Iterator<Block> var1 = self.getCollisionBlocks().iterator();

        Block block;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            block = var1.next();
        } while(!(block instanceof BlockLava));

        return true;
    }

    private static boolean enableHeadYaw(Entity self){
        return !(self instanceof EntityItem);
    }

    //код взят из PowerNukkitX
    @Inject(type = TypeHook.BEFORE_REPLACE, className = "cn.nukkit.entity.Entity")
    public static void updateMovement(Entity self){
        try {
            if (!enableHeadYaw(self)) {
                self.headYaw = self.yaw;
            }
            double diffPosition = (self.x - self.lastX) * (self.x - self.lastX) + (self.y - self.lastY) * (self.y - self.lastY) + (self.z - self.lastZ) * (self.z - self.lastZ);
            double diffRotation = (enableHeadYaw(self) ? (self.headYaw - self.lastHeadYaw) * (self.headYaw - self.lastHeadYaw) : 0) + (self.yaw - self.lastYaw) * (self.yaw - self.lastYaw) + (self.pitch - self.lastPitch) * (self.pitch - self.lastPitch);

            double diffMotion = (self.motionX - self.lastMotionX) * (self.motionX - self.lastMotionX) + (self.motionY - self.lastMotionY) * (self.motionY - self.lastMotionY) + (self.motionZ - self.lastMotionZ) * (self.motionZ - self.lastMotionZ);

            if (diffPosition > 0.0001 || diffRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
                final Method method = Entity.class.getDeclaredMethod("getBaseOffset");
                method.setAccessible(true);
                self.addMovement(self.x, self.isPlayer ? self.y : self.y + ((Float) method.invoke(self)), self.z, self.yaw, self.pitch, self.headYaw);

                self.lastX = self.x;
                self.lastY = self.y;
                self.lastZ = self.z;

                self.lastPitch = self.pitch;
                self.lastYaw = self.yaw;
                self.lastHeadYaw = self.headYaw;

                self.positionChanged = true;
            } else {
                self.positionChanged = false;
            }

            if (diffMotion > 0.0025 || (diffMotion > 0.0001 && self.getMotion().lengthSquared() <= 0.0001)) { //0.05 ** 2
                self.lastMotionX = self.motionX;
                self.lastMotionY = self.motionY;
                self.lastMotionZ = self.motionZ;

                self.addMotion(self.motionX, self.motionY, self.motionZ);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //код взят из PowerNukkitX
    @Inject
    public static boolean onUpdate(EntityItem self, int currentTick) {
        try{
            if (self.closed) {
                return false;
            }

            int tickDiff = currentTick - self.lastUpdate;

            if (tickDiff <= 0 && !self.justCreated) {
                return true;
            }

            self.lastUpdate = currentTick;

            if (self.age % 60 == 0 && self.onGround && self.getItem() != null && self.isAlive()) {
                if (self.getItem().getCount() < self.getItem().getMaxStackSize()) {
                    for (Entity entity : self.getLevel().getNearbyEntities(self.getBoundingBox().grow(1, 1, 1), self, false)) {
                        if (entity instanceof EntityItem) {
                            if (!entity.isAlive()) {
                                continue;
                            }
                            Item closeItem = ((EntityItem) entity).getItem();
                            if (!closeItem.equals(self.getItem(), true, true)) {
                                continue;
                            }
                            if (!entity.isOnGround()) {
                                continue;
                            }
                            int newAmount = self.getItem().getCount() + closeItem.getCount();
                            if (newAmount > self.getItem().getMaxStackSize()) {
                                continue;
                            }
                            entity.close();
                            self.getItem().setCount(newAmount);
                            EntityEventPacket packet = new EntityEventPacket();
                            packet.eid = self.getId();
                            packet.data = newAmount;
                            packet.event = EntityEventPacket.MERGE_ITEMS;
                            Server.broadcastPacket(self.getViewers().values(), packet);
                        }
                    }
                }
            }

            boolean hasUpdate = self.entityBaseTick(tickDiff);

            boolean lavaResistant = self.fireProof || self.getItem() != null && self.fireProof;

            if (!lavaResistant && (self.isInsideOfFire() || isInsideOfLava(self))) {
                self.kill();
            }

            if (self.isAlive()) {
                final Field field = EntityItem.class.getDeclaredField("pickupDelay");
                field.setAccessible(true);

                int pickupDelay = field.getInt(self);
                if (pickupDelay > 0 && pickupDelay < 32767) {
                    pickupDelay -= tickDiff;
                    if (pickupDelay < 0) {
                        pickupDelay = 0;
                    }
                    field.set(self, pickupDelay);
                }/* else { // Done in Player#checkNearEntities
                for (Entity entity : this.level.getNearbyEntities(this.boundingBox.grow(1, 0.5, 1), this)) {
                    if (entity instanceof Player) {
                        if (((Player) entity).pickupEntity(this, true)) {
                            return true;
                        }
                    }
                }
            }*/

                int bid = self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 0);
                if (bid == BlockID.WATER || bid == BlockID.STILL_WATER
                        || (bid = self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 1)) == BlockID.WATER
                        || bid == BlockID.STILL_WATER
                ) {
                    //item is fully in water or in still water
                    self.motionY -= self.getGravity() * -0.015;
                } else if (lavaResistant && (
                        self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 0) == BlockID.WATER
                                || self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 0) == BlockID.STILL_LAVA
                                || self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 1) == BlockID.WATER
                                || self.level.getBlockIdAt((int) self.x, (int) self.boundingBox.getMaxY(), (int) self.z, 1) == BlockID.STILL_LAVA
                )) {
                    //item is fully in lava or in still lava
                    self.motionY -= self.getGravity() * -0.015;
                } else if (self.isInsideOfWater() || lavaResistant && isInsideOfLava(self)) {
                    self.motionY = self.getGravity() - 0.06; //item is going up in water, don't let it go back down too fast
                } else {
                    self.motionY -= self.getGravity(); //item is not in water
                }

                final Method method = Entity.class.getDeclaredMethod("checkObstruction", double.class, double.class, double.class);
                method.setAccessible(true);
                if((Boolean) method.invoke(self, self.x, self.y, self.z)) {
                    hasUpdate = true;
                }

                self.move(self.motionX, self.motionY, self.motionZ);

                double friction = 1 - self.getDrag();

                if (self.onGround && (Math.abs(self.motionX) > 0.00001 || Math.abs(self.motionZ) > 0.00001)) {
                    friction *= self.getLevel().getBlock(self.temporalVector.setComponents((int) Math.floor(self.x), (int) Math.floor(self.y - 1), (int) Math.floor(self.z))).getFrictionFactor();
                }

                self.motionX *= friction;
                self.motionY *= 1 - self.getDrag();
                self.motionZ *= friction;

                if (self.onGround) {
                    self.motionY *= -0.5;
                }

                self.updateMovement();

                if (self.age > 6000) {
                    ItemDespawnEvent ev = new ItemDespawnEvent(self);
                    self.getServer().getPluginManager().callEvent(ev);
                    if (ev.isCancelled()) {
                        self.age = 0;
                    } else {
                        self.kill();
                        hasUpdate = true;
                    }
                }
            }

            return hasUpdate || !self.onGround || Math.abs(self.motionX) > 0.00001 || Math.abs(self.motionY) > 0.00001 || Math.abs(self.motionZ) > 0.00001;
        }catch (Exception e){
            e.printStackTrace();
            self.closed = true;
        }

        return false;
    }
}
