package com.zhekasmirnov.apparatus.multiplayer;

import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.SocketDataChannel;
import com.zhekasmirnov.innercore.mod.build.Config;

public class NetworkConfig {
    private int defaultPort = 2304;
    private int clientProtocolId = SocketDataChannel.PROTOCOL_ID;
    private int initializationTimeout = 90 * 1000;

    private int nativeChannelPingPongTimeout = 30 * 1000;

    private boolean socketConnectionAllowed = true;
    private boolean localNativeProtocolForced = false;
    private boolean nativeProtocolPrioritizedForRemoteConnection = false;

    public boolean isSocketConnectionAllowed() {
        return socketConnectionAllowed;
    }

    public void setSocketConnectionAllowed(boolean socketConnectionAllowed) {
        this.socketConnectionAllowed = socketConnectionAllowed;
    }

    public boolean isLocalNativeProtocolForced() {
        return localNativeProtocolForced;
    }

    public void setLocalNativeProtocolForced(boolean localNativeProtocolForced) {
        this.localNativeProtocolForced = localNativeProtocolForced;
    }

    public boolean isNativeProtocolPrioritizedForRemoteConnection() {
        return nativeProtocolPrioritizedForRemoteConnection;
    }

    public void setNativeProtocolPrioritizedForRemoteConnection(boolean nativeProtocolPrioritizedForRemoteConnection) {
        this.nativeProtocolPrioritizedForRemoteConnection = nativeProtocolPrioritizedForRemoteConnection;
    }

    public int getNativeChannelPingPongTimeout() {
        return nativeChannelPingPongTimeout;
    }

    public void setNativeChannelPingPongTimeout(int nativeChannelPingPongTimeout) {
        this.nativeChannelPingPongTimeout = nativeChannelPingPongTimeout;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int getClientProtocolId() {
        return clientProtocolId;
    }

    public void setClientProtocolId(int clientProtocolId) {
        this.clientProtocolId = clientProtocolId;
    }

    public void setInitializationTimeout(int initializationTimeout) {
        this.initializationTimeout = initializationTimeout;
    }

    public int getInitializationTimeout() {
        return initializationTimeout;
    }

    public void updateFromEngineConfig() {
        // first try to connect via native protocol
        setNativeProtocolPrioritizedForRemoteConnection(EngineConfig.getBoolean("network.remote_native_protocol_prioritized", false));
        // instead of direct channel, use native protocol for local client-server connection
        setLocalNativeProtocolForced(EngineConfig.getBoolean("network.local_native_protocol_forced", false));
        // enable socket server
        setSocketConnectionAllowed(EngineConfig.getBoolean("network.enable_socket_server", true));
    }
}
