package com.zhekasmirnov.apparatus.multiplayer.util.list;

import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.StaticEntity;
import com.zhekasmirnov.apparatus.adapter.innercore.game.common.Vector3;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

/**
 * Stores references to a list of clients, iterates only over connected clients, all disconnected ones are dropped
 */
public class ConnectedClientList implements Iterable<ConnectedClient> {
    private static final List<WeakReference<ConnectedClientList>> allClientLists = new ArrayList<>();

    private static void globalRefresh() {
        List<ConnectedClientList> listsToRefresh = new ArrayList<>();
        synchronized (allClientLists) {
            Java8BackComp.removeIf(allClientLists, ref -> {
                ConnectedClientList list = ref.get();
                if (list != null) {
                    listsToRefresh.add(list);
                    return false;
                }
                return true;
            });
        }
        for (ConnectedClientList list : listsToRefresh) {
            list.forcedRefresh();
        }
    }

    private static void clearOnShutdown() {
        synchronized (allClientLists) {
            allClientLists.clear();
        }
    }

    static {
        JobExecutor executor = Network.getSingleton().getServerThreadJobExecutor();
        Network.getSingleton().getServer().addOnClientConnectedListener(client -> executor.add(ConnectedClientList::globalRefresh));
        Network.getSingleton().getServer().addOnClientDisconnectedListener((ConnectedClient client, String reason) -> executor.add(ConnectedClientList::globalRefresh));
        Network.getSingleton().getServer().addShutdownListener(server -> clearOnShutdown());
    }


    private final Set<ConnectedClient> clients = new HashSet<>();

    public interface ClientConsumer {
        void accept(ConnectedClient client);
    }

    public interface Policy {
        boolean check(ConnectedClient client);
    }

    public interface Listener {
        void onAdd(ConnectedClient client);
        void onRemove(ConnectedClient client);
    }

    private final List<Listener> listeners = new ArrayList<>();

    private Policy addPolicy = null;
    private int addPolicyTimeout = 200;
    private long addPolicyNextRefresh = 0;
    private Policy removePolicy = null;
    private int removePolicyTimeout = 10;
    private long removePolicyNextRefresh = 0;

    public ConnectedClientList(boolean addToGlobalRefreshList) {
        if (addToGlobalRefreshList) {
            synchronized (allClientLists) {
                allClientLists.add(new WeakReference<>(this));
            }
        }
    }

    public ConnectedClientList() {
        this(true);
    }

    public void add(ConnectedClient client) {
        if (client != null && !client.isClosed()) {
            synchronized (this) {
                if (removePolicy == null || !removePolicy.check(client)) {
                    clients.add(client);
                    for (Listener listener : listeners) {
                        listener.onAdd(client);
                    }
                }
            }
        }
    }

