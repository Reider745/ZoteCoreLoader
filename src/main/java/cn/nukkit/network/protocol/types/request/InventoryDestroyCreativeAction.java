package cn.nukkit.network.protocol.types.request;

/**
 * @author geNAZt
 */
public class InventoryDestroyCreativeAction extends InventoryTransferAction {

    public InventoryDestroyCreativeAction() {
        super(true, false);
    }

}
