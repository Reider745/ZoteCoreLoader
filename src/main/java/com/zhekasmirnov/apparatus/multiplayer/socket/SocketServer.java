package com.zhekasmirnov.apparatus.multiplayer.socket;

import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannelFactory;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer {
    private final Object lock = new Object();

    private ServerSocket serverSocket = null;
    private boolean isRunning = false;

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
            serverSocket = new ServerSocket(port);
            new Thread(() -> {
                ThreadTypeMarker.markThreadAs(ThreadTypeMarker.Mark.SERVER);
                 while (isRunning) {
                     Socket socket;
                     try {
                         socket = serverSocket.accept();
                     } catch (IOException exception) {
                         continue;
                     }

                     try {
                         int protocol = socket.getInputStream().read();
                         DataChannel channel = DataChannelFactory.newDataChannel(socket, protocol);

                         boolean isChannelManaged = false;
                         for (IClientConnectListener listener : clientConnectListeners) {
                             if (listener.onClientConnected(channel, socket, isChannelManaged)) {
                                 isChannelManaged = true;
                             }
                         }
                     } catch (IOException exception) {
                         ICLog.e("socket server error", "error in creating channel", exception);
                         exception.printStackTrace();
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
