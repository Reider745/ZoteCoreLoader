package com.reider745.behavior;

import com.reider745.behavior.entities.EntitiesContent;
import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorVersionTest {
    @Test
    void comparesPatchNumbersNumericallyAndNormalizesShortVersions() {
        assertEquals(BehaviorVersion.parse("1.16"), BehaviorVersion.parse("1.16.0"));
        assertTrue(BehaviorVersion.parse("1.16.100").compareTo(BehaviorVersion.parse("1.16.20")) > 0);
        assertThrows(IllegalArgumentException.class, () -> BehaviorVersion.parse("1.16.bad"));
        assertThrows(IllegalArgumentException.class, () -> BehaviorVersion.requireSupported("1.17.0", "1.8"));
    }

    @Test
    void entity116AcceptsOptionalDescriptionFlagsAndPatchVersions() {
        for (String format : new String[]{"1.8.0", "1.16", "1.16.0", "1.16.100", "1.16.200", "1.16.201"}) {
            JSONObject json = new JSONObject("""
                    {"minecraft:entity": {
                      "description": {"identifier":"test:mob","runtime_identifier":"minecraft:pig"},
                      "components": {"minecraft:health":{"value":12,"max":30},"minecraft:movement":{"value":0.25}}
                    }}
                    """).put("format_version", format);
            EntityContentFactory factory = new EntitiesContent().parse(json);
            assertEquals("test:mob", factory.description.identifier);
            assertFalse(factory.description.isExperimental);
            assertFalse(factory.description.isSpawnable);
            assertFalse(factory.description.isSummonable);
            assertEquals("minecraft:pig", factory.description.runtimeIdentifier);
            assertEquals(12, factory.health.defaultValue);
            assertEquals(30, factory.health.max);
            assertEquals(0.25f, factory.movement);
        }
    }
}
