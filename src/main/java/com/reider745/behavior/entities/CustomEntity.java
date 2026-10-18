package com.reider745.behavior.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import com.reider745.behavior.entities.runtime.*;
import com.reider745.behavior.loots.MobLoot;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class CustomEntity extends BaseEntity implements cn.nukkit.entity.custom.CustomEntity {
    protected EntityContentFactory factory;
    private MobRuntime runtime;
    private NukkitMobHost host;
    private MobAI ai;
    private JSONObject components = new JSONObject();
    private double tickSeconds = .05;
    private double animationTime;
    private int breathingTicks;
    private double explosionFuse = Double.NaN;
    private double ageRemaining = Double.NaN;
    private final Map<String, Double> spawnTimers = new HashMap<>();
    private final Map<Integer, Integer> interactCooldowns = new HashMap<>();
    private Item hand = Item.AIR_ITEM.clone();
    private Item offHand = Item.AIR_ITEM.clone();
    private boolean deathEvent;
    private MobAnimations animations;
    private final Map<Player, cn.nukkit.utils.DummyBossBar> bossBars = new HashMap<>();
    public int tickMove;
    public int tickDamage;
    public final int timeDamage = 30;

    public CustomEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        runtime();
    }

    @Override
    protected void initEntity() {
        EntityContentFactory definition = getFactory();
        setMaxHealth(definition.health.max);
        if (!namedTag.contains("Health")) namedTag.putFloat("Health", definition.health.defaultValue);
        super.initEntity();
    }

    public EntityContentFactory getFactory() {
        if (factory != null) return factory;
        String id = namedTag.getString("___id___");
        EntityContentFactory prototype = EntityContentFactory.factoryMap.get(id);
        if (prototype == null) throw new IllegalStateException("Unknown behavior entity: " + id);
        factory = prototype.instance(prototype.definition.components(List.of()));
        return factory;
    }

    public MobRuntime runtime() {
        if (runtime == null) {
            host = new NukkitMobHost(this);
            runtime = new MobRuntime(getFactory().definition, host);
            ai = new MobAI(this, host);
            animations = new MobAnimations(runtime);
            JSONObject saved = namedTag.contains("BehaviorState") ? new JSONObject(namedTag.getString("BehaviorState")) : null;
            runtime.initialize(saved);
            if (saved != null) {
                ageRemaining = saved.optDouble("age_remaining", Double.NaN);
                breathingTicks = saved.optInt("air", 0);
                explosionFuse = saved.optDouble("fuse", Double.NaN);
                animations.restore(saved.optJSONObject("animations"));
            }
            if (namedTag.contains("BehaviorHand")) hand = cn.nukkit.nbt.NBTIO.getItemHelper(namedTag.getCompound("BehaviorHand"));
            if (namedTag.contains("BehaviorOffHand")) offHand = cn.nukkit.nbt.NBTIO.getItemHelper(namedTag.getCompound("BehaviorOffHand"));
            if (saved == null) {
                JSONObject equipment = components.optJSONObject("minecraft:equipment");
                if (equipment != null && equipment.has("table")) {
                    for (Item item : MobLoot.generate(getFactory().definition.source(), equipment.getString("table"), host)) {
                        equip(item.isHelmet() ? "head" : item.isChestplate() ? "chest" : item.isLeggings() ? "legs"
                                : item.isBoots() ? "feet" : "main_hand", item);
                    }
                }
            }
        }
        return runtime;
    }

    public NukkitMobHost host() { runtime(); return host; }
    public JSONObject components() { return components; }
    public double tickSeconds() { return tickSeconds; }
    public double animationTime() { return animationTime; }
    public void animationTime(double time) { animationTime = time; }

    public void applyComponents(JSONObject next, boolean initial) {
        JSONObject previous = components;
        factory = EntityContentFactory.factoryMap.get(getFactory().description.identifier).instance(next);
        components = next;
        JSONObject health = next.optJSONObject("minecraft:health");
        if (health != null && (initial || !Objects.equals(String.valueOf(previous.opt("minecraft:health")), health.toString()))) {
            int value = (int) runtime.duration(health.opt("value"), getHealth());
            int maximum = (int) runtime.duration(health.opt("max"), value);
            setMaxHealth(Math.max(1, maximum));
            setHealth(Math.min(maximum, Math.max(0, initial && namedTag.contains("BehaviorState") ? namedTag.getFloat("Health") : value)));
        } else {
            if (health == null) setMaxHealth(Math.max(1, factory.health.max));
            if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        }
        fireProof = factory.isFireImmune;
        entityCollisionReduction = scalar(next.opt("minecraft:push_through"), 0);
        setScale((float) scalar(next.opt("minecraft:scale"), 1));
        setBaby(next.has("minecraft:is_baby"));
        setNoClip(next.optJSONObject("minecraft:physics") != null
                && !next.getJSONObject("minecraft:physics").optBoolean("has_collision", true));
        setCanBeSavedWithChunk(!next.has("minecraft:instant_despawn"));
        for (Map.Entry<String, Integer> property : Map.of("variant", DATA_VARIANT, "mark_variant", DATA_MARK_VARIANT,
                "skin_id", DATA_SKIN_ID).entrySet()) {
            setDataProperty(new IntEntityData(property.getValue(), (int) scalar(next.opt("minecraft:" + property.getKey()), 0)));
        }
        for (Map.Entry<String, Integer> flag : Map.of("is_tamed", DATA_FLAG_TAMED, "is_saddled", DATA_FLAG_SADDLED,
                "is_sheared", DATA_FLAG_SHEARED, "is_charged", DATA_FLAG_CHARGED, "is_ignited", DATA_FLAG_IGNITED,
                "is_chested", DATA_FLAG_CHESTED).entrySet()) {
            setDataFlag(DATA_FLAGS, flag.getValue(), next.has("minecraft:" + flag.getKey())
                    || flag.getKey().equals("is_tamed") && namedTag.contains("BehaviorOwnerUUID"));
        }
        if (next.has("minecraft:color")) setDataProperty(new cn.nukkit.entity.data.ByteEntityData(DATA_COLOR,
                (int) scalar(next.opt("minecraft:color"), 0)));
        recalculateBoundingBox();
        if (!Objects.equals(String.valueOf(previous.opt("minecraft:ageable")), String.valueOf(next.opt("minecraft:ageable")))) ageRemaining = Double.NaN;
        if (!next.has("minecraft:explode")) explosionFuse = Double.NaN;
        if (next.has("minecraft:instant_despawn")) close();
    }

    public double scalar(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof JSONObject object) value = object.opt("value");
        return value == null ? fallback : runtime.duration(value, fallback);
    }

    public void feedBaby(double fraction) {
        JSONObject ageable = components.optJSONObject("minecraft:ageable");
        if (ageable == null || !isBaby()) return;
        if (Double.isNaN(ageRemaining)) ageRemaining = runtime.duration(ageable.opt("duration"), 1200);
        ageRemaining *= 1 - Math.max(0, Math.min(1, fraction));
    }

    public boolean canInteract(int index) { return ticksLived >= interactCooldowns.getOrDefault(index, 0); }
    public void interactionUsed(int index, double cooldown) { interactCooldowns.put(index, ticksLived + (int) (cooldown * 20)); }

    @Override public float getWidth() { return getFactory().collision.width; }
    @Override public float getLength() { return getWidth(); }
    @Override public float getHeight() { return getFactory().collision.height; }
    @Override public int getNetworkId() { return -1; }
    @Override public EntityDefinition getEntityDefinition() { return getFactory().getEntityDefinition(); }
    @Override public cn.nukkit.utils.Identifier getIdentifier() {
        return namedTag == null ? null : new cn.nukkit.utils.Identifier(getFactory().description.identifier);
    }
    @Override public String getName() {
        return factory == null ? "Behavior entity" : "entity." + factory.description.identifier + ".name";
    }
    @Override protected DataPacket createAddEntityPacket() {
        AddEntityPacket packet = (AddEntityPacket) super.createAddEntityPacket();
        packet.id = getFactory().description.identifier;
        return packet;
    }

    @Override public boolean onUpdate(int currentTick) {
        if (closed) return false;
        runtime();
        int ticks = lastUpdate == 0 ? 1 : Math.max(1, Math.min(100, currentTick - lastUpdate));
        lastUpdate = currentTick;
        tickSeconds = ticks / 20d;
        if (!isAlive()) {
            if (++deadTicks >= 23) close();
            return !closed;
        }
        try {
            runtime.tick(ticks);
            if (closed || !isAlive()) return !closed;
            animations.tick(tickSeconds);
            // EntityLiving's land-only drowning must not also damage actors with custom respiration.
            if (components.has("minecraft:breathable")) setAirTicks(400);
            entityBaseTick(ticks);
            if (!isAlive()) return true;
            for (var update : factory.updates) {
                // Breathable uses the component flags below, not EntityLiving's default land respiration.
                if (!(update instanceof com.reider745.behavior.entities.formats.tags.components.life.BreathableComponent)) update.onUpdate(this);
            }
            lifeCycle(ticks);
            if (closed || !isAlive()) return !closed;
            if (components.has("minecraft:projectile")) MobProjectile.tick(this, ticks);
            else updateMove(ticks);
            for (var bar : bossBars.values()) bar.setLength(100 * getHealth() / getMaxHealth());
        } catch (RuntimeException e) {
            Logger.error("Behavior entity " + getFactory().description.identifier + " at " + x + "," + y + "," + z, e);
            close(); // A faulty actor must not abort the server tick or repeatedly flood the log.
        }
        return !closed;
    }

    private void lifeCycle(int ticks) {
        JSONObject breathable = components.optJSONObject("minecraft:breathable");
        if (breathable != null) {
            int block = level.getBlock(this).getId();
            boolean breathing = block == 10 || block == 11 ? breathable.optBoolean("breathes_lava", false)
                    : isInsideOfWater() ? breathable.optBoolean("breathes_water", false)
                    : isInsideOfSolid() ? breathable.optBoolean("breathes_solids", false) : breathable.optBoolean("breathes_air", true);
            if (breathing) breathingTicks = 0;
            else {
                breathingTicks += ticks;
                double supply = breathable.optDouble("total_supply", 15) * 20;
                int period = Math.max(1, (int) (breathable.optDouble("suffocate_time", 0) * 20) + 20);
                if (breathingTicks > supply && breathingTicks % period < ticks)
                    attack(new EntityDamageEvent(this, isInsideOfWater() ? EntityDamageEvent.DamageCause.DROWNING : EntityDamageEvent.DamageCause.SUFFOCATION, 2));
            }
            setAirTicks((int) Math.max(0, breathable.optDouble("total_supply", 15) * 20 - breathingTicks));
        }
        JSONObject ageable = components.optJSONObject("minecraft:ageable");
        if (ageable != null && isBaby()) {
            if (Double.isNaN(ageRemaining)) ageRemaining = runtime.duration(ageable.opt("duration"), 1200);
            ageRemaining -= tickSeconds;
            if (ageRemaining <= 0) {
                setBaby(false);
                runtime.trigger(ageable.opt("grow_up"));
                ageRemaining = Double.POSITIVE_INFINITY;
            }
        }
        JSONObject explode = components.optJSONObject("minecraft:explode");
        if (explode != null) {
            if (Double.isNaN(explosionFuse) && explode.optBoolean("fuse_lit", false)) explosionFuse = runtime.duration(explode.opt("fuse_length"), 3);
            if (Double.isFinite(explosionFuse)) {
                explosionFuse -= tickSeconds;
                if (explosionFuse <= 0) {
                    Explosion explosion = new Explosion(getPosition(), explode.optDouble("power", 3), this);
                    explosion.setIncendiary(explode.optBoolean("causes_fire", false));
                    if (explode.optBoolean("breaks_blocks", true) && level.getGameRules().getBoolean(GameRule.MOB_GRIEFING)) explosion.explodeA();
                    explosion.explodeB();
                    close();
                }
            }
        }
        Object spawn = components.opt("minecraft:spawn_entity");
        JSONArray spawns = spawn instanceof JSONArray a ? a : spawn instanceof JSONObject o ? new JSONArray().put(o) : new JSONArray();
        for (int i = 0; i < spawns.length(); i++) {
            JSONObject config = spawns.getJSONObject(i);
            String key = config.toString();
            double time = spawnTimers.getOrDefault(key, config.optDouble("min_wait_time", 300)
                    + host.random() * (config.optDouble("max_wait_time", 600) - config.optDouble("min_wait_time", 300)));
            time -= tickSeconds;
            if (time <= 0) {
                if (MobFilters.test(config.opt("filters"), host)) {
                    for (int n = 0; n < Math.min(16, config.optInt("num_to_spawn", 1)); n++) {
                        spawnChild(config.optString("spawn_entity", config.optString("entity_type")), config.optString("spawn_event"), new Vector3(x, y, z));
                        if (config.has("spawn_item")) level.dropItem(this, com.reider745.behavior.recipes.RecipeItems.result(config.get("spawn_item")));
                    }
                    if (config.has("spawn_sound")) level.addSound(this, config.getString("spawn_sound"));
                }
                time = config.optBoolean("single_use", false) ? Double.POSITIVE_INFINITY
                        : config.optDouble("min_wait_time", 300) + host.random() * Math.max(0,
                        config.optDouble("max_wait_time", 600) - config.optDouble("min_wait_time", 300));
            }
            spawnTimers.put(key, time);
        }
        JSONObject transform = components.optJSONObject("minecraft:transformation");
        if (transform != null) {
            double delay = transform.optJSONObject("delay") == null ? 0 : transform.getJSONObject("delay").optDouble("value", 0);
            double time = spawnTimers.getOrDefault("transformation", delay) - tickSeconds;
            spawnTimers.put("transformation", time);
            if (time <= 0) {
                String[] into = transform.getString("into").split("<", 2);
                Entity replacement = spawnChild(into[0], into.length > 1 ? into[1].replace(">", "") : "", this);
                if (replacement != null) {
                    if (transform.optBoolean("keep_owner", false)) replacement.namedTag.putLong("BehaviorOwner", namedTag.getLong("BehaviorOwner"));
                    close();
                }
            }
        }
        JSONObject despawn = components.optJSONObject("minecraft:despawn");
        if (despawn != null && !components.has("minecraft:persistent")) {
            double distance = level.getPlayers().values().stream().mapToDouble(player -> player.distance(this)).min().orElse(100000);
            JSONObject ranges = despawn.optJSONObject("despawn_from_distance");
            if (distance > (ranges == null ? 128 : ranges.optDouble("max_distance", 128))
                    && MobFilters.test(despawn.opt("filters"), host)) close();
        }
    }

    @Override public Vector3 updateMove(int ticks) {
        runtime();
        return ai.tick(ticks);
    }

    public Entity spawnChild(String identifier, String event, Vector3 position) {
        if (identifier == null || identifier.isEmpty()) return null;
        FullChunk chunk = level.getChunkIfLoaded(position.getFloorX() >> 4, position.getFloorZ() >> 4);
        if (chunk == null) return null;
        CompoundTag nbt = Entity.getDefaultNBT(position);
        Entity child = Entity.createEntity(identifier, chunk, nbt);
        if (child != null) {
            if (child instanceof CustomEntity custom && event != null && !event.isEmpty()) custom.runtime().event(event);
            child.spawnToAll();
        }
        return child;
    }

    @Override public boolean attack(EntityDamageEvent damage) {
        if (factory == null || runtime == null) return super.attack(damage);
        Entity previousOther = host.other;
        host.other = damage instanceof EntityDamageByEntityEvent event ? event.getDamager() : null;
        try {
            JSONObject sensor = components.optJSONObject("minecraft:damage_sensor");
            Object triggers = sensor == null ? null : sensor.opt("triggers");
            JSONArray entries = triggers instanceof JSONArray a ? a : triggers instanceof JSONObject o ? new JSONArray().put(o) : new JSONArray();
            for (int i = 0; i < entries.length(); i++) {
                JSONObject trigger = entries.getJSONObject(i);
                if (!trigger.optString("cause", "all").equals("all")
                        && !trigger.getString("cause").equalsIgnoreCase(damage.getCause().name())) continue;
                JSONObject event = trigger.optJSONObject("on_damage");
                if (event != null && !MobFilters.test(event.opt("filters"), host)) continue;
                if (!trigger.optBoolean("deals_damage", true)) damage.setCancelled();
                damage.setDamage(Math.max(0, damage.getDamage() + trigger.optFloat("damage_modifier", 0))
                        * trigger.optFloat("damage_multiplier", 1));
                runtime.trigger(event);
            }
            boolean accepted = super.attack(damage);
            if (accepted) {
                if (host.other != null && components.has("minecraft:behavior.hurt_by_target")) setTarget(host.other);
                runtime.trigger(components.opt("minecraft:on_hurt"));
                if (host.other instanceof Player) runtime.trigger(components.opt("minecraft:on_hurt_by_player"));
            }
            return accepted;
        } finally { host.other = previousOther; }
    }

    @Override public void setTarget(Entity target) {
        Entity previous = getTarget();
        super.setTarget(target);
        this.target = target;
        if (runtime != null && previous != target) runtime.trigger(components.opt(target == null
                ? "minecraft:on_target_escape" : "minecraft:on_target_acquired"));
    }

    @Override public void kill() {
        if (!deathEvent && runtime != null) {
            deathEvent = true;
            runtime.trigger(components.opt("minecraft:on_death"));
            runtime.event("minecraft:entity_died");
        }
        super.kill();
    }

    @Override public void saveNBT() {
        super.saveNBT();
        namedTag.putString("id", getFactory().description.identifier);
        namedTag.putString("___id___", getFactory().description.identifier);
        namedTag.putCompound("BehaviorHand", cn.nukkit.nbt.NBTIO.putItemHelper(hand));
        namedTag.putCompound("BehaviorOffHand", cn.nukkit.nbt.NBTIO.putItemHelper(offHand));
        if (runtime != null) {
            JSONObject saved = runtime.save().put("air", breathingTicks).put("animations", animations.save());
            if (Double.isFinite(ageRemaining)) saved.put("age_remaining", ageRemaining);
            if (Double.isFinite(explosionFuse)) saved.put("fuse", explosionFuse);
            namedTag.putString("BehaviorState", saved.toString());
        }
    }

    @Override public Item[] getDrops() {
        JSONObject loot = components.optJSONObject("minecraft:loot");
        try {
            List<Item> drops = loot == null ? new ArrayList<>() : MobLoot.generate(getFactory().definition.source(), loot.optString("table"), host);
            JSONObject equipment = components.optJSONObject("minecraft:equipment");
            if (equipment != null) {
                JSONArray chances = equipment.optJSONArray("slot_drop_chance");
                for (String slot : List.of("main_hand", "off_hand", "head", "chest", "legs", "feet")) {
                    double chance = .085;
                    if (chances != null) for (int i = 0; i < chances.length(); i++) {
                        JSONObject entry = chances.getJSONObject(i);
                        String key = entry.optString("slot").replace("slot.weapon.", "").replace("slot.armor.", "")
                                .replace("mainhand", "main_hand").replace("offhand", "off_hand");
                        if (slot.equals(key)) chance = entry.optDouble("drop_chance", .085);
                    }
                    Item item = equipped(slot);
                    if (item != null && !item.isNull() && host.random() < chance) drops.add(item.clone());
                }
            }
            return drops.toArray(Item[]::new);
        } catch (IllegalArgumentException e) {
            host.warn("loot: " + e.getMessage());
            return new Item[0];
        }
    }

    @Override public int getKillExperience() {
        JSONObject reward = components.optJSONObject("minecraft:experience_reward");
        return reward == null || runtime == null ? 0 : (int) Math.max(0, runtime.molang().evaluate(reward.opt("on_death")));
    }

    @Override public boolean canDespawn() { return false; } // Data-driven despawn is handled above.
    @Override public boolean canBePushed() { return getFactory().isPushable; }
    @Override public boolean onInteract(Player player, Item item) {
        runtime();
        if (getFactory().isNameable && applyNameTag(player, item)) return true;
        return MobInteractions.interact(this, player, item);
    }
    @Override public void setOnFire(int seconds) {
        if (getFactory().isFireImmune) return;
        super.setOnFire(seconds);
        if (runtime != null) runtime.trigger(components.opt("minecraft:on_ignite"));
    }

    public void command(String command) { getServer().dispatchCommand(new MobCommandSender(this), command.startsWith("/") ? command.substring(1) : command); }
    public Item equipped(String slot) {
        return switch (slot) {
            case "off_hand", "offhand" -> offHand;
            case "head" -> armor == null ? Item.AIR_ITEM : armor[0];
            case "chest", "torso" -> armor == null ? Item.AIR_ITEM : armor[1];
            case "legs" -> armor == null ? Item.AIR_ITEM : armor[2];
            case "feet" -> armor == null ? Item.AIR_ITEM : armor[3];
            default -> hand;
        };
    }
    public void equip(String slot, Item item) {
        if (slot.equals("off_hand")) offHand = item;
        else if (slot.equals("main_hand") || slot.equals("hand")) hand = item;
        else {
            if (armor == null) armor = new Item[]{Item.AIR_ITEM, Item.AIR_ITEM, Item.AIR_ITEM, Item.AIR_ITEM};
            int index = switch (slot) { case "head" -> 0; case "chest", "torso" -> 1; case "legs" -> 2; default -> 3; };
            armor[index] = item;
        }
        for (Player player : hasSpawned.values()) sendEquipment(player);
    }

    @Override public void spawnTo(Player player) {
        super.spawnTo(player);
        if (!hasSpawned.containsValue(player)) return;
        sendEquipment(player);
        if (components.has("minecraft:boss") && !bossBars.containsKey(player)) {
            JSONObject boss = components.getJSONObject("minecraft:boss");
            var bar = new cn.nukkit.utils.DummyBossBar.Builder(player).text(boss.optString("name", getName()))
                    .length(100 * getHealth() / getMaxHealth()).build();
            bar.create(); bossBars.put(player, bar);
        }
    }

    private void sendEquipment(Player player) {
        var main = new cn.nukkit.network.protocol.MobEquipmentPacket();
        main.eid = getId(); main.item = hand; main.inventorySlot = 0; main.hotbarSlot = 0;
        player.dataPacket(main);
        var off = new cn.nukkit.network.protocol.MobEquipmentPacket();
        off.eid = getId(); off.item = offHand; off.windowId = 119;
        player.dataPacket(off);
        if (armor != null) {
            var equipment = new cn.nukkit.network.protocol.MobArmorEquipmentPacket();
            equipment.eid = getId(); equipment.slots = armor; equipment.body = Item.AIR_ITEM;
            player.dataPacket(equipment);
        }
    }

    @Override public void despawnFrom(Player player) {
        var bar = bossBars.remove(player);
        if (bar != null) bar.destroy();
        super.despawnFrom(player);
    }

    @Override public void knockBack(Entity attacker, double strength, double x, double z) {
        super.knockBack(attacker, strength * Math.max(0, 1 - scalar(components.opt("minecraft:knockback_resistance"), 0)), x, z);
    }

    @Override public cn.nukkit.math.Vector3f getMountedOffset(Entity passenger) {
        JSONObject rideable = components.optJSONObject("minecraft:rideable");
        if (rideable != null) {
            Object seats = rideable.opt("seats");
            JSONObject seat = seats instanceof JSONObject object ? object : seats instanceof JSONArray array && !array.isEmpty()
                    ? array.getJSONObject(Math.min(Math.max(0, passengers.indexOf(passenger)), array.length() - 1)) : null;
            JSONArray position = seat == null ? null : seat.optJSONArray("position");
            if (position != null) return new cn.nukkit.math.Vector3f(position.getFloat(0), position.getFloat(1), position.getFloat(2));
        }
        return super.getMountedOffset(passenger);
    }

    // Compatibility helpers used by the legacy component API.
    public void setTargetBlock(Vector3 target) { this.target = target; }
    public void targetPositionMovementUpdate() { this.target = getPosition(); }
    public Block isSolid(double x, double y, double z) {
        Block block = level.getBlock((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        return block.isSolid() ? block : null;
    }
    public Block raycast(Entity target) {
        Vector3 origin = getPosition().add(0, getEyeHeight(), 0);
        Vector3 delta = target.getPosition().add(0, target.getEyeHeight(), 0).subtract(origin);
        int steps = Math.max(1, (int) Math.ceil(delta.length() * 3));
        Vector3 step = delta.divide(steps);
        for (int i = 0; i < steps; i++) {
            Block block = isSolid(origin.x, origin.y, origin.z);
            if (block != null) return block;
            origin = origin.add(step);
        }
        return null;
    }
}
