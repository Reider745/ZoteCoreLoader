package com.reider745.network;

import cn.nukkit.Player;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import com.reider745.network.request.*;
import com.reider745.network.session.*;
import com.reider745.network.transaction.DropItemTransaction;
import com.reider745.network.transaction.InventoryTransaction;
import com.reider745.network.transaction.Transaction;
import com.reider745.network.transaction.TransactionGroup;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;

public class ItemStackRequestPacket extends BasePacket {
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
                        action = new InventoryMoveAction((byte) type);
                        break;
                    case 1:
                        action = new InventoryPlaceAction((byte) type);
                        break;
                    case 2:
                        action = new InventorySwapAction((byte) type);
                        break;
                    case 3:
                        action = new InventoryDropAction((byte)type);
                        break;
                    case 4:
                        action = new InventoryDestroyCreativeAction((byte) type);
                        break;
                    case 5:
                        action = new InventoryConsumeAction((byte) type);
                        break;
                    case 9:
                    case 10:
                        action = new InventoryCraftAction((byte) type);
                        break;
                    case 11:
                        action = new InventoryGetCreativeAction((byte) type);
                        break;
                    case 13:
                    case 14:
                        action = new InventoryCraftingResultAction((byte)type);
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

    /*
        legacyRequestId
        transactionType
        hasNetworkIds
        actions
     */

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
    public static final byte CONTAINER_FURNACE = 24;

    private Inventory getInventory(int windowId, Session session) {
        switch (windowId) {
            case OFFHAND:
                return player.getOffhandInventory();
            case HOTBAR:
            case INVENTORY:
            case ARMOR:
            case CRAFTING_INPUT:
            case COMBINED_INVENTORY:
            case CRAFTING_OUTPUT:
            case CREATED_OUTPUT:
                return player.getInventory();
            case CURSOR:
                return player.getCursorInventory();
            case ENCHANTMENT_TABLE_INPUT:
            case ENCHANTMENT_TABLE_MATERIAL:
            case CONTAINER_FURNACE:
            case CONTAINER:
                return session.getOutput();
        }
       /* switch (windowId){
            case HOTBAR, ARMOR, INVENTORY, CRAFTING_INPUT, COMBINED_INVENTORY, CRAFTING_OUTPUT, CREATED_OUTPUT, CONTAINER:
                return player.getInventory();
            case OFFHAND:
                return player.getOffhandInventory();
            case CURSOR:
                return player.getCursorInventory();
            case CONTAINER_FURNACE, OPEN_CONTAINER:
                return player.getUIInventory();
        }*/

        Inventory inventory = player.getWindowById(windowId);
        if(inventory == null)
            throw new RuntimeException("Error get inventory "+windowId);
        return null;
    }
    
