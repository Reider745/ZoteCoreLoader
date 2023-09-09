package com.zhekasmirnov.apparatus.multiplayer.mod;

import com.zhekasmirnov.apparatus.cpp.NativeIdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IdConversionMap {
    public static void loadClass() {
        // forces class to load and register listeners
    }

    static {
        Network.getSingleton().addServerInitializationPacket("system.id_map",
                client -> getSingleton().localMapAsJson(),
                (JSONObject data, String meta) -> {
                    getSingleton().updateConversionMap(data);
                    // NativeIdPlaceholderGenerator.rebuildFromServerPacket(data);
                });
    }

    private static final IdConversionMap singleton = new IdConversionMap();

    public static IdConversionMap getSingleton() {
        return singleton;
    }


    private final Map<String, Integer> localIdMap = new HashMap<>();
    private final Map<Integer, Integer> fromLocalToServerMap = new HashMap<>();
    private final Map<Integer, Integer> fromServerToLocalMap = new HashMap<>();

    private JSONObject localMapAsJson() {
        return new JSONObject(localIdMap);
    }

    private void updateConversionMap(JSONObject json) {
        fromLocalToServerMap.clear();
        fromServerToLocalMap.clear();
        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String name = it.next();
            int serverId = json.optInt(name);
            if (serverId != 0 && localIdMap.containsKey(name)) {
                int localId = localIdMap.get(name);
                fromServerToLocalMap.put(serverId, localId);
                fromLocalToServerMap.put(localId, serverId);
            }
        }
        rebuildNativeConversionMap();
    }

    public void clearLocalIdMap() {
        localIdMap.clear();
    }

    public void registerId(String name, int id) {
        localIdMap.put(name, id);
    }

    public void unregisterId(String name) {
        localIdMap.remove(name);
    }

    public void registerIdsFromMap(String namespace, Map<String, Integer> name) {
        for (Map.Entry<String, Integer> entry : name.entrySet()) {
            registerId((namespace != null ? namespace + ":" : "") + entry.getKey(), entry.getValue());
        }
    }

    public void rebuildNativeConversionMap() {
        /*NativeIdConversionMap.clearAll();
        for (Map.Entry<Integer, Integer> entry : fromLocalToServerMap.entrySet()) {
            NativeIdConversionMap.mapConversion(entry.getKey(), entry.getValue());
        }*/
    }


    public static int localToServer(int id) {
        return Java8BackComp.getOrDefault(getSingleton().fromLocalToServerMap, id, id);
    }

    public static int serverToLocal(int id) {
        return Java8BackComp.getOrDefault(getSingleton().fromServerToLocalMap, id, id);
    }
}
