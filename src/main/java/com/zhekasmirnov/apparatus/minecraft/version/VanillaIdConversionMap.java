package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.apparatus.cpp.NativeVanillaIdConversionMap;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VanillaIdConversionMap {
    private static final VanillaIdConversionMap singleton = new VanillaIdConversionMap();

    public static VanillaIdConversionMap getSingleton() {
        return singleton;
    }


    private VanillaIdConversionMap() {

    }

    private boolean tryLoadFromAsset(String asset, Map<String, Map<String, Integer>> scopedIdMap, boolean override) {
        try {
            JSONObject json = FileTools.getAssetAsJSON(asset);
            loadJsonIntoMap(json, scopedIdMap, override);
            ICLog.d("VanillaIdConversionMap", "loaded ids from asset " + asset);
            return true;
        } catch (JSONException | NullPointerException e) {
            return false;
        }
    }

    public Map<String, Map<String, Integer>> loadScopedIdMapFromAssets() {
        Map<String, Map<String, Integer>> scopedIdMap = new HashMap<>();

        tryLoadFromAsset("innercore/id/numeric_ids.json", scopedIdMap, false); // main
        int index = 0;
        while (tryLoadFromAsset("innercore/id/numeric_ids_override_" + index + ".json", scopedIdMap, false)) {
            index++;
        }
        tryLoadFromAsset("innercore/numeric_ids.json", scopedIdMap, false); // legacy main
        fixMissingItemIds(scopedIdMap);

        return scopedIdMap;
    }

    public void reloadFromAssets() {
        reloadFrom(loadScopedIdMapFromAssets());
    }

    private void fixMissingItemIds(Map<String, Map<String, Integer>> scopedIdMap){
        Map<String, Integer> scopeBlocks = scopedIdMap.get("blocks");
        Map<String, Integer> scopeItems = scopedIdMap.get("items");

        if(scopeBlocks == null || scopeItems == null)
            return;

        for(Map.Entry<String, Integer> entry: scopeBlocks.entrySet()){
            int blockId = entry.getValue();

            String key = entry.getKey();
            Integer itemId = scopeItems.get(key);
            int newItemId = blockId > 255? 255 - blockId: blockId;
            if(itemId == null || itemId != newItemId){
                if(!scopeItems.containsValue(newItemId)){
                    if(!scopeItems.containsKey(key)){
                        scopeItems.put(key, newItemId);
                    } else {
                        // NOT REQUIRED ON RELEASE
                        // String message = String.format("Scope already contains item id %d for block \"%s\", tried to assign id %d", 
                        //     itemId, key, newItemId);
                        // Logger.warning(message);
                    }                    
                } else {
                    // NOT REQUIRED ON RELEASE
                    // String existingKey = null;
                    // for (Map.Entry<String, Integer> entry1 : scopeItems.entrySet()) {
                    //     if (entry1.getValue().equals(newItemId)) {
                    //         existingKey = entry1.getKey();
                    //     }
                    // }
                    // String message = String.format("Scope already contains item id %d for block \"%s\", tried to assign it to block \"%s\" (current numeric id: %d)", 
                    //         newItemId, existingKey, key, itemId);
                    // Logger.warning(message);
                }
            }
        }
    }

    public void loadJsonIntoMap(JSONObject json, Map<String, Map<String, Integer>> scopedIdMap, boolean override) {
        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject scopeJson = json.optJSONObject(key);
            if (scopeJson != null) {
                Map<String, Integer> scope = Java8BackComp.computeIfAbsent(scopedIdMap, key, key0 -> new HashMap<String, Integer>());
                for (Iterator<String> iter = scopeJson.keys(); iter.hasNext(); ) {
                    String name = iter.next();
                    int id = scopeJson.optInt(name);
                    if (id != 0) {
                        if (override || !scope.containsKey(name)) {
                            scope.put(name, id);
                        }
                    }
                }
            }
        }
    }

    public synchronized void reloadFrom(Map<String, Map<String, Integer>> scopedIdMap) {
        if (!MinecraftVersions.getCurrent().isFeatureSupported(MinecraftVersion.FEATURE_VANILLA_ID_MAPPING)) {
            ICLog.d("VanillaIdConversionMap", "vanilla id remapping is not required on this version of the game");
            return;
        }

        // do not clear vanilla ids, because they are consumed only once
        // NativeVanillaIdConversionMap.clearAll();
        for (Map.Entry<String, Map<String, Integer>> scope : scopedIdMap.entrySet()) {
            String scopeName = scope.getKey();
            for (Map.Entry<String, Integer> nameAndId : scope.getValue().entrySet()) {
                String name = nameAndId.getKey();
                int id = nameAndId.getValue();
                switch (scopeName) {
                    case "blocks":
                        NativeVanillaIdConversionMap.addBlockId(name, id);
                        break;
                    case "items":
                        NativeVanillaIdConversionMap.addItemId(name, id);
                        break;
                    default:
                        // unknown, just ignore
                        break;
                }
            }
        }
    }
}
