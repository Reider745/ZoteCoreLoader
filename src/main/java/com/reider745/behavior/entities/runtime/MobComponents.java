package com.reider745.behavior.entities.runtime;

import java.util.Set;

/** Components implemented by the per-actor runtime rather than legacy component adapters. */
public final class MobComponents {
    private MobComponents() {}
    public static final Set<String> NAMES = Set.of(
            "scale", "variant", "mark_variant", "skin_id", "color", "is_baby", "is_tamed", "is_saddled",
            "is_sheared", "is_charged", "is_ignited", "is_chested", "is_stackable", "persistent",
            "timer", "environment_sensor", "damage_sensor", "on_hurt", "on_hurt_by_player", "on_death",
            "on_target_acquired", "on_target_escape", "on_ignite", "loot", "experience_reward",
            "interact", "tameable", "breedable", "ageable", "healable", "rideable", "equippable", "equipment",
            "knockback_resistance", "push_through", "follow_range", "flying_speed", "underwater_movement",
            "movement.fly", "movement.hover", "movement.sway", "movement.amphibious", "movement.generic",
            "movement.jump", "movement.skip", "navigation.fly", "navigation.hover", "navigation.swim",
            "navigation.generic", "navigation.climb", "jump.dynamic", "can_fly", "floats_in_liquid",
            "despawn", "instant_despawn", "transformation", "spawn_entity", "shooter", "projectile", "explode",
            "behavior.float", "behavior.nearest_attackable_target", "behavior.hurt_by_target",
            "behavior.melee_attack", "behavior.delayed_attack", "behavior.ranged_attack",
            "behavior.random_stroll", "behavior.random_swim", "behavior.random_fly", "behavior.random_hover",
            "behavior.random_look_around", "behavior.look_at_player", "behavior.look_at_entity",
            "behavior.follow_owner", "behavior.follow_parent", "behavior.move_towards_target",
            "behavior.tempt", "behavior.panic", "behavior.avoid_mob_type", "behavior.breed",
            "behavior.leap_at_target", "behavior.swell", "behavior.sit", "behavior.stay_while_sitting");
    public static boolean supports(String name) {
        return name.startsWith("minecraft:") && NAMES.contains(name.substring(10));
    }
}
