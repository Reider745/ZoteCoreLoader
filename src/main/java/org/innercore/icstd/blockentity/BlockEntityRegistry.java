package org.innercore.icstd.blockentity;

import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.BlockEntityRegistryBindGenBase;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.innercore.icstd.blockentity.script.ScriptBlockEntity;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.component.ComponentUtils;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.scene.GameObjectHandle;
import org.innercore.icstd.scene.GameObjectManager;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

@ApiBinding
public class BlockEntityRegistry extends BlockEntityRegistryBindGenBase {
    private static final HashMap<String, BlockEntityType> lookupByTypeName = new HashMap<>();
    private static final HashMap<Class<?>, BlockEntityType> lookupByClass = new HashMap<>();
    private static final HashMap<Integer, BlockEntityType> lookupByBlockId = new HashMap<>();

    public static BlockEntityType lookupType(Class<?> cls) {
        return lookupByClass.get(cls);
    }

    public static BlockEntityType lookupType(String typeName) {
        return lookupByTypeName.get(typeName);
    }

    public static BlockEntityType lookupType(int blockId) {
        return lookupByBlockId.get(blockId);
    }

    public static BlockEntityType registerTypeNoInit(String typeName, Class typeClass, int blockId, IBlockEntityFactory factory, boolean registerLookupByClass, boolean canFail, boolean silent) {
        if (lookupByTypeName.containsKey(typeName)) {
            String msg = "duplicate block entity factory registered for typeName=" + typeName + " existing=" + lookupByTypeName.get(typeName).factory + " new=" + factory;
            if (!silent) ICLog.e("BlockEntityRegistry", msg);
            if (!canFail) throw new IllegalArgumentException(msg);
            return null;
        }
        if (registerLookupByClass && lookupByClass.containsKey(typeClass)) {
            String msg = "duplicate block entity factory registered for typeClass=" + typeClass + " existing=" + lookupByClass.get(typeClass).factory + " new=" + factory;
            if (!silent) ICLog.e("BlockEntityRegistry", msg);
            if (!canFail) throw new IllegalArgumentException(msg);
            return null;
        }
        if (lookupByBlockId.containsKey(blockId)) {
            String msg = "duplicate block entity factory registered for blockId=" + blockId + " existing=" + lookupByBlockId.get(blockId).factory + " new=" + factory;
            if (!silent) ICLog.e("BlockEntityRegistry", msg);
            if (!canFail) throw new IllegalArgumentException(msg);
            return null;
        }
        BlockEntityType rec = new BlockEntityType();
        rec.typeName = typeName;
        rec.typeClass = typeClass;
        rec.blockId = blockId;
        rec.factory = factory;
        lookupByTypeName.put(typeName, rec);
        if (registerLookupByClass) lookupByClass.put(typeClass, rec);
        lookupByBlockId.put(blockId, rec);
        return rec;
    }

    public static<T extends BlockEntityComponent> BlockEntityType registerType(String typeName, Class<T> cls, int blockId, IBlockEntityFactory<T> factory, boolean registerLookupByClass, boolean canFail, boolean silent) {
        BlockEntityType rec = registerTypeNoInit(typeName, cls, blockId, factory, registerLookupByClass, canFail, silent);
        if (rec == null)
            return null;
        rec.initForClass();
        return rec;
    }

    public static<T extends BlockEntityComponent> BlockEntityType registerType(String typeName, Class<T> cls, int blockId, IBlockEntityFactory<T> factory) {
        return registerType(typeName, cls, blockId, factory, true, true, false);
    }

    @ApiBinding("registerType")
    public static boolean registerTypeForScript(int blockId, String typeName, String serializationTypeId, NetworkEntityType netEntityType, Function factory) {
        BlockEntityType type = registerTypeNoInit(typeName, ScriptBlockEntity.class, blockId, () -> {
            Object scriptObject = factory.call(Compiler.assureContextForCurrentThread(), factory.getParentScope(), factory.getParentScope(), new Object[0]);
            if (!(scriptObject instanceof ScriptableObject)) {
                throw new IllegalArgumentException("BlockEntityRegistry.registerScriptType: factory didn't return an object, got " + scriptObject);
            }
            return new ScriptBlockEntity(lookupType(typeName), (ScriptableObject) scriptObject, true);
        }, /* lookupByClass */ false, /* canFail */ true, /* silent */ false);
        if (type == null)
            return false;
        type.serializationTypeId = serializationTypeId;
        type.netEntityType = netEntityType;
        type.initForClass();
        return true;
    }

    @ApiBinding
    public static void addExistingScriptBlockEntityToServerScene(String typeName, ScriptableObject scriptObj, int dimensionId, int x, int y, int z) {
        ScriptBlockEntity blockEntity = new ScriptBlockEntity(lookupType(typeName), scriptObj, true);
        GameObjectHandle handle = GameObjectManager.getForDimension(dimensionId).create(blockEntity).placeAtBlockPos(x, y, z);
        ComponentUtils.safeCallCreateObjectFor(blockEntity, handle);
    }

    public static BlockEntityComponent createForBlock(int blockId) {
        BlockEntityType rec = lookupByBlockId.get(blockId);
        if (rec == null)
            return null;
        try {
            return rec.factory.createBlockEntity();
        } catch (Throwable err) {
            DialogHelper.reportNonFatalError("block entity factory " + rec.factory + " for blockId=" + blockId + " failed with exception", err);
            return null;
        }
    }

    public static BlockEntityComponent createForBlockAndPlaceAtScene(GameObjectManager scene, int blockId, BlockPos pos) {
        BlockEntityComponent blockEntity = createForBlock(blockId);
        if (blockEntity == null) {
            return null;
        }
        GameObjectHandle handle = scene.create(blockEntity).placeAtBlockPos(pos);
        ComponentUtils.safeCallCreateObjectFor(blockEntity, handle);
        return blockEntity;
    }

    public static BlockSource initBlockEntity(BlockEntityComponent blockEntity) {
        GameObjectManager scene = blockEntity.getObjectHandle().getScene();
        if (scene == null || scene.isDefaultScene()) {
            throw new IllegalStateException("invalid scene " + scene + " for block entity " + blockEntity);
        }
        if (scene.isServerSide()) {
            return BlockSource.getDefaultForDimension(scene.getDimensionId());
        } else {
            return BlockSource.getCurrentClientRegion();
        }
    }
}
