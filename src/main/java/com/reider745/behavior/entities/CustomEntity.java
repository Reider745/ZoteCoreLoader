package com.reider745.behavior.entities;

import cn.nukkit.block.BlockID;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import com.reider745.behavior.entities.formats.tags.components.navigation.NavigationComponentsTag;

import java.util.Optional;
import java.util.Random;

public class CustomEntity extends BaseEntity implements cn.nukkit.entity.custom.CustomEntity {
    public CustomEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private Vector3 targetPos = null;

    @Override
    public Vector3 updateMove(int tickDiff) {
        if (this.isMovement() && !isImmobile()) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            final double gravity_value = this.getGravity() * 4;
            final Vector3 pos = this.getPosition();

            if(factory.isPhysics && this.level.getBlockIdAt((int) pos.x, (int) (pos.y - gravity_value), (int) pos.z) == BlockID.AIR) {// Гравитация
                this.motionY -= gravity_value;

                this.move(this.motionX, this.motionY, this.motionZ);
                this.updateMovement();
                return null;
            }

            if(factory.isMovement){// Движение
                if(targetPos == null)
                    targetPositionMovementUpdate();



                if(pos.distance(targetPos) < .4){
                    targetPos = null;
                }else{
                    final Vector3 move = pos.subtract(targetPos).
                            divide(targetPos.distance(pos) * 2);

                    motionX += move.x * factory.movement;
                    motionZ += move.z * factory.movement;
                }
            }

            final Vector3 move = new Vector3(this.motionX, this.motionY, this.motionZ);

            this.move(this.motionX, this.motionY, this.motionZ);
            this.updateMovement();

            return pos.add(move);
        }
        return null;
    }

    public void targetPositionMovementUpdate(){// Определение новой позиции
        final Random random = new Random();
        final Vector3 target = this.getPosition().clone();



        target.x += random.nextInt(5) - random.nextInt(5);
        target.y += random.nextInt(5) - random.nextInt(5);
        target.z += random.nextInt(5) - random.nextInt(5);

        this.targetPos = target;
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
        this.setHealth(factory.health.defaultValue);
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
    public int getMaxHealth() {
        return getFactory().health.max;
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
