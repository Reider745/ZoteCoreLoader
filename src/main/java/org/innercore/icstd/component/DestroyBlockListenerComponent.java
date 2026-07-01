package org.innercore.icstd.component;

import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.world.DestroyBlockContext;

public interface DestroyBlockListenerComponent {
    void onBlockDestroyed(BlockSource region, BlockPos pos, DestroyBlockContext ctx);
}
