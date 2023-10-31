package cn.nukkit.entity.passive;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.PowerNukkitXOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author Erik Miller | EinBexiii
 */
@Since("1.3.1.0-PN")
public class EntityStrider extends EntityAnimal implements EntityWalkable {

    public final static int NETWORK_ID = 125;

    public EntityStrider(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.7f;
    }

    @PowerNukkitXOnly
    @Since("1.19.50-r2")
    @Override
    public int getFrostbiteInjury() {
        return 5;
    }

    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Strider";
    }
}
