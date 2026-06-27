package com.zhekasmirnov.apparatus.mcpe;

import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState;
import org.innercore.icstd.region.BlockSource;

// this is a stub class for DungeonUtility, it MUST NOT be used in code
@Deprecated
public class NativeBlockSource extends BlockSource {
    public NativeBlockSource(long ptr, int refCntChange, Object stub) {
        super((int) ptr, false, false);
    }

    public NativeBlockSource(long pointer, boolean isFinalizable) {
        super(pointer, isFinalizable);
    }

    public NativeBlockSource(int dimension, boolean b1, boolean b2) {
        super(dimension, b1, b2);
    }

    public NativeBlockSource(int dimension) {
        super(dimension);
    }

    public static NativeBlockSource getDefaultForDimension(int dimension) {
        BlockSource region = BlockSource.getDefaultForDimension(dimension);
        return region != null ? new NativeBlockSource(region.getDimension(), false) : null;
    }

    public static NativeBlockSource getDefaultForActor(long actor) {
        BlockSource region = BlockSource.getDefaultForActor(actor);
        return region != null ? new NativeBlockSource(region.level.getId(), false) : null;
    }

    public static NativeBlockSource getCurrentClientRegion() {
        BlockSource region = BlockSource.getCurrentClientRegion();
        return region != null ? new NativeBlockSource(region.level.getId(), false) : null;
    }

    public static NativeBlockSource getCurrentWorldGenRegion() {
        BlockSource region = BlockSource.getCurrentWorldGenRegion();
        return region != null ? new NativeBlockSource(region.level.getId(), false) : null;
    }

    public BlockState getBlock(int x, int y, int z) {
        return new BlockState(getBlockIdDataAndState(x, y, z));
    }

    public BlockState getExtraBlock(int x, int y, int z) {
        return new BlockState(getExtraBlockIdDataAndState(x, y, z));
    }
}
