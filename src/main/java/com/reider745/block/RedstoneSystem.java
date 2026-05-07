package com.reider745.block;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import com.zhekasmirnov.innercore.api.NativeCallback;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RedstoneSystem {
    private static final Int2ObjectMap<Map<Block, Byte>> redstoneSignals = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    @Nullable
    private static Map<Block, Byte> getBlocks(Block block) {
        if(block.isValid()) {
            return redstoneSignals.computeIfAbsent(block.getLevel().getId(), (id) -> Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>()));
        }
        return null;
    }

    public static void onChangeRedstone(Block block) {
        Map<Block, Byte> blocks = getBlocks(block);

        if(blocks != null) {
            Level level = block.getLevel();
            int signal = level.isBlockIndirectlyGettingPowered(block);

            if(!blocks.containsKey(block) || blocks.get(block) != signal) {
                blocks.put(block, (byte) signal);

                NativeCallback.onRedstoneSignalChange((int) block.x, (int) block.y, (int) block.z,
                        signal, !block.isPowerSource(), level);
            }
        }
    }

    public static void onBreak(Block block) {
        Map<Block, Byte> blocks = getBlocks(block);

        if(blocks != null) {
            blocks.remove(block);
        }
    }

    public static void onUnloadLevel(Level level) {
        redstoneSignals.remove(level.getId());
    }
}
