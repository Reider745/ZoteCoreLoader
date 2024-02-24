package com.zhekasmirnov.apparatus.multiplayer;

import com.zhekasmirnov.apparatus.adapter.innercore.game.Minecraft;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerHandler;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.InitializationPacketException;
import com.zhekasmirnov.apparatus.multiplayer.server.ModdedServer;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.NativeArray;

public class NetworkJsAdapter {
    private final Network network;

    public NetworkJsAdapter(Network network) {
        this.network = network;
    }

    public Network getNetworkInstance() {
        return network;
    }

    public ModdedServer getServer() {
        return network.getServer();
    }

    @Deprecated(since = "Zote")
    public com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient getClient() {
        return network.getClient();
    }

    public NativeArray getConnectedClients() {
        return ScriptableObjectHelper.createArray(network.getServer().getConnectedClients());
    }

    public NativeArray getConnectedPlayers() {
        return ScriptableObjectHelper.createArray(network.getServer().getConnectedPlayers());
    }

    public ConnectedClient getClientForPlayer(long player) {
        return network.getServer().getConnectedClientForPlayer(player);
    }

    public NetworkPlayerHandler getHandlerForPlayer(long player) {
        return NetworkPlayerRegistry.getSingleton().getHandlerFor(player);
    }

    @Deprecated(since = "Zote")
    public void addClientInitializationPacket(String name, Network.ClientInitializationPacketSender sender,
            ConnectedClient.InitializationPacketListener receiver) {
        network.addClientInitializationPacket(name, sender, receiver);
    }

    @SuppressWarnings("deprecation")
    public void addServerInitializationPacket(String name, Network.ServerInitializationPacketSender sender,
            com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener receiver) {
        network.addServerInitializationPacket(name, sender, receiver);
    }

    @Deprecated(since = "Zote")
    public void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener listener, JobExecutor executor) {
        network.addClientPacket(name, listener, executor);
    }

    @Deprecated(since = "Zote")
    public void addClientPacket(String name, com.zhekasmirnov.apparatus.multiplayer.client.ModdedClient.OnPacketReceivedListener listener) {
        network.addClientPacket(name, listener);
    }

    public void addServerPacket(String name, ModdedServer.OnPacketReceivedListener listener, JobExecutor executor) {
        network.addServerPacket(name, listener, executor);
    }

    public void addServerPacket(String name, ModdedServer.OnPacketReceivedListener listener) {
        network.addServerPacket(name, listener);
    }

    public void sendToAllClients(String packetName, Object data) {
        ThreadTypeMarker.assertServerThread();
        network.getServer().sendToAll(packetName, data);
    }

    @Deprecated(since = "Zote")
    public void sendToServer(String packetName, Object data) {
        ThreadTypeMarker.assertClientThread();
        network.getClient().send(packetName, data);
    }

    public void sendServerMessage(String message) {
        ThreadTypeMarker.assertServerThread();
        network.getServer().sendMessageToAll(message);
    }

    @Deprecated(since = "Zote")
    public int serverToLocalId(int id) {
        return IdConversionMap.serverToLocal(id);
    }

    @Deprecated(since = "Zote")
    public int localToServerId(int id) {
        return IdConversionMap.localToServer(id);
    }

    public boolean inRemoteWorld() {
        return Minecraft.getGameState() == Minecraft.GameState.REMOTE_WORLD;
    }

    public void throwInitializationPacketError(String message) throws InitializationPacketException {
        throw new InitializationPacketException(message);
    }
}
