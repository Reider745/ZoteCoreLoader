package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeBlock;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 08.08.2017.
 */

public class BlockRegistry {
    public static final String LOGGER_TAG = "INNERCORE-BLOCKS";

    private static FileLoader loader;
    private static HashMap<IDDataPair, BlockVariant> blockVariantMap = new HashMap<>();

    public static void onInit() {
        ICLog.d(LOGGER_TAG, "reading saved mappings...");
        loader = new FileLoader(ModPackContext.getInstance().getCurrentModPack()
                .getRequestHandler(ModPackDirectory.DirectoryType.CONFIG).get("innercore", "ids.json"));
    }

    public static void onModsLoaded() {
        Callback.invokeAPICallback("PreBlocksDefined");

        loader.save();

        Callback.invokeAPICallback("BlocksDefined");
        ICLog.d(LOGGER_TAG, "complete");
    }

    private static void addBlockVariants(int uid, String inputNameId, NativeBlock block,
            ScriptableObject variantsScriptable) {
        int data = 0;
        Object[] keys = variantsScriptable.getAllIds();

        if (keys.length == 0) {
            throw new IllegalArgumentException("no variants found in variant array while creating block " + inputNameId
                    + ", variants must be formatted as [{name: 'name', textures:[['name', index], ...], inCreative: true/false}, ...]");
        }

        for (Object key : keys) {
            Object _val = null;
            if (key instanceof Integer) {
                _val = variantsScriptable.get((int) key, variantsScriptable);
            }
            if (key instanceof String) {
                _val = variantsScriptable.get((String) key, variantsScriptable);
            }
            if (_val != null && _val instanceof ScriptableObject) {
                BlockVariant variant = new BlockVariant(uid, data++, (ScriptableObject) _val);
                if (variant.name != null) {
                    block.addVariant(variant.name, variant.textures, variant.textureIds);
                } else {
                    block.addVariant(variant.textures, variant.textureIds);
                }
                if (variant.inCreative) {
                    NativeItem.addToCreative(uid, 1, data - 1, null);
                }
                blockVariantMap.put(new IDDataPair(uid, variant.data), variant);
            }
        }
    }

    public static void createBlock(int uid, String nameId, ScriptableObject variantsScriptable, SpecialType type) {
        if (!IDRegistry.getNameByID(uid).equals(nameId)) {
            throw new IllegalArgumentException(
                    "numeric uid " + uid + IDRegistry.getNameByID(uid) + " doesn't match string id " + nameId);
        }

        if (IDRegistry.isVanilla(uid)) {
            ICLog.e(LOGGER_TAG, "cannot create block with vanilla id " + uid, new RuntimeException());
            return;
        }

        NativeBlock block = NativeBlock.createBlock(uid, NativeAPI.convertNameId(nameId), "blank", 0);
        addBlockVariants(uid, nameId, block, variantsScriptable);
        type.setupBlock(uid);
    }

    public static void createLiquidBlockPair(int id1, String nameId1, int id2, String nameId2,
            ScriptableObject variantsScriptable, SpecialType type, int tickDelay, boolean isRenewable) {
        NativeBlock[] blocks = NativeBlock.createLiquidBlock(id1, NativeAPI.convertNameId(nameId1), id2,
                NativeAPI.convertNameId(nameId2), "blank", 0, tickDelay, isRenewable);
        for (NativeBlock block : blocks) {
            addBlockVariants(block.getId(), "liquid pair: " + nameId1 + ", " + nameId2, block, variantsScriptable);
            type.setupBlock(block.getId());
        }
    }

    public static void createBlock(int uid, String nameId, ScriptableObject variants) {
        createBlock(uid, nameId, variants, SpecialType.DEFAULT);
    }

    public static void setShape(int uid, int data, float x1, float y1, float z1, float x2, float y2, float z2) {
        BlockShape shape = new BlockShape(x1, y1, z1, x2, y2, z2);
        shape.setToBlock(uid, data);
        BlockVariant variant = getBlockVariant(uid, data);
        if (variant != null) {
            variant.shape = shape;
        }
    }

    public static BlockVariant getBlockVariant(int uid, int data) {
        IDDataPair key = new IDDataPair(uid, data);
        return blockVariantMap.get(key);
    }
}
