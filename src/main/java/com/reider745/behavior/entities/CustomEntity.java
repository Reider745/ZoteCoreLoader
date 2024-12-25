package com.reider745.behavior.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockCarpet;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.DummyBossBar;
import com.reider745.behavior.entities.formats.tags.components.IUpdateEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomEntity extends BaseEntity implements cn.nukkit.entity.custom.CustomEntity {
    public CustomEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private int tickJump = -1;
    private final int maxTimeJump = 15;// Сколько времени нужно для прыжка
    public int tickMove = 0;
    private final int maxTimeMove = 80;
    public int tickDamage = 0;
    public final int timeDamage = 30;

    public Block isSolid(double x, double y, double z){
        final Block block = this.level.getBlock((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        /*this.level.addParticle(
                new SmokeParticle(new Vector3(x, y, z)),
                this.getServer().getOnlinePlayers().values()
        );*/
        return block.getId() != BlockID.AIR && block.isSolid() ? block : null;
    }

    public Block raycast(Entity ent1, Entity ent2){
        final Vector3 pos1 = ent1.getPosition()
                .add(0, getHeight() * .75);
        final Vector3 pos2 = ent2.getPosition();
        final int iterations = (int) (pos2.distance(pos1) * 3d);
        return raycast(pos1, pos2.subtract(pos1).divide(iterations), iterations);
    }

    public Block raycast(Entity ent2){
        return raycast(this, ent2);
    }

    public Block raycast(Vector3 pos, Vector3 move, int iterations){
        for(int i = 0;i < iterations;i++){
            final Block check = isSolid(pos.x, pos.y, pos.z);
            if(check != null) {
                return check;
            }

            pos.x += move.x;
            pos.y += move.y;
            pos.z += move.z;
        }
        return null;
    }

    public Block raycast(Vector3 pos, Vector3 move){
        return raycast(pos, move, 4);
    }


    // Jump for PNX 2.0
    public double getJumpingMotion(double jumpY) {
        if (this.isInsideOfWater()) {
            if (jumpY > 0 && jumpY < 0.2) {
                return 0.25;
            } else if (jumpY < 0.51) {
                return 0.45;
            } else if (jumpY < 1.01) {
                return 0.6;
            } else {
                return 0.7;
            }
        } else {
            if (jumpY > 0 && jumpY < 0.2) {
                return 0.15;
            } else if (jumpY < 0.51) {
                return 0.35;
            } else if (jumpY < 1.01) {
                return 0.5;
            } else {
                return 0.6;
            }
        }
    }

    private boolean canJump(Block block) {
        if (block.isSolid()) return true;
        else if (block instanceof BlockCarpet) return true;
        else return block.getId() == BlockID.FLOWER_POT_BLOCK || block.getId() == BlockID.CAKE_BLOCK;
    }

    @Override
    public Vector3 updateMove(int tickDiff) {
        if (this.isMovement() && !isImmobile()) {
            if(factory.isPushable){
                this.move(this.motionX, this.motionY, this.motionZ);
                this.updateMovement();
            }

            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            final double gravity_value = this.getGravity() * 4;
            final Vector3 pos = this.getPosition().clone();

            motionX += factory.liftForce.x / 20;
            motionY += factory.liftForce.y / 20;
            motionZ += factory.liftForce.z / 20;

            if(factory.isPhysics && isSolid(pos.x, pos.y - 0.001,pos.z) == null) {
                this.motionY -= gravity_value;
            }

            if(factory.isMovement){// Движение
                tickMove++;
                if(target == null || tickMove >= maxTimeMove) {
                    targetPositionMovementUpdate();
                }

                if(pos.distance(target) < 1){
                    target = null;
                }else{
                    Vector3 move = target.subtract(pos)
                            .divide(target.distance(pos) * 2);

                    motionX += move.x * factory.movement;
                    motionZ += move.z * factory.movement;

                    if(factory.canClimb) {
                        if(this.getHeight() <= 1){
                            if(raycast(pos, move) != null && raycast(pos.add(0, 1.2, 0), move) != null)
                                targetPositionMovementUpdate();
                        }else
                            if(raycast(pos.add(0, 1.2, 0), move) != null)
                                targetPositionMovementUpdate();
                    }
                }
            }



            if(factory.isJump){// Прыжки
                if(tickJump == -1){// Условие определяющие начало прыжка
                    final Vector3 check = new Vector3(this.motionX, 0, this.motionZ);

                    // Проверка на один блок перед ним и на 0 гравитацию
                    if(raycast(pos.add(0, 0.2), check, 9) != null && raycast(pos.add(0, 1.2), check, 9) == null && motionY == 0) {
                        tickJump = 0;
                    }
                }else{
                    tickJump++;

                    final var xzLength = Math.sqrt(motionX * motionX + motionZ * motionZ);
                    final var k = factory.movement / xzLength * 0.33;
                    final var dx = motionX * k;
                    final var dz = motionZ * k;

                    final Block[] collisionBlocks = this.level.getCollisionBlocks(
                            this.getBoundingBox().getOffsetBoundingBox(dx, 0, dz), false, false, this::canJump);

                    if (motionY < 0)
                        this.motionY = 0;

                    final double maxY = Arrays.stream(collisionBlocks).map(b -> b.getCollisionBoundingBox().getMaxY()).max(Double::compareTo).orElse(0.0d);
                    motionY += getJumpingMotion(maxY - this.getY()) / 6d;

                    if(tickJump > maxTimeJump){
                        tickJump = -1;
                    }
                }
            }

            final Vector3 move = new Vector3(this.motionX, this.motionY, this.motionZ)
                    .multiply(2)
                    .add(pos);

            this.move(this.motionX, this.motionY, this.motionZ);
            this.updateMovement();

            return move;
        }
        return null;
    }


    public void setTargetBlock(@NotNull Vector3 target) {
        this.target = target;
    }

    public void targetPositionMovementUpdate(){// Определение новой позиции
        if(followTarget != null){
            this.target = followTarget.getPosition();
            return;
        }else if(factory.navigationRandomStroll != null) {
            factory.navigationRandomStroll.onUpdate(this);
            return;
        }

        this.target = this.getPosition();
    }

    @Override
    public int getKillExperience() {
        return 0;
    }


    protected EntityContentFactory factory;
    public EntityContentFactory getFactory(){
        if(factory != null)
            return factory;

        factory = EntityContentFactory.factoryMap.get(this.namedTag.getString("___id___"));
        initDef();
        return factory;
    }

    private void initDef(){
        this.setMaxHealth(factory.health.max);
        this.setHealth(factory.health.defaultValue);
        this.fireProof = !factory.isFireImmune;
    }

    @Override
    public float getWidth() {
        return getFactory().collision.width;
    }

    @Override
    public float getLength() {
        return this.getWidth();
    }

    @Override
    public float getHeight() {
        return getFactory().collision.height;
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return getFactory().getEntityDefinition();
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket packet = (AddEntityPacket) super.createAddEntityPacket();
        packet.id = getFactory().description.identifier;
        return packet;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        for(IUpdateEntity tag : factory.updates)
            tag.onUpdate(this);

        if (!this.isAlive()) {
            if (++this.deadTicks >= 23) {
                this.close();
                return false;
            }
            return true;
        }

        int tickDiff = currentTick - this.lastUpdate;
        this.lastUpdate = currentTick;
        this.entityBaseTick(tickDiff);

        if(factory.nearestAttackableTarget != null)
            factory.nearestAttackableTarget.onUpdate(this);

        Optional.ofNullable(this.updateMove(tickDiff))
                .ifPresent(this::lookAt);

        return true;
    }

    @Override
    public int getNetworkId() {
        return -1;
    }

    @Override
    public void setOnFire(int seconds) {
        if(!factory.isFireImmune)
            super.setOnFire(seconds);
    }

    @Override
    public void setNameTagVisible(boolean value) {
        if(factory.isNameable)
            super.setNameTagVisible(value);
    }

    @Override
    public String getName() {
        return "entity."+factory.description.identifier+".name";
    }

    private final ConcurrentHashMap<Player, DummyBossBar> bossBars = new ConcurrentHashMap<>();

    @Override
    public void spawnTo(Player player) {
        if(getFactory().boss != null) {
            DummyBossBar bossBar = new DummyBossBar.Builder(player)
                    .text(this.getName())
                    .length(this.getMaxHealth())
                    .build();
            bossBar.create();
            bossBars.put(player, bossBar);
        }
        super.spawnTo(player);
    }

    @Override
    public void setTarget(Entity target) {
        this.setTargetBlock(target);
        super.setTarget(target);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if(source instanceof EntityDamageByEntityEvent event){
             this.setTarget(event.getDamager());
        }
        return super.attack(source);
    }

    @Override
    public void despawnFrom(Player player) {
        if(factory.boss != null){
            DummyBossBar dummyBossBar = bossBars.get(player);
            if(dummyBossBar != null){
                dummyBossBar.destroy();
            }
        }
        super.despawnFrom(player);
    }
}
