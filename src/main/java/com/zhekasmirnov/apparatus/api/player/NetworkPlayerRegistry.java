package com.zhekasmirnov.apparatus.api.player;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.NativeAPI;
//import com.zhekasmirnov.innercore.api.runtime.Callback;

import java.util.HashMap;
import java.util.Map;

public class NetworkPlayerRegistry {
    private static final NetworkPlayerRegistry singleton = new NetworkPlayerRegistry();

    public static NetworkPlayerRegistry getSingleton() {
        return singleton;
    }

    static {
        Network.getSingleton().getServer().addOnClientConnectedListener(client -> getSingleton().addHandlerFor(client));
        Network.getSingleton().getServer().addOnClientDisconnectedListener((client, reason) -> getSingleton().removeHandlerFor(client));
    }

    public static void loadClass() {
        // forces class to load and register listeners
    }


    private final Map<Long, NetworkPlayerHandler> handlerMap = new HashMap<>();
    private NetworkPlayerHandler localPlayerHandler = null;

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
                //Network.getSingleton().getServerThreadJobExecutor().add(() -> Callback.invokeAPICallback("ServerPlayerLeft", handler.getActor().getUid()));
            }
        }
    }

    public NetworkPlayerHandler getHandlerFor(long uid) {
        return handlerMap.get(uid);
    }

    public NetworkPlayerHandler getLocalPlayerHandler() {
        return localPlayerHandler;
    }

    // callbacks

    public void onTick() {
        synchronized (handlerMap) {
            for (NetworkPlayerHandler handler : handlerMap.values()) {
                handler.onTick();
            }
        }
    }

    public void onLocalTick() {
        /*if (localPlayerHandler == null) {
            long player = NativeAPI.getLocalPlayer();
            if (NativeAPI.getHealth(player) > 0) {
                localPlayerHandler = new NetworkPlayerHandler(player, true);
            }
        }
        NetworkPlayerHandler handler = localPlayerHandler;
        if (handler != null) {
            handler.onTick();
        }*/
    }

    public void onGameLeft(boolean isServer) {
        if (!isServer) {
            localPlayerHandler = null;
        }
    }

    public void onEntityHurt(long entityUid, long attacker, int damageValue, int damageType, boolean bool1, boolean bool2) {
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