    public void handle(){
        int protocol = player.protocol;
        Session session = null;

        for (Request request : requests) {
            Item getItem = null;
            Item resultCrafting = null;
            TransactionGroup transactionGroup = new TransactionGroup(player);


            for (InventoryActionRequest action : request.actions) {
                Logger.debug(action.toString());
                if(action instanceof InventoryGetCreativeAction) {
                    if (player.getGamemode() == 1) {
                        int slot = (int) ((InventoryGetCreativeAction) action).getCreativeItemId();

                        Item item = Item.getCreativeItem(protocol, slot - 1);
                        item = item.clone();
                        item.setCount(64);

                        session = new CreativeSession(player);
                        session.addInput(item, 0);
                    } else {

                    }
                    /*InventoryGetCreativeAction getCreative = (InventoryGetCreativeAction) action;
                    getItem = Item.getCreativeItem(protocol, ((int) getCreative.getCreativeItemId()) - 1);*/
                }else if(action instanceof InventoryDestroyCreativeAction destroyCreativeAction){
                    if (player.getGamemode() == 1) {
                        ItemStackRequestSlotInfo source = destroyCreativeAction.getSource();

                        Item item = getItemStack(destroyCreativeAction.getSource(), session);
                        if (destroyCreativeAction.getAmount() <= item.getCount()) {
                            int remaining = item.getCount() - destroyCreativeAction.getAmount();
                            Inventory inventory = getInventory(source.getWindowId(), session);

                            if (remaining > 0) {
                                item.setCount(remaining);
                            } else {
                                inventory.setItem(destroyCreativeAction.getSource().getSlot(), Item.AIR_ITEM.clone());
                            }
                        }
                    } else {

                    }
                }else if (action instanceof InventoryConsumeAction) {
                    InventoryConsumeAction consumeAction = (InventoryConsumeAction) action;

                    if (session == null) {
                        //resp = new PacketItemStackResponse.Response(PacketItemStackResponse.ResponseResult.Error, request.getRequestId(), null);
                        break;
                    } else {
                        ItemStackRequestSlotInfo source = ((InventoryConsumeAction) action).getSource();

                        // Get the item
                        Item item = getItemStack(source, session);
                        if (consumeAction.getAmount() <= item.getCount()) {
                            item.setCount(item.getCount() - consumeAction.getAmount());
                            Item cl = item.clone();
                            cl.setCount(consumeAction.getAmount());
                            item = cl;
                        }

                        int slot = fixSlotInput(source);
                        session.addInput(item, slot);

                        item = getItemStack(source, session);
                    }
                } else if(action instanceof InventoryTransferAction){
                    handleInventoryTransfer((InventoryTransferAction) action, transactionGroup, request, session);
                    /*InventoryTransferAction transfer = (InventoryTransferAction) action;

                    int winIdSrc = transfer.getSource().getWindowId();
                    int slotSrc = fixSlotId(winIdSrc, transfer.getSource().getSlot());
                    Inventory output = getInventory(winIdSrc);
                    Inventory input = null;

                    if(!transfer.hasDestination()){
                        output.setItem(slotSrc, Item.AIR_ITEM.clone());
                        continue;
                    }else
                        input = getInventory(transfer.getDestination().getWindowId());

                    int winIdDes = transfer.getDestination().getWindowId();
                    int slotDes = fixSlotId(winIdDes, transfer.getDestination().getSlot());

                    if(getItem != null && input != null){
                        getItem = getItem.clone();
                        getItem.setCount(getItem.getMaxStackSize());
                        input.setItem(slotDes, getItem);
                        continue;
                    }

                    if(resultCrafting != null && input != null){
                        Item[] items = input.addItem(resultCrafting);
                        for (Item item : items)
                            player.dropItem(item);
                        continue;
                    }

                    if(input != null){
                        if(winIdDes == CRAFTING_OUTPUT)
                            continue;

                        Item item_1 = output.getItem(slotSrc);
                        Item item_2 = input.getItem(slotDes);

                        if(item_1.getId() == item_2.getId()){
                            if(item_1.getCount() == transfer.getAmount()){
                                item_1.count -= transfer.getAmount();
                                item_2.count += transfer.getAmount();

                                if(item_1.getCount() < 1)
                                    output.clear(slotSrc);

                                input.setItem(slotDes, item_2);
                                continue;
                            }

                            output.setItem(slotSrc, Item.AIR_ITEM.clone());
                            item_2.count += item_1.count;
                            input.setItem(slotDes, item_2);
                            continue;
                        }else if(item_1.getId() != item_2.getId()) {
                            if(item_1.getCount() != transfer.getAmount() && item_2.getId() == 0){
                                item_1.count -= transfer.getAmount();;

                                if(item_1.getCount() < 1)
                                    output.clear(slotSrc);

                                Item res = item_1.clone();
                                res.count = transfer.getAmount();
                                input.setItem(slotDes, res);
                                continue;
                            }
                            output.setItem(slotSrc, item_2);
                            input.setItem(slotDes, item_1);
                            continue;
                        }
                        throw new RuntimeException("Not transfer");
                    }else
                        output.clear(slotSrc);
*/
                }/*else if(action instanceof InventoryCraftingResultAction){
                    if(getItem != null) continue;

                    InventoryCraftingResultAction crafting = (InventoryCraftingResultAction) action;
                    resultCrafting = crafting.getResultItems().get(0);
                    resultCrafting.setCount(crafting.getAmount());
                }*/else if(action instanceof InventoryDropAction drop) {
                    handleInventoryDrop((InventoryDropAction) action, transactionGroup, request, session);

                    /*int winId = drop.getSource().getWindowId();
                    int slotSrc = fixSlotId(winId, drop.getSource().getSlot());

                    Inventory inventory = getInventory(winId);
                    Item item = inventory.getItem(slotSrc);

                    Item dropped_item = item.clone();
                    dropped_item.setCount(drop.getAmount());
                    player.dropAndGetItem(dropped_item);

                    item.setCount(item.getCount()-drop.getAmount());
                    inventory.setItem(slotSrc, item);*/
                }else if (action instanceof InventoryCraftAction) {
                   /* if (player.getUIInventory() != null &&
                            connection.entity().currentOpenContainer() instanceof EnchantmentTableInventory) {

                        session = new EnchantingSession(connection)
                                .selectOption(((InventoryCraftAction) action).getRecipeId());
                    } else {*/
                        session = new CraftingSession(player)
                                .findRecipe((int) ((InventoryCraftAction) action).getRecipeId());
                   // }
                } else if (action instanceof InventoryCraftingResultAction) {
                    if (session == null) {
                        //resp = new PacketItemStackResponse.Response(PacketItemStackResponse.ResponseResult.Error, request.getRequestId(), null);
                        break;
                    } else if (session instanceof CraftingSession) {
                        ((CraftingSession) session)
                                .setCraftingResult((InventoryCraftingResultAction) action)
                                .setAmountOfCrafts((byte) ((InventoryCraftingResultAction) action).getAmount());
                    }
                }
            }

            transactionGroup.execute(true);
        }

        if (session != null) {
            session.postProcess();
        }
    }

