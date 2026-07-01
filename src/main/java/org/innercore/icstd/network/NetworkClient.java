package org.innercore.icstd.network;

import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import org.innercore.icstd.annotations.ApiHidden;

// we can't just move ConnectedClient here, as it is used and not easily stubbed,
// and besides, there is not much to use of its contents in ICSTD, so make a mirror
public class NetworkClient {
    final ConnectedClient impl;
    @ApiHidden public NetworkClient(ConnectedClient impl) { this.impl = impl; }
}
