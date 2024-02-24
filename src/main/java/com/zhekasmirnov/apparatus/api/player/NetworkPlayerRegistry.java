package com.zhekasmirnov.apparatus.api.player;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.runtime.Callback;

import java.util.HashMap;
import java.util.Map;

public class NetworkPlayerRegistry {
    private static final NetworkPlayerRegistry singleton = new NetworkPlayerRegistry();

    public static NetworkPlayerRegistry getSingleton() {
        return singleton;
    }

    static {
        Network.getSingleton().getServer().addOnClientConnectedListener(client -> getSingleton().addHandlerFor(client));
        Network.getSingleton().getServer()
                .addOnClientDisconnectedListener((client, reason) -> getSingleton().removeHandlerFor(client));
    }

    public static void loadClass() {
        // forces class to load and register listeners
    }

    private final Map<Long, NetworkPlayerHandler> handlerMap = new HashMap<>();

    private NetworkPlayerRegistry() {
    }

    private void addHandlerFor(ConnectedClient client) {
        synchronized (handlerMap) {
            handlerMap.put(client.getPlayerUid(), new NetworkPlayerHandler(client.getPlayerUid(), false));
        }
    }

    private void removeHandlerFor(ConnectedClient client) {
        synchronized (handlerMap) {
            NetworkPlayerHandler handler = handlerMap.remove(client.getPlayerUid());
            if (handler != null) {
                Network.getSingleton().getServerThreadJobExecutor()
                        .add(() -> Callback.invokeAPICallback("ServerPlayerLeft", handler.getActor().getUid()));
            }
        }
    }

    public NetworkPlayerHandler getHandlerFor(long uid) {
        return handlerMap.get(uid);
    }

    @Deprecated(since = "Zote")
    public NetworkPlayerHandler getLocalPlayerHandler() {
        return null;
    }

    // callbacks

    public void onTick() {
        synchronized (handlerMap) {
            for (NetworkPlayerHandler handler : handlerMap.values()) {
                handler.onTick();
            }
        }
    }

    @Deprecated(since = "Zote")
    public void onLocalTick() {
    }

    public void onGameLeft(boolean isServer) {
    }

    public void onEntityHurt(long entityUid, long attacker, int damageValue, int damageType, boolean bool1,
            boolean bool2) {
        NetworkPlayerHandler handler = handlerMap.get(entityUid);
        if (handler != null) {
            handler.onHurt(attacker, damageValue, damageType, bool1, bool2);
        }
    }

    public void onPlayerEat(long playerUid, int food, float ratio) {
        NetworkPlayerHandler handler = handlerMap.get(playerUid);
        if (handler != null) {
            handler.onEat(food, ratio);
        }
    }
}
