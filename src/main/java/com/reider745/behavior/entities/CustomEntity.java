package com.reider745.behavior.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityBoss;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.DummyBossBar;
import com.reider745.InnerCoreServer;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationComponentsTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
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

    public Block raycast(Vector3 pos, Vector3 move, int iterations){
        double x = pos.x;
        for(int i = 0;i < iterations;i++){
            final Block check = isSolid(pos.x, pos.y, pos.z);
            if(check != null) {
                // this.level.setBlock(check, Block.get(BlockID.NETHERITE_BLOCK));
                return check;
            }
            pos.x += move.x;
            pos.y += move.y;
            pos.z += move.z;
        }
        return null;
    }

    public Block raycast(Vector3 pos, Vector3 move){
        return raycast(pos, move, 3);
    }

    @Override
    public Vector3 updateMove(int tickDiff) {
        if (this.isMovement() && !isImmobile()) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            final double gravity_value = this.getGravity() * 4;
            final Vector3 pos = this.getPosition().clone();

            if(factory.isPhysics && isSolid(pos.x, pos.y - gravity_value,pos.z) == null) {// Гравитация
                this.motionY -= gravity_value;
            }

            if(factory.isMovement){// Движение
                tickMove++;
                if(target == null || tickMove >= maxTimeMove)
                    targetPositionMovementUpdate();

                if(pos.distance(target) < 1){
                    target = null;
                }else{
                    Vector3 move = target.subtract(pos)
                            .divide(target.distance(pos) * 2);

                    motionX += move.x * factory.movement;
                    motionZ += move.z * factory.movement;

                    // Если припятсвие в два блока меняем точку назначения
                    if(raycast(pos, move) != null && raycast(pos.add(0, 1, 0), move) != null)
                        targetPositionMovementUpdate();
                }
            }



            if(factory.isJump){// Прыжки
                if(tickJump == -1){// Условие определяющие начало прыжка
                    final Vector3 check = new Vector3(this.motionX, 0, this.motionZ);

                    // Проверка на один блок перед ним и на 0 гравитацию
                    if(raycast(pos, check) != null && raycast(pos.add(0, 1, 0), check) == null && motionY == 0)
                        tickJump = 0;
                }else{
                    tickJump++;

                    final double max_minus = maxTimeJump / 2d;
                    final double minus = (tickJump - max_minus);

                    if (motionY < 0)
                        this.motionY = 0;

                    this.motionY += (gravity_value / 2) * ((minus * minus) / (max_minus * max_minus));


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

        for(NavigationComponentsTag tag : factory.navigations_compononets)
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
