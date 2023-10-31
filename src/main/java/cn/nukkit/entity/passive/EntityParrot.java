package cn.nukkit.entity.passive;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityFlyable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author PikyCZ
 */
public class EntityParrot extends EntityAnimal implements EntityFlyable {

    public static final int NETWORK_ID = 30;

    public EntityParrot(FullChunk chunk, CompoundTag nbt) {
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
        return "Parrot";
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 1.0f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(6);
        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.FEATHER)};
    }
}
