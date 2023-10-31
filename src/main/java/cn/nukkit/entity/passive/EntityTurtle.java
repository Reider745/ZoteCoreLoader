package cn.nukkit.entity.passive;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntitySwimmable;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author PetteriM1
 */
public class EntityTurtle extends EntityAnimal implements EntitySwimmable, EntityWalkable {

    public static final int NETWORK_ID = 74;

    public EntityTurtle(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Turtle";
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.6f;
        }
        return 1.2f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.2f;
        }
        return 0.4f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(30);
        super.initEntity();
    }

    @PowerNukkitOnly
    public void setBreedingAge(int ticks) {

    }

    @PowerNukkitOnly
    public void setHomePos(Vector3 pos) {

    }
}
