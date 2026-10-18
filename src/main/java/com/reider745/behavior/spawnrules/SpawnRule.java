package com.reider745.behavior.spawnrules;

import com.reider745.behavior.entities.runtime.MobFilters;
import com.reider745.behavior.entities.runtime.MobHost;
import org.json.JSONArray;
import org.json.JSONObject;
import team.unnamed.mocha.runtime.value.Value;

import java.util.*;

/** Parsed conditions and a pure eligibility predicate, shared by tests and live spawning. */
public final class SpawnRule {
    public record Environment(int y, int light, int difficulty, int biome, boolean sky, boolean water,
                              String ground, String block, int nearbySurface, int nearbyUnderground) {}
    public final String identifier;
    public final String population;
    private final List<JSONObject> conditions = new ArrayList<>();
    private static final Set<String> SUPPORTED = Set.of("minecraft:spawns_on_surface", "minecraft:spawns_underground",
            "minecraft:spawns_underwater", "minecraft:brightness_filter", "minecraft:difficulty_filter",
            "minecraft:height_filter", "minecraft:biome_filter", "minecraft:weight", "minecraft:herd",
            "minecraft:density_limit", "minecraft:spawns_on_block_filter", "minecraft:spawns_on_block_prevented_filter",
            "minecraft:permute_type");

    public SpawnRule(JSONObject json) {
        JSONObject description = json.getJSONObject("description");
        identifier = description.getString("identifier");
        population = description.optString("population_control", "animal");
        JSONArray entries = json.optJSONArray("conditions");
        if (entries != null) for (int i = 0; i < entries.length(); i++) {
            JSONObject condition = entries.getJSONObject(i);
            for (String key : condition.keySet()) {
                if (!SUPPORTED.contains(key)) throw new IllegalArgumentException("Unsupported spawn condition: " + key);
            }
            conditions.add(new JSONObject(condition.toString()));
        }
    }

    public List<JSONObject> eligible(Environment environment) {
        List<JSONObject> result = new ArrayList<>();
        for (JSONObject condition : conditions) if (matches(condition, environment)) result.add(condition);
        return result;
    }

    public static boolean matches(JSONObject condition, Environment env) {
        boolean surface = condition.has("minecraft:spawns_on_surface");
        boolean underground = condition.has("minecraft:spawns_underground");
        if ((surface || underground) && !(surface && env.sky || underground && !env.sky)) return false;
        if (condition.has("minecraft:spawns_underwater") != env.water) return false;
        JSONObject height = condition.optJSONObject("minecraft:height_filter");
        if (height != null && (env.y < height.optInt("min", -64) || env.y > height.optInt("max", 320))) return false;
        JSONObject brightness = condition.optJSONObject("minecraft:brightness_filter");
        if (brightness != null && (env.light < brightness.optInt("min", 0) || env.light > brightness.optInt("max", 15))) return false;
        JSONObject difficulty = condition.optJSONObject("minecraft:difficulty_filter");
        if (difficulty != null && (env.difficulty < difficulty(difficulty.opt("min"), 0)
                || env.difficulty > difficulty(difficulty.opt("max"), 3))) return false;
        JSONObject density = condition.optJSONObject("minecraft:density_limit");
        if (env.sky ? env.nearbySurface >= (density == null ? 8 : density.optInt("surface", 8))
                : env.nearbyUnderground >= (density == null ? 8 : density.optInt("underground", 8))) return false;
        Object blocks = condition.opt("minecraft:spawns_on_block_filter");
        if (blocks != null && !contains(blocks, env.ground)) return false;
        blocks = condition.opt("minecraft:spawns_on_block_prevented_filter");
        if (blocks != null && contains(blocks, env.ground)) return false;
        return MobFilters.test(condition.opt("minecraft:biome_filter"), new MobHost() {
            public Object query(String name, List<Value> args) { return 0; }
            public Object filter(String test, String subject, String domain, Object value) {
                return switch (test) {
                    case "has_biome_tag" -> BiomeTags.get(env.biome).contains(value.toString());
                    case "is_biome" -> value instanceof Number number ? env.biome == number.intValue() : BiomeTags.get(env.biome).contains(value.toString());
                    case "is_underwater", "in_water" -> env.water;
                    default -> null;
                };
            }
            public void componentsChanged(JSONObject components, boolean initial) {}
            public void action(String action, Object value) {}
            public double random() { return 0; }
        });
    }

    private static boolean contains(Object input, String block) {
        JSONArray entries = input instanceof JSONArray array ? array : new JSONArray().put(input);
        for (int i = 0; i < entries.length(); i++) {
            Object entry = entries.get(i);
            String name = entry instanceof JSONObject object ? object.optString("name") : entry.toString();
            if (name.equals(block) || ("minecraft:" + name).equals(block)) return true;
        }
        return false;
    }

    private static int difficulty(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        return switch (String.valueOf(value)) { case "peaceful" -> 0; case "easy" -> 1; case "normal" -> 2; case "hard" -> 3; default -> fallback; };
    }

    public static double weight(JSONObject condition) {
        JSONObject weight = condition.optJSONObject("minecraft:weight");
        return weight == null ? 100 : Math.max(0, weight.optDouble("default", 100));
    }

    public static String entityType(JSONObject condition, String fallback, double random) {
        JSONArray types = condition.optJSONArray("minecraft:permute_type");
        if (types == null) return fallback;
        double total = 0;
        for (int i = 0; i < types.length(); i++) total += Math.max(0, types.getJSONObject(i).optDouble("weight", 1));
        double selected = random * total;
        for (int i = 0; i < types.length(); i++) {
            JSONObject type = types.getJSONObject(i);
            selected -= Math.max(0, type.optDouble("weight", 1));
            if (selected < 0) return type.getString("entity_type");
        }
        return fallback;
    }
}
