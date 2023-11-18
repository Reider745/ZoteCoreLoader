package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.server.ServerStopEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.innercore.api.NativeCallback;

public class EventListener implements Listener {
    @EventHandler
    public void use(PlayerInteractEvent event) {
        Vector3 pos = event.getTouchVector();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        NativeCallback.onItemUsed((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true, player.getHealth() > 0, player.getId());
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event){
        Block block = event.getBlock();
        NativeCallback.onBlockDestroyed((int) block.x, (int) block.y, (int) block.z, event.getFace().getIndex(), event.getPlayer().getId());
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event){
        Block block = event.getBlock();
        NativeCallback.onBlockBuild((int) block.x, (int) block.y, (int) block.z, 0, event.getPlayer().getId());
    }

    @EventHandler
    public void chunkGeneration(ChunkPopulateEvent event){
        FullChunk fullChunk = event.getChunk();
        NativeBlockSource.level_current = event.getLevel();
        NativeCallback.onChunkPostProcessed(fullChunk.getX(), fullChunk.getZ());
        NativeBlockSource.level_current = null;
    }
}