package cn.nukkit.network.protocol.types.request;

import cn.nukkit.network.protocol.DataPacket;

public class InventoryTransferAction extends InventoryActionRequest {

    private final boolean hasAmount;
    private final boolean hasDestination;

    private int amount;
    private ItemStackRequestSlotInfo source;
    private ItemStackRequestSlotInfo destination;

    public InventoryTransferAction(boolean hasAmount, boolean hasDestination) {
        this.hasAmount = hasAmount;
        this.hasDestination = hasDestination;
    }

    @Override
    public void deserialize(DataPacket packet) throws Exception {
        if (this.hasAmount) {
            this.amount = packet.getByte();
        }

        this.source = new ItemStackRequestSlotInfo();
        this.source.deserialize(packet);

        if (this.hasDestination) {
            this.destination = new ItemStackRequestSlotInfo();
            this.destination.deserialize(packet);
        }
    }

    public int getAmount() {
        return amount;
    }

    public boolean hasAmount() {
        return hasAmount;
    }

    public ItemStackRequestSlotInfo getSource() {
        return source;
    }

    public ItemStackRequestSlotInfo getDestination() {
        return destination;
    }

    public boolean hasDestination() {
        return hasDestination;
    }

    @Override
    public int weight() {
        return 5;
    }

    @Override
    public String toString() {
        return "InventoryTransferAction{" +
                "hasAmount=" + hasAmount +
                ", hasDestination=" + hasDestination +
                ", amount=" + amount +
                ", source=" + source +
                ", destination=" + destination +
                '}';
    }
}
