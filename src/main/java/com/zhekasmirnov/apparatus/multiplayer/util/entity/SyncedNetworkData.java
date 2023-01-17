package com.zhekasmirnov.apparatus.multiplayer.util.entity;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.*;

/**
 * This object must be created on both server and client side with same name, after
 * this it will provide access to data, synced between server and all added clients
 */
public class SyncedNetworkData implements ConnectedClientList.Listener {
    private static final Map<String, SyncedNetworkData> serverSyncedData = new HashMap<>();
    private static final Map<String, SyncedNetworkData> receivedSyncedData = new HashMap<>();

    @JSStaticFunction
    public static SyncedNetworkData getClientSyncedData(String name) {
        return Java8BackComp.computeIfAbsent(receivedSyncedData, name, key -> new SyncedNetworkData(name, false));
    }

    static {
        Network.getSingleton().addClientPacket("system.synced_data.data", (JSONObject map, String meta) -> {
            if (map.length() > 0) {
                SyncedNetworkData data = getClientSyncedData(meta);
                for (Iterator<String> it = map.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        data.put(key, map.get(key), true);
                    } catch (Throwable err) {
                        ICLog.e("INNERCORE", "failed to run synced data listener", err);
                    }
                }
            }
        }, null);

        Network.getSingleton().addServerPacket("system.synced_data.data", (ConnectedClient client, JSONObject map, String meta) -> {
            SyncedNetworkData data = serverSyncedData.get(meta);
            if (data != null && map.length() > 0) {
                for (Iterator<String> it = map.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        Object val = map.get(key);
                        DataVerifier verifier = data.verifierMap.get(key);
                        if (verifier == null) {
                            verifier = data.globalVerifier;
                        }
                        if (verifier != null) {
                            try {
                                val = verifier.verify(key, val);
                            } catch (Throwable err) {
                                ICLog.e("INNERCORE", "failed to run synced data verifier for key " + key, err);
                            }
                            if (val == null) {
                                continue;
                            }
                        }
                        try {
                            data.put(key, map.get(key), true);
                        } catch (Throwable err) {
                            ICLog.e("INNERCORE", "failed to run synced data listener", err);
                        }
                    } catch (JSONException ignore) {
                    }
                }
                data.apply();
            }
        }, null);

        Network.getSingleton().addServerShutdownListener(server -> serverSyncedData.clear());
        Network.getSingleton().addClientShutdownListener(reason -> receivedSyncedData.clear());
    }

    @Override
    public void onAdd(ConnectedClient client) {
        client.send("system.synced_data.data#" + name, new JSONObject(data));
    }

    @Override
    public void onRemove(ConnectedClient client) {

    }


    public interface DataVerifier {
        Object verify(String key, Object val);
    }

    public interface OnDataChangedListener {
        void onChanged(SyncedNetworkData data, String key, boolean isExternal);
    }

    private final String name;
    private final boolean isServer;

    private final ModdedClient clientInstance = Network.getSingleton().getClient();
    private ConnectedClientList clients = new ConnectedClientList();

    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, DataVerifier> verifierMap = new HashMap<>();
    private DataVerifier globalVerifier = null;
    private final Map<String, Object> dirtyData = new HashMap<>();
    private final List<OnDataChangedListener> dataChangedListeners = new ArrayList<>();


    protected SyncedNetworkData(String name, boolean isServer) {
        this.name = name;
        this.isServer = isServer;
        clients.addListener(this);
    }

    public SyncedNetworkData(String name) {
        this(name, true);
        ThreadTypeMarker.assertServerThread();
        serverSyncedData.put(name, this);
    }

    public SyncedNetworkData() {
        this("SND" + UUID.randomUUID().toString());
    }

    public SyncedNetworkData(ConnectedClientList list, String name) {
        this(name);
        setClients(list);
    }

    public SyncedNetworkData(ConnectedClientList list) {
        this();
        setClients(list);
    }

    public ConnectedClientList getClients() {
        return clients;
    }

    public void setClients(ConnectedClientList clients) {
        this.clients.removeListener(this);
        this.clients = clients;
        this.clients.addListener(this);
        clients.send("system.synced_data.data#" + name, new JSONObject(data));
    }

    public String getName() {
        return name;
    }

    public boolean isServer() {
        return isServer;
    }


    public void addClient(ConnectedClient client) {
        if (!isServer) {
            throw new IllegalStateException("this is client SyncedNetworkData instance");
        }
        clients.add(client);
    }

    protected void put(String key, Object val, boolean isExternal) {
        if (!Java8BackComp.equals(data.get(key), val)) {
            data.put(key, val);
            if (!isExternal) {
                synchronized (dirtyData) {
                    dirtyData.put(key, val);
                }
            }
            for (OnDataChangedListener listener : dataChangedListeners) {
                listener.onChanged(this, key, isExternal);
            }
        }
    }

    public void apply() {
        if (!dirtyData.isEmpty()) {
            synchronized (dirtyData) {
                if (isServer) {
                    ThreadTypeMarker.assertServerThread();
                    clients.send("system.synced_data.data#" + name, new JSONObject(dirtyData));
                } else {
                    ThreadTypeMarker.assertClientThread();
                    clientInstance.send("system.synced_data.data#" + name, new JSONObject(dirtyData));
                }
                dirtyData.clear();
            }
        }
    }

    public void sendChanges() {
        this.apply();
    }

    public void addOnDataChangedListener(OnDataChangedListener listener) {
        dataChangedListeners.add(listener);
    }

    public void removeOnDataChangedListener(OnDataChangedListener listener) {
        dataChangedListeners.remove(listener);
    }

    public void removeAllListeners() {
        dataChangedListeners.clear();
    }

    public void addVerifier(String key, DataVerifier verifier) {
        verifierMap.put(key, verifier);
    }

    public void setGlobalVerifier(DataVerifier globalVerifier) {
        this.globalVerifier = globalVerifier;
    }

    public DataVerifier getGlobalVerifier() {
        return globalVerifier;
    }

    @Override
    public String toString() {
        return "SyncedNetworkData{" +
                "name='" + name + '\'' +
                ", data=" + data +
                '}';
    }

    public Object getObject(String key) {
        return data.get(key);
    }

    public int getInt(String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public long getLong(String key, long fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).longValue() : fallback;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public float getFloat(String key, float fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).floatValue() : fallback;
    }

    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    public double getDouble(String key, double fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public String getString(String key, String fallback) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public boolean getBoolean(String key, boolean fallback) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public void putObject(String key, Object value) {
        put(key, value, false);
    }

    public void putInt(String key, int value) {
        put(key, value, false);
    }

    public void putLong(String key, long value) {
        put(key, value, false);
    }

    public void putFloat(String key, float value) {
        put(key, value, false);
    }

    public void putDouble(String key, double value) {
        put(key, value, false);
    }

    public void putString(String key, String value) {
        put(key, value, false);
    }

    public void putBoolean(String key, boolean value) {
        put(key, value, false);
    }
}
