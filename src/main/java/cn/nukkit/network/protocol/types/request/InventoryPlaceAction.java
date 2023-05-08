package cn.nukkit.network.protocol.types.request;

public class InventoryPlaceAction extends InventoryTransferAction {

    public InventoryPlaceAction() {
        super(true, true);
    }

    @Override
    public int weight() {
        return 2;
    }
}
