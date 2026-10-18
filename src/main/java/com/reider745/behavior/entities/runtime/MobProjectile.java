package com.reider745.behavior.entities.runtime;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import com.reider745.behavior.entities.CustomEntity;
import org.json.JSONObject;

public final class MobProjectile {
    private MobProjectile() {}

    public static void tick(CustomEntity projectile, int ticks) {
        JSONObject definition = projectile.components().getJSONObject("minecraft:projectile");
        for (int tick = 0; tick < Math.min(5, ticks); tick++) {
            Vector3 start = projectile.getPosition();
            Vector3 motion = projectile.getMotion();
            double distance = motion.length();
            int steps = Math.max(1, (int) Math.ceil(distance * 4));
            if (steps > 256) { projectile.close(); return; }
            Vector3 delta = motion.divide(steps);
            for (int i = 0; i < steps; i++) {
                Vector3 position = start.add(delta.multiply(i + 1));
                if (projectile.level.getChunkIfLoaded(position.getFloorX() >> 4, position.getFloorZ() >> 4) == null) {
                    projectile.close(); return;
                }
                if (projectile.level.getBlock(position).isSolid()) {
                    hit(projectile, null, definition);
                    return;
                }
                double radius = Math.max(.1, projectile.getWidth() / 2);
                var box = new SimpleAxisAlignedBB(position.x-radius,position.y-radius,position.z-radius,
                        position.x+radius,position.y+radius,position.z+radius);
                for (Entity target : projectile.level.getNearbyEntities(box, projectile)) {
                    if (!target.isAlive() || target.getId() == projectile.namedTag.getLong("BehaviorShooter")) continue;
                    hit(projectile, target, definition);
                    return;
                }
            }
            projectile.setPosition(start.add(motion));
            double drag = projectile.isInsideOfWater() ? definition.optDouble("liquid_inertia", .6)
                    : definition.optDouble("inertia", .99);
            projectile.setMotion(motion.multiply(drag).add(0, -definition.optDouble("gravity", .05), 0));
            projectile.setRotation(Math.toDegrees(Math.atan2(-motion.x, motion.z)),
                    -Math.toDegrees(Math.atan2(motion.y, Math.hypot(motion.x, motion.z))));
        }
        projectile.updateMovement();
        if (projectile.ticksLived > 1200) projectile.close();
    }

    private static void hit(CustomEntity projectile, Entity target, JSONObject definition) {
        JSONObject onHit = definition.optJSONObject("on_hit");
        if (onHit == null) { projectile.close(); return; }
        var host = projectile.host();
        Entity previousOther = host.other;
        host.other = target;
        try {
            JSONObject impact = onHit.optJSONObject("impact_damage");
            if (target != null && impact != null) {
                Entity shooter = projectile.level.getEntity(projectile.namedTag.getLong("BehaviorShooter"));
                target.attack(new EntityDamageByEntityEvent(shooter == null ? projectile : shooter, target,
                        EntityDamageEvent.DamageCause.PROJECTILE, (float) projectile.runtime().duration(impact.opt("damage"), 2)));
                if (definition.optBoolean("should_bounce", false)) projectile.motionY *= -1;
                if (onHit.has("catch_fire")) target.setOnFire(5);
            }
            JSONObject effect = onHit.optJSONObject("mob_effect");
            if (target != null && effect != null) {
                Effect potion = Effect.getEffectByName(effect.optString("effect"));
                if (potion != null) target.addEffect(potion.setDuration(effect.optInt("duration", 1) * 20)
                        .setAmplifier(effect.optInt("amplifier", 0)));
            }
            JSONObject event = onHit.optJSONObject("definition_event");
            if (event != null && (target != null && event.optBoolean("affect_projectile", true)
                    || target == null && event.optBoolean("affect_projectile", true))) projectile.runtime().trigger(event);
            if (onHit.has("stick_in_ground") && target == null) {
                projectile.setMotion(new Vector3());
                return;
            }
            projectile.close();
        } finally { host.other = previousOther; }
    }
}
