package com.zhekasmirnov.apparatus.multiplayer.server;

import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.channel.ChannelInterface;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;

import java.io.IOException;
import java.util.*;

public class ConnectedClient extends Thread implements ChannelInterface.OnPacketReceivedListener {
    public enum ClientState {
        CREATED,
        INITIALIZING,
        OPEN,
        INIT_FAILED,
        CLOSED
    }

    private final ModdedServer server;
    private final ChannelInterface channel;
    public long playerUid = -1;

    private ClientState state = ClientState.CREATED;
    private OnStateChangedListener stateChangedListener;

    private IOException disconnectCause;
    private String disconnectPacket = null;
    private OnDisconnectListener disconnectListener;

    private final Map<String, List<InitializationPacketListener>> initializationPacketListenerMap = new HashMap<>();
    private final Set<String> remainingInitializationPackets = new HashSet<>();
    private InitializationPacketException initializationPacketFailureCause = null;

    public interface InitializationPacketListener {
        void onPacketReceived(ConnectedClient client, Object data, Class<?> dataType) throws InitializationPacketException;
    }

    public interface OnDisconnectListener {
        void onDisconnect(ConnectedClient client, String disconnectPacket, IOException disconnectCause);
    }

    public interface OnStateChangedListener {
        void onStateChanged(ConnectedClient client, ClientState newState);
    }

    public ConnectedClient(ModdedServer server, DataChannel channel, String client) {
        this.server = server;
        this.channel = new ChannelInterface(channel);
        this.channel.addListener(this);
        SetLocalPlayerAsInitializedPacket.clients.put(client, this);
        /*this.addInitializationPacketListener("system.player_entity", (ConnectedClient client, Object data, Class<?> dataType) -> {
            try {
                Logger.debug("system.player_entity");
                playerUid = Long.parseLong(data.toString());
            } catch (NumberFormatException e) {
                throw new InitializationPacketException("invalid player packet data: " + data, e);
            }
        });*/
    }

    public void setClientState(ClientState state) {
        if (this.state != state) {
            this.state = state;
            if (stateChangedListener != null) {
                stateChangedListener.onStateChanged(this, state);
            }
        }
    }

    public ClientState getClientState() {
        return state;
    }

    public boolean isClosed() {
        return state == ClientState.CLOSED;
    }


    public void setDisconnectListener(OnDisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    public void setStateChangedListener(OnStateChangedListener stateChangedListener) {
        this.stateChangedListener = stateChangedListener;
    }

    public void addInitializationPacketListener(String name, InitializationPacketListener listener) {
        List<InitializationPacketListener> listeners = Java8BackComp.computeIfAbsent(initializationPacketListenerMap, name, (String key) -> new ArrayList<>());
        remainingInitializationPackets.add(name + "$" + listeners.size());
        listeners.add(listener);
    }


    @Override
    public void onPacketReceived(String name, Object data, Class<?> dataType) {
        if (name.equals("system.client_disconnect")) {
            disconnectPacket = data.toString();
            channel.close();
        }
        if (state != ClientState.INITIALIZING) {
            return;
        }
        List<InitializationPacketListener> listeners = initializationPacketListenerMap.get(name);
        if (listeners != null) {
            int index = 0;
            for (InitializationPacketListener listener : listeners) {
                try {
                    listener.onPacketReceived(this, data, dataType);
                    remainingInitializationPackets.remove(name + "$" + index++);
                } catch (InitializationPacketException e) {
                    initializationPacketFailureCause = e;
                    setClientState(ClientState.INIT_FAILED);
                    disconnect("initialization packet " + name + " rejected: " + e.getMessage());
                    return;
                }
            }
        }
        if (remainingInitializationPackets.isEmpty()) {
            setClientState(ClientState.OPEN);
            send("system.client_connection_allowed", "");
        }
    }

    
    @Override
    public void run() {
        ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);
        if (state != ClientState.CREATED) {
            throw new IllegalStateException(state.toString());
        }
        setClientState(ClientState.INITIALIZING);
        if (remainingInitializationPackets.isEmpty()) {
            setClientState(ClientState.OPEN);
        }

        server.addWatchdogAction(server.getConfig().getInitializationTimeout(), () -> {
            if (!remainingInitializationPackets.isEmpty() && !isClosed()) {
                StringBuilder sb = new StringBuilder();
                sb.append("client initialization ").append(remainingInitializationPackets.size()).append(" packets timed out: ");
                for (String remainingInitPacket : remainingInitializationPackets) {
                    sb.append(remainingInitPacket).append(" ");
                }
                setClientState(ClientState.INIT_FAILED);
                disconnect(sb.toString());
            }
        });

        channel.getChannel().setBrokenChannelListener(exception -> {
            // try to send exception to client before channel will be closed
            disconnect("IOException occurred: " + exception.getMessage());
        });

        send("system.client_awaiting_init", "");

        channel.listenerLoop();
        setClientState(ClientState.CLOSED);

        if (disconnectListener != null) {
            disconnectListener.onDisconnect(this, disconnectPacket, disconnectCause);
        }
    }


    public long getPlayerUid() {
        return playerUid;
    }

    public ChannelInterface getChannelInterface() {
        return channel;
    }

    public IOException getDisconnectCause() {
        return disconnectCause;
    }

    public String getDisconnectPacket() {
        return disconnectPacket;
    }

    public InitializationPacketException getInitializationPacketFailureCause() {
        return initializationPacketFailureCause;
    }


    public void send(String name, Object data) {
        channel.send(name, data);
    }

    public<T> void send(String name, T data, Class<T> dataType) {
        channel.send(name, data, dataType);
    }

    public void sendMessage(String message) {
        send("system.message", message);
    }

    public void disconnect(String reason) {
        if (state != ClientState.CLOSED) {
            disconnectPacket = reason;
            channel.send("system.server_disconnect", reason);
            channel.shutdownAndAwaitDisconnect(2000);
        }
    }

    public void disconnect() {
        disconnect("no further information");
    }

}
