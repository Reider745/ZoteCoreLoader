package cn.nukkit.entity.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityInteractable;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.vehicle.VehicleDamageByEntityEvent;
import cn.nukkit.event.vehicle.VehicleDamageEvent;
import cn.nukkit.event.vehicle.VehicleDestroyByEntityEvent;
import cn.nukkit.event.vehicle.VehicleDestroyEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityVehicle extends Entity implements EntityRideable, EntityInteractable {

    protected boolean rollingDirection = true;

    public EntityVehicle(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public int getRollingAmplitude() {
        return this.getDataPropertyInt(DATA_HURT_TIME);
    }

    public void setRollingAmplitude(int time) {
        this.setDataProperty(new IntEntityData(DATA_HURT_TIME, time));
    }

    public int getRollingDirection() {
        return this.getDataPropertyInt(DATA_HURT_DIRECTION);
    }

    public void setRollingDirection(int direction) {
        this.setDataProperty(new IntEntityData(DATA_HURT_DIRECTION, direction));
    }

    public int getDamage() {
        return this.getDataPropertyInt(DATA_HEALTH); // false data name (should be DATA_DAMAGE_TAKEN)
    }

    public void setDamage(int damage) {
        this.setDataProperty(new IntEntityData(DATA_HEALTH, damage));
    }

    @Override
    public String getInteractButtonText(Player player) {
        return "action.interact.mount";
    }

    @Override
    public boolean canDoInteraction() {
        return passengers.isEmpty();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        // The rolling amplitude
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1);
        }

        // A killer task
        if (this.level != null) {
            if (y < 0) {
                kill();
            }
        } else if (y < -16) {
            kill();
        }
        // Movement code
        updateMovement();

        //Check riding
        if (this.riding == null) {
            for (Entity entity : level.fastNearbyEntities(this.boundingBox.grow(0.20000000298023224, 0.0D, 0.20000000298023224), this)) {
                if (entity instanceof EntityLiving entityLiving) {
                    entityLiving.collidingWith(this);
                }
            }
        }
        return true;
    }

    protected boolean performHurtAnimation() {
        setRollingAmplitude(9);
        setRollingDirection(rollingDirection ? 1 : -1);
        rollingDirection = !rollingDirection;
        return true;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {

        boolean instantKill = false;

        if (source instanceof EntityDamageByEntityEvent) {
            final Entity damagingEntity = ((EntityDamageByEntityEvent) source).getDamager();

            final VehicleDamageByEntityEvent byEvent = new VehicleDamageByEntityEvent(this, damagingEntity, source.getFinalDamage());

            getServer().getPluginManager().callEvent(byEvent);

            if (byEvent.isCancelled())
                return false;

            instantKill = damagingEntity instanceof Player && ((Player) damagingEntity).isCreative();
        } else {

            final VehicleDamageEvent damageEvent = new VehicleDamageEvent(this, null, source.getFinalDamage());

            getServer().getPluginManager().callEvent(damageEvent);

            if (damageEvent.isCancelled())
                return false;
        }

        if (instantKill || getHealth() - source.getFinalDamage() < 1) {
            if (source instanceof EntityDamageByEntityEvent) {
                final Entity damagingEntity = ((EntityDamageByEntityEvent) source).getDamager();
                final VehicleDestroyByEntityEvent byDestroyEvent = new VehicleDestroyByEntityEvent(this, damagingEntity);

                getServer().getPluginManager().callEvent(byDestroyEvent);

                if (byDestroyEvent.isCancelled())
                    return false;
            } else {

                final VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(this);

                getServer().getPluginManager().callEvent(destroyEvent);

                if (destroyEvent.isCancelled())
                    return false;
            }
        }

        if (instantKill)
            source.setDamage(1000);

        return super.attack(source);
    }
}
