package org.innercore.icstd.network;

public interface INetPacket {
    // must be a valid serializer id, registered with an object type implementing INetPacket,
    // packets of other types are not deserialized on receive
    String getPacketType();

    void process(NetworkClient fromClient, String meta);
}
