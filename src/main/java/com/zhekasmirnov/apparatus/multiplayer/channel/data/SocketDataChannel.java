package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import com.reider745.network.innercore.PlayerSocketSendingThread;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketDataChannel extends DataChannel {
    public static final int PROTOCOL_ID = 1;

    public final Socket socket;

    private final DataInputStream inputStream;
    public final PlayerSocketSendingThread sendingThread;


    public SocketDataChannel(Socket socket) throws IOException {
        this.socket = socket;
        socket.setKeepAlive(true);
        inputStream = new DataInputStream(socket.getInputStream());
        this.sendingThread = new PlayerSocketSendingThread(socket, this);
    }

    @Override
    public void sendImpl(DataPacket packet) throws IOException {
        sendingThread.send(packet);
    }

    @Override
    public DataPacket receiveImpl() throws IOException {
        String name = inputStream.readUTF();
        int formatId = inputStream.readByte();
        int size = inputStream.readInt();

        if (size < 0 || size > 1024 * 1024 * 64) {
            throw new IOException("packet has invalid size " + size);
        }

        byte[] data = new byte[size];
        inputStream.readFully(data);
        return new DataPacket(name, formatId, data);
    }

    @Override
    public void closeImpl() throws IOException {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendingThread.close();
        try {
            socket.shutdownOutput();
            socket.shutdownInput();
        }catch (Exception ignore){} finally {
            socket.close();
        }
    }

    @Override
    protected boolean canSendingLock() {
        return false;
    }

    @Override
    public int getProtocolId() {
        return PROTOCOL_ID;
    }

    @Override
    public String getClient() {
        return socket.getInetAddress().toString();
    }
}
