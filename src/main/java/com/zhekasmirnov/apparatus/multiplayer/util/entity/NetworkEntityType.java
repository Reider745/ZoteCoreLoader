package com.zhekasmirnov.apparatus.multiplayer.util.entity;

import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import com.zhekasmirnov.apparatus.util.Java8BackComp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkEntityType {
    private static final Map<String, NetworkEntityType> entityTypes = new HashMap<>();

    public static NetworkEntityType getByName(String name) {
        return entityTypes.get(name);
    }




    @Deprecated(since = "Zote")
    public interface OnClientEntityAddedListener {
        Object onAdded(NetworkEntity entity, Object initPacket);
    }

    @Deprecated(since = "Zote")
    public interface OnClientEntityRemovedListener {
        void onRemoved(Object target, NetworkEntity entity, Object removePacket);
    }

    public interface OnClientListSetupListener {
        void setup(ConnectedClientList list, Object target, NetworkEntity entity);
    }

    public interface OnEntitySetupListener {
        void setup(NetworkEntity entity, boolean isServer);
    }

    @Deprecated(since = "Zote")
    public interface OnClientPacketListener {
        void onReceived(Object target, NetworkEntity entity, Object packetData, String extra);
    }

    public interface OnServerPacketListener {
        void onReceived(Object target, NetworkEntity entity, ConnectedClient client, Object packetData, String extra);
    }

    public interface ClientAddPacketFactory {
        Object newPacket(Object target, NetworkEntity entity, ConnectedClient client);
    }

    public interface ClientRemovePacketFactory {
        Object newPacket(Object target, NetworkEntity entity, ConnectedClient client);
    }


    private final String typeName;

    private OnEntitySetupListener entitySetupListener = null;
    private OnClientListSetupListener clientListSetupListener = null;
    @Deprecated(since = "Zote")
    private OnClientEntityAddedListener clientEntityAddedListener = null;
    @Deprecated(since = "Zote")
    private OnClientEntityRemovedListener clientEntityRemovedListener = null;
    private ClientAddPacketFactory clientAddPacketFactory = null;
    private ClientRemovePacketFactory clientRemovePacketFactory = null;

    private boolean isDuplicateAddPacketAllowed = false;

    @Deprecated(since = "Zote")
    private final Map<String, List<OnClientPacketListener>> clientPacketListenerMap = new HashMap<>();
    private final Map<String, List<OnServerPacketListener>> serverPacketListenerMap = new HashMap<>();


    public NetworkEntityType(String typeName) {
        this.typeName = typeName;
        entityTypes.put(typeName, this);
    }

    public String getTypeName() {
        return typeName;
    }

    public NetworkEntityType setClientListSetupListener(OnClientListSetupListener clientListSetupListener) {
        this.clientListSetupListener = clientListSetupListener;
        return this;
    }

    public NetworkEntityType setEntitySetupListener(OnEntitySetupListener entitySetupListener) {
        this.entitySetupListener = entitySetupListener;
        return this;
    }

    public NetworkEntityType setClientAddPacketFactory(ClientAddPacketFactory clientAddPacketFactory) {
        this.clientAddPacketFactory = clientAddPacketFactory;
        return this;
    }

    public NetworkEntityType setClientRemovePacketFactory(ClientRemovePacketFactory clientRemovePacketFactory) {
        this.clientRemovePacketFactory = clientRemovePacketFactory;
        return this;
    }

    @Deprecated(since = "Zote")
    public NetworkEntityType setClientEntityAddedListener(OnClientEntityAddedListener clientEntityAddedListener) {
        this.clientEntityAddedListener = clientEntityAddedListener;
        return this;
    }

    @Deprecated(since = "Zote")
    public NetworkEntityType setClientEntityRemovedListener(OnClientEntityRemovedListener clientEntityRemovedListener) {
        this.clientEntityRemovedListener = clientEntityRemovedListener;
        return this;
    }

    @Deprecated(since = "Zote")
    public NetworkEntityType addClientPacketListener(String name, OnClientPacketListener listener) {
        Java8BackComp.computeIfAbsent(clientPacketListenerMap, name, key -> new ArrayList<>()).add(listener);
        return this;
    }

    public NetworkEntityType addServerPacketListener(String name, OnServerPacketListener listener) {
        Java8BackComp.computeIfAbsent(serverPacketListenerMap, name, key -> new ArrayList<>()).add(listener);
        return this;
    }


    public boolean isDuplicateAddPacketAllowed() {
        return isDuplicateAddPacketAllowed;
    }

    public void setupEntity(NetworkEntity entity) {
        if (entitySetupListener != null) {
            entitySetupListener.setup(entity, entity.isServer());
        }
    }

    public void setupClientList(ConnectedClientList list, NetworkEntity entity) {
        if (clientListSetupListener != null) {
            clientListSetupListener.setup(list, entity.getTarget(), entity);
        }
    }

    public Object newClientAddPacket(NetworkEntity entity, ConnectedClient client) {
        return clientAddPacketFactory != null ? clientAddPacketFactory.newPacket(entity.getTarget(), entity, client) : "";
    }

    public Object newClientRemovePacket(NetworkEntity entity, ConnectedClient client) {
        return clientRemovePacketFactory != null ? clientRemovePacketFactory.newPacket(entity.getTarget(), entity, client) : "";
    }

    @Deprecated(since = "Zote")
    public void onClientEntityAdded(NetworkEntity entity, Object packet) {
        Object target = null;
        if (clientEntityAddedListener != null) {
            target = clientEntityAddedListener.onAdded(entity, packet);
        }
        entity.setTarget(target);
    }

    @Deprecated(since = "Zote")
    public void onClientEntityRemoved(NetworkEntity entity, Object packet) {
        if (clientEntityRemovedListener != null) {
            clientEntityRemovedListener.onRemoved(entity.getTarget(), entity, packet);
        }
    }

    @Deprecated(since = "Zote")
    public void onClientEntityRemovedDueShutdown(NetworkEntity entity) {
        onClientEntityRemoved(entity, null);
    }

    @Deprecated(since = "Zote")
    public void onClientPacket(NetworkEntity entity, String name, Object data, String meta) {
        List<OnClientPacketListener> listeners = clientPacketListenerMap.get(name);
        if (listeners != null) {
            for (OnClientPacketListener listener : listeners) {
                listener.onReceived(entity.getTarget(), entity, data, meta);
            }
        }
    }

    public void onServerPacket(NetworkEntity entity, ConnectedClient client, String name, Object data, String meta) {
        List<OnServerPacketListener> listeners = serverPacketListenerMap.get(name);
        if (listeners != null) {
            for (OnServerPacketListener listener : listeners) {
                listener.onReceived(entity.getTarget(), entity, client, data, meta);
            }
        }
    }
}
