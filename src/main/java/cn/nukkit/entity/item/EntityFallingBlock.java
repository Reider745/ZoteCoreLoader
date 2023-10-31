package cn.nukkit.entity.item;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLava;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityBlockChangeEvent;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.Item;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelEventPacket;

/**
 * @author MagicDroidX
 */
public class EntityFallingBlock extends Entity {

    public static final int NETWORK_ID = 66;
    protected int blockId;
    protected int damage;
    protected @PowerNukkitOnly boolean breakOnLava;
    protected @PowerNukkitOnly boolean breakOnGround;

    public EntityFallingBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.98f;
    }

    @Override
    public float getLength() {
        return 0.98f;
    }

    @Override
    public float getHeight() {
        return 0.98f;
    }

    @Override
    protected float getGravity() {
        return 0.04f;
    }

    @Override
    protected float getDrag() {
        return 0.02f;
    }

    @Override
    protected float getBaseOffset() {
        return 0.49f;
    }

    @Override
    public boolean canCollide() {
        return blockId == BlockID.ANVIL;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (namedTag != null) {
            if (namedTag.contains("TileID")) {
                blockId = namedTag.getInt("TileID");
            } else if (namedTag.contains("Tile")) {
                blockId = namedTag.getInt("Tile");
                namedTag.putInt("TileID", blockId);
            }

            if (namedTag.contains("Data")) {
                damage = namedTag.getByte("Data");
            }
        }

        breakOnLava = namedTag.getBoolean("BreakOnLava");
        breakOnGround = namedTag.getBoolean("BreakOnGround");

        if (blockId == 0) {
            close();
            return;
        }

        this.fireProof = true;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_FIRE_IMMUNE, true);

        setDataProperty(new IntEntityData(DATA_VARIANT, GlobalBlockPalette.getOrCreateRuntimeId(this.getBlock(), this.getDamage())));
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return blockId == BlockID.ANVIL;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return source.getCause() == DamageCause.VOID && super.attack(source);
    }

    @Override
    public boolean onUpdate(int currentTick) {

        if (closed) {
            return false;
        }

        int tickDiff = currentTick - lastUpdate;
        if (tickDiff <= 0 && !justCreated) {
            return true;
        }

        lastUpdate = currentTick;

        boolean hasUpdate = entityBaseTick(tickDiff);

        if (isAlive()) {
            motionY -= getGravity();

            move(motionX, motionY, motionZ);

            float friction = 1 - getDrag();

            motionX *= friction;
            motionY *= 1 - getDrag();
            motionZ *= friction;

            Vector3 pos = (new Vector3(x - 0.5, y, z - 0.5)).round();

            if (breakOnLava && level.getBlock(pos.subtract(0, 1, 0)) instanceof BlockLava) {
                close();
                if (this.level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
                    dropItems();
                }
                level.addParticle(new DestroyBlockParticle(pos, Block.get(getBlock(), getDamage())));
                return true;
            }

            if (onGround) {
                close();
                Block block = level.getBlock(pos);

                Vector3 floorPos = (new Vector3(x - 0.5, y, z - 0.5)).floor();
                Block floorBlock = this.level.getBlock(floorPos);
                if (this.getBlock() == Block.SNOW_LAYER && floorBlock.getId() == Block.SNOW_LAYER && (floorBlock.getDamage() & 0x7) != 0x7) {
                    int mergedHeight = (floorBlock.getDamage() & 0x7) + 1 + (this.getDamage() & 0x7) + 1;
                    if (mergedHeight > 8) {
                        EntityBlockChangeEvent event = new EntityBlockChangeEvent(this, floorBlock, Block.get(Block.SNOW_LAYER, 0x7));
                        this.server.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            this.level.setBlock(floorPos, event.getTo(), true);

                            Vector3 abovePos = floorPos.up();
                            Block aboveBlock = this.level.getBlock(abovePos);
                            if (aboveBlock.getId() == Block.AIR) {
                                EntityBlockChangeEvent event2 = new EntityBlockChangeEvent(this, aboveBlock, Block.get(Block.SNOW_LAYER, mergedHeight - 8 - 1));
                                this.server.getPluginManager().callEvent(event2);
                                if (!event2.isCancelled()) {
                                    this.level.setBlock(abovePos, event2.getTo(), true);
                                }
                            }
                        }
                    } else {
                        EntityBlockChangeEvent event = new EntityBlockChangeEvent(this, floorBlock, Block.get(Block.SNOW_LAYER, mergedHeight - 1));
                        this.server.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            this.level.setBlock(floorPos, event.getTo(), true);
                        }
                    }
                } else if (block.getId() > 0 && block.isTransparent() && !block.canBeReplaced() || this.getBlock() == Block.SNOW_LAYER && block instanceof BlockLiquid) {
                    if (this.getBlock() != Block.SNOW_LAYER ? this.level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS) : this.level.getGameRules().getBoolean(GameRule.DO_TILE_DROPS)) {
                        dropItems();
                    }
                } else {
                    EntityBlockChangeEvent event = new EntityBlockChangeEvent(this, block, Block.get(getBlock(), getDamage()));
                    server.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        if (!breakOnGround)
                            getLevel().setBlock(pos, event.getTo(), true);
                        else {
                            if (this.level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
                                dropItems();
                            }
                            level.addParticle(new DestroyBlockParticle(pos, Block.get(getBlock(), getDamage())));
                        }

                        if (event.getTo().getId() == Item.ANVIL) {
                            getLevel().addLevelEvent(block, LevelEventPacket.EVENT_SOUND_ANVIL_FALL);

                            Entity[] e = level.getCollidingEntities(this.getBoundingBox(), this);
                            for (Entity entity : e) {
                                if (entity instanceof EntityLiving && fallDistance > 0) {
                                    entity.attack(new EntityDamageByBlockEvent(event.getTo(), entity, DamageCause.FALLING_BLOCK, Math.min(40f, Math.max(0f, fallDistance * 2f))));
                                }
                            }
                        }
                    }
                }
                hasUpdate = true;
            }

            updateMovement();
        }

        return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001;
    }

    public int getBlock() {
        return blockId;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void saveNBT() {
        namedTag.putInt("TileID", blockId);
        namedTag.putByte("Data", damage);
    }

    @Override
    public boolean canBeMovedByCurrents() {
        return false;
    }

    @Override
    public void resetFallDistance() {
        if (!this.closed) { // For falling anvil: do not reset fall distance before dealing damage to entities
            this.highestPosition = this.y;
        }
    }


    @PowerNukkitOnly
    @Since("1.5.1.0-PN")
    @Override
    public String getOriginalName() {
        return "Falling Block";
    }

    private void dropItems() {
        for (var i : Block.get(this.getBlock(), this.getDamage()).getDrops(Item.AIR_ITEM)) {
            getLevel().dropItem(this, i);
        }
    }
}
