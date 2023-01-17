package com.zhekasmirnov.apparatus.multiplayer.util.entity;

import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

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
        Network.getSingleton().addClientPacket("system.entity.add", (Object data, String meta, Class<?> dataType) -> {
            synchronized (clientEntities) {
                String[] metaArr = meta.split("#");
                if (metaArr.length == 2) {
                    NetworkEntityType entityType = NetworkEntityType.getByName(metaArr[0]);
                    String entityName = metaArr[1];
                    if (entityType != null && entityName.length() > 0) {
                        if (!clientEntities.containsKey(entityName)) {
                            NetworkEntity entity = new NetworkEntity(entityType, null, entityName, false);
                            clientEntities.put(entityName, entity);
                            entity.getClientExecutor().add(() -> entityType.onClientEntityAdded(entity, data));
                        } else {
                            if (entityType.isDuplicateAddPacketAllowed()) {
                                NetworkEntity entity = clientEntities.get(entityName);
                                if (entity != null) {
                                    entity.getClientExecutor().add(() -> entityType.onClientEntityAdded(entity, data));
                                }
                            } else {
                                Logger.debug("duplicate add packet received for network entity " + entityName + ", it will be ignored");
                            }
                        }
                    }
                }
            }
        }, null);

        Network.getSingleton().addClientPacket("system.entity.remove", (Object data, String meta, Class<?> dataType) -> {
            synchronized (clientEntities) {
                NetworkEntity entity = clientEntities.get(meta);
                if (entity != null) {
                    entity.isRemoved = true;
                    entity.getClientExecutor().add(() -> entity.type.onClientEntityRemoved(entity, data));
                    clientEntities.remove(meta);
                }
            }
        }, null); // use entity executor instead

        Network.getSingleton().addClientPacket("system.entity.packet", (Object data, String meta, Class<?> dataType) -> {
            synchronized (clientEntities) {
                int sepIndex = meta.indexOf('#');
                if (sepIndex != -1) {
                    NetworkEntity entity = clientEntities.get(meta.substring(0, sepIndex));
                    String packetName = meta.substring(sepIndex + 1);
                    sepIndex = packetName.indexOf('#');
                    if (sepIndex != -1) {
                        meta = packetName.substring(sepIndex + 1);
                        packetName = packetName.substring(0, sepIndex);
                    } else {
                        meta = null;
                    }
                    if (entity != null) {
                        String finalPacketName = packetName;
                        String finalMeta = meta;
                        entity.getClientExecutor().add(() -> {
                            entity.type.onClientPacket(entity, finalPacketName, data, finalMeta);
                        });
                    }
                }
            }
        }, null); // use entity executor instead

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

        Network.getSingleton().addClientShutdownListener(reason -> {
            synchronized (clientEntities) {
                for (NetworkEntity entity : clientEntities.values()) {
                    entity.removeOnShutdown();
                }
                clientEntities.clear();
            }
        });
    }

    private final String name;
    private final NetworkEntityType type;
    private final boolean isServer;
    private Object target = null;

    private boolean isRemoved = false;

    private final ModdedClient clientInstance = Network.getSingleton().getClient();
    private final ConnectedClientList clients = new ConnectedClientList();

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

    public JobExecutor getClientExecutor() {
        return clientExecutor;
    }

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
            } else {
                ThreadTypeMarker.assertClientThread();
                clientInstance.send("system.entity.packet#" + this.name + "#" + name, data);
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
            } else {
                Network.getSingleton().getInstantJobExecutor().add(() -> {
                    type.onClientEntityRemovedDueShutdown(this);
                });
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
