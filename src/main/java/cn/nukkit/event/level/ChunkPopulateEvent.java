package cn.nukkit.event.level;

import cn.nukkit.event.HandlerList;
import cn.nukkit.level.format.FullChunk;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.innercore.api.NativeCallback;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ChunkPopulateEvent extends ChunkEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public ChunkPopulateEvent(FullChunk chunk) {
        super(chunk);
        NativeBlockSource.level_current = chunk.getProvider().getLevel();
        NativeCallback.onPreChunkPostProcessed(chunk.getX(), chunk.getZ());
        NativeCallback.onChunkPostProcessed(chunk.getX(), chunk.getZ());
    }

}