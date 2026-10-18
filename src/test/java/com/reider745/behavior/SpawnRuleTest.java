package com.reider745.behavior;

import com.reider745.behavior.spawnrules.SpawnRule;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpawnRuleTest {
    private SpawnRule.Environment environment(int light, int difficulty, int population) {
        return new SpawnRule.Environment(64, light, difficulty, 1, true, false, "minecraft:grass", "minecraft:air", population, 0);
    }
    @Test void checksLightDifficultyBiomeSurfaceAndPopulation() {
        JSONObject condition = new JSONObject("""
                {"minecraft:spawns_on_surface":{},"minecraft:brightness_filter":{"min":0,"max":7},
                 "minecraft:difficulty_filter":{"min":"easy","max":"hard"},
                 "minecraft:height_filter":{"min":20,"max":90},
                 "minecraft:spawns_on_block_filter":["minecraft:grass"],
                 "minecraft:density_limit":{"surface":4},
                 "minecraft:biome_filter":{"all_of":[{"test":"has_biome_tag","value":"overworld"},
                 {"test":"has_biome_tag","value":"animal"}]}}
                """);
        assertTrue(SpawnRule.matches(condition, environment(7, 1, 3)));
        assertFalse(SpawnRule.matches(condition, environment(8, 1, 3)));
        assertFalse(SpawnRule.matches(condition, environment(7, 0, 3)));
        assertFalse(SpawnRule.matches(condition, environment(7, 1, 4)));
        assertFalse(SpawnRule.matches(condition, new SpawnRule.Environment(10, 0, 2, 1,
                false, false, "minecraft:stone", "minecraft:air", 0, 0)));
    }
    @Test void choosesWeightedVariantsAndRejectsUnknownSpawnConditions() {
        JSONObject condition = new JSONObject("""
                {"minecraft:permute_type":[{"weight":1,"entity_type":"test:a"},{"weight":3,"entity_type":"test:b"}]}
                """);
        assertEquals("test:a", SpawnRule.entityType(condition, "fallback", .1));
        assertEquals("test:b", SpawnRule.entityType(condition, "fallback", .5));
        assertThrows(IllegalArgumentException.class, () -> new SpawnRule(new JSONObject("""
                {"description":{"identifier":"test:mob"},"conditions":[{"minecraft:unknown_condition":{}}]}
                """)));
    }
}
