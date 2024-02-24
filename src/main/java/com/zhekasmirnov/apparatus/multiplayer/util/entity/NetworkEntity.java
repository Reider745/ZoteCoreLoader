package com.zhekasmirnov.apparatus.multiplayer.util.entity;

import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkEntity {
    private static final Map<String, NetworkEntity> serverEntities = new HashMap<>();
    private static final Map<String, NetworkEntity> clientEntities = new HashMap<>();

    private static final ThreadLocal<ConnectedClient> currentClient = new ThreadLocal<>();

    public static void loadClass() {
        // forces class to load and register listeners
    }

    public static NetworkEntity getClientEntityInstance(String name) {
        return clientEntities.get(name);
    }

    static {
        Network.getSingleton().addServerPacket("system.entity.packet", (ConnectedClient client, Object data, String meta, Class<?> dataType) -> {
            synchronized (serverEntities) {
                int sepIndex = meta.indexOf('#');
                if (sepIndex != -1) {
                    NetworkEntity entity = serverEntities.get(meta.substring(0, sepIndex));
                    String packetName = meta.substring(sepIndex + 1);
                    sepIndex = packetName.indexOf('#');
                    if (sepIndex != -1) {
                        meta = packetName.substring(sepIndex + 1);
                        packetName = packetName.substring(0, sepIndex);
                    } else {
                        meta = null;
                    }
                    if (entity != null && entity.clients.contains(client)) {
                        String finalPacketName = packetName;
                        String finalMeta = meta;
                        entity.getServerExecutor().add(() -> {
                            currentClient.set(client);
                            entity.type.onServerPacket(entity, client, finalPacketName, data, finalMeta);
                            currentClient.set(null);
                        });
                    }
                }
            }
        }, null); // use entity executor instead

        Network.getSingleton().addServerShutdownListener(server -> {
            synchronized (serverEntities) {
                for (NetworkEntity entity : serverEntities.values()) {
                    entity.removeOnShutdown();
                }
                serverEntities.clear();
            }
        });
    }

    private final String name;
    private final NetworkEntityType type;
    private final boolean isServer;
    private Object target = null;

    private boolean isRemoved = false;

    private final ConnectedClientList clients = new ConnectedClientList();

    @Deprecated(since = "Zote")
    private JobExecutor clientExecutor = Network.getSingleton().getClientThreadJobExecutor();
    private JobExecutor serverExecutor = Network.getSingleton().getServerThreadJobExecutor();

    protected NetworkEntity(NetworkEntityType type, Object target, String name, boolean isServer) {
        if (type == null) {
            throw new NullPointerException("networkEntityType cannot be null");
        }

        this.name = name;
        this.type = type;
        this.target = target;
        this.isServer = isServer;

        type.setupEntity(this);

        if (isServer) {
            clients.addListener(new ConnectedClientList.Listener() {
                @Override
                public void onAdd(ConnectedClient client) {
                    if (!isRemoved) {
                        client.send("system.entity.add#" + type.getTypeName() + "#" + name, type.newClientAddPacket(NetworkEntity.this, client));
                    }
                }

                @Override
                public void onRemove(ConnectedClient client) {
                    client.send("system.entity.remove#" + name, type.newClientRemovePacket(NetworkEntity.this, client));
                }
            });

            type.setupClientList(clients, this);
        }
    }

    public NetworkEntity(NetworkEntityType type, Object target, String name) {
        this(type, target, name, true);
        ThreadTypeMarker.assertServerThread();
        serverEntities.put(name, this);
    }

    public NetworkEntity(NetworkEntityType type, Object target) {
        this(type, target, type.getTypeName() + UUID.randomUUID().toString());
    }

    public NetworkEntity(NetworkEntityType type) {
        this(type, null);
    }

    public NetworkEntityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isServer() {
        return isServer;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public ConnectedClientList getClients() {
        return clients;
    }

    public void refreshClients() {
        clients.refresh();
    }

    @Deprecated(since = "Zote")
    public JobExecutor getClientExecutor() {
        return clientExecutor;
    }

    @Deprecated(since = "Zote")
    public void setClientExecutor(JobExecutor clientExecutor) {
        if (clientExecutor == null) {
            throw new NullPointerException("clientExecutor cannot be null");
        }
        this.clientExecutor = clientExecutor;
    }

    public JobExecutor getServerExecutor() {
        return serverExecutor;
    }

    public void setServerExecutor(JobExecutor serverExecutor) {
        if (serverExecutor == null) {
            throw new NullPointerException("serverExecutor cannot be null");
        }
        this.serverExecutor = serverExecutor;
    }

    public void send(String name, Object data) {
        synchronized (this) {
            if (isRemoved) {
                return;
            }
            if (isServer) {
                ThreadTypeMarker.assertServerThread();
                clients.send("system.entity.packet#" + this.name + "#" + name, data);
            }
        }
    }

    public void send(ConnectedClient client, String name, Object data) {
        synchronized (this) {
            if (isRemoved) {
                return;
            }
            if (!isServer) {
                throw new IllegalStateException();
            }
            ThreadTypeMarker.assertServerThread();
            client.send("system.entity.packet#" + this.name + "#" + name, data);
        }
    }

    public void respond(String name, Object data) {
        synchronized (this) {
            if (isRemoved) {
                return;
            }
            ConnectedClient client = currentClient.get();
            if (client == null) {
                throw new IllegalStateException("respond() must be called only in server packet callback");
            }
            send(client, name, data);
        }
    }

    private void removeOnShutdown() {
        synchronized (this) {
            if (isRemoved) {
                return;
            }
            isRemoved = true;
            if (isServer) {
                clients.clear();
            }
        }
    }

    public void remove() {
        synchronized (this) {
            if (isServer) {
                ThreadTypeMarker.assertServerThread();
                clients.setAddPolicy(null);
                clients.clear();
                serverEntities.remove(name);
                isRemoved = true;
            } else {
                throw new IllegalStateException("remove() can be called only on server network entity");
            }
        }
    }

    public boolean isRemoved() {
        return isRemoved;
    }
}
