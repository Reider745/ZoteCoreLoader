package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketDataChannel extends DataChannel {
    public static final int PROTOCOL_ID = 1;

    public final Socket socket;
    public final DataInputStream inputStream;
    public final DataOutputStream outputStream;

    public SocketDataChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        socket.setKeepAlive(true);
    }

    @Override
    public void sendImpl(DataPacket packet) throws IOException {
        outputStream.writeUTF(packet.name);
        outputStream.writeByte(packet.formatId);
        outputStream.writeInt(packet.data.length);
        outputStream.write(packet.data);
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
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.shutdownOutput();
            socket.shutdownInput();
        } finally {
            socket.close();
        }
    }

    @Override
    public int getProtocolId() {
        return PROTOCOL_ID;
    }
}
