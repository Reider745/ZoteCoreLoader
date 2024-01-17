package com.zhekasmirnov.apparatus.multiplayer.server;

import android.util.Pair;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.apparatus.multiplayer.NetworkConfig;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.*;
import com.zhekasmirnov.apparatus.multiplayer.socket.SocketServer;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ModdedServer implements SocketServer.IClientConnectListener, NativeNetworking.ConnectionListener, ConnectedClient.OnStateChangedListener, ConnectedClient.OnDisconnectListener {
    private boolean isRunning = false;
    private final SocketServer socketServer;

    private final NetworkConfig config;

    private final List<ConnectedClient> initializingClients = new ArrayList<>();
    private final List<ConnectedClient> connectedClients = new ArrayList<>();
    private final Map<Long, ConnectedClient> connectedClientByPlayerUid = new HashMap<>();

    private final Object networkThreadQueueMonitor = new Object();
    private final Queue<Runnable> networkThreadQueue = new ConcurrentLinkedDeque<>();

    private final List<OnClientConnectedListener> onClientConnectedListeners = new ArrayList<>();
    private final List<OnClientConnectionRequestedListener> onClientConnectionRequestedListeners = new ArrayList<>();
    private final List<OnClientDisconnectedListener> onClientDisconnectedListeners = new ArrayList<>();
    private final Map<String, List<ConnectedClient.InitializationPacketListener>> initializationPacketListenerMap = new HashMap<>();
    private final Map<String, List<OnPacketReceivedListener>> onPacketReceivedListenerMap = new HashMap<>();
    private final List<OnShutdownListener> onShutdownListeners = new ArrayList<>();


    public interface OnClientConnectionRequestedListener {
        void onConnectionRequested(ConnectedClient client);
    }

    public interface OnClientConnectedListener {
        void onClientConnected(ConnectedClient client);
    }

    public interface OnClientDisconnectedListener {
        void onClientDisconnected(ConnectedClient client, String reason);
    }

    public interface TypedInitializationPacketListener<T> {
        void onPacketReceived(ConnectedClient client, T data) throws InitializationPacketException;
    }

    public interface OnPacketReceivedListener {
        void onPacketReceived(ConnectedClient client, Object data, String meta, Class<?> dataType);
    }

    public interface TypedOnPacketReceivedListener<T> {
        void onPacketReceived(ConnectedClient client, T data, String meta);
    }

    public interface OnShutdownListener {
        void onShutdown(ModdedServer server);
    }

    public ModdedServer(NetworkConfig config) {
        this.config = config;
        socketServer = new SocketServer(this);
        socketServer.addClientConnectListener(this);
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public List<ConnectedClient> getInitializingClients() {
        return initializingClients;
    }

    public List<ConnectedClient> getConnectedClients() {
        return connectedClients;
    }

    public ConnectedClient getConnectedClientForPlayer(long player) {
        return connectedClientByPlayerUid.get(player);
    }

    public List<Long> getConnectedPlayers() {
        return new ArrayList<>(connectedClientByPlayerUid.keySet());
    }

    public void addOnClientConnectedListener(OnClientConnectedListener listener) {
        onClientConnectedListeners.add(listener);
    }

    public void addOnClientConnectionRequestedListener(OnClientConnectionRequestedListener listener) {
        onClientConnectionRequestedListeners.add(listener);
    }

    public void addOnClientDisconnectedListener(OnClientDisconnectedListener listener) {
        onClientDisconnectedListeners.add(listener);
    }

    public void addUntypedInitializationPacketListener(String name, ConnectedClient.InitializationPacketListener listener) {
        Java8BackComp.computeIfAbsent(initializationPacketListenerMap, name, (String key) -> new ArrayList<>()).add(listener);
    }

    public<T> void addInitializationPacketListener(String name, TypedInitializationPacketListener<T> listener) {
        addUntypedInitializationPacketListener(name, (ConnectedClient client, Object data, Class<?> dataType) -> {
            try {
                //noinspection unchecked
                listener.onPacketReceived(client, (T) data);
            } catch (ClassCastException e) {
                throw new InitializationPacketException(e.getMessage(), e);
            }
        });
    }

    public void addUntypedPacketReceivedListener(String name, OnPacketReceivedListener listener) {
        Java8BackComp.computeIfAbsent(onPacketReceivedListenerMap, name, (String key) -> new ArrayList<>()).add(listener);
    }

    public<T> void addPacketReceivedListener(String name, TypedOnPacketReceivedListener<T> listener) {
        addUntypedPacketReceivedListener(name, (ConnectedClient client, Object data, String meta, Class<?> dataType) -> {
            try {
                //noinspection unchecked
                listener.onPacketReceived(client, (T) data, meta);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        });
    }

    public void addShutdownListener(OnShutdownListener listener) {
        onShutdownListeners.add(listener);
    }

    public void onClientConnected(DataChannel channel, String cl) {
        ConnectedClient client = new ConnectedClient(this, channel, cl);
        client.setDisconnectListener(this);
        client.setStateChangedListener(this);

        for (OnClientConnectionRequestedListener listener : onClientConnectionRequestedListeners) {
            listener.onConnectionRequested(client);
        }

        for (String name : initializationPacketListenerMap.keySet()) {
            if(InnerCoreServer.isDebugInnerCoreNetwork())
                System.out.println("received initialization packet player=" + client.getPlayerUid() + " name="+name);
            for (ConnectedClient.InitializationPacketListener listener : initializationPacketListenerMap.get(name)) {
                client.addInitializationPacketListener(name, listener);
            }
        }

        client.getChannelInterface().addListener((String name, Object data, Class<?> dataType) -> {
            if (InnerCoreServer.isDebugInnerCoreNetwork())
                System.out.println("received packet player=" + client.getPlayerUid() + " name="+name);
            int separatorIndex = name.indexOf('#');
            String meta = null;
            if (separatorIndex != -1) {
                meta = name.substring(separatorIndex + 1);
                name = name.substring(0, separatorIndex);
            }

            List<OnPacketReceivedListener> listeners = onPacketReceivedListenerMap.get(name);
            if (listeners != null) {
                for (OnPacketReceivedListener listener : listeners) {
                    listener.onPacketReceived(client, data, meta, dataType);
                }
            }
        });

        client.start();
    }

    @Override
    public boolean onClientConnected(DataChannel channel, Socket socket, boolean isChannelAlreadyManaged) {
        onClientConnected(channel, socket.getInetAddress().toString());
        return true;
    }

    @Override
    public void onNativeChannelConnected(NativeDataChannel channel,String sender) {
        if (EngineConfig.isDeveloperMode()) {
            Logger.debug("client connected via native protocol");
        }
        onClientConnected(channel, sender);
    }

    public DataChannel openLocalClientChannel(String sender) {
        /*try {
            Socket socket = new Socket("localhost", getConfig().getDefaultPort());
            socket.getOutputStream().write(SocketDataChannel.PROTOCOL_ID);
            return new SocketDataChannel(socket);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }*/

        if (config.isLocalNativeProtocolForced()) {
            NativeDataChannel channel = new NativeDataChannel(NativeNetworking.getOrCreateClientToServerChannel());
            if (!channel.pingPong(10000)) {
                Logger.debug("LAN Server Error", "failed to ping-pong local native channel");
            } else {
                Logger.debug("successfully opened local native channel");
            }
            return channel;
        } else {
            Pair<DirectDataChannel, DirectDataChannel> channelPair = DirectDataChannel.createDirectChannelPair();
            onClientConnected(channelPair.first, sender);
            return channelPair.second;
        }
    }

    @Override
    public void onDisconnect(ConnectedClient client, String disconnectPacket, IOException disconnectCause) {
        for (OnClientDisconnectedListener listener : onClientDisconnectedListeners) {
            listener.onClientDisconnected(client, client.getDisconnectPacket());
        }
    }

    @Override
    public void onStateChanged(ConnectedClient client, ConnectedClient.ClientState newState) {
        switch (newState) {
            case INITIALIZING:
                synchronized (connectedClients) {
                    initializingClients.add(client);
                }
                break;
            case OPEN:
                synchronized (connectedClients) {
                    initializingClients.remove(client);
                    connectedClients.add(client);
                    connectedClientByPlayerUid.put(client.getPlayerUid(), client);
                }
                for (OnClientConnectedListener listener : onClientConnectedListeners) {
                    listener.onClientConnected(client);
                }
                break;
            case INIT_FAILED:
            case CLOSED:
                synchronized (connectedClients) {
                    initializingClients.remove(client);
                    connectedClients.remove(client);
                    connectedClientByPlayerUid.remove(client.getPlayerUid());
                }
                break;
        }
    }

    public void runOnNetworkThread(Runnable action) {
        networkThreadQueue.add(action);
        networkThreadQueueMonitor.notifyAll();
    }

    public void start(int port) {
        try {
            NativeNetworking.addConnectionListener(this);
            if (config.isSocketConnectionAllowed()) {
                socketServer.start(port);
                Logger.debug("modded server started (socket port " + port + ")");
            } else {
                socketServer.close();
                Logger.debug("modded server started (native protocol only)");
            }
            isRunning = true;
            new Thread(this::networkThreadLoop).start();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void shutdown() {
        if (isRunning) {
            NativeNetworking.removeConnectionListener(this);
            for (OnShutdownListener listener : onShutdownListeners) {
                listener.onShutdown(this);
            }
            List<ConnectedClient> clientsToDisconnect = new ArrayList<>();
            synchronized (connectedClients) {
                clientsToDisconnect.addAll(connectedClients);
                clientsToDisconnect.addAll(initializingClients);
            }
            for (ConnectedClient client : clientsToDisconnect) {
                client.disconnect("server closed");
            }
            socketServer.close();
            isRunning = false;
        }
    }

    public void sendToAll(String name, Object data) {
        synchronized (connectedClients) {
            for (ConnectedClient client : connectedClients) {
                client.send(name, data);
            }
        }
    }

    public<T> void sendToAll(String name, T data, Class<T> dataType) {
        synchronized (connectedClients) {
            for (ConnectedClient client : connectedClients) {
                client.send(name, data, dataType);
            }
        }
    }

    public void sendMessageToAll(String message) {
        synchronized (connectedClients) {
            for (ConnectedClient client : connectedClients) {
                client.sendMessage(message);
            }
        }
    }

    public void networkThreadLoop() {
        ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);
        while (isRunning) {
            Runnable action = networkThreadQueue.poll();
            if (action != null) {
                action.run();
            }
            try {
                synchronized (networkThreadQueueMonitor) {
                    networkThreadQueueMonitor.wait(100);
                }
            } catch (InterruptedException ignore) { }
        }
    }

    // TODO: maybe make in one optimized thread
    public void addWatchdogAction(int timeout, Runnable action) {
        new Thread(() -> {
            ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return;
            }
            action.run();
        }).start();
    }
}
