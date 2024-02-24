package com.zhekasmirnov.apparatus.multiplayer.client;

import com.zhekasmirnov.apparatus.multiplayer.NetworkConfig;
import com.zhekasmirnov.apparatus.multiplayer.channel.ChannelInterface;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;

@Deprecated(since = "Zote")
public class ModdedClient implements ChannelInterface.OnPacketReceivedListener {
    private long playerUid = -1;

    public ModdedClient(NetworkConfig config) {
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
    }

    public void addOnRequestingConnectionListener(OnRequestingConnectionListener listener) {
    }

    public void addOnDisconnectedListener(OnDisconnectedListener listener) {
    }

    public void addUntypedPacketReceivedListener(String name, OnPacketReceivedListener listener) {
    }

    public <T> void addPacketReceivedListener(String name, TypedOnPacketReceivedListener<T> listener) {
    }

    public void addUntypedInitializationPacketListener(String name, OnPacketReceivedListener listener) {
    }

    public <T> void addInitializationPacketListener(String name, TypedOnPacketReceivedListener<T> listener) {
    }

    public void addShutdownListener(OnShutdownListener listener) {
    }

    @Override
    public void onPacketReceived(String name, Object data, Class<?> dataType) {
    }

    public void start(DataChannel dataChannel) {
    }

    public void shutdown(String reason) {
    }

    public void shutdown() {
    }

    public boolean awaitAllInitializationPackets(int timeout) {
        return true;
    }

    public boolean awaitAllInitializationPackets() {
        return true;
    }

    public boolean isRunning() {
        return false;
    }

    public boolean isConnectionEstablished() {
        return false;
    }

    public void setPlayerUid(long playerUid) {
        this.playerUid = playerUid;
    }

    public long getPlayerUid() {
        return playerUid;
    }

    public void send(String name, Object data) {
    }

    public <T> void send(String name, T data, Class<T> dataType) {
    }
}