    public int fixSlotId(int windowId, int slot){
        switch (windowId) {
            case ENCHANTMENT_TABLE_INPUT:
                return 0;
            case ENCHANTMENT_TABLE_MATERIAL:
                return 1;
            case CRAFTING_INPUT:
                if (slot >= Values.CRAFTING_INPUT_OFFSET) {
                    return slot - Values.CRAFTING_INPUT_OFFSET;
                }

                return slot - Values.CRAFTING_INPUT_SMALL_OFFSET;
            case CREATED_OUTPUT:
                return slot - Values.OUTPUT_OFFSET;
            case ARMOR:
                return 36 + slot;
            case CONTAINER:
                return 40 + slot;
        }

        return  slot;
    }

    private static class Values {
        public static final int CRAFTING_INPUT_SMALL_OFFSET = 28;
        public static final int CRAFTING_INPUT_OFFSET = 32;
        public static final int OUTPUT_OFFSET = 50;
    }

    private int fixSlotInput(ItemStackRequestSlotInfo info) {
        int slot = info.getSlot();

        switch (info.getWindowId()) {
            case ENCHANTMENT_TABLE_INPUT:
                return 0;
            case ENCHANTMENT_TABLE_MATERIAL:
                return 1;
            case CRAFTING_INPUT:
                if (slot >= Values.CRAFTING_INPUT_OFFSET) {
                    return slot - Values.CRAFTING_INPUT_OFFSET;
                }

                return slot - Values.CRAFTING_INPUT_SMALL_OFFSET;
            case CREATED_OUTPUT:
                return slot - Values.OUTPUT_OFFSET;
            case ARMOR:
                return 36 + slot;
        }

        return info.getSlot();
    }

    private int fixSlotOutput(Transaction<?,?,?> info) {
        switch (info.getInventoryWindowId()) {
            case ENCHANTMENT_TABLE_INPUT:
                return 14;
            case ENCHANTMENT_TABLE_MATERIAL:
                return 15;
            case CRAFTING_INPUT:
                if (info.inventory().getSize() == 4) {
                    return (byte) (info.slot() + Values.CRAFTING_INPUT_SMALL_OFFSET);
                }

                return (byte) (info.slot() + Values.CRAFTING_INPUT_OFFSET);
            case CREATED_OUTPUT:
                return (byte) (info.slot() + Values.OUTPUT_OFFSET);
            case ARMOR:
                return (byte) (info.slot() + info.inventory().getSize());
        }

        return info.slot();
    }

    private Item getItemStack(ItemStackRequestSlotInfo requestSlotInfo, Session session) {
        Inventory inventory = getInventory(requestSlotInfo.getWindowId(), session);
        Item itemStack = inventory.getItem(fixSlotInput(requestSlotInfo));

        // TODO: check for item stack id

        return itemStack;
    }

