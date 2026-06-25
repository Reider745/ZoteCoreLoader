package org.innercore.icstd.network;

import org.innercore.icstd.serialization.IScopedReader;
import org.innercore.icstd.serialization.SerializationContext;

public interface IPacketListener {
    void process(NetworkClient fromClient, IScopedReader io, SerializationContext ctx, String meta);
}
