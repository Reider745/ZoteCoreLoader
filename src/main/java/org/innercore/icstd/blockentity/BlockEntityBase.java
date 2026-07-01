package org.innercore.icstd.blockentity;

import org.innercore.icstd.inventory.ItemContainer;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.ChunkPos;
import org.innercore.icstd.component.ContainerComponent;
import org.innercore.icstd.component.DestroyBlockListenerComponent;
import org.innercore.icstd.component.DestroyObjectComponent;
import org.innercore.icstd.component.InitObjectComponent;
import org.innercore.icstd.scene.CreateObjectComponent;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.scene.GameObjectHandle;
import org.innercore.icstd.scene.LoadableComponent;
import org.innercore.icstd.scene.PeriodicObjectCheckComponent;
import org.innercore.icstd.serialization.ISerializableObject;
import org.innercore.icstd.updatable.UpdatableComponent;
import org.innercore.icstd.updatable.UpdatableHandle;
import org.innercore.icstd.updatable.UpdatableScheduler;
import org.innercore.icstd.world.DestroyBlockContext;

public class BlockEntityBase implements BlockEntityComponent, CreateObjectComponent, LoadableComponent, ISerializableObject, PeriodicObjectCheckComponent, DestroyBlockListenerComponent, DestroyObjectComponent, InitObjectComponent {
    public final boolean isServerSide;

    protected GameObjectHandle objHandle;
    protected UpdatableHandle updHandle;
    protected BlockSource region;
    protected BlockPos blockPos;
    // protected NetworkEntity netEntity;

    protected final BlockEntityType type;
    private boolean isLoaded = false;

    @Override
    public String getSerializationTypeId() {
        return type.serializationTypeId;
    }

    protected BlockEntityBase(BlockEntityType type, boolean isServerSide) {
        this.isServerSide = isServerSide;
        this.type = type;
    }

    protected BlockEntityBase(boolean isServerSide) {
        this.isServerSide = isServerSide;
        type = BlockEntityRegistry.lookupType(getClass());
        if (type == null) {
            throw new NullPointerException("no block entity type registered for class " + getClass());
        }
    }

    @Override
    public void createObject(GameObjectHandle handle) {
        objHandle = handle;
        region = BlockEntityRegistry.initBlockEntity(this);
        blockPos = handle.getBlockPos();
        if (blockPos == null)
            throw new IllegalStateException("block pos is null for block entity " + this + " handle=" + handle);
    }

    @Override
    public void init() {
        /* if (type.netEntityType != null) {
            netEntity = new NetworkEntity(type.netEntityType);
        } */
    }

    @Override
    public void destroy() {
        if (this instanceof ContainerComponent) {
            ItemContainer container = ((ContainerComponent) this).getContainer();
            if (container != null) {
                container.close();
                container.removeEntity();
            }
        }
        /* if (netEntity != null) {
            netEntity.remove();
            netEntity = null;
        } */
    }

    protected void doDestroy(DestroyBlockContext ctx) {
        if (updHandle != null) {
            updHandle.destroy();
        }
        objHandle.destroyNow();
    }

    @Override
    public void doPeriodicObjectCheck() {
        int blockId = region.getBlockId(blockPos.x, blockPos.y, blockPos.z);
        if (blockId != type.blockId && region.isChunkLoaded(new ChunkPos(blockPos))) {
            doDestroy(new DestroyBlockContext());
        }
    }

    @Override
    public void onBlockDestroyed(BlockSource region, BlockPos pos, DestroyBlockContext ctx) {
        doDestroy(ctx);
    }

    @Override
    public void load() {
        isLoaded = true;
        if (this instanceof UpdatableComponent && updHandle == null) {
            updHandle = (isServerSide ? UpdatableScheduler.createOnServer((UpdatableComponent) this) : UpdatableScheduler.createOnClient((UpdatableComponent) this));
            updHandle.schedule(1); // tick once after load to ensure correct DT
        }
    }

    @Override
    public void unload() {
        isLoaded = false;
        if (updHandle != null) {
            updHandle.destroy();
            updHandle = null;
        }
    }

    @Override
    public GameObjectHandle getObjectHandle() {
        return objHandle;
    }

    @Override
    public BlockEntityType getBlockEntityType() {
        return type;
    }

    public BlockSource getRegion() {
        return region;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public BlockEntityType getType() {
        return type;
    }
}
