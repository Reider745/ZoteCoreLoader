package com.zhekasmirnov.innercore.api.unlimited;

import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by zheka on 22.08.2017.
 */

public class IDRegistry {
    public static final int ITEM_ID_OFFSET = 2048;
    public static final int BLOCK_ID_OFFSET = 8192;

    public static final int MAX_ID = 65536;

    private static final int MAX_UNAPPROVED_IDS = 750;
    private static final String TYPE_BLOCK = "block";
    private static final String TYPE_ITEM = "item";

    private static int unapprovedIds = 0;

    private static final HashMap<Integer, String> nameById = new HashMap<>();
    private static final HashMap<Integer, String> vanillaNameById = new HashMap<>();
    private static final HashMap<Integer, String> vanillaTileById = new HashMap<>();
    private static final HashMap<String, Boolean> approvedIds = new HashMap<>();

    private static final HashMap<String, Integer> itemIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> blockIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> vanillaIdShortcut = new HashMap<>();

    private static final ScriptableObject itemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject blockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaItemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaBlockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaTileIds = ScriptableObjectHelper.createEmpty();

    static {
        try {
            Map<String, Map<String, Integer>> scopedIdMap = VanillaIdConversionMap.getSingleton()
                    .loadScopedIdMapFromAssets();

            Map<String, Integer> blocksMap = scopedIdMap.get("blocks");
            Map<String, Integer> itemsMap = scopedIdMap.get("items");

            if (itemsMap != null) {
                for (Map.Entry<String, Integer> nameAndId : itemsMap.entrySet()) {
                    String stringId = nameAndId.getKey();
                    int numericId = nameAndId.getValue();
                    vanillaNameById.put(numericId, stringId);
                    vanillaIdShortcut.put(stringId, numericId);
                    if (blocksMap != null && blocksMap.containsKey(stringId)) {
                        vanillaBlockIds.put(stringId, vanillaBlockIds, numericId);
                    } else {
                        vanillaItemIds.put(stringId, vanillaItemIds, numericId);
                    }
                }
            }

            if (blocksMap != null) {
                for (Map.Entry<String, Integer> nameAndId : blocksMap.entrySet()) {
                    String stringId = nameAndId.getKey();
                    int numericId = nameAndId.getValue();
                    vanillaTileIds.put(stringId, vanillaTileIds, numericId);
                    vanillaTileById.put(numericId, stringId);

                    if (numericId > 255) {
                        numericId = 255 - numericId;
                    }
                    if (!vanillaBlockIds.has(stringId, vanillaBlockIds) && !vanillaNameById.containsKey(numericId)) {
                        vanillaBlockIds.put(stringId, vanillaBlockIds, numericId);
                        vanillaNameById.put(numericId, stringId);
                        vanillaIdShortcut.put(stringId, numericId);
                    }
                }
            }

        } catch (Exception e) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "Unable to read vanilla numeric IDs", e);
        }
    }

    static void approve(String name, String type) {
        approvedIds.put(type + "$" + name, true);
    }

    static boolean isApproved(String name, String type) {
        return approvedIds.containsKey(type + "$" + name);
    }

    static void putId(String name, int id) {
        if (id >= BLOCK_ID_OFFSET) {
            blockIdShortcut.put(name, id);
            blockIds.put(name, blockIds, id);
        } else if (id >= ITEM_ID_OFFSET) {
            itemIdShortcut.put(name, id);
            itemIds.put(name, itemIds, id);
        }

        nameById.put(id, name);
    }

    public static int getIdByNameId(String id) {
        if (id.startsWith("item_")) {
            return CustomItem.getIdForText(id);
        }
        return CustomBlock.getIdForText(id);
    }

    static boolean isOccupied(int id) {
        return nameById.containsKey(id)
                || vanillaNameById.containsKey(id)
                || vanillaTileById.containsKey(id);
    }

    private static LinkedList<String> unapprovedBlocks = new LinkedList<>();
    private static int blockIdIterator = BLOCK_ID_OFFSET;
    private static boolean blockIdLooped = false;

    @JSStaticFunction
    public static int genBlockID(String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG,
                    "block string id " + name + " contains unicode characters, it will not be created",
                    new RuntimeException());
            return 0;
        }

        if (vanillaNameById.values().contains("block_" + name) || vanillaTileById.values().contains(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG,
                    "block string id " + name + " is a vanilla string ID, so the item won't be created",
                    new RuntimeException());
            return 0;
        }

        approve(name, TYPE_BLOCK);
        if (blockIdShortcut.containsKey(name)) {
            return blockIdShortcut.get(name);
        }

        while (isOccupied(blockIdIterator)) {
            blockIdIterator++;
            if (blockIdIterator > MAX_ID) {
                if (blockIdLooped) {
                    throw new RuntimeException("ID LIMIT EXCEEDED while registring block string id " + name);
                } else {
                    blockIdLooped = true;
                    blockIdIterator = 0;
                }
            }
        }

        putId(name, blockIdIterator);
        return blockIdIterator++;
    }

    private static LinkedList<String> unapprovedItems = new LinkedList<>();
    private static int itemIdIterator = ITEM_ID_OFFSET;
    private static boolean itemIdLooped = false;

    @JSStaticFunction
    public static int genItemID(String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG,
                    "item string id " + name + " contains unicode characters, it will not be created",
                    new RuntimeException());
            return 0;
        }

        if (vanillaNameById.values().contains("item_" + name) || vanillaTileById.values().contains(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG,
                    "item string id " + name + " is a vanilla string ID, so the item won't be created",
                    new RuntimeException());
            return 0;
        }

        approve(name, TYPE_ITEM);
        if (itemIdShortcut.containsKey(name)) {
            return itemIdShortcut.get(name);
        }

        while (isOccupied(itemIdIterator)) {
            itemIdIterator++;
            if (itemIdIterator > MAX_ID) {
                if (itemIdLooped) {
                    throw new RuntimeException("ID LIMIT EXCEEDED while registring item string id " + name);
                } else {
                    itemIdLooped = true;
                    itemIdIterator = 0;
                }
            }
        }

        putId(name, itemIdIterator);
        return itemIdIterator++;
    }

    @JSStaticFunction
    public static String getNameByID(int id) {
        return nameById.get(id);
    }

    @JSStaticFunction
    public static String getStringIdAndTypeForItemId(int id) {
        return NativeAPI.getStringIdAndTypeForIntegerId(id);
    }

    @JSStaticFunction
    public static String getTypeForItemId(int id) {
        String idAndType = NativeAPI.getStringIdAndTypeForIntegerId(id);
        if (idAndType != null) {
            return idAndType.split(":")[0];
        } else {
            return null;
        }
    }

    @JSStaticFunction
    public static String getStringIdForItemId(int id) {
        String idAndType = NativeAPI.getStringIdAndTypeForIntegerId(id);
        if (idAndType != null) {
            try {
                return idAndType.split(":")[1].split("#")[0];
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getIDByName(String name) {
        if (vanillaIdShortcut.containsKey(name)) {
            return vanillaIdShortcut.get(name);
        }
        return 0;
    }

    @JSStaticFunction
    public static boolean isVanilla(int id) {
        return id == 0 || vanillaNameById.containsKey(id) || vanillaTileById.containsKey(id);
    }

    public static String getStringIdAndTypeForVanillaId(int id) {
        return vanillaTileById.containsKey(id > 255 ? 255 - id : id)
                ? "block:" + vanillaTileById.get(id > 255 ? 255 - id : id)
                : vanillaNameById.containsKey(id) ? "item:" + vanillaNameById.get(id) : null;
    }

    @JSStaticFunction
    public static int ensureBlockId(int id) {
        if ((vanillaNameById.containsKey(id) || vanillaTileById.containsKey(id + 255)) && id < 0) {
            return id + 255;
        } else {
            return id;
        }
    }

    @JSStaticFunction
    public static int ensureItemId(int id) {
        if (vanillaTileById.containsKey(id) && id > 255) {
            return 255 - id;
        } else {
            return id;
        }
    }

    static JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            JSONObject blocks = new JSONObject();
            JSONObject items = new JSONObject();

            for (String name : blockIdShortcut.keySet()) {
                blocks.put(name, blockIdShortcut.get(name));
                if (!isApproved(name, TYPE_BLOCK)) {
                    unapprovedItems.add(name);
                    unapprovedIds++;
                }
            }

            for (String name : itemIdShortcut.keySet()) {
                items.put(name, itemIdShortcut.get(name));
                if (!isApproved(name, TYPE_ITEM)) {
                    unapprovedBlocks.add(name);
                    unapprovedIds++;
                }
            }

            if (unapprovedIds > MAX_UNAPPROVED_IDS) {
                ICLog.d(BlockRegistry.LOGGER_TAG, "too many unused IDs, clearing...");
                for (String name : unapprovedItems) {
                    items.remove(name);
                }

                for (String name : unapprovedBlocks) {
                    blocks.remove(name);
                }
            }

            obj.put("blocks", blocks);
            obj.put("items", items);
            return obj;
        } catch (Exception e) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "failed to save string id bindings", e);
        }
        return null;
    }

    static void fromJson(JSONObject obj) {
        JSONObject blocks = obj.optJSONObject("blocks");
        JSONObject items = obj.optJSONObject("items");
        try {
            if (blocks != null) {
                JSONArray keys = blocks.names();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String key = keys.optString(i);
                        if (key != null) {
                            putId(key, blocks.optInt(key));
                        }
                    }
                }
            }

            if (items != null) {
                JSONArray keys = items.names();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String key = keys.optString(i);
                        if (key != null) {
                            putId(key, items.optInt(key));
                        }
                    }
                }
            }
        } catch (Exception e) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "failed to load string id bindings", e);
        }
    }

    public static void injectAPI(ScriptableObject scope) {
        scope.put("BlockID", scope, blockIds);
        scope.put("ItemID", scope, itemIds);
        scope.put("VanillaItemID", scope, vanillaItemIds);
        scope.put("VanillaBlockID", scope, vanillaBlockIds);
        scope.put("VanillaTileID", scope, vanillaTileIds);
    }

    public static void rebuildNetworkIdMap() {
        IdConversionMap map = IdConversionMap.getSingleton();
        map.registerIdsFromMap("item", itemIdShortcut);
        map.registerIdsFromMap("block", blockIdShortcut);
    }
}
