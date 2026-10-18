package com.reider745.behavior.entities.runtime;

import cn.nukkit.Player;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.behavior.entities.CustomEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Priority-driven goals. Target selection and locomotion are independent from attack cooldowns. */
public final class MobAI {
    private final CustomEntity entity;
    private final NukkitMobHost host;
    private final MobPathfinder pathfinder;
    private int targetDelay;
    private double attackDelay;
    private int wanderDelay;
    private Vector3 wander;
    private JSONObject previous;
    private List<Map.Entry<String, JSONObject>> goals = List.of();

    public MobAI(CustomEntity entity, NukkitMobHost host) {
        this.entity = entity;
        this.host = host;
        pathfinder = new MobPathfinder(entity);
    }

    public Vector3 tick(int ticks) {
        JSONObject components = entity.components();
        if (components != previous) {
            List<Map.Entry<String, JSONObject>> list = new ArrayList<>();
            for (String key : components.keySet()) if (key.startsWith("minecraft:behavior.")) {
                JSONObject config = components.optJSONObject(key);
                if (config != null) list.add(Map.entry(key.substring(19), config));
            }
            list.sort(Comparator.comparingInt(entry -> entry.getValue().optInt("priority", 0)));
            goals = list;
            previous = components;
        }
        attackDelay = Math.max(0, attackDelay - ticks / 20d);
        targetDelay -= ticks;
        if (targetDelay <= 0) { selectTarget(components); targetDelay = 10; }
        Entity target = entity.getTarget();
        if (target != null && (!valid(target) || target.distance(entity) > scalar(components, "minecraft:follow_range", 32))) entity.setTarget(null);
        target = entity.getTarget();
        Vector3 destination = null;
        double multiplier = 1;
        boolean flying = components.has("minecraft:can_fly") || components.has("minecraft:movement.fly")
                || components.has("minecraft:navigation.fly") || components.has("minecraft:navigation.hover");
        boolean swimming = components.has("minecraft:navigation.swim") && entity.isInsideOfWater();
        boolean floatUp = false;
        for (Map.Entry<String, JSONObject> entry : goals) {
            String goal = entry.getKey();
            JSONObject config = entry.getValue();
            Vector3 selected = null;
            boolean active = false;
            switch (goal) {
                case "float" -> { if (entity.isInsideOfWater()) floatUp = true; }
                case "sit", "stay_while_sitting" -> {
                    if (entity.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_SITTING)) active = true;
                }
                case "melee_attack", "delayed_attack", "move_towards_target", "ranged_attack" -> {
                    if (target != null) {
                        active = true;
                        double distance = entity.distance(target);
                        double reach = goal.equals("ranged_attack") ? config.optDouble("attack_radius", 15)
                                : config.optDouble("reach_multiplier", 2) * (entity.getWidth() + target.getWidth()) / 2;
                        if (distance > Math.max(1, reach) || entity.raycast(target) != null) selected = target;
                        else if (attackDelay <= 0 && !goal.equals("move_towards_target")) {
                            if (goal.equals("ranged_attack")) shoot(target, components);
                            else {
                                double damage = scalar(components, "minecraft:attack", entity.getFactory().damage);
                                target.attack(new EntityDamageByEntityEvent(entity, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, (float) damage));
                                JSONObject attack = components.optJSONObject("minecraft:attack");
                                if (attack != null && attack.has("effect_name")) {
                                    cn.nukkit.potion.Effect effect = cn.nukkit.potion.Effect.getEffectByName(attack.getString("effect_name"));
                                    if (effect != null) target.addEffect(effect.setDuration(attack.optInt("effect_duration", 0) * 20));
                                }
                            }
                            attackDelay = config.optDouble("attack_interval", config.optDouble("attack_interval_min", 1));
                        }
                        entity.lookAt(target.getPosition().add(0, target.getEyeHeight(), 0));
                    }
                }
                case "panic" -> {
                    if (entity.isOnFire() || entity.getLastDamageCause() != null && entity.ticksLived % 100 < 60) {
                        selected = randomDestination(components, flying); active = true;
                    }
                }
                case "avoid_mob_type" -> {
                    JSONArray types = config.optJSONArray("entity_types");
                    Entity avoid = nearest(config.optDouble("max_dist", 8), candidate -> {
                        if (types == null) return false;
                        Entity saved = host.other; host.other = candidate;
                        try {
                            for (int i = 0; i < types.length(); i++)
                                if (MobFilters.test(types.getJSONObject(i).opt("filters"), host)) return true;
                            return false;
                        } finally { host.other = saved; }
                    });
                    if (avoid != null) {
                        Vector3 away = entity.getPosition().subtract(avoid).normalize().multiply(8);
                        selected = entity.getPosition().add(away); active = true;
                    }
                }
                case "tempt" -> {
                    JSONArray items = config.optJSONArray("items");
                    Entity player = nearest(config.optDouble("within_radius", 10), candidate -> candidate instanceof Player p
                            && items != null && MobInteractions.matches(items, p.getInventory().getItemInHand()));
                    if (player != null) {
                        selected = entity.distance(player) > 2 ? player : null;
                        entity.lookAt(player); active = true;
                    }
                }
                case "follow_owner" -> {
                    Entity owner = host.subject("owner");
                    if (owner != null && entity.distance(owner) > config.optDouble("stop_distance", 2)) {
                        selected = owner; active = true;
                    }
                }
                case "follow_parent" -> {
                    if (entity.isBaby()) {
                        Entity parent = nearest(16, candidate -> candidate instanceof CustomEntity custom && !custom.isBaby()
                                && custom.getFactory().description.identifier.equals(entity.getFactory().description.identifier));
                        if (parent != null && parent.distance(entity) > 2) { selected = parent; active = true; }
                    }
                }
                case "breed" -> {
                    if (entity.isInLove() && !entity.isBaby()) {
                        Entity mate = nearest(8, candidate -> candidate instanceof CustomEntity custom && custom.isInLove()
                                && !custom.isBaby() && custom.getFactory().description.identifier.equals(entity.getFactory().description.identifier));
                        if (mate instanceof CustomEntity custom) {
                            active = true; selected = mate;
                            if (entity.distance(mate) < 2) { MobInteractions.breed(entity, custom); selected = null; }
                        }
                    }
                }
                case "leap_at_target" -> {
                    if (target != null && entity.onGround && entity.distance(target) > 2 && entity.distance(target) < 5 && host.random() < .05) {
                        entity.motionY = config.optDouble("yd", .4); selected = target; active = true;
                    }
                }
                case "swell" -> {
                    if (target != null && entity.distance(target) < config.optDouble("start_distance", 3)) {
                        entity.runtime().event("minecraft:start_exploding"); active = true;
                    }
                }
                case "random_stroll", "random_swim", "random_fly", "random_hover" -> {
                    selected = randomDestination(components, flying || swimming); active = selected != null;
                }
                case "look_at_player", "look_at_entity" -> {
                    Entity look = nearest(config.optDouble("look_distance", 6), candidate -> goal.equals("look_at_entity") || candidate instanceof Player);
                    if (look != null) entity.lookAt(look.getPosition().add(0, look.getEyeHeight(), 0));
                }
                case "random_look_around" -> {
                    if (host.random() < .01) entity.setRotation(entity.yaw + host.random() * 120 - 60, entity.pitch);
                }
            }
            if (active) {
                destination = selected;
                multiplier = config.optDouble("speed_multiplier", 1);
                break;
            }
        }
        boolean canMove = entity.getFactory().isMovement || components.keySet().stream().anyMatch(key -> key.startsWith("minecraft:movement."));
        double speed = scalar(components, swimming ? "minecraft:underwater_movement" : flying ? "minecraft:flying_speed" : "minecraft:movement",
                entity.getFactory().movement) * multiplier;
        Vector3 waypoint = canMove && destination != null && !entity.isImmobile()
                ? pathfinder.next(destination, ticks, flying, swimming) : null;
        if (waypoint != null) {
            Vector3 direction = waypoint.subtract(entity);
            double horizontal = Math.hypot(direction.x, direction.z);
            if (horizontal > .01) {
                entity.motionX = direction.x / horizontal * speed;
                entity.motionZ = direction.z / horizontal * speed;
            }
            if (flying || swimming) entity.motionY = Math.max(-speed, Math.min(speed, direction.y));
            else if (waypoint.y > entity.y + .2 && entity.onGround) entity.motionY = .42;
            if (components.optJSONObject("minecraft:navigation.walk") != null
                    && components.getJSONObject("minecraft:navigation.walk").optBoolean("can_open_doors", false)) {
                Vector3 ahead = entity.add(direction.normalize());
                if (entity.level.getBlock(ahead) instanceof BlockDoor door && !door.isOpen()) door.toggle(null);
            }
            if (target == null) entity.lookAt(waypoint);
        } else {
            entity.motionX *= .6; entity.motionZ *= .6;
        }
        for (int i = 0; i < Math.min(ticks, 5); i++) {
            if (floatUp) entity.motionY = Math.max(.08, entity.motionY);
            // Apply the full jump impulse before gravity, then integrate each elapsed tick.
            // Applying gravity first cuts a normal jump below the height of one block.
            entity.move(entity.motionX, entity.motionY, entity.motionZ);
            if (entity.isCollidedVertically) entity.motionY = 0;
            boolean inWater = entity.isInsideOfWater();
            if (!floatUp && !flying && entity.getFactory().isPhysics)
                entity.motionY -= inWater ? .02 : entity.getGravity();
            entity.motionY *= inWater ? .8 : .98;
        }
        entity.updateMovement();
        return destination;
    }

    private double scalar(JSONObject components, String key, double fallback) {
        Object value = components.opt(key);
        if (key.equals("minecraft:attack") && value instanceof JSONObject json) value = json.opt("damage");
        return entity.scalar(value, fallback);
    }

    private Vector3 randomDestination(JSONObject components, boolean threeDimensional) {
        if (--wanderDelay > 0 && wander != null && entity.distanceSquared(wander) > 1) return wander;
        wanderDelay = 80 + (int) (host.random() * 80);
        double angle = host.random() * Math.PI * 2;
        double distance = 4 + host.random() * 6;
        wander = entity.getPosition().add(Math.cos(angle) * distance, threeDimensional ? host.random() * 6 - 3 : 0,
                Math.sin(angle) * distance);
        return wander;
    }

    private interface Candidate { boolean test(Entity candidate); }
    private Entity nearest(double radius, Candidate predicate) {
        Entity result = null; double best = radius * radius;
        Vector3 p = entity;
        for (Entity candidate : entity.level.getNearbyEntities(new SimpleAxisAlignedBB(p.x-radius,p.y-radius,p.z-radius,p.x+radius,p.y+radius,p.z+radius), entity)) {
            double distance = entity.distanceSquared(candidate);
            if (distance < best && valid(candidate) && predicate.test(candidate)) { best = distance; result = candidate; }
        }
        return result;
    }

    private boolean valid(Entity candidate) {
        return candidate != entity && candidate.isAlive() && !candidate.closed
                && (!(candidate instanceof Player player) || !player.isCreative() && !player.isSpectator());
    }

    private void selectTarget(JSONObject components) {
        JSONObject selector = components.optJSONObject("minecraft:behavior.nearest_attackable_target");
        if (selector == null || entity.getServer().getDifficulty() == 0) return;
        JSONArray types = selector.optJSONArray("entity_types");
        if (types == null) return;
        Entity target = nearest(scalar(components, "minecraft:follow_range", 32), candidate -> {
            if (!selector.optBoolean("must_see", true) || entity.raycast(candidate) == null) {
                Entity previousOther = host.other;
                host.other = candidate;
                try {
                    for (int i = 0; i < types.length(); i++) {
                        JSONObject type = types.getJSONObject(i);
                        if (entity.distance(candidate) <= type.optDouble("max_dist", 16) && MobFilters.test(type.opt("filters"), host)) return true;
                    }
                } finally { host.other = previousOther; }
            }
            return false;
        });
        if (target != null || !components.has("minecraft:behavior.hurt_by_target")) entity.setTarget(target);
    }

    private void shoot(Entity target, JSONObject components) {
        JSONObject shooter = components.optJSONObject("minecraft:shooter");
        String id = shooter == null ? "minecraft:arrow" : shooter.optString("def", "minecraft:arrow");
        Vector3 start = entity.getPosition().add(0, entity.getEyeHeight(), 0);
        var chunk = entity.level.getChunkIfLoaded(start.getFloorX() >> 4, start.getFloorZ() >> 4);
        if (chunk == null) return;
        Vector3 motion = target.getPosition().add(0, target.getEyeHeight() * .7, 0).subtract(start).normalize().multiply(1.5);
        CompoundTag nbt = Entity.getDefaultNBT(start, motion);
        Entity projectile = Entity.createEntity(id, chunk, nbt, entity);
        if (projectile != null) {
            if (projectile instanceof EntityProjectile shot) shot.shootingEntity = entity;
            if (projectile instanceof CustomEntity custom) custom.namedTag.putLong("BehaviorShooter", entity.getId());
            projectile.spawnToAll();
        }
    }
}
