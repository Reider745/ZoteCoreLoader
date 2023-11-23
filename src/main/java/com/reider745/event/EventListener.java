package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerEatFoodEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import com.reider745.api.CallbackHelper;
import com.zhekasmirnov.innercore.api.NativeCallback;

public class EventListener implements Listener {
    public static void preventedCallback(Event event, CallbackHelper.ICallbackApply apply){
        CallbackHelper.apply(event, apply);
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Vector3 pos = event.getTouchVector();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        PlayerInteractEvent.Action action = event.getAction();

        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            preventedCallback(event, () -> NativeCallback.onItemUsed((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true, player.getHealth() > 0, player.getId()));
      //  else if(action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
           // preventedCallback(event, () -> NativeCallback._onBlockDestroyStarted((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), player.getId()));


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

    /*@EventHandler
    public void restoneUpdate(RedstoneUpdateEvent event){
        Block block = event.getBlock();
        NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int) block.z, 0, false, event.getBlock().getLevel());
    }*/

    @EventHandler
    public void projectileHit(ProjectileHitEvent event){
        /*Vector3 pos = event.
        NativeCallback.onThrowableHit(event.getEntity().getId(), );*/
    }

    @EventHandler
    public void chunkGeneration(ChunkPopulateEvent event){
        FullChunk fullChunk = event.getChunk();
        Level level = event.getLevel();
        CallbackHelper.ICallbackApply apply = () -> NativeCallback.onChunkPostProcessed(fullChunk.getX(), fullChunk.getZ());

        switch (level.getDimension()){
            case Level.DIMENSION_OVERWORLD -> CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_OVER_WORLD, level, apply);
            case Level.DIMENSION_NETHER -> CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_NETHER, level, apply);
            case Level.DIMENSION_THE_END -> CallbackHelper.applyRegion(CallbackHelper.Type.GENERATION_CHUNK_END, level, apply);
        }
    }

    @EventHandler
    public void damagetEntity(EntityDamageEvent event){
        Entity self = event.getEntity();

        Entity attacker;
        if(event instanceof EntityDamageByChildEntityEvent child) {
            attacker = child.getChild();
            preventedCallback(event, () ->
                NativeCallback.onEntityAttacked(self.getId(), attacker == null ? -1 : attacker.getId()));
        }else {
            attacker = null;
        }

        preventedCallback(event, () ->
                NativeCallback.onEntityHurt(self.getId(), attacker == null ? -1 : attacker.getId(), event.getCause().ordinal(), (int) event.getDamage() * 2, event.canBeReducedByArmor(), event.isBreakShield()));//последний 2 boolean тут врменн
    }
}