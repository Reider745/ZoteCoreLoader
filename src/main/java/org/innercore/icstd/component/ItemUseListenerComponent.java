package org.innercore.icstd.component;

import org.innercore.icstd.common.BlockPosAndSide;
import org.innercore.icstd.common.Vec3f;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.region.BlockSource;

public interface ItemUseListenerComponent {
    void onItemUse(BlockSource source, BlockPosAndSide pos, ItemInstance item, long playerUid, Vec3f exactPos);
}
