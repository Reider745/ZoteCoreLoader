package org.innercore.icstd.blockentity;

public interface IBlockEntityFactory<T extends BlockEntityComponent> {
    T createBlockEntity();
}
