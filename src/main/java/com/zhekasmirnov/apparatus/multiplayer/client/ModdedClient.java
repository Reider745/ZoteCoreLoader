package com.zhekasmirnov.apparatus.multiplayer.client;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkConfig;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.channel.ChannelInterface;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.*;

public class ModdedClient implements ChannelInterface.OnPacketReceivedListener {
    private final Object lock = new Object();

    private final NetworkConfig config;

    private boolean isRunning = false;
    private boolean isConnectionEstablished = false;
    private ChannelInterface channel = null;

    private long playerUid = -1;

    private String serverDisconnectReason = null;
    private final List<OnConnectedListener> onConnectedListeners = new ArrayList<>();
    private final List<OnRequestingConnectionListener> onRequestingConnectionListeners = new ArrayList<>();
    private final List<OnDisconnectedListener> onDisconnectedListeners = new ArrayList<>();
    private final List<OnShutdownListener> onShutdownListeners = new ArrayList<>();

    private final Map<String, List<OnPacketReceivedListener>> onPacketReceivedListenerMap = new HashMap<>();
    private final Set<String> serverInitializationPackets = new HashSet<>();
    private final Set<String> remainingServerInitializationPackets = new HashSet<>();
    private final Object serverInitializationPacketsMonitor = new Object();

    public ModdedClient(NetworkConfig config) {
        this.config = config;
    }

    public interface OnConnectedListener {
        void onConnected();
    }

    public interface OnRequestingConnectionListener {
        void onRequestingConnection();
    }

    public interface OnDisconnectedListener {
        void onDisconnected(String reason);
    }

    public interface OnPacketReceivedListener {
        void onPacketReceived(Object data, String meta, Class<?> dataType);
    }

    public interface TypedOnPacketReceivedListener<T> {
        void onPacketReceived(T data, String meta);
    }

    public interface OnShutdownListener {
        void onShutdown(ModdedClient server);
    }


    public void addOnConnectedListener(OnConnectedListener listener) {
        onConnectedListeners.add(listener);
    }

    public void addOnRequestingConnectionListener(OnRequestingConnectionListener listener) {
        onRequestingConnectionListeners.add(listener);
    }

    public void addOnDisconnectedListener(OnDisconnectedListener listener) {
        onDisconnectedListeners.add(listener);
    }

    public void addUntypedPacketReceivedListener(String name, OnPacketReceivedListener listener) {
        Java8BackComp.computeIfAbsent(onPacketReceivedListenerMap, name, (String key) -> new ArrayList<>()).add(listener);
    }

