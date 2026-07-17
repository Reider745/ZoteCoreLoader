package org.innercore.icstd.network;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.serialization.IScopedReader;
import org.innercore.icstd.serialization.IScopedWriter;
import org.innercore.icstd.serialization.ObjectSerializer;
import org.innercore.icstd.serialization.SerializationContext;
import org.innercore.internal.serialization.JsonIO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkModule {
    private static final HashMap<String, List<IPacketListener>> clientPacketListeners = new HashMap<>();
    private static final HashMap<String, List<IPacketListener>> serverPacketListeners = new HashMap<>();

    private static void sendPacketData(boolean isServer, NetworkClient toClient, String name, Object data) {
        if (isServer)
            ThreadTypeMarker.assertServerThread();
        else
            ThreadTypeMarker.assertClientThread();
        if (isServer) {
            if (toClient == null) {
                Network.getSingleton().getServer().sendToAll(name, data);
            } else {
                toClient.impl.send(name, data);
            }
        } else {
            Network.getSingleton().getClient().send(name, data);
        }
    }

    private static IScopedWriter sendPacketImpl(boolean isServer, NetworkClient toClient, String name) {
        return new JsonIO.ScopedWriter() {
            @Override
            public void endScope() {
                super.endScope();
                sendPacketData(isServer, toClient, name, getResult());
            }
        };
    }

    public List<NetworkClient> getAllClients() {
        return Network.getSingleton().getServer().getConnectedClientsForApi();
    }

    private static void sendPacketImpl(boolean isServer, NetworkClient toClient, INetPacket packet) {
        SerializationContext ctx = new SerializationContext();
        String typeId = packet.getPacketType();
        if (typeId == null) {
            throw new IllegalStateException("INetPacket type is null");
        } else if (!ObjectSerializer.isTypeRegistered(typeId)) {
            throw new IllegalStateException("INetPacket type is not registered: " + typeId);
        }
        JsonIO.ScopedWriter writer = new JsonIO.ScopedWriter();
        if (ObjectSerializer.serializeObjectUnpacked(writer, packet, typeId, ctx, true))
            sendPacketData(isServer, toClient, typeId, writer.getResult());
        // TODO: report error
    }

    public static IScopedWriter sendToServer(String name) {
        return sendPacketImpl(false, null, name);
    }

    public static IScopedWriter sendToClient(NetworkClient toClient, String name) {
        if (toClient == null) throw new IllegalArgumentException("toClient is null");
        return sendPacketImpl(true, toClient, name);
    }

    public static IScopedWriter broadcastToClients(String name) {
        return sendPacketImpl(true, null, name);
    }

    public static void sendToServer(INetPacket packet) {
        sendPacketImpl(false, null, packet);
    }

    public static void sendToClient(NetworkClient toClient, INetPacket packet) {
        if (toClient == null) throw new IllegalArgumentException("toClient is null");
        sendPacketImpl(true, toClient, packet);
    }

    public static void broadcastToClients(INetPacket packet) {
        sendPacketImpl(true, null, packet);
    }

    public static void addClientPacketListener(String name, IPacketListener listener) {
        List<IPacketListener> listeners = clientPacketListeners.get(name);
        if (listeners == null) {
            clientPacketListeners.put(name, listeners = new ArrayList<>());
        }
        listeners.add(listener);
    }

    public static void removeClientPacketListener(String name, IPacketListener listener) {
        List<IPacketListener> listeners = clientPacketListeners.get(name);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public static void addServerPacketListener(String name, IPacketListener listener) {
        List<IPacketListener> listeners = serverPacketListeners.get(name);
        if (listeners == null) {
            serverPacketListeners.put(name, listeners = new ArrayList<>());
        }
        listeners.add(listener);
    }

    public static void removeServerPacketListener(String name, IPacketListener listener) {
        List<IPacketListener> listeners = serverPacketListeners.get(name);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @ApiHidden
    public static void processPacketImpl(NetworkClient fromClient, String name, Object data, String meta) {
        ObjectSerializer.SerializerRecord record = ObjectSerializer.getSerializerRecordByTypeId(name);
        if (record == null)
            return;
        if (data instanceof JSONObject || data instanceof JSONArray || data == null) {
            (fromClient != null ? Network.getSingleton().getServerThreadJobExecutor() : Network.getSingleton().getClientThreadJobExecutor()).add(() -> {
                SerializationContext ctx = new SerializationContext();
                List<IPacketListener> listeners = (fromClient != null ? serverPacketListeners : clientPacketListeners).get(name);
                if (listeners != null) {
                    for (IPacketListener listener : listeners) {
                        IScopedReader io = new JsonIO.ScopedReader(data);
                        listener.process(fromClient, io, ctx, meta);
                        io.forceEndScope(ctx);
                    }
                }
                // run deserializer on the correct thread
                if (INetPacket.class.isAssignableFrom(record.getObjectType())) {
                    Object maybePacket = ObjectSerializer.deserializeObjectUnpacked(new JsonIO.ScopedReader(data), name, Object.class, ctx, true);
                    if (maybePacket instanceof INetPacket) {
                        ((INetPacket) maybePacket).process(fromClient, meta);
                    }
                }
            });
        }
    }

}
