package cn.nukkit.network.protocol.types.request;

import cn.nukkit.network.protocol.DataPacket;

public class InventoryDropAction extends InventoryActionRequest {

    private int amount;
    private ItemStackRequestSlotInfo source;
    private boolean random;

    @Override
    public void deserialize(DataPacket packet) throws Exception {
        this.amount = packet.getByte();
        this.source = new ItemStackRequestSlotInfo();
        this.source.deserialize(packet);
        this.random = packet.getBoolean();
    }

    @Override
    public int weight() {
        return 5;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStackRequestSlotInfo getSource() {
        return source;
    }

    public boolean isRandom() {
        return random;
    }

    @Override
    public String toString() {
        return "InventoryDropAction{" +
                "amount=" + amount +
                ", source=" + source.toString() +
                ", random=" + random +
                '}';
    }
}
