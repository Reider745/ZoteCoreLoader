package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import java.io.IOException;
import java.net.Socket;

public class DataChannelFactory {
    public static DataChannel newDataChannel(Socket socket, int protocolId) throws IOException {
        switch (protocolId) {
            case DirectDataChannel.PROTOCOL_ID:
                throw new IOException("failed to create DataChannel: DirectDataChannel is not a network channel protocol");
            case SocketDataChannel.PROTOCOL_ID:
                return new SocketDataChannel(socket);
        }
        throw new IOException("failed to create DataChannel: invalid protocol id " + protocolId);
    }
}
