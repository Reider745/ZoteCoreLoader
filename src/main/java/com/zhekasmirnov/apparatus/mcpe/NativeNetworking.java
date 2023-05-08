package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.InnerCorePacket;
import com.zhekasmirnov.apparatus.multiplayer.channel.codec.StringChannelCodec;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataPacket;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.NativeDataChannel;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NativeNetworking {
    public interface ConnectionListener {
        void onNativeChannelConnected(NativeDataChannel channel, String sender);
    }

    private static final List<ConnectionListener> connectionListeners = new ArrayList<>();

    public static void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public static void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }


    public static class NativeChannelImpl {
        private static final DataPacket channelClosedPacket = new DataPacket("", 0, null);
        private static final int packetPollTimeout = 20 * 1000;
        private static final int initialPacketPollTimeout = 75 * 1000;

        private final String client;
        private boolean isClosed = false;
        private long channelOpenedTime = 0;

        private final BlockingQueue<DataPacket> packets = new LinkedBlockingQueue<>();
        private final BlockingQueue<DataPacket> pongs = new LinkedBlockingQueue<>();

        private NativeChannelImpl(String client) {
            this.client = client;
        }

        public String getClient() {
            return client;
        }

        public String getClientRepresentation() {
            return client == null ? "server" : "client:" + client;
        }

        public boolean isServerSide() {
            return client != null;
        }

        public DataPacket receive() throws IOException {
            if (channelOpenedTime == 0) {
                channelOpenedTime = System.currentTimeMillis();
            }
            try {
                // await packet
                DataPacket packet = packets.poll(packetPollTimeout, TimeUnit.MILLISECONDS);
                // if no packets were received in a long time start pinging loop
                while (packet == null) {
                    // try to ping other side
                    pongs.clear();
                    sendPing();
                    // await main packet
                    packet = packets.poll(packetPollTimeout, TimeUnit.MILLISECONDS);
                    // if pong packet was not received during this period close channel
                    DataPacket pongPacket = pongs.poll(0, TimeUnit.MILLISECONDS);
                    if (packet == null && pongPacket == null && System.currentTimeMillis() > channelOpenedTime + initialPacketPollTimeout) {
                        Logger.debug("native channel \"" + getClientRepresentation() + "\" has not responded for a ping in " + packetPollTimeout + " milliseconds, it will be closed");
                        isClosed = true;
                        unlink();
                        throw new IOException("native channel \"" + getClientRepresentation() + "\" closed (no response for a ping)");
                    }
                }
                if (packet.equals(channelClosedPacket)) {
                    isClosed = true;
                    unlink();
                    throw new IOException("native channel \"" + getClientRepresentation() + "\" closed (manually)");
                }
                return packet;
            } catch (InterruptedException e) {
                isClosed = true;
                unlink();
                throw new IOException("native channel \"" + getClientRepresentation() + "\" closed (thread interrupted)", e);
            }
        }

        public void sendPing() throws IOException {
            send(new DataPacket("system.native_ping", StringChannelCodec.FORMAT_ID, new byte[0]));
        }

        public boolean pingPong(int timeout) throws Exception {
            pongs.clear();
            sendPing();
            return pongs.poll(timeout, TimeUnit.MILLISECONDS) != null;
        }

        private void onReceived(DataPacket packet) throws IOException {
            // UserDialog.toast("received " + packet.name + "=" + new String(packet.data) + " from " + client);
            try {
                if ("system.native_ping".equals(packet.name)) {
                    send(new DataPacket("system.native_pong", StringChannelCodec.FORMAT_ID, new byte[0]));
                } else if ("system.native_pong".equals(packet.name)) {
                    pongs.put(packet);
                } else {
                    packets.put(packet);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void close()  {
            try {
                isClosed = true;
                packets.put(channelClosedPacket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void unlink() {
            if (client != null) {
                synchronized (serverToClientChannelMap) {
                    serverToClientChannelMap.remove(client);
                }
            } else {
                clientToServerChannel = null;
            }
        }

        public void closeAndUnlink() {
            close();
            unlink();
        }

        public boolean isClosed() {
            return isClosed;
        }

        public void send(DataPacket packet) throws IOException{
            // UserDialog.toast("sending " + packet.name + "=" + (packet.data != null ? new String(packet.data) : null) + " to " + client);
            if (client != null) {
                sendPacketToClient(client, packet.name, packet.formatId, packet.data);
            } else {
                sendPacketToServer(packet.name, packet.formatId, packet.data);
            }
        }
    }

    private static NativeChannelImpl clientToServerChannel = null;
    private static final Map<String, NativeChannelImpl> serverToClientChannelMap = new HashMap<>();

    public static NativeChannelImpl getClientToServerChannel() {
        return clientToServerChannel;
    }

    public static NativeChannelImpl getOrCreateClientToServerChannel() {
        if (clientToServerChannel == null) {
            clientToServerChannel = new NativeChannelImpl(null);
        }
        return clientToServerChannel;
    }

    public static NativeChannelImpl getServerToClientChannel(String client) {
        return serverToClientChannelMap.get(client);
    }


    public static void onLevelLeft(boolean isServer) {
        synchronized (serverToClientChannelMap) {
            if (isServer) {
                for (NativeChannelImpl channel : serverToClientChannelMap.values()) {
                    channel.close();
                }
                serverToClientChannelMap.clear();
            } else {
                if (clientToServerChannel != null) {
                    clientToServerChannel.close();
                    clientToServerChannel = null;
                }
            }
        }
    }

    public static void onServerPacketReceived(String sender, String name, int formatId) throws IOException {
        synchronized (serverToClientChannelMap) {
            Java8BackComp.computeIfAbsent(serverToClientChannelMap, sender, key -> {
                NativeChannelImpl channel = new NativeChannelImpl(sender);
                for (ConnectionListener listener : connectionListeners) {
                    listener.onNativeChannelConnected(new NativeDataChannel(channel), sender);
                }
                return channel;
            }).onReceived(new DataPacket(name, formatId, getCurrentNativePacketBytesNonNull()));
        }
    }

    public static void onClientPacketReceived(String name, int formatId) throws IOException {
        if (clientToServerChannel == null) {
            clientToServerChannel = new NativeChannelImpl(null);
        }
        clientToServerChannel.onReceived(new DataPacket(name, formatId, getCurrentNativePacketBytesNonNull()));
    }

    private static byte[] getCurrentNativePacketBytesNonNull() {
        return getCurrentNativePacketBytes();
    }


    public static class NetworkLoopHandler {
        public static final int GLOBAL_HANDLER = 1;
        public static final int SERVER_HANDLER = 2;
        public static final int CLIENT_HANDLER = 4;

        private final int domain;
        private final int updateDelay;
        private final boolean boolPar;

        public NetworkLoopHandler(int domain, int updateDelay, boolean boolPar) {
            this.domain = domain;
            this.updateDelay = updateDelay;
            this.boolPar = boolPar;
        }

        public NetworkLoopHandler(int domain, boolean boolPar) {
            this(domain, 100, boolPar);
        }

        public NetworkLoopHandler(int domain) {
            this(domain, false);
        }

        private int sessionId = 0;
        private Thread thread = null;

        public NetworkLoopHandler start() {
            stop();
            int session = sessionId;

            thread = new Thread(() -> {
                while (session == sessionId) {
                    if ((domain & GLOBAL_HANDLER) != 0) {
                        runMinecraftNetworkEventLoop(boolPar);
                    }
                    if ((domain & SERVER_HANDLER) != 0) {
                        runServerNetworkEventLoop(boolPar);
                    }
                    if ((domain & CLIENT_HANDLER) != 0) {
                        runClientNetworkEventLoop(boolPar);
                    }
                    try {
                        //noinspection BusyWait
                        Thread.sleep(updateDelay);
                    } catch (InterruptedException ignore) { }
                }
            });
            thread.start();

            return this;
        }

        public NetworkLoopHandler stop() {
            sessionId++;
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            return this;
        }
    }

    // native methods

    private static void runServerNetworkEventLoop(boolean b){

    }
    private static void runClientNetworkEventLoop(boolean b){

    }
    private static void runMinecraftNetworkEventLoop(boolean b){

    }

    private static byte[] getCurrentNativePacketBytes(){
        return InnerCorePacket.getCurrentNativePacketBytes();
    }
    private static void sendPacketToClient(String client, String name, int formatId, byte[] data){
        InnerCorePacket.sendPacketToClient(client, name, formatId, data);
    }
    private static void sendPacketToServer(String name, int formatId, byte[] data){
        InnerCorePacket.sendPacketToServer(name, formatId, data);
    }
}
