package com.reider745.behavior.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;


public class CustomEntity extends Entity implements cn.nukkit.entity.custom.CustomEntity {
    public CustomEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    protected EntityContentFactory factory;
    public EntityContentFactory getFactory(){
        if(factory != null)
            return factory;

        factory = EntityContentFactory.factoryMap.get(this.namedTag.getString("___id___"));
        return factory;
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
    public int getNetworkId() {
        return -1;
    }

    @Override
    public void setOnFire(int seconds) {
        if(!factory.isFireImmune)
            super.setOnFire(seconds);
    }
}
