package com.zhekasmirnov.apparatus.multiplayer.util.list;

import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

import java.lang.ref.WeakReference;


/**
 * Stores reference to single connected client, as client is closed, it will always return null
 */
public class ConnectedClientRef {
    private WeakReference<ConnectedClient> client;

    public ConnectedClientRef(ConnectedClient client) {
        this.client = new WeakReference<>(client);
    }

    public ConnectedClient get() {
        ConnectedClient client = this.client.get();
        if (client != null && client.isClosed()) {
            client = null;
            this.client = new WeakReference<>(null);
        }
        return client;
    }

    public boolean has() {
        ConnectedClient client = this.client.get();
        return client != null && !client.isClosed();
    }
}
