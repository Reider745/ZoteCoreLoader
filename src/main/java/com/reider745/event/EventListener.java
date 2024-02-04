package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
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
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.event.server.ServerCommandEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;

import com.reider745.api.CallbackHelper;
import com.reider745.entity.EntityMethod;
import com.reider745.entity.EntityMotion;
import com.reider745.hooks.ItemUtils;
import com.reider745.hooks.PlayerHooks;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimension;

public class EventListener implements Listener {
    public static final Object DEALING_LOCK = new Object();
    public static Object dealingEvent = null;

    public static void consumeEvent(Event event, CallbackHelper.ICallbackApply apply, boolean isPrevent) {
        CallbackHelper.apply(event, apply, isPrevent);
    }

    public static void consumeEvent(Event event, CallbackHelper.ICallbackApply apply) {
        consumeEvent(event, apply, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.equals(dealingEvent) || Boolean.TRUE.equals(dealingEvent)) {
            return;
        }

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEatFood(PlayerEatFoodEvent event) {
        final Food food = event.getFood();
        consumeEvent(event, () -> NativeCallback.onPlayerEat(food.getRestoreFood(), food.getRestoreSaturation(),
                event.getPlayer().getId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();
        consumeEvent(event, () -> NativeCallback.onBlockBuild((int) block.x, (int) block.y, (int) block.z, 0,
                event.getPlayer().getId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedstoneUpdate(RedstoneUpdateEvent event) {
        final Block block = event.getBlock();
        final Level level = event.getBlock().getLevel();

        NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int) block.z, level.getStrongPower(block),
                true, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        final Entity entity = event.getEntity();
        final Vector3 hit = entity.getPosition();
        final Block block = entity.getLevelBlock();
        final MovingObjectPosition mop = event.getMovingObjectPosition();
        final Item item = EntityMethod.getItemFromProjectile(entity.getId());
        final NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);

        NativeCallback.onThrowableHit(event.getEntity().getId(), (float) hit.x, (float) hit.y, (float) hit.z,
                entity.getId(), (int) block.x, (int) block.y, (int) block.z, mop != null ? mop.sideHit : -1,
                item.getId(), item.count, item.getDamage(), extra != null ? extra.getValue() : 0,
                mop != null && mop.entityHit != null ? mop.entityHit.getId() : 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
                    CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK_END, level, applyPre);
                    CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_END, level, applyPost);
                }

                default -> {
                    CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_CUSTOM, level, applyPost);
                    CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK_CUSTOM, level, applyPre);
                }
            }
        } catch (Exception e) {
            Logger.error("EventListener.onChunkPopulate(event)", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        final Entity entity = event.getEntity();

        EntityMotion.added(entity);

        if (entity instanceof EntityXPOrb xpOrb) {
            NativeCallback.onExpOrbsSpawned(entity.getLevel(), xpOrb.getExp(), (float) entity.x, (float) entity.y,
                    (float) entity.z, entity.getId());
        }
        NativeCallback.onEntityAdded(entity.getId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDespawn(EntityDespawnEvent event) {
        final Entity entity = event.getEntity();

        EntityMotion.remove(entity);
        NativeCallback.onEntityRemoved(entity.getId());
    }

    public static int convertDamageCauseToEnum(DamageCause cause) {
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

    public static DamageCause convertEnumToDamageCause(int cause) {
        return switch (cause) {
            case 1 -> DamageCause.CONTACT;
            case 2 -> DamageCause.ENTITY_ATTACK;
            case 4 -> DamageCause.SUFFOCATION;
            case 5 -> DamageCause.FALL;
            case 6 -> DamageCause.HOT_FLOOR;
            case 7 -> DamageCause.FIRE_TICK;
            case 8 -> DamageCause.LAVA;
            case 9 -> DamageCause.DROWNING;
            case 10 -> DamageCause.BLOCK_EXPLOSION;
            case 11 -> DamageCause.ENTITY_EXPLOSION;
            case 12 -> DamageCause.VOID;
            case 13 -> DamageCause.CUSTOM;
            case 14 -> DamageCause.MAGIC;
            case 16 -> DamageCause.HUNGER;
            case 18 -> DamageCause.THORNS;
            case 19 -> DamageCause.PROJECTILE;
            case 22 -> DamageCause.MAGMA;
            case 24 -> DamageCause.LIGHTNING;
            default -> DamageCause.SUICIDE;
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.equals(dealingEvent) || Boolean.TRUE.equals(dealingEvent)) {
            return;
        }

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        final Entity entity = event.getEntity();
        final EntityDamageEvent damageEvent = entity.getLastDamageCause();
        final Entity attacker = damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent
                ? damageByEntityEvent.getDamager()
                : null;

        NativeCallback.onEntityDied(entity.getId(), attacker != null ? attacker.getId() : -1,
                convertDamageCauseToEnum(damageEvent != null ? damageEvent.getCause() : DamageCause.CUSTOM));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Entity entity = event.getEntity();
        final EntityDamageEvent damageEvent = entity.getLastDamageCause();
        final Entity attacker = damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent
                ? damageByEntityEvent.getDamager()
                : null;

        consumeEvent(event, () -> NativeCallback.onEntityDied(entity.getId(), attacker != null ? attacker.getId() : -1,
                convertDamageCauseToEnum(damageEvent != null ? damageEvent.getCause() : DamageCause.CUSTOM)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplosion(BlockExplosionPrimeEvent event) {
        if (event.equals(dealingEvent) || Boolean.TRUE.equals(dealingEvent)) {
            return;
        }

        final Location pos = event.getBlock().getLocation();

        consumeEvent(event, () -> NativeCallback.onExplode((float) pos.x, (float) pos.y, (float) pos.z,
                (float) event.getForce(), -1, event.isIncendiary(), event.isBlockBreaking(), Float.MAX_VALUE));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplosion(EntityExplosionPrimeEvent event) {
        if (event.equals(dealingEvent) || Boolean.TRUE.equals(dealingEvent)) {
            return;
        }

        final Entity entity = event.getEntity();
        final Position pos = entity.getPosition();

        consumeEvent(event, () -> NativeCallback.onExplode((float) pos.x, (float) pos.y, (float) pos.z,
                (float) event.getForce(), entity.getId(), false, event.isBlockBreaking(), Float.MAX_VALUE));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        final Vector3 position = event.getClickedPos();

        NativeCallback.onInteractWithEntity(event.getEntity().getId(), event.getPlayer().getId(), (float) position.x,
                (float) position.y, (float) position.z);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(InventoryPickupItemEvent event) {
        final long dropEntity = event.getItem().getId();
        final long entity = event.getInventory().getHolder() instanceof Entity pickupEntity
                ? pickupEntity.getId()
                : -1;
        final Item item = event.getItem().getItem();
        final int count = item != null ? item.getCount() : 0;

        consumeEvent(event, () -> NativeCallback.onEntityPickUpDrop(entity, dropEntity, count));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockUpdate(BlockGrowEvent event) {
        final Block block = event.getBlock();
        final Block newBlock = event.getNewState();

        consumeEvent(event,
                () -> NativeCallback.onBlockChanged((int) block.x, (int) block.y, (int) block.z, block.getId(),
                        block.getDamage(), newBlock.getId(), newBlock.getDamage(), block.getFullId(),
                        newBlock.getFullId(), block.getLevel()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(EntityTeleportEvent event) {
        final int from = event.getFrom().level.getDimension();
        final int to = event.getFrom().level.getDimension();

        if (CustomDimension.getDimensionById(to) != null || CustomDimension.getDimensionById(from) != null)
            NativeCallback.onCustomDimensionTransfer(event.getEntity().getId(), from, to);
    }

    public void onServerCommand(ServerCommandEvent event) {
        final CommandSender sender = event.getSender();
        final Position position = sender.getPosition();
        final long entityUid = sender.isEntity() ? sender.asEntity().getId() : 0;

        consumeEvent(event, () -> NativeCallback.onServerCommand(event.getCommand(), (float) position.x,
                (float) position.y, (float) position.z, entityUid));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLoginEvent(PlayerPreLoginEvent event) {
        final Player player = event.getPlayer();
        for (Player p : new ArrayList<>(player.getServer().getOnlinePlayers().values())) {
            final String username = p.getName();
            if (p != player && username != null) {
                if (username.equalsIgnoreCase(player.getName()) || player.getUniqueId().equals(p.getUniqueId())) {
                    event.setKickMessage("disconnectionScreen.loggedinOtherLocation");
                    event.setCancelled();
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginEvent(PlayerLoginEvent event) {
        ConnectedClient client = PlayerHooks.getForPlayer(event.getPlayer());
        if (client == null) {
            event.setKickMessage("You are should connect via Inner Core!");
            event.setCancelled();
            return;
        }

        final String username = event.getPlayer().getName();
        consumeEvent(event, () -> NativeCallback.onPlayerLogin(client, username, (message) -> {
            if (message != null) {
                event.setKickMessage(message);
            }
            event.setCancelled();
        }));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        EntityItem itemEntity = event.getEntity();
        Item nukkitItem = itemEntity.getItem();

        if (nukkitItem != null) {
            EntryItem item = NukkitIdConvertor.getInnerCoreForNukkit(nukkitItem.getId(), nukkitItem.getDamage());
            CustomManager manager = CustomManager.getFor(item.id);
            if (manager != null && manager.containsKey("fire_resistant")) {
                boolean fireproof = manager.get("fire_resistant", false);
                itemEntity.fireProof = fireproof;
                ReflectHelper.setField(itemEntity, "floatsInLava", fireproof);
            }
        }
    }

    // TODO: onPathNavigationDone

    // TODO: onBlockSpawnResources

    // TODO: onBlockEventEntityInside

    // TODO: onBlockEventEntityStepOn

    // TODO: onBlockEventNeighbourChange (not only custom blocks)

    // TODO: onItemUsedNoTarget (not only custom items)

    // TODO: onItemUseReleased (not only custom items)

    // TODO: onItemUseComplete (not only custom items)

    // TODO: onItemDispensed

    // TODO: onWorkbenchCraft
}
