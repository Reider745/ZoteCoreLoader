package com.reider745.behavior.spawnrules;

import cn.nukkit.level.biome.EnumBiome;

import java.util.*;

/** Bedrock 1.16 biome families used by spawn-rule filters. */
public final class BiomeTags {
    private static final Map<Integer, Set<String>> TAGS = new HashMap<>();
    private BiomeTags() {}
    static {
        for (EnumBiome biome : EnumBiome.values()) {
            String name = biome.name().toLowerCase(Locale.ROOT);
            Set<String> tags = new HashSet<>();
            if (Set.of("hell", "soul_sand_valley", "crimson_forest", "warped_forest", "basalt_deltas").contains(name)) {
                tags.add("nether"); tags.add(name.equals("hell") ? "nether_wastes" : name);
            } else if (name.equals("end")) {
                tags.add("the_end");
            } else {
                tags.add("overworld");
                tags.add("overworld_generation");
                if (name.contains("ocean")) { tags.add("ocean"); if (name.contains("deep")) tags.add("deep"); }
                else if (name.contains("river")) tags.add("river");
                else if (name.contains("mushroom")) tags.add("mooshroom_island");
                else if (name.contains("beach") || name.equals("stone_beach")) tags.add("beach");
                else tags.add("animal");
                if (name.contains("plains")) tags.add("plains");
                if (name.contains("desert")) tags.add("desert");
                if (name.contains("forest")) tags.add("forest");
                if (name.contains("taiga")) tags.add("taiga");
                if (name.contains("jungle")) tags.add("jungle");
                if (name.contains("savanna")) tags.add("savanna");
                if (name.contains("swamp")) tags.add("swamp");
                if (name.contains("mesa")) tags.add("mesa");
                if (name.contains("hills") || name.contains("mountain")) tags.add("hills");
                if (name.contains("extreme_hills")) tags.add("extreme_hills");
                if (name.contains("roofed")) tags.add("roofed");
                if (name.contains("birch")) tags.add("birch");
                if (name.contains("bamboo")) tags.add("bamboo");
                if (name.contains("mega")) tags.add("mega");
                if (name.contains("ice") || name.contains("frozen") || name.contains("cold") || name.contains("snow")) {
                    tags.add("frozen"); tags.add("cold");
                }
                if (name.endsWith("_m")) tags.add("mutated");
            }
            tags.add(name);
            tags.add("monster");
            TAGS.put(biome.id, Set.copyOf(tags));
        }
    }
    public static Set<String> get(int biome) { return TAGS.getOrDefault(biome, Set.of()); }
}
