package com.zhekasmirnov.apparatus.multiplayer.socket;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannelFactory;
import com.zhekasmirnov.apparatus.multiplayer.server.ModdedServer;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import cn.nukkit.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer {
    private final Object lock = new Object();

    private ServerSocket serverSocket = null;
    private boolean isRunning = false;
    @SuppressWarnings("unused")
    private ModdedServer server;

    public SocketServer(ModdedServer server) {
        if (server == null) {
            throw new NullPointerException("server == null");
        }
        this.server = server;
    }

    @Deprecated
    public SocketServer() {
        this(Network.getSingleton().getServer());
    }

    private final List<IClientConnectListener> clientConnectListeners = new ArrayList<>();

    public interface IClientConnectListener {
        boolean onClientConnected(DataChannel channel, Socket socket, boolean isChannelAlreadyManaged);
    }

    public void addClientConnectListener(IClientConnectListener listener) {
        clientConnectListeners.add(listener);
    }

    public void start(int port) throws IOException {
        synchronized (lock) {
            if (isRunning) {
                throw new IllegalStateException("SocketServer is already running");
            }
            isRunning = true;
            serverSocket = new ServerSocket(port, Server.getInstance().getMaxPlayers());
            new Thread(() -> {
                ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);
                while (isRunning) {
                    Socket socket;
                    try {
                        socket = serverSocket.accept();
                    } catch (Exception exception) {
                        continue;
                    }

                    if (InnerCoreServer.isDebugInnerCoreNetwork())
                        System.out.println("client connected via socket at ip=" + socket.getInetAddress() + " port=" + port);

                    try {
                        int protocol = socket.getInputStream().read();
                        DataChannel channel = DataChannelFactory.newDataChannelViaSupportedProtocol(socket, protocol);

                        if (channel == null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        boolean isChannelManaged = false;
                        for (IClientConnectListener listener : clientConnectListeners) {
                            if (listener.onClientConnected(channel, socket, isChannelManaged)) {
                                isChannelManaged = true;
                            }
                        }
                    } catch (IOException exception) {
                        Logger.error("socket server error", "error in creating channel", exception);
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public void close() {
        synchronized (lock) {
            isRunning = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket = null;
            }
        }
    }
}
