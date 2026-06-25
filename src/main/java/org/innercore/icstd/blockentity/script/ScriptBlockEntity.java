package org.innercore.icstd.blockentity.script;

import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.innercore.icstd.inventory.ItemContainer;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.innercore.icstd.blockentity.BlockEntityBase;
import org.innercore.icstd.blockentity.BlockEntityType;
import org.innercore.icstd.common.BlockPosAndSide;
import org.innercore.icstd.common.ChunkPos;
import org.innercore.icstd.common.Vec3f;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.innercore.icstd.component.ContainerComponent;
import org.innercore.icstd.component.DiscardObjectComponent;
import org.innercore.icstd.component.ItemUseListenerComponent;
import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.scene.FinalizeObjectComponent;
import org.innercore.icstd.scene.GameObjectHandle;
import org.innercore.icstd.script.ScriptObjectComponent;
import org.innercore.icstd.serialization.*;
import org.innercore.icstd.updatable.UpdatableComponent;
import org.innercore.icstd.world.DestroyBlockContext;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptBlockEntity extends BlockEntityBase implements SerializationMethodsComponent, UpdatableComponent, ContainerComponent, ItemUseListenerComponent, ScriptObjectComponent, FinalizeObjectComponent, DiscardObjectComponent {
    private final ScriptableObject scriptObject;
    private final Scriptable parentScope;

    private static final Object[] NO_ARGS = new Object[0];
    private final Object[] ONE_ARG = new Object[1];

    public ScriptBlockEntity(BlockEntityType blockEntityType, ScriptableObject scriptObject, boolean isServerSide) {
        super(blockEntityType, isServerSide);
        this.scriptObject = scriptObject;
        parentScope = scriptObject.getParentScope();
    }

    @Override
    public void createObject(GameObjectHandle handle) {
        super.createObject(handle);
        scriptObject.put("_objHandle", scriptObject, objHandle);
        scriptObject.put("blockSource", scriptObject, region);
        callMethod("_createdImpl", NO_ARGS);
        // this is a legacy callback only for script block entities, it is used to do some setup and
        // should be called as part of creation before init
        Callback.invokeAPICallback("TileEntityAdded", scriptObject, true);
    }

    @Override
    public void finalizeObject() {
        callMethod("finalized", NO_ARGS);
        // this is a legacy callback only for script block entities, it is meant as finalization
        Callback.invokeAPICallback("TileEntityRemoved", scriptObject);
    }

    @Override
    public void load() {
        super.load();
        updHandle.schedule(1).setRecurringSchedule(1);
        scriptObject.put("isLoaded", scriptObject, true);
        scriptObject.put("_handle", scriptObject, updHandle);
        callMethod("_loadImpl", NO_ARGS);
    }

    @Override
    public void unload() {
        super.unload();
        scriptObject.put("isLoaded", scriptObject, false);
        callMethod("unload", NO_ARGS);
    }

    @Override
    public void update(int dt) {
        ONE_ARG[0] = dt;
        callMethod("update", ONE_ARG);
    }

    private boolean isDestroyCalled = false;

    @Override
    protected void doDestroy(DestroyBlockContext ctx) {
        ONE_ARG[0] = ctx; // FIXME: DestroyBlockContext must use codegen
        if (!isDestroyCalled)
            callMethod("_destroyImpl", new Object[]{ctx, true});
        isDestroyCalled = true;
        super.doDestroy(ctx);
    }

    @Override
    public void destroy() {
        if (!isDestroyCalled)
            callMethod("_destroyImpl", new Object[]{null, false});
        isDestroyCalled = true;
        super.destroy();
    }

    @Override
    public void discard() {
        callMethod("discard", NO_ARGS);
    }

    private Object callMethod(String name, Object[] args) {
        Object fn = scriptObject.get(name, scriptObject);
        if (fn instanceof Function) {
            return ((Function) fn).call(Compiler.assureContextForCurrentThread(), parentScope, scriptObject, args);
        }
        return null;
    }

    @Override
    public String getSerializationTypeId() {
        return ObjectSerializer.getSerializerTypeIdFromObject(scriptObject);
    }

    @Override
    public ItemContainer getContainer() {
        Object o = scriptObject.get("container", scriptObject);
        if (o instanceof ItemContainer) {
            // FIXME: this won't work until ItemContainer is transferred to codegen
            return (ItemContainer) o;
        }
        return null;
    }

    @Override
    public void doPeriodicObjectCheck() {
        super.doPeriodicObjectCheck();
        int blockId = region.getBlockId(blockPos.x, blockPos.y, blockPos.z);
        if (blockId != type.getBlockId() && region.isChunkLoaded(new ChunkPos(blockPos))) {
            // scripts can override it
            int typeBlockId = ScriptableObjectHelper.getIntProperty(scriptObject, "blockID", type.getBlockId());
            if (blockId != typeBlockId) {
                doDestroy(new DestroyBlockContext());
            }
        }
        // onCheckerTick in script tile entities is never used, so don't try to call it
    }

    @Override
    public void onItemUse(BlockSource source, BlockPosAndSide pos, ItemInstance item, long playerUid, Vec3f exactPos) {
        callMethod("onItemClick", new Object[]{item.id, item.count, item.data, new GenericScriptPos3(pos), playerUid, item.extra});
    }

    @Override
    public void read(IScopedReader io, SerializationContext ctx, String typeId) {
        callMethod("_readImpl", new Object[]{ ScriptableObjectSerializer.readScriptable(io, ctx) });
    }

    @Override
    public void write(IScopedWriter io, SerializationContext ctx, String typeId) {
        ScriptableObjectSerializer.writeScriptable(io, callMethod("_writeImpl", NO_ARGS), ctx);
    }

    @Override
    public Object getScriptObject() {
        return scriptObject;
    }
}
