package cn.nukkit.network.protocol;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.request.*;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import sun.awt.im.SimpleInputMethodWindow;

import java.util.ArrayList;

public class ItemStackRequestPacket extends DataPacket {
    public static class Request {
        private long requestId;
        private ArrayList<InventoryActionRequest> actions;

        public void deserialize(DataPacket packet) throws Exception {
            this.requestId = packet.getUnsignedVarInt();

            int amountOfActions = (int) packet.getUnsignedVarInt();
            this.actions = new ArrayList<>(amountOfActions);
            for (int j = 0; j < amountOfActions; j++) {
                // Read type
                InventoryActionRequest action;
                int type = packet.getByte();
                switch (type) {
                    case 0:
                        action = new InventoryMoveAction();
                        break;
                    case 1:
                        action = new InventoryPlaceAction();
                        break;
                    case 2:
                        action = new InventorySwapAction();
                        break;
                    case 3:
                        action = new InventoryDropAction();
                        break;
                    case 4:
                        action = new InventoryDestroyCreativeAction();
                        break;
                    case 5:
                        action = new InventoryConsumeAction();
                        break;
                    case 9:
                    case 10:
                        action = new InventoryCraftAction();
                        break;
                    case 11:
                        action = new InventoryGetCreativeAction();
                        break;
                    case 13:
                    case 14:
                        action = new InventoryCraftingResultAction();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }

                action.deserialize(packet);
                this.actions.add(action);
            }

            int amountOfWords = (int) packet.getUnsignedVarInt();
            ArrayList<String> wordToFilter = new ArrayList<>(amountOfWords);
            for (int i = 0; i < amountOfWords; i++) {
                wordToFilter.add(packet.getString());
            }
        }

        public long getRequestId() {
            return requestId;
        }

        public ArrayList<InventoryActionRequest> getActions() {
            return actions;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "requestId=" + requestId +
                    ", actions=" + actions +
                    '}';
        }
    }

    private ArrayList<Request> requests;

    @Override
    public byte pid() {
        return ProtocolInfo.ITEM_STACK_REQUEST_PACKET;
    }

    @Override
    public void decode() {
        int amountOfRequests = (int) this.getUnsignedVarInt();
        this.requests = new ArrayList<>(amountOfRequests);
        for (int i = 0; i < amountOfRequests; i++) {
            Request request = new Request();
            try {
                request.deserialize(this);
            }catch (Exception e){
                Logger.error(e.getMessage());
            }
            this.requests.add(request);
        }

        handle();
    }

    @Override
    public void encode() {

    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        return "ItemStackRequestPacket{" +
                "requests=" + requests +
                '}';
    }

    public static final byte PLAYER = 0;

    public static final byte ARMOR = 6;
    public static final byte CONTAINER = 7;
    public static final byte COMBINED_INVENTORY = 12;
    public static final byte CRAFTING_INPUT = 13;
    public static final byte CRAFTING_OUTPUT = 14;
    public static final byte ENCHANTMENT_TABLE_INPUT = 21;
    public static final byte ENCHANTMENT_TABLE_MATERIAL = 22;
    public static final byte HOTBAR = 27;
    public static final byte INVENTORY = 28;
    public static final byte OFFHAND = 33;
    public static final byte CURSOR = 58;
    public static final byte CREATED_OUTPUT = 59;
    public static final byte OPEN_CONTAINER = 60;
    public static final byte OFFHAND_DEPRECATED = 119;
    public static final byte ARMOR_DEPRECATED = 120;
    public static final byte CURSOR_DEPRECATED = 124;

    private Inventory getInventory(int windowId) {
        switch (windowId) {
            case OFFHAND:
                return client_player.getOffhandInventory();
            case HOTBAR:
            case INVENTORY:
            case ARMOR:
            case CRAFTING_INPUT:
            case COMBINED_INVENTORY:
                return client_player.getInventory();
            case CURSOR:
                return client_player.getCursorInventory();
            case ENCHANTMENT_TABLE_INPUT:
            case ENCHANTMENT_TABLE_MATERIAL:
            case CONTAINER:
                return client_player.opened_container == null ? client_player.getInventory() : client_player.opened_container;
            case CRAFTING_OUTPUT:
            //case CREATED_OUTPUT:
                //return client_player.getOutput();
        }
        Logger.debug("Пизда:"+windowId);
        return client_player.getWindowById(windowId);
    }
    
    public void handle(){
        for (Request request : requests) {
            Item getItem = null;
            Item resultCrafting = null;
            for (InventoryActionRequest action : request.actions) {
                Logger.debug(action.toString());
                if(action instanceof InventoryGetCreativeAction) {
                    InventoryGetCreativeAction getCreative = (InventoryGetCreativeAction) action;
                    getItem = Item.getCreativeItem(((int) getCreative.getCreativeItemId()) - 1);
                }else if(action instanceof InventoryTransferAction){
                    InventoryTransferAction transfer = (InventoryTransferAction) action;
                    Inventory output = getInventory(transfer.getSource().getWindowId());
                    Inventory input = null;
                    if(transfer.hasDestination())
                        input = getInventory(transfer.getDestination().getWindowId());

                    if(getItem != null && input != null){
                        getItem = getItem.clone();
                        getItem.setCount(64);
                        input.setItem(transfer.getDestination().getSlot(), getItem);
                        continue;
                    }

                    if(resultCrafting != null && input != null){
                        Item[] items = input.addItem(resultCrafting);
                        for (Item item : items)
                            client_player.dropItem(item);
                        continue;
                    }


                    if(input != null){
                        Item item_1 = output.getItem(transfer.getSource().getSlot());
                        Item item_2 = input.getItem(transfer.getDestination().getSlot());

                        if(item_1.getCount() == transfer.getAmount())
                            output.setItem(transfer.getSource().getSlot(), item_2);
                        input.setItem(transfer.getDestination().getSlot(), item_1);
                    }else
                        output.clear(transfer.getSource().getSlot());

                }else if(action instanceof InventoryCraftingResultAction){
                    if(getItem != null) continue;
                    InventoryCraftingResultAction crafting = (InventoryCraftingResultAction) action;
                    resultCrafting = crafting.getResultItems().get(0);
                    resultCrafting.setCount(crafting.getAmount());
                }else if(action instanceof InventoryDropAction) {
                    InventoryDropAction drop = (InventoryDropAction) action;
                    Inventory inventory = getInventory(drop.getSource().getWindowId());

                    Item item = inventory.getItem(drop.getSource().getSlot());

                    Item dropped_item = item.clone();
                    dropped_item.setCount(drop.getAmount());
                    client_player.dropAndGetItem(dropped_item);

                    item.setCount(item.getCount()-drop.getAmount());
                    inventory.setItem(drop.getSource().getSlot(), item);
                }
            }
        }
    }
}
