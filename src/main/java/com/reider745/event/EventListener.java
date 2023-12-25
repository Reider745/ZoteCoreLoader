package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockExplosionPrimeEvent;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerEatFoodEvent;
import cn.nukkit.event.player.PlayerExperienceChangeEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;

import com.reider745.api.CallbackHelper;
import com.reider745.entity.EntityMethod;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class EventListener implements Listener {

    public static void consumeEvent(Event event, CallbackHelper.ICallbackApply apply, boolean isPrevent) {
        CallbackHelper.apply(event, apply, isPrevent);
    }

    public static void consumeEvent(Event event, CallbackHelper.ICallbackApply apply) {
        consumeEvent(event, apply, false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Vector3 pos = event.getTouchVector();
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            consumeEvent(event,
                    () -> NativeCallback.onItemUsed((int) block.x, (int) block.y, (int) block.z,
                            event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true,
                            player.getHealth() > 0, player.getId()));
        }
    }

    public static void onBlockBreak(BlockBreakEvent event, boolean isNukkitPrevent) {
        final Block block = event.getBlock();
        consumeEvent(event, () -> NativeCallback.onBlockDestroyed((int) block.x, (int) block.y, (int) block.z,
                event.getFace().getIndex(), event.getPlayer().getId()), isNukkitPrevent);
    }

    @EventHandler
    public void onEatFood(PlayerEatFoodEvent event) {
        final Food food = event.getFood();
        consumeEvent(event, () -> NativeCallback.onPlayerEat(food.getRestoreFood(), food.getRestoreSaturation(),
                event.getPlayer().getId()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();
        consumeEvent(event, () -> NativeCallback.onBlockBuild((int) block.x, (int) block.y, (int) block.z, 0,
                event.getPlayer().getId()));
    }

    @EventHandler
    public void onRedstoneUpdate(RedstoneUpdateEvent event) {
        final Block block = event.getBlock();
        final Level level = event.getBlock().getLevel();

        NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int) block.z, level.getStrongPower(block),
                true, level);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof EntitySnowball) { // TODO?
            final Vector3 pos = entity.getPosition();
            final Block block = entity.getLevelBlock();
            final Item item = EntityMethod.getItemFromProjectile(entity.getId());
            final NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);

            NativeCallback.onThrowableHit(event.getEntity().getId(), (float) pos.x, (float) pos.y, (float) pos.z,
                    entity.getId(), (int) block.x, (int) block.y, (int) block.z, block.getDamage(),
                    item.getId(), item.count, item.getDamage(), extra != null ? extra.getValue() : 0);
        }
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        final FullChunk fullChunk = event.getChunk();
        final Level level = event.getLevel();

        final int X = fullChunk.getX();
        final int Z = fullChunk.getZ();

        final CallbackHelper.ICallbackApply applyPre = () -> NativeCallback.onPreChunkPostProcessed(X, Z);
        // TODO: onBiomeMapGenerated
        final CallbackHelper.ICallbackApply applyPost = () -> NativeCallback.onChunkPostProcessed(X, Z);

        try {
            switch (level.getDimension()) {
                case Level.DIMENSION_OVERWORLD -> {
                    CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK, level, applyPre);
                    CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_OVERWORLD, level, applyPost);
                }
                case Level.DIMENSION_NETHER -> {
                    CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK_NETHER, level, applyPre);
                    CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_NETHER, level, applyPost);
                }

                case Level.DIMENSION_THE_END -> {
                    CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_END, level, applyPost);
                    CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK_END, level, applyPre);
                }
            }
        } catch (Exception e) {
            Logger.error("EventListener.onChunkPopulate(event)", e);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof EntityXPOrb xpOrb) {
            NativeCallback.onExpOrbsSpawned(entity.getLevel(), xpOrb.getExp(), (float) entity.x, (float) entity.y,
                    (float) entity.z, entity.getId());
        }
        NativeCallback.onEntityAdded(entity.getId());
    }

    @EventHandler
    public void onEntityDespawn(EntityDespawnEvent event) {
        NativeCallback.onEntityRemoved(event.getEntity().getId());
    }

    private static int convertDamageCauseToEnum(DamageCause cause) {
        return switch (cause) {
            case CONTACT -> 1;
            case ENTITY_ATTACK -> 2;
            case SUFFOCATION -> 4;
            case FALL -> 5;
            case HOT_FLOOR, FIRE -> 6;
            case FIRE_TICK -> 7;
            case LAVA -> 8;
            case DROWNING -> 9;
            case BLOCK_EXPLOSION -> 10;
            case ENTITY_EXPLOSION -> 11;
            case SUICIDE, VOID -> 12;
            case CUSTOM -> 13;
            case MAGIC -> 14;
            case HUNGER -> 16;
            case THORNS -> 18;
            case PROJECTILE -> 19;
            case MAGMA -> 22;
            case LIGHTNING -> 24;
            default -> 0;
        };
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final Entity attacker = event instanceof EntityDamageByEntityEvent damageByEntity
                ? damageByEntity.getDamager()
                : null;

        if (attacker instanceof Player) {
            consumeEvent(event,
                    () -> NativeCallback.onEntityAttacked(entity.getId(), attacker != null ? attacker.getId() : -1));
        }

        consumeEvent(event,
                () -> NativeCallback.onEntityHurt(entity.getId(), attacker != null ? attacker.getId() : -1,
                        convertDamageCauseToEnum(event.getCause()), (int) (event.getDamage() * 2f),
                        event.canBeReducedByArmor(), event.isBreakShield())); // последний 2 boolean тут временно
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final Entity entity = event.getEntity();
        final EntityDamageEvent damageEvent = entity.getLastDamageCause();
        final Entity attacker = damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent
                ? damageByEntityEvent.getDamager() : null;

        NativeCallback.onEntityDied(entity.getId(), attacker != null ? attacker.getId() : -1,
                convertDamageCauseToEnum(damageEvent != null ? damageEvent.getCause() : DamageCause.CUSTOM));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Entity entity = event.getEntity();
        final EntityDamageEvent damageEvent = entity.getLastDamageCause();
        final Entity attacker = damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent
                ? damageByEntityEvent.getDamager() : null;

        consumeEvent(event, () -> NativeCallback.onEntityDied(entity.getId(), attacker != null ? attacker.getId() : -1,
                convertDamageCauseToEnum(damageEvent != null ? damageEvent.getCause() : DamageCause.CUSTOM)));
    }

    @EventHandler
    public void onBlockExplosion(BlockExplosionPrimeEvent event) {
        final Location pos = event.getBlock().getLocation();

        consumeEvent(event, () -> NativeCallback.onExplode((float) pos.x, (float) pos.y, (float) pos.z,
                (float) event.getForce(), -1, event.isIncendiary(), event.isBlockBreaking(),
                (float) event.getFireChance()));
    }

    @EventHandler
    public void onEntityExplosion(EntityExplosionPrimeEvent event) {
        final Entity entity = event.getEntity();
        final Position pos = entity.getPosition();

        consumeEvent(event, () -> NativeCallback.onExplode((float) pos.x, (float) pos.y, (float) pos.z,
                (float) event.getForce(), entity.getId(), false, event.isBlockBreaking(), 0f));
    }

    @EventHandler
    public void onExperienceChange(PlayerExperienceChangeEvent event) {
        final Player player = event.getPlayer();
        final int newExperienceLevel = event.getNewExperienceLevel();
        final int oldExperienceLevel = event.getOldExperienceLevel();

        if (newExperienceLevel != oldExperienceLevel) {
            consumeEvent(event,
                    () -> NativeCallback.onPlayerLevelAdded(newExperienceLevel - oldExperienceLevel, player.getId()));
        }

        final int newExperience = event.getNewExperience();
        final int oldExperience = event.getOldExperience();

        if (newExperience != oldExperience) {
            consumeEvent(event, () -> NativeCallback.onPlayerExpAdded(newExperience - oldExperience, player.getId()));
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        final Vector3 position = event.getClickedPos();

        NativeCallback.onInteractWithEntity(event.getEntity().getId(), event.getPlayer().getId(), (float) position.x,
                (float) position.y, (float) position.z);
    }

    @EventHandler
    public void onPickupItem(InventoryPickupItemEvent event) {
        final long dropEntity = event.getItem().getId();
        final long entity = event.getInventory().getHolder() instanceof Entity pickupEntity
                ? pickupEntity.getId()
                : -1;
        final Item item = event.getItem().getItem();
        final int count = item != null ? item.getCount() : 0;

        consumeEvent(event, () -> NativeCallback.onEntityPickUpDrop(entity, dropEntity, count));
    }

    @EventHandler
    public void onBlockUpdate(BlockGrowEvent event) {
        final Block block = event.getBlock();
        final Block newBlock = event.getNewState();

        consumeEvent(event,
                () -> NativeCallback.onBlockChanged((int) block.x, (int) block.y, (int) block.z, block.getId(),
                        block.getDamage(), newBlock.getId(), newBlock.getDamage(), block.getFullId(),
                        newBlock.getFullId(), block.getLevel()));
    }

    // TODO: onPathNavigationDone

    // TODO: onAnimateBlockTick

    // TODO: onBlockSpawnResources

    // TODO: onBlockEventEntityInside

    // TODO: onBlockEventEntityStepOn

    // TODO: onBlockEventNeighbourChange

    // TODO: onItemUsedNoTarget (not only custom items)

    // TODO: onItemUseReleased (not only custom items)

    // TODO: onItemUseComplete (not only custom items)

    // TODO: onItemDispensed

    // TODO: onEnchantPostAttack, onEnchantPostHurt, onEnchantGetDamageBonus, onEnchantGetProtectionBonus

    // TODO: onWorkbenchCraft

    // TODO: onCustomDimensionTransfer
}
