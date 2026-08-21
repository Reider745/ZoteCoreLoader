package com.zhekasmirnov.innercore.api.unlimited;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import com.reider745.api.ZoteOnly;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;

/**
 * Created by zheka on 22.08.2017.
 */

public class IDRegistry {
    public static final int ITEM_ID_OFFSET = 2048;
    public static final int BLOCK_ID_OFFSET = 8192;

    public static final int MAX_ID = 65536;

    private static final int MAX_UNAPPROVED_IDS = 750;
    private static final HashSet<String> approvedIds = new HashSet<>();
    private static int unapprovedIds = 0;

    private static final Int2ObjectMap<String> nameById = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<String> vanillaNameById = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<String> vanillaTileById = new Int2ObjectOpenHashMap<>();

    private static final HashMap<String, Integer> itemIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> blockIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> vanillaIdShortcut = new HashMap<>();
    private static final HashMap<String, String> fullIdToRegisteredId = new HashMap<>();
    private static final HashMap<String, ScriptableObject> namespaceIdShortcut = new HashMap<>();

    private static final ScriptableObject itemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject blockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaItemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaBlockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaTileIds = ScriptableObjectHelper.createEmpty();


    static {
        try {
            Map<String, Map<String, Integer>> scopedIdMap = VanillaIdConversionMap.getSingleton().loadScopedIdMapFromAssets();

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
                    fullIdToRegisteredId.put("minecraft:" + stringId, "!minecraft:" + stringId);
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
                    fullIdToRegisteredId.put("minecraft:" + stringId, "!minecraft:" + stringId);
                }
            }
        } catch (Exception e) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "Unable to read vanilla numeric IDs", e);
        }
    }

    static void putId(String name, int id) {
        int aliasOffset = name.indexOf(':');
        nameById.put(id, name);

        if (id >= BLOCK_ID_OFFSET) {
            blockIdShortcut.put(name, id);
            if (aliasOffset == -1) {
                String fullNameId = "minecraft:block_" + NativeAPI.convertNameId(name);

                fullIdToRegisteredId.put(fullNameId, name);
                blockIdShortcut.put(fullNameId, id);
            }
        } else if (id >= ITEM_ID_OFFSET) {
            itemIdShortcut.put(name, id);
            if (aliasOffset == -1) {
                String fullNameId = "minecraft:item_" + NativeAPI.convertNameId(name);
                fullIdToRegisteredId.put(fullNameId, name);
                itemIdShortcut.put(fullNameId, id);
            }
        }

        if (aliasOffset != -1) {
            if (!fullIdToRegisteredId.containsKey(name)) {
                fullIdToRegisteredId.put(name, name);
            }
        }
    }

    static boolean isOccupied(int id) {
        return nameById.containsKey(id)
                || vanillaNameById.containsKey(id)
                || vanillaTileById.containsKey(id);
    }

    public static int getScriptableItemId(String nameId) {
        return ScriptableObjectHelper.getIntProperty(itemIds, nameId, 0);
    }

    public static int getScriptableBlockId(String nameId) {
        return ScriptableObjectHelper.getIntProperty(blockIds, nameId, 0);
    }

    private static LinkedList<String> unapprovedBlocks = new LinkedList<>();
    private static int blockIdIterator = BLOCK_ID_OFFSET;
    private static boolean blockIdLooped = false;

    @JSStaticFunction
    public static int genBlockID(final String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "block string id " + name + " contains unicode characters, it will not be created", new RuntimeException());
            return 0;
        }

        int aliasOffset = name.indexOf(':');
        String preferredId = NativeAPI.convertNameId(name);
        if (aliasOffset != -1 && !name.equals(preferredId)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "namespaced block string id " + name + " should be formatted in snake_case to be registered (e.g. " + preferredId + ")", new RuntimeException());
            return 0;
        }

        String fullNameId = aliasOffset != -1 ? name : "minecraft:block_" + preferredId;
        String aliasedNameId = fullIdToRegisteredId.get(fullNameId);
        // check conflict with blocks
        if (itemIdShortcut.containsKey(fullNameId)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "block id " + name + " conflicts with item id " + fullNameId + " (" + aliasedNameId + "), it will not be created", new RuntimeException());
            return 0;
        }
        int numericId = 0;
        if (aliasedNameId != null && !aliasedNameId.equals(name)) {
            // check conflict with vanilla
            if (aliasedNameId.charAt(0) == '!') {
                ICLog.e(BlockRegistry.LOGGER_TAG, "block id " + name + " conflicts with vanilla id " + fullNameId + ", it will not be created", new RuntimeException());
                return 0;
            }
            // alias with other blocks
            if (blockIdShortcut.containsKey(fullNameId)) {
                numericId = blockIdShortcut.get(fullNameId);
                // if (!approvedIds.contains(fullNameId))
                //     ICLog.i(BlockRegistry.LOGGER_TAG, "block id " + name + " (" + fullNameId + ") is aliased to " + aliasedNameId + " (" + numericId + ")");
            }
        }

        approvedIds.add(fullNameId);
        if (blockIdShortcut.containsKey(name)) {
            numericId = blockIdShortcut.get(name);
        } else if (numericId == 0 && blockIdShortcut.containsKey(fullNameId)) {
            numericId = blockIdShortcut.get(fullNameId);
            putId(name, numericId);
        } else {
            if (numericId == 0) {
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
                numericId = blockIdIterator++;
            }
            putId(name, numericId);
        }

        String unescapedName = aliasOffset != -1 ? name.replace(":", "__") : name;
        if (aliasOffset != -1) {
            blockIds.put(name, blockIds, numericId);
        }
        blockIds.put(unescapedName, blockIds, numericId);
        if (aliasOffset != -1) {
            ScriptableObject namespaceScriptable = useNamespace(name.substring(0, aliasOffset));
            namespaceScriptable.put(name.substring(aliasOffset + 1), namespaceScriptable, numericId);
        }
        return numericId;
    }


    private static LinkedList<String> unapprovedItems = new LinkedList<>();
    private static int itemIdIterator = ITEM_ID_OFFSET;
    private static boolean itemIdLooped = false;

    @JSStaticFunction
    public static int genItemID(String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "item string id " + name + " contains unicode characters, it will not be created", new RuntimeException());
            return 0;
        }

        int aliasOffset = name.indexOf(':');
        String preferredId = NativeAPI.convertNameId(name);
        if (aliasOffset != -1 && !name.equals(preferredId)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "namespaced item string id " + name + " should be formatted in snake_case to be registered (e.g. " + preferredId + ")", new RuntimeException());
            return 0;
        }

        String fullNameId = aliasOffset != -1 ? name : "minecraft:item_" + preferredId;
        String aliasedNameId = fullIdToRegisteredId.get(fullNameId);
        // check conflict with items
        if (blockIdShortcut.containsKey(fullNameId)) {
            ICLog.e(BlockRegistry.LOGGER_TAG, "item id " + name + " conflicts with block id " + fullNameId + " (" + aliasedNameId + "), it will not be created", new RuntimeException());
            return 0;
        }
        int numericId = 0;
        if (aliasedNameId != null && !aliasedNameId.equals(name)) {
            // check conflict with vanilla
            if (aliasedNameId.charAt(0) == '!') {
                ICLog.e(BlockRegistry.LOGGER_TAG, "item id " + name + " conflicts with vanilla id " + fullNameId + ", it will not be created", new RuntimeException());
                return 0;
            }
            // alias with other items
            if (itemIdShortcut.containsKey(fullNameId)) {
                numericId = itemIdShortcut.get(fullNameId);
                // if (!approvedIds.contains(fullNameId))
                //     ICLog.i(BlockRegistry.LOGGER_TAG, "item id " + name + " (" + fullNameId + ") is aliased to " + aliasedNameId + " (" + numericId + ")");
            }
        }

        approvedIds.add(fullNameId);
        if (itemIdShortcut.containsKey(name)) {
            numericId = itemIdShortcut.get(name);
        } else if (numericId == 0 && itemIdShortcut.containsKey(fullNameId)) {
            numericId = itemIdShortcut.get(fullNameId);
            putId(name, numericId);
        } else {
            if (numericId == 0) {
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
                numericId = itemIdIterator++;
            }
            putId(name, numericId);
        }

        String unescapedName = aliasOffset != -1 ? name.replace(":", "__") : name;
        if (aliasOffset != -1) {
            itemIds.put(name, itemIds, numericId);
        }
        itemIds.put(unescapedName, itemIds, numericId);
        if (aliasOffset != -1) {
            ScriptableObject namespaceScriptable = useNamespace(name.substring(0, aliasOffset));
            namespaceScriptable.put(name.substring(aliasOffset + 1), namespaceScriptable, numericId);
        }
        return numericId;
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
            return idAndType.split(":", 2)[0];
        }
        return null;
    }

    @JSStaticFunction
    public static String getStringIdForItemId(int id) {
        String idAndType = NativeAPI.getStringIdAndTypeForIntegerId(id);
        if (idAndType != null) {
            try {
                return idAndType.split(":", 2)[1].split("#")[0];
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return null;
    }

    public static String getFullBlockNameId(String name) {
        int aliasOffset = name.indexOf(':');
        if (aliasOffset == -1) {
            name = "minecraft:block_" + NativeAPI.convertNameId(name);
        }
        return name;
    }

    @ZoteOnly

    @Deprecated
    public static int getIdByNameId(String id) {
        if (id.startsWith("item_")) {
            int numericId = CustomItem.getIdForText(id);
            if (numericId != 0) return numericId;
        }
        return CustomBlock.getIdForText(id);
    }

    public static String getFullItemNameId(String name) {
        int aliasOffset = name.indexOf(':');
        if (aliasOffset == -1) {
            name = "minecraft:item_" + NativeAPI.convertNameId(name);
        }
        return name;
    }

    public static int getCustomBlockIdByName(String name) {
        name = getFullBlockNameId(name);
        if (blockIdShortcut.containsKey(name)) {
            return blockIdShortcut.get(name);
        }
        return 0;
    }

    @ZoteOnly
    @Deprecated
    public static String getStringIdAndTypeForVanillaId(int id) {
        return vanillaTileById.containsKey(id > 255 ? 255 - id : id)
                ? "block:" + vanillaTileById.get(id > 255 ? 255 - id : id)
                : vanillaNameById.containsKey(id) ? "item:" + vanillaNameById.get(id) : null;
    }

    public static int getCustomItemIdByName(String name) {
        name = getFullItemNameId(name);
        if (itemIdShortcut.containsKey(name)) {
            return itemIdShortcut.get(name);
        }
        return 0;
    }

    @JSStaticFunction
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

    @JSStaticFunction
    public static ScriptableObject useNamespace(String namespace) {
        if (namespaceIdShortcut.containsKey(namespace)) {
            return namespaceIdShortcut.get(namespace);
        }
        ScriptableObject scriptable = new ScriptableObject() {
            @Override
            public String getClassName() {
                return "IDRegistryNamespace";
            }
        };
        namespaceIdShortcut.put(namespace, scriptable);
        return scriptable;
    }

    static JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            JSONObject blocks = new JSONObject();
            JSONObject items = new JSONObject();

            for (String name : blockIdShortcut.keySet()) {
                if (name.indexOf(':') != -1) {
                    blocks.put(name, blockIdShortcut.get(name));
                    if (!approvedIds.contains(name)) {
                        unapprovedItems.add(name);
                        unapprovedIds++;
                    }
                }
            }

            for (String name : itemIdShortcut.keySet()) {
                if (name.indexOf(':') != -1) {
                    items.put(name, itemIdShortcut.get(name));
                    if (!approvedIds.contains(name)) {
                        unapprovedBlocks.add(name);
                        unapprovedIds++;
                    }
                }
            }

            if (unapprovedIds > MAX_UNAPPROVED_IDS) {
                ICLog.d(BlockRegistry.LOGGER_TAG, "too many unused IDs, clearing...");
                for (String name: unapprovedItems) {
                    items.remove(name);
                }

                for (String name: unapprovedBlocks) {
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
                            int numericId = blocks.optInt(key);
                            String fullNameId = getFullBlockNameId(key);
                            if (blockIdShortcut.containsKey(fullNameId)) {
                                int aliasedId = blockIdShortcut.get(fullNameId);
                                ICLog.i(BlockRegistry.LOGGER_TAG, "loaded block id " + key + "=" + numericId + " is aliased with " + getNameByID(aliasedId) + "=" + aliasedId + " fullId=" + fullNameId);
                                numericId = aliasedId;
                            }
                            putId(key, numericId);
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
                            int numericId = items.optInt(key);
                            String fullNameId = getFullItemNameId(key);
                            if (itemIdShortcut.containsKey(fullNameId)) {
                                int aliasedId = itemIdShortcut.get(fullNameId);
                                ICLog.i(BlockRegistry.LOGGER_TAG, "loaded item id " + key + "=" + numericId + " is aliased with " + getNameByID(aliasedId) + "=" + aliasedId + " fullId=" + fullNameId);
                                numericId = aliasedId;
                            }
                            putId(key, numericId);
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