    public<T> void addPacketReceivedListener(String name, TypedOnPacketReceivedListener<T> listener) {
        addUntypedPacketReceivedListener(name, (Object data, String meta, Class<?> dataType) -> {
            try {
                //noinspection unchecked
                listener.onPacketReceived((T) data, meta);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        });
    }

    public void addUntypedInitializationPacketListener(String name, OnPacketReceivedListener listener) {
        addUntypedPacketReceivedListener(name, listener);
        serverInitializationPackets.add(name);
    }

    public<T> void addInitializationPacketListener(String name, TypedOnPacketReceivedListener<T> listener) {
        addPacketReceivedListener(name, listener);
        serverInitializationPackets.add(name);
    }

    public void addShutdownListener(OnShutdownListener listener) {
        onShutdownListeners.add(listener);
    }


    @Override
    public void onPacketReceived(String name, Object data, Class<?> dataType) {
        if (name.equals("system.server_disconnect")) {
            synchronized (lock) {
                serverDisconnectReason = data.toString();
                channel.close();
                isRunning = false;
                isConnectionEstablished = false;
            }
            return;
        }

        if (name.equals("system.client_connection_allowed")) {
            isConnectionEstablished = true;
            for (OnConnectedListener listener : onConnectedListeners) {
                listener.onConnected();
            }
            return;
        }

        if (name.equals("system.ping")) {
            channel.send("system.ping", data);
            return;
        }

        if (name.equals("system.message")) {
            Network.getSingleton().getClientThreadJobExecutor().add(() -> Logger.debug(data.toString()));
        }

        int separatorIndex = name.indexOf('#');
        String meta = null;
        if (separatorIndex != -1) {
            meta = name.substring(separatorIndex + 1);
            name = name.substring(0, separatorIndex);
        }

        List<OnPacketReceivedListener> listeners = onPacketReceivedListenerMap.get(name);
        if (listeners != null) {
            for (OnPacketReceivedListener listener : listeners) {
                listener.onPacketReceived(data, meta, dataType);
            }
        }

        if (!remainingServerInitializationPackets.isEmpty()) {
            if (remainingServerInitializationPackets.remove(name)) {
                synchronized (serverInitializationPacketsMonitor) {
                    serverInitializationPacketsMonitor.notifyAll();
                }
            }
        }
    }

    public void start(DataChannel dataChannel) {
        shutdown("client restarted");
        synchronized (lock) {
            isRunning = true;
            isConnectionEstablished = false;
            channel = new ChannelInterface(dataChannel);

            remainingServerInitializationPackets.addAll(serverInitializationPackets);
            channel.addListener(this);

            dataChannel.setBrokenChannelListener(exception -> {
                // try to send exception to server before channel will be closed
                shutdown("IOException occurred: " + exception.getMessage());
            });

            new Thread(() -> {
                ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.CLIENT);
                serverDisconnectReason = null;
                channel.listenerLoop();
                isRunning = false;
                isConnectionEstablished = false;
                playerUid = -1;
                for (OnDisconnectedListener listener : onDisconnectedListeners)  {
                    listener.onDisconnected(serverDisconnectReason);
                }
            }).start();

            // run it parallel with listener loop, so it wont block it
            for (OnRequestingConnectionListener listener : onRequestingConnectionListeners) {
                listener.onRequestingConnection();
            }

            if (playerUid != -1) {
                channel.send("system.player_entity", "" + playerUid);
            }
        }
    }

    public void shutdown(String reason) {
        synchronized (lock) {
            if (isRunning) {
                for (OnShutdownListener listener : onShutdownListeners) {
                    listener.onShutdown(this);
                }
                channel.send("system.client_disconnect", reason);
                channel.shutdownAndAwaitDisconnect(2000);
                isRunning = false;
                isConnectionEstablished = false;
            }
        }
    }

    public void shutdown() {
        shutdown("client disconnected due shutdown");
    }

    public boolean awaitAllInitializationPackets(int timeout) {
        long start = System.currentTimeMillis();
        while (!remainingServerInitializationPackets.isEmpty() && start + timeout > System.currentTimeMillis()) {
            try {
                synchronized (serverInitializationPacketsMonitor) {
                    serverInitializationPacketsMonitor.wait(start + timeout - System.currentTimeMillis());
                }
            } catch (InterruptedException e) {
                return remainingServerInitializationPackets.isEmpty();
            }
        }

        return remainingServerInitializationPackets.isEmpty();
    }

    public boolean awaitAllInitializationPackets() {
        while (!remainingServerInitializationPackets.isEmpty()) {
            try {
                synchronized (serverInitializationPacketsMonitor) {
                    serverInitializationPacketsMonitor.wait();
                }
            } catch (InterruptedException e) {
                return remainingServerInitializationPackets.isEmpty();
            }
        }

        return true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isConnectionEstablished() {
        return isConnectionEstablished;
    }

    public void setPlayerUid(long playerUid) {
        synchronized (lock) {
            this.playerUid = playerUid;
            if (isRunning && playerUid != -1) {
                channel.send("system.player_entity", "" + playerUid);
            }
        }
    }

    public long getPlayerUid() {
        return playerUid;
    }

    public void send(String name, Object data) {
        channel.send(name, data);
    }

    public<T> void send(String name, T data, Class<T> dataType) {
        channel.send(name, data, dataType);
    }
}
