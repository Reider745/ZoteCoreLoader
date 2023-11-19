package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerEatFoodEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.event.server.ServerStopEvent;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeCallback;

public class EventListener implements Listener {
    public interface ICallbackApply {
        void apply();
    }

    public static void preventedCallback(Event event, ICallbackApply apply){
        NativeAPI.reloadPrevent();
        apply.apply();
        if(NativeAPI.isDefaultPrevented())
            event.setCancelled(true);
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Vector3 pos = event.getTouchVector();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        PlayerInteractEvent.Action action = event.getAction();

        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            preventedCallback(event, () -> NativeCallback.onItemUsed((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true, player.getHealth() > 0, player.getId()));
        else if(action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
            preventedCallback(event, () -> NativeCallback._onBlockDestroyStarted((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), player.getId()));


    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event){
        Block block = event.getBlock();
        preventedCallback(event, () -> NativeCallback.onBlockDestroyed((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), event.getPlayer().getId()));
    }

    @EventHandler
    public void playerEat(PlayerEatFoodEvent event){
        Food food = event.getFood();
        preventedCallback(event, () ->  NativeCallback.onPlayerEat(food.getRestoreFood(), food.getRestoreSaturation(), event.getPlayer().getId()));
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event){
        Block block = event.getBlock();
        preventedCallback(event, () -> NativeCallback.onBlockBuild((int) block.x, (int) block.y, (int) block.z, 0, event.getPlayer().getId()));
    }

    @EventHandler
    public void restoneUpdate(RedstoneUpdateEvent event){
        Block block = event.getBlock();
        NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int) block.z, 0, false, event.getBlock().getLevel());
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event){
        /*Vector3 pos = event.
        NativeCallback.onThrowableHit(event.getEntity().getId(), );*/
    }

    @EventHandler
    public void chunkGeneration(ChunkPopulateEvent event){
        FullChunk fullChunk = event.getChunk();
        NativeBlockSource.level_current = event.getLevel();
        NativeCallback.onChunkPostProcessed(fullChunk.getX(), fullChunk.getZ());
        NativeBlockSource.level_current = null;
    }
}