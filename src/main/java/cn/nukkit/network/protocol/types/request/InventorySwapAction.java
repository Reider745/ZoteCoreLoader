package cn.nukkit.network.protocol.types.request;

public class InventorySwapAction extends InventoryTransferAction {
    public InventorySwapAction() {
        super(false, true);
    }
}
