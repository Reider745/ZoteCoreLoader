package cn.nukkit.entity.item;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;

@PowerNukkitOnly
public class EntityPotionLingering extends EntityPotion {

    @PowerNukkitOnly
    public static final int NETWORK_ID = 101;

    @PowerNukkitOnly
    public EntityPotionLingering(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @PowerNukkitOnly
    public EntityPotionLingering(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        setDataFlag(DATA_FLAGS, DATA_FLAG_LINGER, true);
    }

    @PowerNukkitOnly
    @Override
    protected void splash(Entity collidedWith) {
        super.splash(collidedWith);
        saveNBT();
        ListTag<?> pos = (ListTag<?>) namedTag.getList("Pos", CompoundTag.class).copy();
        EntityAreaEffectCloud entity = (EntityAreaEffectCloud) Entity.createEntity("AreaEffectCloud", getChunk(),
                new CompoundTag().putList(pos)
                        .putList(new ListTag<>("Rotation")
                                .add(new FloatTag("", 0))
                                .add(new FloatTag("", 0))
                        )
                        .putList(new ListTag<>("Motion")
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0))
                        )
                        .putShort("PotionId", potionId)
        );

        Effect effect = Potion.getEffect(potionId, true);

        if (effect != null && entity != null) {
            entity.cloudEffects.add(effect/*.setDuration(1)*/.setVisible(false).setAmbient(false));
            entity.spawnToAll();
        }
    }


    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Lingering Potion";
    }
}
