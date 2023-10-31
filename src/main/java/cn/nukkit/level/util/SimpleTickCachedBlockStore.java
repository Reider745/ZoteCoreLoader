package cn.nukkit.level.util;

import cn.nukkit.api.PowerNukkitXOnly;
import cn.nukkit.api.Since;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;

import java.util.concurrent.ConcurrentHashMap;

@PowerNukkitXOnly
@Since("1.6.0.0-PNX")
public final class SimpleTickCachedBlockStore implements TickCachedBlockStore {
    private final ConcurrentHashMap<Integer, Block> tickCachedBlockStore;
    private final Level level;

    public SimpleTickCachedBlockStore(Level level) {
        this.tickCachedBlockStore = new ConcurrentHashMap<>(32, 0.75f);
        this.level = level;
    }

    @Override
    public void clearCachedStore() {
        this.tickCachedBlockStore.clear();
    }

    @Override
    public void saveIntoCachedStore(Block block, int x, int y, int z, int layer) {
        tickCachedBlockStore.put((int) Level.localBlockHash(x, y, z), block);
    }

    @Override
    public Block getFromCachedStore(int x, int y, int z, int layer) {
        return tickCachedBlockStore.get(Level.localBlockHash(x, y, z));
    }

    @Override
    public Block computeFromCachedStore(int x, int y, int z, int layer, CachedBlockComputer cachedBlockComputer) {
        return tickCachedBlockStore.computeIfAbsent((int) Level.localBlockHash(x, y, z), ignore -> cachedBlockComputer.compute());
    }
}
