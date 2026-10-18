package com.reider745.behavior;

import com.reider745.api.CustomManager;
import com.reider745.behavior.items.ItemsContent;
import com.reider745.item.CustomItemClass;
import com.reider745.item.ItemMethod.PropertiesNames;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemsContentTest {
    private CustomManager manager() {
        CustomManager manager = new CustomManager(2050, CustomItemClass.class, "item", "test:item");
        manager.put(PropertiesNames.NAME, "Default");
        return manager;
    }

    @Test
    void parsesLegacyAnd116100Properties() {
        CustomManager legacy = manager();
        ItemsContent.applyProperties(legacy, new JSONObject("""
                {"minecraft:max_stack_size":{"value":16},"minecraft:max_damage":25,
                 "minecraft:hand_equipped":{"value":true}}
                """));
        assertEquals(16, (int) legacy.get(PropertiesNames.MAX_STACK));
        assertEquals(25, (int) legacy.get(PropertiesNames.MAX_DAMAGE));
        assertEquals(true, legacy.get(PropertiesNames.HAND_EQUIPPED));

        CustomManager modern = manager();
        ItemsContent.applyProperties(modern, new JSONObject("""
                {"minecraft:max_stack_size":1,"minecraft:durability":{"max_durability":200},
                 "minecraft:display_name":{"value":"Test Sword"}}
                """));
        assertEquals(1, (int) modern.get(PropertiesNames.MAX_STACK));
        assertEquals(200, (int) modern.get(PropertiesNames.MAX_DAMAGE));
        assertEquals("Test Sword", modern.get(PropertiesNames.NAME));
    }

    @Test
    void validatesFormatNamespaceAndProperties() {
        JSONObject json = new JSONObject("""
                {"format_version":"1.16.100","minecraft:item":{"description":{"identifier":"test:item"}}}
                """);
        assertTrue(ItemsContent.components(json).isEmpty());
        json.put("format_version", "1.17.0");
        assertThrows(IllegalArgumentException.class, () -> ItemsContent.components(json));
        json.put("format_version", "1.16");
        json.getJSONObject("minecraft:item").getJSONObject("description").put("identifier", "minecraft:stone");
        assertThrows(IllegalArgumentException.class, () -> ItemsContent.components(json));
        assertThrows(IllegalArgumentException.class, () -> ItemsContent.applyProperties(manager(),
                new JSONObject("{\"minecraft:max_stack_size\":0}")));
    }

    @Test
    void supportsNamedAndNumericFoodSaturationModifiers() {
        assertEquals(0.6f, ItemsContent.saturation(new JSONObject()));
        assertEquals(0.8f, ItemsContent.saturation(new JSONObject("{\"saturation_modifier\":\"good\"}")));
        assertEquals(0.3f, ItemsContent.saturation(new JSONObject("{\"saturation_modifier\":0.3}")));
    }
}
