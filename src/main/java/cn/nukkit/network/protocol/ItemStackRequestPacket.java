package cn.nukkit.network.protocol;

import cn.nukkit.network.protocol.types.request.*;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;

public class ItemStackRequestPacket extends DataPacket {
    public static class Request {
        private long requestId;
        private ArrayList<InventoryAction> actions;

        public void deserialize(DataPacket packet) throws Exception {
            this.requestId = packet.getUnsignedVarInt();

            int amountOfActions = (int) packet.getUnsignedVarInt();
            this.actions = new ArrayList<>(amountOfActions);
            for (int j = 0; j < amountOfActions; j++) {
                // Read type
                InventoryAction action;
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

        public ArrayList<InventoryAction> getActions() {
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

        Logger.debug(toString());
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
}