    private void handleInventoryDrop(InventoryDropAction dropAction,
                                                                 TransactionGroup transactionGroup,
                                                                 Request request,
                                     Session session) {
        Inventory inventory = getInventory(dropAction.getSource().getWindowId(), session);
        Item source = inventory.getItem(dropAction.getSource().getSlot());

        int sourceSlot = fixSlotInput(dropAction.getSource());

        // Create new item with the correct drop amount
        InventoryTransaction<?,?,?> inventoryTransactionSource;
        if (dropAction.getAmount() == source.getCount()) {
            // We need to replace the source with air
            inventoryTransactionSource = new InventoryTransaction<>(
                    player, inventory,
                    sourceSlot, source, Item.get(0),
                    (byte) dropAction.getSource().getWindowId());
        } else {
            Item cl = source.clone();
            cl.setCount(source.getCount() - dropAction.getAmount());
            inventoryTransactionSource = new InventoryTransaction<>(
                    player, inventory,
                    sourceSlot, source,cl,
                    (byte) dropAction.getSource().getWindowId());
        }
        Item cl = source.clone();
        cl.setCount(dropAction.getAmount());
        DropItemTransaction<?> dropItemTransaction = new DropItemTransaction<>(
                player.getLocation().add(0, player.getEyeHeight(), 0),
                player.getDirectionVector(),
                //player.getDirection().normalize().multiply(0.4f),
                cl);

        transactionGroup.addTransaction(inventoryTransactionSource);
        transactionGroup.addTransaction(dropItemTransaction);

        //return null;
    }

    private void handleInventoryTransfer(InventoryTransferAction transferAction,
                                                                     TransactionGroup transactionGroup,
                                                                     Request request,
                                         Session session) {
        Inventory sourceInventory = getInventory(transferAction.getSource().getWindowId(), session);
        if (session != null) {
            if (!session.process()) {
                System.out.println("return");
                return;
                //return new PacketItemStackResponse.Response(PacketItemStackResponse.ResponseResult.Error, request.getRequestId(), null);
            }
        }

        int sourceSlot = fixSlotInput(transferAction.getSource());
        int destinationSlot = fixSlotInput(transferAction.getDestination());

        Item destination = getItemStack(transferAction.getDestination(), session);

        Item source = getItemStack(transferAction.getSource(), session);
        if (transferAction.hasAmount()) {
            if (transferAction.getAmount() <= source.getCount()) {
                int remaining = source.getCount() - transferAction.getAmount();
                InventoryTransaction<?,?,?> inventoryTransactionSource;

                if (remaining > 0) {
                    Item cl = source.clone();
                    cl.setCount(remaining);

                    inventoryTransactionSource = new InventoryTransaction<>(
                            player, getInventory(transferAction.getSource().getWindowId(), session),
                            sourceSlot, source, cl,
                            (byte) transferAction.getSource().getWindowId());

                } else {
                    inventoryTransactionSource = new InventoryTransaction<>(
                            player, getInventory(transferAction.getSource().getWindowId(), session),
                            sourceSlot, source, Item.get(0),
                            (byte)transferAction.getSource().getWindowId());
                }

                transactionGroup.addTransaction(inventoryTransactionSource);

                source = source.clone();
                source.setCount(transferAction.getAmount());

                if (source.equals(destination)) {
                    source.setCount(destination.getCount() + source.getCount());
                }

                InventoryTransaction<?,?,?> inventoryTransactionDestination = new InventoryTransaction<>(
                        player, getInventory(transferAction.getDestination().getWindowId(), session),
                        destinationSlot, destination, source,
                        (byte) transferAction.getDestination().getWindowId());

                transactionGroup.addTransaction(inventoryTransactionDestination);
            } else {
                return;
                //return new PacketItemStackResponse.Response(PacketItemStackResponse.ResponseResult.Error, request.getRequestId(), null);
            }
        } else {
            InventoryTransaction<?,?,?> inventoryTransactionSource = new InventoryTransaction<>(
                    player, getInventory(transferAction.getSource().getWindowId(), session),
                    sourceSlot, source, destination,
                    (byte) transferAction.getSource().getWindowId());
            InventoryTransaction<?,?,?> inventoryTransactionDestination = new InventoryTransaction<>(
                    player, getInventory(transferAction.getDestination().getWindowId(), session),
                    destinationSlot, destination, source,
                    (byte) transferAction.getDestination().getWindowId());

            transactionGroup.addTransaction(inventoryTransactionSource);
            transactionGroup.addTransaction(inventoryTransactionDestination);
        }

        //return null;
    }

}
