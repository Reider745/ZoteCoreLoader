package cn.nukkit.event.vehicle;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class VehicleDestroyEvent extends VehicleEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    @PowerNukkitOnly
    public VehicleDestroyEvent(final Entity vehicle) {
        super(vehicle);
    }

}
