package cn.nukkit.entity.passive;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPanda extends EntityAnimal implements EntityWalkable {

    public static final int NETWORK_ID = 113;

    public EntityPanda(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 1.7f;
    }

    @Override
    public float getHeight() {
        return 1.5f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }


    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Panda";
    }
}
