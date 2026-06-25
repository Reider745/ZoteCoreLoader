package org.innercore.icstd.blockentity;

import org.innercore.icstd.scene.GameObjectHandle;

public interface BlockEntityComponent {
    BlockEntityType getBlockEntityType();
    GameObjectHandle getObjectHandle();
}
