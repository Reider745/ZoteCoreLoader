package cn.nukkit.network.protocol.types.request;

import cn.nukkit.network.protocol.DataPacket;



public class ItemStackRequestSlotInfo {

    private int windowId;
    private int slot;
    private int itemStackId;

    public void deserialize(DataPacket packet) throws Exception {
        this.windowId =  packet.getByte();
        this.slot = packet.getByte();
        this.itemStackId = packet.getVarInt();
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlot() {
        return slot;
    }

    public int getItemStackId() {
        return itemStackId;
    }

    @Override
    public String toString() {
        return "ItemStackRequestSlotInfo{" +
                "windowId=" + windowId +
                ", slot=" + slot +
                ", itemStackId=" + itemStackId +
                '}';
    }
}
