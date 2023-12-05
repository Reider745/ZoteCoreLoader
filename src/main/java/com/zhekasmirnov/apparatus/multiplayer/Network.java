package com.zhekasmirnov.apparatus.multiplayer;

import cn.nukkit.Player;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.adapter.innercore.game.Minecraft;
import com.zhekasmirnov.apparatus.job.InstantJobExecutor;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.job.MainThreadJobExecutor;
//import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannelFactory;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.NativeDataChannel;
import com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ModdedServer;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static final Network singleton = new Network();

    public static Network getSingleton() {
        return singleton;
    }


    private final NetworkConfig config = new NetworkConfig();
    private final ModdedServer server = new ModdedServer(config);
    private final ModdedClient client = new ModdedClient(config);
    private boolean isRunningLanServer = false;

    private final JobExecutor instantJobExecutor = new InstantJobExecutor("Default Instant Network Executor");
    private final JobExecutor serverThreadJobExecutor = new MainThreadJobExecutor(MainThreadQueue.serverThread, "Default Server Network Executor");
    private final JobExecutor clientThreadJobExecutor = new MainThreadJobExecutor(MainThreadQueue.localThread, "Default Client Network Executor");

    private Network() {
        client.addOnRequestingConnectionListener(() -> {
            if (EngineConfig.isDeveloperMode()) {
                Logger.debug("client: requesting connection");
            }
        });

        client.addOnConnectedListener(() -> {
            Logger.debug("client: connected to server");
        });

        client.addOnDisconnectedListener(reason -> {
            if (!isRunningLanServer) {
                Logger.debug("Disconnected By Server");
            }
            Minecraft.leaveGame();
        });

        server.addOnClientConnectionRequestedListener((ConnectedClient client) -> {
            JSONObject object = new JSONObject();
            object.put("fix", true);
            client.send("server_fixed.inventory", object);
            //Network.getSingleton().addClientInitializationPacket("server_fixed.inventory", () -> object, (v ,v1) -> {});
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
            Logger.debug("client disconnected, reason: " + reason);
            if (EngineConfig.isDeveloperMode()) {
                Logger.debug("server: client disconnected player=" + client.getPlayerUid() + " reason=" + reason);
            } else {
                Logger.debug("server: client disconnected reason=" + reason);
            }
        });
    }

    public ModdedServer getServer() {
        return server;
    }

    public ModdedClient getClient() {
        return client;
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public JobExecutor getInstantJobExecutor() {
        return instantJobExecutor;
    }

    public JobExecutor getClientThreadJobExecutor() {
        return clientThreadJobExecutor;
    }

    public JobExecutor getServerThreadJobExecutor() {
        return serverThreadJobExecutor;
    }

    public void startServer(int port) {
        //shutdown();
        getConfig().updateFromEngineConfig();
        server.start(port);
    }

    public void startServer() {
        startServer(config.getDefaultPort());
    }

    private DataChannel tryOpenClientToServerSocketDataChannel(String address, int port) throws IOException {
        Socket socket = new Socket(address, port);
        int protocolId = config.getClientProtocolId();
        socket.getOutputStream().write(protocolId);
        return DataChannelFactory.newDataChannel(socket, protocolId);
    }

    private DataChannel tryOpenClientToServerNativeDataChannel(int timeout) throws IOException {
        NativeDataChannel channel = new NativeDataChannel(NativeNetworking.getOrCreateClientToServerChannel());
        if (!channel.pingPong(timeout)) {
            throw new IOException("failed to open modded native channel");
        }
        return channel;
    }

    public void startClient(String address, int port) throws IOException {
        shutdown();
        isRunningLanServer = false;

        NativeNetworking.NetworkLoopHandler handler = new NativeNetworking.NetworkLoopHandler(
                NativeNetworking.NetworkLoopHandler.GLOBAL_HANDLER | NativeNetworking.NetworkLoopHandler.CLIENT_HANDLER
        ).start();

        getConfig().updateFromEngineConfig();

        DataChannel channel;
        if (config.isNativeProtocolPrioritizedForRemoteConnection()) {
            try {
                channel = tryOpenClientToServerNativeDataChannel(getConfig().getNativeChannelPingPongTimeout());
            } catch (IOException e) {
                if (EngineConfig.isDeveloperMode()) {
                    //UserDialog.toast("failed to connect via native protocol, trying socket connection");
                }
                channel = tryOpenClientToServerSocketDataChannel(address, port);
            }
        } else {
            try {
                channel = tryOpenClientToServerSocketDataChannel(address, port);
            } catch (IOException e) {
                if (EngineConfig.isDeveloperMode()) {
                   // UserDialog.toast("failed to connect via socket, trying native protocol");
                }
                channel = tryOpenClientToServerNativeDataChannel(getConfig().getNativeChannelPingPongTimeout());
            }
        }

        if (channel instanceof NativeDataChannel) {
            //UserDialog.toast("connected to " + address + " via native protocol");
        } else {
            //UserDialog.toast("connected to " + address + " via socket at port " + port);
        }

        client.start(channel);

        if (!client.awaitAllInitializationPackets(config.getInitializationTimeout())) {
            //UserDialog.dialog("Connection Error", "Failed to connect to server - not all initialization packets were sent to client");
            handler.stop();
            shutdown();
        } else {
            //UserDialog.toast("successfully started client");
        }

        handler.stop();
    }

    public void startClient(String address) throws IOException {
        startClient(address, config.getDefaultPort());
    }

    public void startLanServer(int port) {
        //shutdown();

        NativeNetworking.NetworkLoopHandler handler = new NativeNetworking.NetworkLoopHandler(
                NativeNetworking.NetworkLoopHandler.GLOBAL_HANDLER | NativeNetworking.NetworkLoopHandler.SERVER_HANDLER | NativeNetworking.NetworkLoopHandler.CLIENT_HANDLER
        ).start();

        startServer(port);
        //client.start(server.openLocalClientChannel());
        isRunningLanServer = true;

        if (!client.awaitAllInitializationPackets(config.getInitializationTimeout())) {
            if (!client.isRunning()) {
                ICLog.i("LAN Server Error", "Failed to startup LAN server - not all initialization packets were sent to client");
            }
            handler.stop();
            shutdown();
        }
        handler.stop();
    }

    public void startLanServer() {
        startLanServer(config.getDefaultPort());
    }

    public void shutdownClient() {
        client.shutdown();
    }

    public void shutdownServer() {
        server.shutdown();
    }

    public void shutdown() {
        shutdownClient();
        shutdownServer();
    }


    public interface ClientInitializationPacketSender {
        Object onSendingInitPacket();
    }

    public interface ServerInitializationPacketSender {
        Object onSendingInitPacket(ConnectedClient client);
    }

    public<T> void addClientInitializationPacket(String name, ClientInitializationPacketSender sender, ModdedServer.TypedInitializationPacketListener<T> receiver) {
        final ModdedClient client = getClient();
        client.addOnRequestingConnectionListener(() -> client.send(name, sender.onSendingInitPacket()));
        getServer().addInitializationPacketListener(name, receiver);
    }

    public void addClientInitializationPacket(String name, ClientInitializationPacketSender sender, ConnectedClient.InitializationPacketListener receiver) {
        final ModdedClient client = getClient();
        client.addOnRequestingConnectionListener(() -> client.send(name, sender.onSendingInitPacket()));
        getServer().addUntypedInitializationPacketListener(name, receiver);
    }

    public<T> void addServerInitializationPacket(String name, ServerInitializationPacketSender sender, ModdedClient.TypedOnPacketReceivedListener<T> receiver) {
        getServer().addOnClientConnectionRequestedListener(client -> client.send(name, sender.onSendingInitPacket(client)));
        getClient().addInitializationPacketListener(name, receiver);
    }

    public void addServerInitializationPacket(String name, ServerInitializationPacketSender sender, ModdedClient.OnPacketReceivedListener receiver) {
        getServer().addOnClientConnectionRequestedListener(client -> client.send(name, sender.onSendingInitPacket(client)));
        getClient().addUntypedInitializationPacketListener(name, receiver);
    }

    public<T> void addClientPacket(String name, ModdedClient.TypedOnPacketReceivedListener<T> listener, JobExecutor executor) {
        if (executor != null) {
            getClient().addPacketReceivedListener(name, (T data, String meta) -> executor.add(() -> listener.onPacketReceived(data, meta)));
        } else {
            getClient().addPacketReceivedListener(name, listener);
        }
    }

    public<T> void addClientPacket(String name, ModdedClient.TypedOnPacketReceivedListener<T> listener) {
        addClientPacket(name, listener, getClientThreadJobExecutor());
    }

    public void addClientPacket(String name, ModdedClient.OnPacketReceivedListener listener, JobExecutor executor) {
        if (executor != null) {
            getClient().addUntypedPacketReceivedListener(name, (Object data, String meta, Class<?> dataType) -> executor.add(() -> listener.onPacketReceived(data, meta, dataType)));
        } else {
            getClient().addUntypedPacketReceivedListener(name, listener);
        }
    }

    public void addClientPacket(String name, ModdedClient.OnPacketReceivedListener listener) {
        addClientPacket(name, listener, getClientThreadJobExecutor());
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

    public void addClientShutdownListener(ModdedClient.OnDisconnectedListener listener) {
        getClient().addOnDisconnectedListener(listener);
    }

    public void addServerShutdownListener(ModdedServer.OnShutdownListener listener) {
        getServer().addShutdownListener(listener);
    }
}
