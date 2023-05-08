package cn.nukkit.network.protocol.types.request;

public class InventoryConsumeAction extends InventoryTransferAction {

    public InventoryConsumeAction() {
        super(true, false);
    }

    @Override
    public int weight() {
        return 8;
    }
}
