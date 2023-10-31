package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityFlyable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author PikyCZ
 */
public class EntityEnderDragon extends EntityMob implements EntityFlyable {

    public static final int NETWORK_ID = 53;

    public EntityEnderDragon(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 13f;
    }

    @Override
    public float getHeight() {
        return 4f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(200);
        super.initEntity();
    }

    @Override
    protected boolean applyNameTag(@NotNull Player player, @NotNull Item item) {
        return false;
    }

    @PowerNukkitOnly
    @Deprecated
    @Override
    public boolean applyNameTag(Item item) {
        return false;
    }

    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Ender Dragon";
    }

    @PowerNukkitOnly
    @Since("FUTURE")
    @Override
    public boolean isBoss() {
        return true;
    }
}