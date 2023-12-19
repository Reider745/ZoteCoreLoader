package com.zhekasmirnov.apparatus.api.container;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.apparatus.mcpe.NativePlayer;
import com.zhekasmirnov.apparatus.multiplayer.ThreadTypeMarker;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType;
import com.zhekasmirnov.apparatus.multiplayer.util.list.ConnectedClientList;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchField;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.*;

public class ItemContainer implements WorkbenchField {
    private static final Object playerInventoryLock = new Object();

    public static void loadClass() {
        // forces class to load and register listeners
    }

    private static final Map<String, UiScreenFactory> screenFactoryMap = new HashMap<>();
    private static final Map<String, Map<String, ClientEventListener>> clientEventListenerMap = new HashMap<>();
    private static final Map<String, List<ClientOnOpenListener>> clientOnOpenListenerMap = new HashMap<>();
    private static final Map<String, List<ClientOnCloseListener>> clientOnCloseListenerMap = new HashMap<>();

    public static void registerScreenFactory(String name, UiScreenFactory factory) {
        screenFactoryMap.put(name, factory);
    }

    public static void addClientEventListener(String typeName, String packetName, ClientEventListener listener) {
        Java8BackComp.computeIfAbsent(clientEventListenerMap, typeName, key -> new HashMap<>()).put(packetName, listener);
    }

    public static void addClientOpenListener(String typeName, ClientOnOpenListener listener) {
        Java8BackComp.computeIfAbsent(clientOnOpenListenerMap, typeName, key -> new ArrayList<>()).add(listener);
    }

    public static void addClientCloseListener(String typeName, ClientOnCloseListener listener) {
        Java8BackComp.computeIfAbsent(clientOnCloseListenerMap, typeName, key -> new ArrayList<>()).add(listener);
    }

    public static ItemContainer getClientContainerInstance(String name) {
        NetworkEntity entity = NetworkEntity.getClientEntityInstance(name);
        if (entity != null) {
            Object target = entity.getTarget();
            if (target instanceof ItemContainer) {
                return (ItemContainer) target;
            }
        }
        return null;
    }


    private static final int saverId = ObjectSaverRegistry.registerSaver("_container2", new ObjectSaver() {
        @Override
        public Object read(ScriptableObject input) {
            Container con = new Container();
            con.slots = input;
            return new ItemContainer(con);
        }

        @Override
        public ScriptableObject save(Object input) {
            if (input instanceof ItemContainer) {
                return ((ItemContainer) input).asLegacyContainer(false).slots;
            }
            return null;
        }
    });

