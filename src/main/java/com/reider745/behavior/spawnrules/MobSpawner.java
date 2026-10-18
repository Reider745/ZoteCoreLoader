package com.reider745.behavior.spawnrules;

import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.InnerCoreServer;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class MobSpawner {
    private static final Map<String, SpawnRule> RULES = new LinkedHashMap<>();
    private static boolean scheduled;
    private record Candidate(SpawnRule rule, JSONObject condition, Vector3 position, double weight) {}
    private MobSpawner() {}

    public static void register(SpawnRule rule) { RULES.put(rule.identifier, rule); }

    public static void start() {
        if (scheduled || RULES.isEmpty()) return;
        scheduled = true;
        Server.getInstance().getScheduler().scheduleRepeatingTask(InnerCoreServer.plugin, MobSpawner::tick, 100);
    }

    private static void tick() {
        for (Level level : Server.getInstance().getLevels().values()) {
            if (!level.isMobSpawningAllowed() || !level.getGameRules().getBoolean(GameRule.DO_MOB_SPAWNING)) continue;
            int attempts = 0;
            for (Player player : level.getPlayers().values()) {
                if (++attempts > 8) break;
                if (!player.isAlive() || player.isSpectator()) continue;
                try { attempt(level, player); }
                catch (RuntimeException e) { Logger.error("Behavior mob spawning in " + level.getName(), e); }
            }
        }
    }

    private static void attempt(Level level, Player player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2);
        double radius = random.nextDouble(24, 64);
        int x = player.getFloorX() + (int) (Math.cos(angle) * radius);
        int z = player.getFloorZ() + (int) (Math.sin(angle) * radius);
        if (level.getChunkIfLoaded(x >> 4, z >> 4) == null) return;
        int surfaceY = level.getHighestBlockAt(x, z) + 1;
        int undergroundY = random.nextInt(Math.max(2, Math.min(120, surfaceY)));
        List<Candidate> candidates = new ArrayList<>();
        double total = 0;
        for (SpawnRule rule : RULES.values()) {
            if (!EntityContentFactory.factoryMap.containsKey(rule.identifier)) continue;
            if (rule.population.equals("monster") && level.getServer().getDifficulty() == 0) continue;
            int surface = 0, underground = 0;
            for (Entity entity : level.getEntities()) {
                if (entity instanceof CustomEntity custom && custom.getFactory().description.identifier.equals(rule.identifier)
                        && Math.abs(entity.x - x) < 64 && Math.abs(entity.z - z) < 64) {
                    if (entity.canSeeSky()) surface++; else underground++;
                }
            }
            for (int y : new LinkedHashSet<>(Arrays.asList(surfaceY, undergroundY, Math.max(1, surfaceY - 2)))) {
                Vector3 position = new Vector3(x + .5, y, z + .5);
                if (!safeDistance(level, position)) continue;
                int block = level.getBlockIdAt(x, y, z);
                boolean water = block == 8 || block == 9;
                if (!water && (!level.getBlock(x, y - 1, z).isSolid() || level.getBlock(x, y, z).isSolid())) continue;
                SpawnRule.Environment env = new SpawnRule.Environment(y, level.getFullLight(position),
                        level.getServer().getDifficulty(), level.getBiomeId(x, z), y >= surfaceY, water,
                        level.getBlock(x, y - 1, z).toItem().getNamespaceId(), level.getBlock(position).toItem().getNamespaceId(),
                        surface, underground);
                for (JSONObject condition : rule.eligible(env)) {
                    double weight = SpawnRule.weight(condition);
                    candidates.add(new Candidate(rule, condition, position, weight));
                    total += weight;
                }
            }
        }
        double selected = random.nextDouble() * total;
        if (total <= 0) return;
        for (Candidate candidate : candidates) {
            selected -= candidate.weight;
            if (selected >= 0) continue;
            JSONObject herd = candidate.condition.optJSONObject("minecraft:herd");
            int min = herd == null ? 1 : herd.optInt("min_size", 1);
            int max = herd == null ? 1 : herd.optInt("max_size", min);
            int count = random.nextInt(Math.max(1, Math.min(16, min)), Math.max(1, Math.min(16, Math.max(min, max))) + 1);
            for (int i = 0; i < count; i++) {
                Vector3 position = i == 0 ? candidate.position : candidate.position.add(random.nextInt(-3, 4), 0, random.nextInt(-3, 4));
                var chunk = level.getChunkIfLoaded(position.getFloorX() >> 4, position.getFloorZ() >> 4);
                if (chunk == null || !safeDistance(level, position)) continue;
                String identifier = SpawnRule.entityType(candidate.condition, candidate.rule.identifier, random.nextDouble());
                EntityContentFactory prototype = EntityContentFactory.factoryMap.getOrDefault(identifier,
                        EntityContentFactory.factoryMap.get(candidate.rule.identifier));
                var box = new cn.nukkit.math.SimpleAxisAlignedBB(position.x - prototype.collision.width / 2,
                        position.y, position.z - prototype.collision.width / 2, position.x + prototype.collision.width / 2,
                        position.y + prototype.collision.height, position.z + prototype.collision.width / 2);
                if (level.hasCollisionBlocks(box)) continue;
                CompoundTag nbt = Entity.getDefaultNBT(position).putString("___id___", identifier);
                Entity entity = Entity.createEntity(identifier, chunk, nbt);
                if (entity instanceof CustomEntity custom && herd != null && herd.has("event")) {
                    custom.runtime().event(herd.getString("event"));
                }
                if (entity != null) entity.spawnToAll();
            }
            return;
        }
    }

    private static boolean safeDistance(Level level, Vector3 position) {
        for (Player player : level.getPlayers().values()) if (player.distanceSquared(position) < 24 * 24) return false;
        return true;
    }
}
