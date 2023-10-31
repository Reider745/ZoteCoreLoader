package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author joserobjr
 * @since 2020-11-20
 */
@Since("1.4.0.0-PN")
@PowerNukkitOnly
public class EntityPiglinBrute extends EntityMob implements EntityWalkable {

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    public final static int NETWORK_ID = 127;

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    public EntityPiglinBrute(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(50);
        super.initEntity();
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @PowerNukkitOnly
    @Override
    public boolean isPreventingSleep(Player player) {
        return true;
    }

    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Piglin Brute";
    }
}
