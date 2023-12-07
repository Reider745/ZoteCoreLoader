package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerEatFoodEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import com.reider745.api.CallbackHelper;
import com.reider745.entity.EntityMethod;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

public class EventListener implements Listener {
    public static void preventedCallback(Event event, CallbackHelper.ICallbackApply apply) {
        CallbackHelper.apply(event, apply);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void use(PlayerInteractEvent event) {
        Vector3 pos = event.getTouchVector();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        PlayerInteractEvent.Action action = event.getAction();

        if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            preventedCallback(event,
                    () -> NativeCallback.onItemUsed((int) block.x, (int) block.y, (int) block.z,
                            event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true,
                            player.getHealth() > 0, player.getId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void breakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        preventedCallback(event, () -> NativeCallback.onBlockDestroyed((int) block.x, (int) block.y, (int) block.z,
                event.getFace().getIndex(), event.getPlayer().getId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerEat(PlayerEatFoodEvent event) {
        Food food = event.getFood();
        preventedCallback(event, () -> NativeCallback.onPlayerEat(food.getRestoreFood(), food.getRestoreSaturation(),
                event.getPlayer().getId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void placeBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();
        preventedCallback(event, () -> NativeCallback.onBlockBuild((int) block.x, (int) block.y, (int) block.z, 0,
                event.getPlayer().getId()));
    }

    @EventHandler
    public void redstoneUpdate(RedstoneUpdateEvent event){
        Block block = event.getBlock();
        Level level = event.getBlock().getLevel();

        NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int)
            block.z, level.getStrongPower(block), true, level);
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof EntitySnowball){
            Vector3 pos = entity.getPosition();
            Block block = entity.getLevelBlock();
            Item item = EntityMethod.getItemFromProjectile(entity.getId());
            NativeItemInstanceExtra extra = ItemUtils.getItemInstanceExtra(item);

            NativeCallback.onThrowableHit(event.getEntity().getId(), (float) pos.x, (float) pos.y, (float) pos.z,
                    entity.getId(), (int) block.x, (int) block.y, (int) block.z, block.getDamage(),
                    item.getId(), item.count, item.getDamage(), extra != null ? extra.getPtr() : 0);
        }
    }

    @EventHandler
    public void chunkGeneration(ChunkPopulateEvent event) {
        FullChunk fullChunk = event.getChunk();
        Level level = event.getLevel();
        CallbackHelper.ICallbackApply apply = () -> NativeCallback.onChunkPostProcessed(fullChunk.getX(),
                fullChunk.getZ());

        CallbackHelper.applyRegion(CallbackHelper.Type.PRE_GENERATION_CHUNK, level, () -> NativeCallback.onPreChunkPostProcessed(
                fullChunk.getX(), fullChunk.getZ())
        );
        switch (level.getDimension()) {
            case Level.DIMENSION_OVERWORLD ->
                CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_OVERWORLD, level, apply);
            case Level.DIMENSION_NETHER ->
                CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_NETHER, level, apply);
            case Level.DIMENSION_THE_END ->
                CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_END, level, apply);
        }
    }

    @EventHandler
    public void damageEntity(EntityDamageEvent event) {
        Entity self = event.getEntity();

        Entity attacker;
        if (event instanceof EntityDamageByEntityEvent child) {
            attacker = child.getDamager();
            preventedCallback(event,
                    () -> NativeCallback.onEntityAttacked(self.getId(), attacker != null ? attacker.getId() : -1));
        } else {
            attacker = null;
        }

        preventedCallback(event,
                () -> NativeCallback.onEntityHurt(self.getId(), attacker != null ? attacker.getId() : -1,
                        event.getCause().ordinal(), (int) event.getDamage() * 2, event.canBeReducedByArmor(),
                        event.isBreakShield())); // последний 2 boolean тут временно
    }

    @EventHandler
    public void addedEntity(EntitySpawnEvent event) {
        NativeCallback.onEntityAdded(event.getEntity().getId());
    }

    @EventHandler
    public void removeEntity(EntityDespawnEvent event) {
        NativeCallback.onEntityRemoved(event.getEntity().getId());
    }
}