    private static final NetworkEntityType containerNetworkEntityType = new NetworkEntityType("sys.container")
            .setClientListSetupListener((list, target, entity) -> {
                ItemContainer container = (ItemContainer) target;
                list.addListener(new ConnectedClientList.Listener() {
                    @Override
                    public void onAdd(ConnectedClient client) {
                        // this event is sent too early, ui is not yet initialized, so on open listener is not called
                    }

                    @Override
                    public void onRemove(ConnectedClient client) {
                        for (ServerOnCloseListener listener : container.onCloseListeners) {
                            listener.onClose(container, client);
                        }
                    }
                });
            })
            .addServerPacketListener("close", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                container.closeFor(client);
            })
            .addServerPacketListener("inv_to_slot", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject packet = (JSONObject) packetData;
                synchronized (container.transactionLock) {
                    long player = client.getPlayerUid();
                    container.handleInventoryToSlotTransaction(player, packet.optInt("inv"), packet.optString("name"), packet.optInt("amount"));
                    container.sendChanges();
                }
            })
            .addServerPacketListener("slot_to_inv", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject packet = (JSONObject) packetData;
                synchronized (container.transactionLock) {
                    long player = client.getPlayerUid();
                    container.handleSlotToInventoryTransaction(player, packet.optString("slot"), packet.optInt("amount"));
                    container.sendChanges();
                }
            })
            .addServerPacketListener("slot_to_slot", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject packet = (JSONObject) packetData;
                synchronized (container.transactionLock) {
                    long player = client.getPlayerUid();
                    container.handleSlotToSlotTransaction(player, packet.optString("slot1"), packet.optString("slot2"), packet.optInt("amount"));
                    container.sendChanges();
                }
            })
            .addServerPacketListener("bindings", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject packet = (JSONObject) packetData;
                container.handleDirtyBindingsPacket(client, packet);
                container.sendChanges();
            })
            .addServerPacketListener("event", (target, entity, client, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                ServerEventListener listener = container.serverEventListenerMap.get(extra);
                if (listener != null) {
                    listener.receive(container, client, packetData);
                }
            })
            .setClientAddPacketFactory((target, entity, client) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject packet = new JSONObject();

                try {
                    JSONObject slots = new JSONObject();
                    packet.put("clientType", container.clientContainerTypeName);
                    packet.put("slots", slots);
                    for (Map.Entry<String, ItemContainerSlot> entry : container.slotMap.entrySet()) {
                        slots.put(entry.getKey(), entry.getValue().asJson());
                    }
                    packet.put("bindings", new JSONObject(container.bindingsMap));
                } catch (JSONException ignore) { }

                return packet;
            })
            .setClientEntityAddedListener((entity, initPacket) -> {
                ItemContainer clientContainer = new ItemContainer(entity);
                JSONObject json = (JSONObject) initPacket;

                JSONObject slots = json.optJSONObject("slots");
                if (slots != null) {
                    for (Iterator<String> it = slots.keys(); it.hasNext(); ) {
                        String key = it.next();
                        JSONObject slotJson = slots.optJSONObject(key);
                        if (slotJson != null) {
                            clientContainer.setSlot(key, new ItemContainerSlot(slotJson, true));
                        }
                    }
                }

                JSONObject bindings = json.optJSONObject("bindings");
                if (bindings != null) {
                    clientContainer.uiAdapter.receiveBindingsFromServer(bindings);
                }

                clientContainer.setClientContainerTypeName(json.optString("clientType"));
                return clientContainer;
            })
            .setClientEntityRemovedListener((target, entity, removePacket) -> {
                ItemContainer container = (ItemContainer) target;
                container.closeUi();
            })
            .addClientPacketListener("open", (target, entity, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                container.openUi(packetData.toString());
            })
            .addClientPacketListener("slots", (target, entity, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject slotsJson = (JSONObject) packetData;
                if (slotsJson != null) {
                    synchronized (container.transactionLock) {
                        for (Iterator<String> it = slotsJson.keys(); it.hasNext(); ) {
                            String key = it.next();
                            JSONObject slotJson = slotsJson.optJSONObject(key);
                            if (slotJson != null) {
                                container.setSlot(key, new ItemContainerSlot(slotJson, true));
                            }
                        }
                    }
                }
            })
            .addClientPacketListener("bindings", (target, entity, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                JSONObject bindingsJson = (JSONObject) packetData;
                if (bindingsJson != null) {
                    container.uiAdapter.receiveBindingsFromServer(bindingsJson);
                }
            })
            .addClientPacketListener("event", (target, entity, packetData, extra) -> {
                ItemContainer container = (ItemContainer) target;
                Map<String, ClientEventListener> listenerMap = clientEventListenerMap.get(container.clientContainerTypeName);
                if (listenerMap != null) {
                    ClientEventListener listener = listenerMap.get(extra);
                    if (listener != null) {
                        IWindow window = container.uiAdapter.getWindow();
                        listener.receive(container, window, window != null ? window.getContent() : null, packetData);
                    }
                }
            });


    public interface Transaction {
        void run(ItemContainer container);
    }

    public interface UiScreenFactory {
        IWindow getByName(ItemContainer container, String name);
    }

    public interface DirtySlotListener {
        void onMarkedDirty(ItemContainer container, String name, ItemContainerSlot slot);
    }

    public interface TransferPolicy {
        int transfer(ItemContainer container, String name, int id, int count, int data, NativeItemInstanceExtra extra, long player);
    }

    public interface BindingValidator {
        Object validate(ItemContainer container, String bindingComposedName, Object value, long player);
    }

    public interface ClientEventListener {
        void receive(ItemContainer container, IWindow window, ScriptableObject windowContent, Object packetData);
    }

    public interface ServerEventListener {
        void receive(ItemContainer container, ConnectedClient client, Object packetData);
    }

    public interface ClientOnOpenListener {
        void onOpen(ItemContainer container, String screenName);
    }

    public interface ClientOnCloseListener {
        void onClose(ItemContainer container);
    }

    public interface ServerOnOpenListener {
        void onOpen(ItemContainer container, ConnectedClient client, String screenName);
    }

    public interface ServerOnCloseListener {
        void onClose(ItemContainer container, ConnectedClient client);
    }


    public final boolean isServer;
    private final NetworkEntity networkEntity;
    private final ItemContainerUiHandler uiAdapter = new ItemContainerUiHandler(this);
    private Object parent = null;

    public final Object transactionLock = new Object();
    private final Map<String, ItemContainerSlot> slotMap = new HashMap<>();
    private final Set<String> dirtySlotSet = new HashSet<>();

    private final Map<String, Object> bindingsMap = new HashMap<>();
    private final Map<String, Object> dirtyBindingsMap = new HashMap<>();

    private TransferPolicy globalAddTransferPolicy = null;
    private TransferPolicy globalGetTransferPolicy = null;
    private final Map<String, TransferPolicy> addTransferPolicyMap = new HashMap<>();
    private final Map<String, TransferPolicy> getTransferPolicyMap = new HashMap<>();
    private DirtySlotListener globalDirtySlotListener = null;
    private final Map<String, DirtySlotListener> dirtySlotListenerMap = new HashMap<>();

    private BindingValidator globalBindingValidator = null;
    private final Map<String, BindingValidator> bindingValidatorMap = new HashMap<>();

    private String clientContainerTypeName = null;
    private final Map<String, ServerEventListener> serverEventListenerMap = new HashMap<>();
    private final List<ServerOnOpenListener> onOpenListeners = new ArrayList<>();
    private final List<ServerOnCloseListener> onCloseListeners = new ArrayList<>();

    // container slots interface for legacy container interface
    public final Scriptable slots = ScriptableObjectHelper.createEmpty();


    private ItemContainer(String entityName) {
        ThreadTypeMarker.assertServerThread();
        ObjectSaverRegistry.registerObject(this, saverId);
        this.isServer = true;
        if (entityName != null) {
            networkEntity = new NetworkEntity(containerNetworkEntityType, this, entityName);
        } else {
            networkEntity = new NetworkEntity(containerNetworkEntityType, this);
        }
    }

    private ItemContainer(NetworkEntity clientEntity) {
        networkEntity = clientEntity;
        isServer = false;
    }

    public ItemContainer() {
        this((String) null);
    }

    // copy legacy container
    public ItemContainer(Container container) {
        this();
        for (Object id : container.slots.getAllIds()) {
            Object slotScriptable = container.slots.get(id.toString(), container.slots);
            if (slotScriptable instanceof ScriptableObject) {
                setSlot(id.toString(), new ItemContainerSlot((ScriptableObject) slotScriptable));
            }
        }
    }

    public NetworkEntity getNetworkEntity() {
        return networkEntity;
    }

    public String getNetworkName() {
        return networkEntity != null ? networkEntity.getName() : null;
    }

    public ItemContainerUiHandler getUiAdapter() {
        if (isServer) {
            throw new IllegalStateException();
        }
        return uiAdapter;
    }

    public IWindow getWindow() {
        return getUiAdapter().getWindow();
    }

    public ScriptableObject getWindowContent() {
        IWindow window = getUiAdapter().getWindow();
        return window != null ? window.getContent() : null;
    }

    public void removeEntity() {
        if (!isServer) {
            throw new IllegalStateException();
        }
        ThreadTypeMarker.assertServerThread();
        networkEntity.remove();
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public Object getParent() {
        return parent;
    }


    public ItemContainer setGlobalAddTransferPolicy(TransferPolicy globalAddTransferPolicy) {
        this.globalAddTransferPolicy = globalAddTransferPolicy;
        return this;
    }

    public ItemContainer setGlobalGetTransferPolicy(TransferPolicy globalGetTransferPolicy) {
        this.globalGetTransferPolicy = globalGetTransferPolicy;
        return this;
    }

    public ItemContainer setSlotAddTransferPolicy(String name, TransferPolicy policy) {
        addTransferPolicyMap.put(name, policy);
        return this;
    }

    public ItemContainer setSlotGetTransferPolicy(String name, TransferPolicy policy) {
        getTransferPolicyMap.put(name, policy);
        return this;
    }

    public ItemContainer setGlobalDirtySlotListener(DirtySlotListener globalDirtySlotListener) {
        this.globalDirtySlotListener = globalDirtySlotListener;
        return this;
    }

    public ItemContainer setDirtySlotListener(String name, DirtySlotListener listener) {
        dirtySlotListenerMap.put(name, listener);
        return this;
    }

    public void sealSlot(String slotName) {
        setSlotAddTransferPolicy(slotName, (container, name, id, count, data, extra, player) -> 0);
        setSlotAddTransferPolicy(slotName, (container, name, id, count, data, extra, player) -> 0);
    }

    public void sealAllSlots() {
        setGlobalAddTransferPolicy((container, name, id, count, data, extra, player) -> 0);
        setGlobalGetTransferPolicy((container, name, id, count, data, extra, player) -> 0);
    }

    public TransferPolicy getAddTransferPolicy(String slot) {
        return Java8BackComp.getOrDefault(addTransferPolicyMap, slot, globalAddTransferPolicy);
    }

    public TransferPolicy getGetTransferPolicy(String slot) {
        return Java8BackComp.getOrDefault(getTransferPolicyMap, slot, globalGetTransferPolicy);
    }


    public void setGlobalBindingValidator(BindingValidator globalBindingValidator) {
        this.globalBindingValidator = globalBindingValidator;
    }

    public void setBindingValidator(String composedBindingName, BindingValidator validator) {
        this.bindingValidatorMap.put(composedBindingName, validator);
    }

    public BindingValidator getBindingValidator(String composedBindingName) {
        return Java8BackComp.getOrDefault(bindingValidatorMap, composedBindingName, globalBindingValidator);
    }



    public void runTransaction(Transaction transaction) {
        synchronized (transactionLock) {
            transaction.run(this);
        }
    }

    public ItemContainerSlot getSlot(String name) {
        if (!slotMap.containsKey(name)) {
            ItemContainerSlot slot = new ItemContainerSlot();
            slot.setContainer(this, name);
            slotMap.put(name, slot);
            slots.put(name, slots, slot);
            return slot;
        } else {
            return slotMap.get(name);
        }
    }

    @Deprecated
    public ItemContainerSlot getFullSlot(String name) {
        return getSlot(name);
    }


    public void markSlotDirty(String name) {
        synchronized (transactionLock) {
            if (isServer) {
                ItemContainerSlot slot = slotMap.get(name);
                if (slot != null) {
                    dirtySlotSet.add(name);
                    if (globalDirtySlotListener != null) {
                        globalDirtySlotListener.onMarkedDirty(this, name, slot);
                    }
                    DirtySlotListener listener = dirtySlotListenerMap.get(name);
                    if (listener != null) {
                        listener.onMarkedDirty(this, name, slot);
                    }
                }
            }
        }
    }

    public void markAllSlotsDirty() {
        synchronized (transactionLock) {
            if (isServer) {
                for (String name : slotMap.keySet()) {
                    markSlotDirty(name);
                }
            }
        }
    }

    public void setSlot(String name, ItemContainerSlot slot) {
        slot.setContainer(this, name);
        slotMap.put(name, slot);
        slots.put(name, slots, slot);
        markSlotDirty(name);
    }

    public void setSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra) {
        ItemContainerSlot slot = slotMap.get(name);
        if (slot != null) {
            slot.setSlot(id, count, data, extra);
        } else {
            setSlot(name, new ItemContainerSlot(id, count, data, extra));
        }
    }

    public void setSlot(String name, int id, int count, int data) {
        setSlot(name, id, count, data, null);
    }

    public int addToSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra, long player) {
        if (count <= 0) {
            return 0;
        }
        ItemContainerSlot slot = getSlot(name);
        if (slot != null && (slot.id == 0 || slot.id == id && slot.data == data && slot.extra == null)) {
            int amount;
            TransferPolicy policy = getAddTransferPolicy(name);
            if (policy != null) {
                amount = policy.transfer(this, name, id, count, data, extra, player);
            } else {
                amount = Math.min(slot.count + count, NativeItem.getMaxStackForId(id, data)) - slot.count;
            }
            if (amount > 0) {
                slot.count += amount;
                slot.id = id;
                slot.data = data;
                slot.extra = extra;
                markSlotDirty(name);
                return amount;
            }
        }
        return 0;
    }

    public int getFromSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra, long player) {
        if (count <= 0) {
            return 0;
        }
        ItemContainerSlot slot = getSlot(name);
        if (slot != null && slot.id == id && slot.data == data && (slot.extra == null || slot.extra.equals(extra))) {
            TransferPolicy policy = getGetTransferPolicy(name);
            int amount = Math.min(slot.count, count);
            if (policy != null) {
                amount = policy.transfer(this, name, id, amount, data, extra, player);
            }
            if (amount > 0) {
                slot.count -= amount;
                slot.validate();
                markSlotDirty(name);
                return amount;
            }
        }
        return 0;
    }

    public void sendChanges() {
        if (isServer) {
            if (!dirtySlotSet.isEmpty()) {
                JSONObject slotPacket = new JSONObject();
                synchronized (transactionLock) {
                    try {
                        for (String name : dirtySlotSet) {
                            ItemContainerSlot slot = slotMap.get(name);
                            if (slot != null) {
                                slotPacket.put(name, slot.asJson());
                            }
                        }
                        dirtySlotSet.clear();
                    } catch (JSONException ignore) {
                    }
                }
                networkEntity.send("slots", slotPacket);
            }
            if (!dirtyBindingsMap.isEmpty()) {
                synchronized (bindingsMap) {
                    networkEntity.send("bindings", new JSONObject(dirtyBindingsMap));
                    dirtyBindingsMap.clear();
                }
            }
        }
    }

    public void dropAt(NativeBlockSource blockSource, float x, float y, float z) {
        synchronized (transactionLock) {
            for (ItemContainerSlot slot : slotMap.values()) {
                slot.dropAt(blockSource, x, y, z);
            }
        }
        sendChanges();
    }

    public void validateAll() {
        synchronized (transactionLock) {
            for (ItemContainerSlot slot : slotMap.values()) {
                slot.validate();
            }
        }
        sendChanges();
    }

    public void validateSlot(String name) {
        getSlot(name).validate();
    }

    public void clearSlot(String name) {
        getSlot(name).clear();
    }

    public void dropSlot(NativeBlockSource blockSource, String name, float x, float y, float z) {
        getSlot(name).dropAt(blockSource, x, y, z);
    }


    // default transactions

    public void sendInventoryToSlotTransaction(int inventorySlot, String slotName, int amount) {
        if (isServer) {
            throw new IllegalStateException();
        }
        try {
            JSONObject packet = new JSONObject();
            packet.put("inv", inventorySlot);
            packet.put("name", slotName);
            packet.put("amount", amount);
            networkEntity.send("inv_to_slot", packet);
        } catch (JSONException ignore) { }
    }

    void handleInventoryToSlotTransaction(long playerUid, int inventorySlot, String slotName, int amount) {
        if (!isServer) {
            throw new IllegalStateException();
        }

        if (slotName != null && amount > 0 && inventorySlot >= 0 && inventorySlot < 36) {
            NativePlayer player = new NativePlayer(playerUid);
            if (player.isValid()) {
                synchronized (playerInventoryLock) {
                    ItemStack itemInstance = player.getInventorySlot(inventorySlot);
                    if (itemInstance.id != 0 && itemInstance.count > 0) {
                        int id = itemInstance.id;
                        int count = itemInstance.count;
                        int data = itemInstance.data;
                        NativeItemInstanceExtra extra = itemInstance.extra;
                        if (amount > itemInstance.count) {
                            amount = itemInstance.count;
                        }
                        amount = addToSlot(slotName, id, amount, data, extra, playerUid);
                        count -= amount;
                        if (count > 0) {
                            player.setInventorySlot(inventorySlot, id, count, data, extra);
                        } else {
                            player.setInventorySlot(inventorySlot, 0, 0, 0, null);
                        }
                    }
                }
            }
        }
    }

    public void sendSlotToSlotTransaction(String slot1, String slot2, int amount) {
        if (isServer) {
            throw new IllegalStateException();
        }
        try {
            JSONObject packet = new JSONObject();
            packet.put("slot1", slot1);
            packet.put("slot2", slot2);
            packet.put("amount", amount);
            networkEntity.send("slot_to_slot", packet);
        } catch (JSONException ignore) { }
    }

    void handleSlotToSlotTransaction(long player, String slotName1, String slotName2, int amount) {
        if (!isServer) {
            throw new IllegalStateException();
        }
    }

    public void sendSlotToInventoryTransaction(String slot, int amount) {
        if (isServer) {
            throw new IllegalStateException();
        }
        try {
            JSONObject packet = new JSONObject();
            packet.put("slot", slot);
            packet.put("amount", amount);
            networkEntity.send("slot_to_inv", packet);
        } catch (JSONException ignore) { }
    }

    void handleSlotToInventoryTransaction(long playerUid, String slotName, int amount) {
        if (!isServer) {
            throw new IllegalStateException();
        }

        if (amount > 0) {
            ItemContainerSlot slot = getSlot(slotName);
            if (slot.id != 0 && slot.count > 0) {
                NativePlayer player = new NativePlayer(playerUid);
                if (player.isValid()) {
                    int id = slot.id;
                    int data = slot.data;
                    NativeItemInstanceExtra extra = slot.extra;
                    amount = getFromSlot(slotName, id, amount, data, extra, playerUid);
                    if (amount > 0) {
                        synchronized (playerInventoryLock) {
                            player.addItemToInventory(id, amount, data, extra, true);
                        }
                    }
                }
            }
        }
    }


    void sendDirtyClientBinding(String key, Object value) {
        if (!isServer) {
            throw new IllegalStateException();
        }
        try {
            JSONObject packet = new JSONObject();
            packet.put(key, value);
            networkEntity.send("bindings", packet);
        } catch (JSONException ignore) { }
    }

    void handleDirtyBindingsPacket(ConnectedClient client, JSONObject packet) {
        long playerUid = client.getPlayerUid();
        for (Iterator<String> it = packet.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = packet.opt(key);
            if (value instanceof Boolean || value instanceof Number || value instanceof String) {
                BindingValidator validator = getBindingValidator(key);
                if (validator != null) {
                    value = validator.validate(this, key, value, playerUid);
                }
                if (value != null) {
                    synchronized (bindingsMap) {
                        bindingsMap.put(key, value);
                        dirtyBindingsMap.put(key, value);
                    }
                }
            }
        }
    }


    public void setBinding(String composedBindingName, Object value) {
        if (value instanceof CharSequence) {
            value = value.toString();
        } else if (!(value instanceof Number || value instanceof Boolean)) {
            throw new IllegalArgumentException("binding value must be number, string or boolean");
        }
        synchronized (bindingsMap) {
            bindingsMap.put(composedBindingName, value);
            dirtyBindingsMap.put(composedBindingName, value);
        }
        if (!isServer) {
            // also update this binding on client
            getUiAdapter().setBindingByComposedName(composedBindingName, value);
        }
    }

    public void setClientBinding(String composedBindingName, Object value) {
        getUiAdapter().setBindingByComposedName(composedBindingName, value);
        synchronized (bindingsMap) {
            bindingsMap.put(composedBindingName, value);
        }
    }

    public Object getBinding(String composedBindingName) {
        return bindingsMap.get(composedBindingName);
    }

    public void setBinding(String elementName, String bindingName, Object value) {
        setBinding(elementName + "::" + bindingName, value);
    }

    public void setClientBinding(String elementName, String bindingName, Object value) {
        setClientBinding(elementName + "::" + bindingName, value);
    }

    public Object getBinding(String elementName, String bindingName) {
        return getBinding(elementName + "::" + bindingName);
    }

    public void setScale(String elementName, float value) {
        setBinding(elementName, "value", value);
    }

    public void setClientScale(String elementName, float value) {
        setClientBinding(elementName, "value", value);
    }

    public Object getValue(String elementName, float value) {
        return getBinding(elementName, "value");
    }

    public void setText(String elementName, String text) {
        setBinding(elementName, "text", text);
    }

    public void setClientText(String elementName, String text) {
        setClientBinding(elementName, "text", text);
    }

    public String getText(String elementName) {
        Object binding = getBinding(elementName, "text");
        return binding instanceof CharSequence ? binding.toString() : null;
    }


    public void setClientContainerTypeName(String clientContainerTypeName) {
        this.clientContainerTypeName = clientContainerTypeName;
    }

    public String getClientContainerTypeName() {
        return clientContainerTypeName;
    }

    public void addServerEventListener(String name, ServerEventListener listener) {
        serverEventListenerMap.put(name, listener);
    }

    public void addServerOpenListener(ServerOnOpenListener listener) {
        onOpenListeners.add(listener);
    }

    public void addServerCloseListener(ServerOnCloseListener listener) {
        onCloseListeners.add(listener);
    }

    public void sendEvent(String name, Object data) {
        if (!isServer) {
            networkEntity.getClientExecutor().add(() -> networkEntity.send("event#" + name, data));
        } else {
            networkEntity.send("event#" + name, data);
        }
    }

    public void sendEvent(ConnectedClient client, String name, Object data) {
        if (!isServer) {
            networkEntity.getClientExecutor().add(() -> networkEntity.send(client, "event#" + name, data));
        } else {
            networkEntity.send(client, "event#" + name, data);
        }
    }

    public void sendResponseEvent(String name, Object data) {
        if (!isServer) {
            networkEntity.getClientExecutor().add(() -> networkEntity.respond("event#" + name, data));
        } else {
            networkEntity.respond("event#" + name, data);
        }
    }


    public void openFor(ConnectedClient client, String screenName) {
        if (!isServer) {
            throw new IllegalStateException();
        }
        networkEntity.getClients().add(client);
        networkEntity.send(client, "open", screenName);
        for (ServerOnOpenListener listener : onOpenListeners) {
            listener.onOpen(this, client, screenName);
        }
    }

    public void closeFor(ConnectedClient client) {
        if (!isServer) {
            throw new IllegalStateException();
        }
        networkEntity.getClients().remove(client);
    }

    public void close() {
        if (!isServer) {
            throw new IllegalStateException();
        }
        networkEntity.getClients().clear();
    }


    public void sendClosed() {
        if (isServer) {
            throw new IllegalStateException();
        }
        List<ClientOnCloseListener> onCloseListener = clientOnCloseListenerMap.get(clientContainerTypeName);
        if (onCloseListener != null) {
            for (ClientOnCloseListener listener : onCloseListener) {
                listener.onClose(this);
            }
        }
        networkEntity.send("close", "");
    }

    private void closeUi() {
        if (isServer) {
            throw new IllegalStateException();
        }
        uiAdapter.close();
    }

    private void openUi(String name) {
        if (isServer) {
            throw new IllegalStateException();
        }
        if (clientContainerTypeName != null) {
            List<ClientOnOpenListener> onOpenListeners = clientOnOpenListenerMap.get(clientContainerTypeName);
            if (onOpenListeners != null) {
                for (ClientOnOpenListener listener : onOpenListeners) {
                    listener.onOpen(this, name);
                }
            }

            UiScreenFactory factory = screenFactoryMap.get(clientContainerTypeName);
            if (factory != null) {
                IWindow window = factory.getByName(this, name);
                if (window != null) {
                    uiAdapter.openAs(window);
                    return;
                }
            }
        }
        sendClosed();
    }


    // saves

    private boolean globalSlotSavingEnabled = true;

    public void setGlobalSlotSavingEnabled(boolean globalSlotSavingEnabled) {
        this.globalSlotSavingEnabled = globalSlotSavingEnabled;
    }

    public boolean isGlobalSlotSavingEnabled() {
        return globalSlotSavingEnabled;
    }

    public void setSlotSavingEnabled(String name, boolean enabled) {
        getSlot(name).setSavingEnabled(enabled);
    }

    public void resetSlotSavingEnabled(String name) {
        getSlot(name).resetSavingEnabled();
    }

    public boolean isSlotSavingEnabled(String name) {
        return slotMap.containsKey(name) ? getSlot(name).isSavingEnabled() : globalSlotSavingEnabled;
    }


    // legacy container backcomp

    public boolean isLegacyContainer() {
        return false;
    }

    public Container asLegacyContainer(boolean allSlots) {
        Container container = new Container(this);
        for (Map.Entry<String, ItemContainerSlot> entry : slotMap.entrySet()) {
            ItemContainerSlot slot = entry.getValue();
            if (!slot.isEmpty() && (allSlots || slot.isSavingEnabled())) {
                container.slots.put(entry.getKey(), container.slots, slot.asScriptable());
            }
        }
        return container;
    }

    public Container asLegacyContainer() {
        return asLegacyContainer(true);
    }


    private String legacyWorkbenchFieldPrefix = null;
    private int workbenchFieldSize = 3;

    public void setWorkbenchFieldPrefix(String legacyWorkbenchFieldPrefix) {
        this.legacyWorkbenchFieldPrefix = legacyWorkbenchFieldPrefix;
    }

    public void setWorkbenchFieldSize(int workbenchFieldSize) {
        this.workbenchFieldSize = workbenchFieldSize;
    }

    @Override
    public AbstractSlot getFieldSlot(int index) {
        return getSlot(legacyWorkbenchFieldPrefix + index);
    }

    @Override
    public AbstractSlot getFieldSlot(int x, int y) {
        if (x >= 0 && y >= 0 && x < workbenchFieldSize && y < workbenchFieldSize) {
            return getSlot(legacyWorkbenchFieldPrefix + (y * workbenchFieldSize + x));
        } else {
            return null;
        }
    }

    @Override
    public Scriptable asScriptableField() {
        Object[] field = new Object[workbenchFieldSize * workbenchFieldSize];
        for (int i = 0; i < field.length; i++) {
            field[i] = getFieldSlot(i);
        }
        return ScriptableObjectHelper.createArray(field);
    }

    @Override
    public int getWorkbenchFieldSize() {
        return workbenchFieldSize;
    }
}