    public void remove(ConnectedClient client) {
        synchronized (this) {
            if (clients.remove(client)) {
                for (Listener listener : listeners) {
                    listener.onRemove(client);
                }
            }
        }
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void setAddPolicy(Policy addPolicy, int timeout, boolean refresh) {
        this.addPolicy = addPolicy;
        this.addPolicyTimeout = timeout;
        if (refresh) {
            forcedRefresh();
        }
    }

    public void setAddPolicy(Policy addPolicy, int timeout) {
        setAddPolicy(addPolicy, timeout, true);
    }

    public void setAddPolicy(Policy addPolicy) {
        setAddPolicy(addPolicy, addPolicyTimeout, true);
    }

    public void setAddPolicyTimeout(int addPolicyTimeout) {
        this.addPolicyTimeout = addPolicyTimeout;
    }

    public void setRemovePolicy(Policy removePolicy, int timeout, boolean refresh) {
        this.removePolicy = removePolicy;
        this.removePolicyTimeout = timeout;
        if (refresh) {
            forcedRefresh();
        }
    }

    public void setRemovePolicy(Policy removePolicy, int timeout) {
        setRemovePolicy(removePolicy, timeout, true);
    }

    public void setRemovePolicy(Policy removePolicy) {
        setRemovePolicy(removePolicy, removePolicyTimeout, true);
    }

    public void setRemovePolicyTimeout(int removePolicyTimeout) {
        this.removePolicyTimeout = removePolicyTimeout;
    }

    public void forcedRefresh() {
        addPolicyNextRefresh = 0;
        removePolicyNextRefresh = 0;
        refresh();
    }

    public void refresh() {
        synchronized (this) {
            long time = System.currentTimeMillis();
            if (removePolicyNextRefresh <= time) {
                Java8BackComp.removeIf(clients, client -> {
                    if (client.isClosed() || (removePolicy != null && removePolicy.check(client))) {
                        for (Listener listener : listeners) {
                            listener.onRemove(client);
                        }
                        return true;
                    }
                    return false;
                });
                removePolicyNextRefresh = time + removePolicyTimeout;
            }

            if (addPolicy != null && addPolicyNextRefresh <= time) {
                for (ConnectedClient client : Network.getSingleton().getServer().getConnectedClients()) {
                    if (!clients.contains(client) && addPolicy.check(client)) {
                        clients.add(client);
                        for (Listener listener : listeners) {
                            listener.onAdd(client);
                        }
                    }
                }
                addPolicyNextRefresh = time + addPolicyTimeout;
            }
        }
    }

    @Override
    public Iterator<ConnectedClient> iterator() {
        refresh();
        return clients.iterator();
    }

    public void forEach(Consumer<? super ConnectedClient> action) {
        refresh();
        synchronized (this) {
            for (ConnectedClient client : clients) {
                action.accept(client);
            }
        }
    }

    // implementation for js
    public void forEachClient(ClientConsumer consumer) {
        refresh();
        synchronized (this) {
            for (ConnectedClient client : clients) {
                consumer.accept(client);
            }
        }
    }

    public boolean contains(ConnectedClient client) {
        return clients.contains(client);
    }

    public void clear() {
        if (!listeners.isEmpty()) {
            forEach(client -> {
                for (Listener listener : listeners) {
                    listener.onRemove(client);
                }
            });
        }
        clients.clear();
    }

    public void dropPoliciesAndClear() {
        setAddPolicy(null);
        setRemovePolicy(null);
        clear();
    }

    public void send(String name, Object data) {
        forEach(client -> client.send(name, data));
    }

    public<T> void send(String name, T data, Class<T> type) {
        forEach(client -> client.send(name, data, type));
    }


    // common policies for easier usage and better performance

    public ConnectedClientList setupAllPlayersPolicy(int updateRate) {
        setAddPolicy(client -> true, updateRate, false);
        setRemovePolicy(null);
        return this;
    }

    public ConnectedClientList setupAllPlayersPolicy() {
        return setupAllPlayersPolicy(5000);
    }

    public ConnectedClientList setupAllInDimensionPolicy(int dimension, int updateRate) {
        setAddPolicy(client -> StaticEntity.getDimension(client.getPlayerUid()) == dimension, updateRate, false);
        setRemovePolicy(client -> StaticEntity.getDimension(client.getPlayerUid()) != dimension, updateRate, true);
        return this;
    }

    public ConnectedClientList setupAllInDimensionPolicy(int dimension) {
        return setupAllInDimensionPolicy(dimension, 1000);
    }

    public ConnectedClientList setupDistancePolicy(Vector3 pos, int dimension, float addDistance, float removeDistance, int updateRate) {
        setAddPolicy(client -> StaticEntity.getDimension(client.getPlayerUid()) == dimension && StaticEntity.getPosition(client.getPlayerUid()).distanceSqr(pos) < addDistance * addDistance, updateRate, true);
        setRemovePolicy(client -> StaticEntity.getDimension(client.getPlayerUid()) != dimension || StaticEntity.getPosition(client.getPlayerUid()).distanceSqr(pos) > removeDistance * removeDistance, updateRate, false);
        return this;
    }

    public ConnectedClientList setupDistancePolicy(Vector3 pos, int dimension, float distance) {
        return setupDistancePolicy(pos, dimension, distance, distance, 1000);
    }

    public ConnectedClientList setupDistancePolicy(float x, float y, float z, int dimension, float addDistance, float removeDistance, int updateRate) {
        return setupDistancePolicy(new Vector3(x, y, z), dimension, addDistance, removeDistance, updateRate);
    }

    public ConnectedClientList setupDistancePolicy(float x, float y, float z, int dimension, float distance) {
        return setupDistancePolicy(x, y, z, dimension, distance, distance, 1000);
    }

    public Set<ConnectedClient> getClientCollection() {
        refresh();
        return clients;
    }
}
