package com.zhekasmirnov.apparatus.multiplayer;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.job.InstantJobExecutor;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.job.MainThreadJobExecutor;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ModdedServer;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;

import java.io.IOException;

public class Network {
    private static final Network singleton = new Network();

    public static Network getSingleton() {
        return singleton;
    }


    private final NetworkConfig config = new NetworkConfig();
    private final ModdedServer server = new ModdedServer(config);
    @Deprecated(since = "Zote")
    private final com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient client = new com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient(config);

    private final JobExecutor instantJobExecutor = new InstantJobExecutor("Default Instant Network Executor");
    private final JobExecutor serverThreadJobExecutor = new MainThreadJobExecutor(MainThreadQueue.serverThread, "Default Server Network Executor");
    private final JobExecutor clientThreadJobExecutor = new MainThreadJobExecutor(MainThreadQueue.localThread, "Default Client Network Executor");

    private Network() {
        server.addOnClientConnectionRequestedListener((ConnectedClient client) -> {
            if (EngineConfig.isDeveloperMode()) {
                Logger.debug("server: client connection requested");
            }
        });

        server.addOnClientConnectedListener((ConnectedClient client) -> {
            if (EngineConfig.isDeveloperMode()) {
                Logger.debug("server: client connected player=" + client.getPlayerUid());
            } else {
                Logger.debug("server: client connected");
            }
        });

        server.addOnClientDisconnectedListener((ConnectedClient client, String reason) -> {
            Logger.debug("server: client disconnected player=" + client.getPlayerUid() + " reason=" + reason);
        });
    }

    public ModdedServer getServer() {
        return server;
    }

    @Deprecated(since = "Zote")
    public com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient getClient() {
        InnerCoreServer.useClientMethod("Network.getClient()");
        return client;
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public JobExecutor getInstantJobExecutor() {
        return instantJobExecutor;
    }

    @Deprecated(since = "Zote")
    public JobExecutor getClientThreadJobExecutor() {
        return clientThreadJobExecutor;
    }

    public JobExecutor getServerThreadJobExecutor() {
        return serverThreadJobExecutor;
    }

    public void startServer(int port) {
        // TODO: shutdown();
        getConfig().updateFromEngineConfig();
        server.start(port);
    }

    public void startServer() {
        startServer(config.getDefaultPort());
    }

    @Deprecated(since = "Zote")
    public void startClient(String address, int port) throws IOException {
    }

    @Deprecated(since = "Zote")
    public void startClient(String address) throws IOException {
    }

    public void startLanServer(int port) {
        // TODO: shutdown();

        NativeNetworking.NetworkLoopHandler handler = new NativeNetworking.NetworkLoopHandler(
                NativeNetworking.NetworkLoopHandler.GLOBAL_HANDLER | NativeNetworking.NetworkLoopHandler.SERVER_HANDLER
        ).start();

        startServer(port);
        handler.stop();
    }

    public void startLanServer() {
        startLanServer(config.getDefaultPort());
    }

    @Deprecated(since = "Zote")
    public void shutdownClient() {
    }

    public void shutdownServer() {
        server.shutdown();
    }

    public void shutdown() {
        shutdownServer();
    }


    @Deprecated(since = "Zote")
    public interface ClientInitializationPacketSender {
        Object onSendingInitPacket();
    }

    public interface ServerInitializationPacketSender {
        Object onSendingInitPacket(ConnectedClient client);
    }

    public<T> void addClientInitializationPacket(String name, ClientInitializationPacketSender sender, ModdedServer.TypedInitializationPacketListener<T> receiver) {
        getServer().addInitializationPacketListener(name, receiver);
    }

    public void addClientInitializationPacket(String name, ClientInitializationPacketSender sender, ConnectedClient.InitializationPacketListener receiver) {
        getServer().addUntypedInitializationPacketListener(name, receiver);
    }

    @SuppressWarnings("deprecation")
    public<T> void addServerInitializationPacket(String name, ServerInitializationPacketSender sender, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.TypedOnPacketReceivedListener<T> receiver) {
        getServer().addOnClientConnectionRequestedListener(client -> client.send(name, sender.onSendingInitPacket(client)));
    }

    @SuppressWarnings("deprecation")
    public void addServerInitializationPacket(String name, ServerInitializationPacketSender sender, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener receiver) {
        getServer().addOnClientConnectionRequestedListener(client -> client.send(name, sender.onSendingInitPacket(client)));
    }

    @Deprecated(since = "Zote")
    public<T> void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.TypedOnPacketReceivedListener<T> listener, JobExecutor executor) {
    }

    @Deprecated(since = "Zote")
    public<T> void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.TypedOnPacketReceivedListener<T> listener) {
    }

    @Deprecated(since = "Zote")
    public void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener listener, JobExecutor executor) {
    }

    @Deprecated(since = "Zote")
    public void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener listener) {
    }

    public<T> void addServerPacket(String name, ModdedServer.TypedOnPacketReceivedListener<T> listener, JobExecutor executor) {
        if (executor != null) {
            getServer().addPacketReceivedListener(name, (ConnectedClient client, T data, String meta) -> executor.add(() -> listener.onPacketReceived(client, data, meta)));
        } else {
            getServer().addPacketReceivedListener(name, listener);
        }
    }

    public<T> void addServerPacket(String name, ModdedServer.TypedOnPacketReceivedListener<T> listener) {
        addServerPacket(name, listener, getServerThreadJobExecutor());
    }

    public void addServerPacket(String name, ModdedServer.OnPacketReceivedListener listener, JobExecutor executor) {
        if (executor != null) {
            getServer().addUntypedPacketReceivedListener(name, (ConnectedClient client, Object data, String meta, Class<?> dataType) -> executor.add(() -> listener.onPacketReceived(client, data, meta, dataType)));
        } else {
            getServer().addUntypedPacketReceivedListener(name, listener);
        }
    }

    public void addServerPacket(String name, ModdedServer.OnPacketReceivedListener listener) {
        addServerPacket(name, listener, getServerThreadJobExecutor());
    }

    @Deprecated(since = "Zote")
    public void addClientShutdownListener(com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnDisconnectedListener listener) {
    }

    public void addServerShutdownListener(ModdedServer.OnShutdownListener listener) {
        getServer().addShutdownListener(listener);
    }
}
