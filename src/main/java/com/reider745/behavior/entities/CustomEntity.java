package com.reider745.behavior.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.BaseEntity;
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
import com.reider745.InnerCoreServer;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationComponentsTag;

import java.util.Optional;
import java.util.Random;

public class CustomEntity extends BaseEntity implements cn.nukkit.entity.custom.CustomEntity {
    public CustomEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private int tickJump = -1;
    private final int maxTimeJump = 20;// Сколько времени нужно для прыжка
    private int tickMove = 0;
    private final int maxTimeMove = 80;

    public boolean isSolid(double x, double y, double z){
        final Block block = this.level.getBlock((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        return block.getId() != BlockID.AIR && block.isSolid();
    }

    @Override
    public Vector3 updateMove(int tickDiff) {
        if (this.isMovement() && !isImmobile()) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            final double gravity_value = this.getGravity() * 4;
            final Vector3 pos = this.getPosition().clone();

            if(factory.isPhysics && !isSolid(pos.x, pos.y - gravity_value,pos.z)) {// Гравитация
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
                }
            }



            if(factory.isJump){// Прыжки
                if(tickJump == -1){// Условие определяющие начало прыжка
                    final Vector3 check = new Vector3(this.motionX, 0, this.motionZ)
                            .multiply(4)
                            .add(pos);

                    this.level.addParticle(
                            new SmokeParticle(check),
                            this.getServer().getOnlinePlayers().values()
                    );
                    if(isSolid(check.x, check.y, check.z))
                        tickJump = 0;
                }else{
                    tickJump++;

                    final double max_minus = maxTimeJump / 2d;
                    final double minus = (tickJump - max_minus);

                    if (motionY < 0)
                        this.motionY = 0;

                    this.motionY += gravity_value * ((minus * minus) / (max_minus * max_minus));


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

    public void targetPositionMovementUpdate(){// Определение новой позиции
        final Random random = new Random();
        final Vector3 pos = this.getPosition();
        final Vector3 target = pos.clone();

        target.x += random.nextInt(5) - random.nextInt(10);
        //target.y += random.nextInt(5) - random.nextInt(10);
        target.z += random.nextInt(5) - random.nextInt(10);

        this.target = target;
        this.tickMove = 0;
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
            tag.update(this);

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

        /*if(currentTick % 20 == 0){
            for(Block block : this.getBlocksAround()){
                this.level.setBlock(block, Block.get(1));
            }
        }*/

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

    public boolean isInTickingRange() {
        return true;
    }
}
