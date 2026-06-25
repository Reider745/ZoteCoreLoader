package org.innercore.icstd.network;

public interface INetPacket {
    String getPacketType();  // must be a valid serializer id
    void process(NetworkClient fromClient, String meta);
}
