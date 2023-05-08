package cn.nukkit.network.protocol.types.request;

public class InventoryMoveAction extends InventoryTransferAction {

    public InventoryMoveAction() {
        super(true, true);
    }
}
