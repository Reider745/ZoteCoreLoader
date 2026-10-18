package com.reider745.behavior.entities.runtime;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.api.VanillaMobsFamily;
import com.reider745.behavior.entities.CustomEntity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import team.unnamed.mocha.runtime.value.Value;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class NukkitMobHost implements MobHost {
    private static final Set<String> WARNED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final CustomEntity entity;
    public Entity other;

    public NukkitMobHost(CustomEntity entity) { this.entity = entity; }
    public double random() { return ThreadLocalRandom.current().nextDouble(); }

    public static List<String> families(Entity entity) {
        return entity instanceof CustomEntity custom ? custom.getFactory().family : VanillaMobsFamily.getFor(entity);
    }

    public Entity subject(String name) {
        return switch (name) {
            case "self" -> entity;
            case "other" -> other;
            case "target" -> entity.getTarget();
            case "damager" -> entity.getLastDamageCause() instanceof EntityDamageByEntityEvent damage ? damage.getDamager() : null;
            case "owner", "parent" -> {
                String uuid = entity.namedTag.getString("BehaviorOwnerUUID");
                Entity owner = uuid.isEmpty() ? null : entity.level.getPlayers().values().stream()
                        .filter(player -> player.getUniqueId().toString().equals(uuid)).findFirst().orElse(null);
                yield owner != null ? owner : entity.level.getEntity(entity.namedTag.contains("BehaviorShooter")
                        ? entity.namedTag.getLong("BehaviorShooter") : entity.namedTag.getLong("BehaviorOwner"));
            }
            case "player" -> other instanceof Player ? other : nearestPlayer();
            default -> null;
        };
    }

    private Player nearestPlayer() {
        Player result = null;
        double distance = Double.MAX_VALUE;
        for (Player player : entity.level.getPlayers().values()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceSquared(entity) < distance) {
                result = player; distance = player.distanceSquared(entity);
            }
        }
        return result;
    }

    public static String identifier(Entity target) {
        if (target instanceof CustomEntity custom) return custom.getFactory().description.identifier;
        if (target instanceof Player) return "minecraft:player";
        return target.getIdentifier() == null ? target.getSaveId() : target.getIdentifier().toString();
    }

    private boolean flag(Entity target, int flag) { return target.getDataFlag(Entity.DATA_FLAGS, flag); }
    private String argument(List<Value> args, int index) { return index < args.size() ? args.get(index).getAsString() : ""; }
    private int axis(List<Value> args) { return args.isEmpty() ? 0 : (int) args.get(0).getAsNumber(); }

    public Object query(String name, List<Value> args) {
        return switch (name) {
            case "health" -> entity.getHealth();
            case "last_hit_by_player" -> subject("damager") instanceof Player;
            case "last_hit_by_player_or_pets" -> subject("damager") instanceof Player
                    || subject("damager") instanceof CustomEntity pet && pet.namedTag.contains("BehaviorOwnerUUID");
            case "max_health" -> entity.getMaxHealth();
            case "is_alive" -> entity.isAlive();
            case "is_baby" -> entity.isBaby();
            case "is_on_ground" -> entity.onGround;
            case "is_in_water" -> entity.isInsideOfWater();
            case "is_in_water_or_rain" -> entity.isInsideOfWater() || entity.level.isRaining() && entity.canSeeSky();
            case "is_on_fire" -> entity.isOnFire();
            case "is_in_lava" -> entity.level.getBlock(entity).getId() == 10 || entity.level.getBlock(entity).getId() == 11;
            case "has_target" -> entity.getTarget() != null;
            case "is_moving" -> entity.motionX * entity.motionX + entity.motionZ * entity.motionZ > .00001;
            case "ground_speed" -> Math.hypot(entity.motionX, entity.motionZ) * 20;
            case "vertical_speed" -> entity.motionY * 20;
            case "position" -> switch (axis(args)) { case 1 -> entity.y; case 2 -> entity.z; default -> entity.x; };
            case "rotation" -> axis(args) == 0 ? entity.pitch : entity.yaw;
            case "body_y_rotation", "head_y_rotation" -> entity.yaw;
            case "body_x_rotation", "head_x_rotation" -> entity.pitch;
            case "time_of_day" -> Math.floorMod(entity.level.getTime(), 24000) / 24000d;
            case "moon_phase" -> entity.level.getMoonPhase(entity.level.getTime());
            case "delta_time" -> entity.tickSeconds();
            case "anim_time" -> entity.animationTime();
            case "is_tamed" -> flag(entity, Entity.DATA_FLAG_TAMED);
            case "is_sitting" -> flag(entity, Entity.DATA_FLAG_SITTING);
            case "is_angry" -> flag(entity, Entity.DATA_FLAG_ANGRY);
            case "is_sheared" -> flag(entity, Entity.DATA_FLAG_SHEARED);
            case "is_saddled" -> flag(entity, Entity.DATA_FLAG_SADDLED);
            case "is_charged", "is_powered" -> flag(entity, Entity.DATA_FLAG_CHARGED);
            case "is_ignited" -> flag(entity, Entity.DATA_FLAG_IGNITED);
            case "is_chested" -> flag(entity, Entity.DATA_FLAG_CHESTED);
            case "is_riding" -> entity.riding != null;
            case "has_rider" -> !entity.passengers.isEmpty();
            case "is_in_love" -> entity.isInLove();
            case "is_sleeping" -> flag(entity, Entity.DATA_FLAG_SLEEPING);
            case "is_leashed" -> flag(entity, Entity.DATA_FLAG_LEASHED);
            case "is_swimming" -> entity.isSwimming();
            case "is_in_contact_with_water" -> entity.isInsideOfWater();
            case "variant" -> entity.getDataPropertyInt(Entity.DATA_VARIANT);
            case "mark_variant" -> entity.getDataPropertyInt(Entity.DATA_MARK_VARIANT);
            case "skin_id" -> entity.getDataPropertyInt(Entity.DATA_SKIN_ID);
            case "scale" -> entity.getScale();
            case "distance_from_camera" -> nearestPlayer() == null ? 100000 : nearestPlayer().distance(entity);
            case "is_family", "is_any_family" -> args.stream().anyMatch(arg -> families(entity).contains(arg.getAsString()));
            case "has_tag", "has_any_tag" -> args.stream().anyMatch(arg -> entity.containTag(arg.getAsString()));
            case "has_all_tags" -> args.stream().allMatch(arg -> entity.containTag(arg.getAsString()));
            case "has_component" -> entity.components().has(argument(args, 0));
            case "looting_level" -> {
                Entity attacker = subject("damager");
                yield attacker == null ? 0 : item(attacker, "main_hand").getEnchantmentLevel(cn.nukkit.item.enchantment.Enchantment.ID_LOOTING);
            }
            case "get_equipped_item_name" -> {
                String nameId = item(entity, argument(args, 0)).getNamespaceId();
                yield nameId.substring(nameId.indexOf(':') + 1);
            }
            case "is_item_name_any" -> {
                String slot = argument(args, 0);
                String id = item(entity, slot).getNamespaceId();
                yield args.stream().skip(1).anyMatch(arg -> arg.getAsString().equals(id));
            }
            default -> {
                warn("Molang query " + name);
                yield 0;
            }
        };
    }

    public Object filter(String test, String subjectName, String domain, Object value) {
        Entity target = subject(subjectName);
        if (target == null) return null;
        String text = String.valueOf(value);
        return switch (test) {
            case "is_family" -> families(target).contains(text);
            case "is_name" -> identifier(target).equals(text) || target.getNameTag().equals(text);
            case "has_tag" -> target.containTag(text);
            case "has_component" -> target instanceof CustomEntity custom && custom.components().has(text);
            case "is_baby" -> target instanceof BaseEntity base && base.isBaby();
            case "is_alive" -> target.isAlive();
            case "is_on_ground" -> target.onGround;
            case "is_in_water", "in_water" -> target.isInsideOfWater();
            case "is_in_water_or_rain" -> target.isInsideOfWater() || target.level.isRaining() && target.canSeeSky();
            case "is_onfire", "is_on_fire" -> target.isOnFire();
            case "is_in_lava" -> target.level.getBlock(target).getId() == 10 || target.level.getBlock(target).getId() == 11;
            case "is_daytime" -> target.level.isDaytime();
            case "is_weather" -> text.equals("rain") ? target.level.isRaining() : text.equals("clear") && !target.level.isRaining();
            case "is_underground" -> !target.canSeeSky();
            case "can_see_sky" -> target.canSeeSky();
            case "in_block" -> target.level.getBlock(target).toItem().getNamespaceId().equals(text);
            case "on_ground" -> target.onGround;
            case "is_difficulty" -> switch (target.getServer().getDifficulty()) {
                case 0 -> "peaceful"; case 1 -> "easy"; case 3 -> "hard"; default -> "normal";
            };
            case "is_game_rule" -> cn.nukkit.level.GameRule.parseString(text).map(rule -> target.level.getGameRules().getBoolean(rule)).orElse(false);
            case "is_variant" -> target.getDataPropertyInt(Entity.DATA_VARIANT);
            case "is_mark_variant" -> target.getDataPropertyInt(Entity.DATA_MARK_VARIANT);
            case "is_skin_id" -> target.getDataPropertyInt(Entity.DATA_SKIN_ID);
            case "is_color" -> target.getDataPropertyByte(Entity.DATA_COLOR);
            case "is_health" -> target.getHealth();
            case "health" -> target.getHealth();
            case "distance_to_nearest_player" -> nearestPlayer() == null ? Double.MAX_VALUE : nearestPlayer().distance(target);
            case "target_distance" -> entity.getTarget() == null ? Double.MAX_VALUE : entity.getTarget().distance(target);
            case "actor_health" -> target.getHealth();
            case "has_target" -> target instanceof BaseEntity base && base.getTarget() != null;
            case "has_equipment" -> item(target, domain).getNamespaceId().equals(text);
            case "has_damage" -> target.getLastDamageCause() != null
                    && target.getLastDamageCause().getCause().name().equalsIgnoreCase(text);
            case "is_owner" -> target == subject("owner");
            case "is_tamed" -> flag(target, Entity.DATA_FLAG_TAMED);
            case "is_sitting" -> flag(target, Entity.DATA_FLAG_SITTING);
            case "is_saddled" -> flag(target, Entity.DATA_FLAG_SADDLED);
            case "is_sheared" -> flag(target, Entity.DATA_FLAG_SHEARED);
            case "is_sneaking" -> flag(target, Entity.DATA_FLAG_SNEAKING);
            case "is_riding" -> target.riding != null;
            case "is_in_love" -> target instanceof BaseEntity base && base.isInLove();
            case "is_moving" -> Math.hypot(target.motionX, target.motionZ) > .001;
            case "clock_time" -> Math.floorMod(target.level.getTime(), 24000) / 24000d;
            case "moon_phase" -> target.level.getMoonPhase(target.level.getTime());
            case "random_chance" -> random() < 1 / Math.max(1, Double.parseDouble(text));
            default -> { warn("filter " + test); yield null; }
        };
    }

    public static Item item(Entity entity, String slot) {
        if (entity instanceof Player player) {
            return switch (slot) {
                case "head" -> player.getInventory().getHelmet();
                case "chest", "torso" -> player.getInventory().getChestplate();
                case "legs" -> player.getInventory().getLeggings();
                case "feet" -> player.getInventory().getBoots();
                case "off_hand", "offhand" -> player.getOffhandInventory().getItem(0);
                default -> player.getInventory().getItemInHand();
            };
        }
        if (entity instanceof CustomEntity custom) return custom.equipped(slot);
        return Item.AIR_ITEM;
    }

    public void componentsChanged(JSONObject components, boolean initial) {
        entity.applyComponents(components, initial);
    }

    public void action(String action, Object value) {
        switch (action) {
            case "trigger" -> {
                JSONObject trigger = (JSONObject) value;
                Entity target = subject(trigger.optString("target", "self"));
                if (target instanceof CustomEntity custom) custom.runtime().event(trigger.optString("event"));
            }
            case "play_sound" -> {
                String sound = value instanceof JSONObject json ? json.optString("sound") : value.toString();
                entity.level.addSound(entity, sound);
            }
            case "particle" -> {
                String effect = value instanceof JSONObject json ? json.optString("effect") : value.toString();
                entity.level.addParticleEffect(entity.asVector3f(), effect, entity.getId(), com.reider745.InnerCoreServer.PROTOCOL);
            }
            case "run_command" -> {
                Object commands = value instanceof JSONObject json ? json.opt("command") : value;
                if (commands instanceof JSONArray array) for (int i = 0; i < array.length(); i++) entity.command(array.getString(i));
                else if (commands != null) entity.command(commands.toString());
            }
            default -> warn("event action " + action);
        }
    }

    public void warn(String feature) {
        String key = entity.getFactory().description.identifier + ": " + feature;
        if (WARNED.add(key)) Logger.warning("Unsupported behavior mob feature " + key);
    }
}
