package com.reider745.behavior.items;

import com.reider745.api.CustomManager;
import com.reider745.behavior.BehaviorContent;
import com.reider745.behavior.BehaviorVersion;
import com.reider745.item.CustomItem;
import com.reider745.item.CustomItemClass;
import com.reider745.item.ItemMethod.PropertiesNames;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import org.json.JSONObject;

import java.util.Set;

public class ItemsContent extends BehaviorContent {
    private static final Set<String> SUPPORTED = Set.of("minecraft:display_name", "minecraft:max_stack_size",
            "minecraft:max_damage", "minecraft:durability", "minecraft:food", "minecraft:hand_equipped");
    private static final Set<String> CLIENT_COMPONENTS = Set.of("minecraft:icon", "minecraft:render_offsets",
            "minecraft:use_animation", "minecraft:foil", "minecraft:creative_category");

    @Override
    public String getTagName() {
        return "minecraft:item";
    }

    @Override
    public String getDefaultDirectory() {
        return "items";
    }

    public static JSONObject components(JSONObject json) {
        BehaviorVersion.requireSupported(json.getString("format_version"), "1.10");
        JSONObject item = json.getJSONObject("minecraft:item");
        String identifier = item.getJSONObject("description").getString("identifier");
        if (!identifier.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || identifier.startsWith("minecraft:")) {
            throw new IllegalArgumentException("Custom item requires a non-vanilla namespaced identifier: " + identifier);
        }
        return item.optJSONObject("components") == null ? new JSONObject() : item.getJSONObject("components");
    }

    private static Object value(JSONObject components, String key, Object fallback) {
        Object value = components.opt(key);
        return value instanceof JSONObject object ? object.opt("value") : value == null ? fallback : value;
    }

    public static void applyProperties(CustomManager manager, JSONObject components) {
        int stack = ((Number) value(components, "minecraft:max_stack_size", 64)).intValue();
        if (stack < 1 || stack > 64) throw new IllegalArgumentException("Item stack size must be between 1 and 64");
        manager.put(PropertiesNames.MAX_STACK, stack);
        int damage = ((Number) value(components, "minecraft:max_damage", 0)).intValue();
        JSONObject durability = components.optJSONObject("minecraft:durability");
        if (durability != null) damage = durability.getInt("max_durability");
        if (damage < 0) throw new IllegalArgumentException("Negative item durability");
        manager.put(PropertiesNames.MAX_DAMAGE, damage);
        manager.put(PropertiesNames.HAND_EQUIPPED, value(components, "minecraft:hand_equipped", false));
        Object display = value(components, "minecraft:display_name", manager.get(PropertiesNames.NAME));
        if (display instanceof String name) manager.put(PropertiesNames.NAME, name);
    }

    @Override
    public void load(JSONObject json, String file) {
        JSONObject components = components(json);
        JSONObject item = json.getJSONObject(getTagName());
        JSONObject description = item.getJSONObject("description");
        String identifier = description.getString("identifier");
        // Validate before allocating an ID or changing any global registry.
        CustomManager properties = new CustomManager(0, CustomItemClass.class, "item", identifier);
        properties.put(PropertiesNames.NAME, "item." + identifier + ".name");
        applyProperties(properties, components);
        JSONObject food = components.optJSONObject("minecraft:food");
        if (food != null && food.optInt("nutrition", 0) < 0) {
            throw new IllegalArgumentException("Negative food nutrition");
        }
        if (food != null && (!Float.isFinite(saturation(food)) || saturation(food) < 0)) {
            throw new IllegalArgumentException("Invalid food saturation");
        }
        int id = IDRegistry.genItemID(identifier);
        if (id <= 0 || id >= IDRegistry.BLOCK_ID_OFFSET) {
            throw new IllegalArgumentException("Cannot allocate item ID for " + identifier);
        }
        if (CustomItem.customItems.containsKey(identifier)) {
            throw new IllegalArgumentException("Item is already registered: " + identifier);
        }
        CustomManager manager = CustomItem.registerItem(identifier, id, properties.get(PropertiesNames.NAME),
                food == null ? CustomItemClass.class : BehaviorFoodItem.class);
        applyProperties(manager, components);
        if (food != null) {
            CustomItem.foods.put(id, food.optInt("nutrition", 0));
            // Saturation is stored as the amount restored, not the Bedrock multiplier.
            manager.put("behavior_food_saturation", saturation(food) * food.optInt("nutrition", 0) * 2);
            for (String key : food.keySet()) {
                if (!Set.of("nutrition", "saturation_modifier", "is_meat").contains(key)) {
                    Logger.warning("Unsupported food property " + key + ", " + file);
                }
            }
        }
        String category = description.optString("category", "items");
        int categoryId = switch (category) {
            case "construction" -> 1;
            case "nature" -> 2;
            case "equipment" -> 3;
            default -> 4;
        };
        manager.put(PropertiesNames.CREATIVE_CATEGORY, categoryId);
        if (!category.equals("none")) CustomItem.addCreative(id, 1, 0, null);
        for (String component : components.keySet()) {
            if (!SUPPORTED.contains(component) && !CLIENT_COMPONENTS.contains(component)) {
                Logger.warning("Unsupported item component " + component + ", " + file);
            }
        }
        if (item.has("events")) Logger.warning("Unsupported item events, " + file);
    }

    public static float saturation(JSONObject food) {
        Object value = food.opt("saturation_modifier");
        if (value instanceof Number number) return number.floatValue();
        return switch (String.valueOf(value)) {
            case "poor" -> 0.1f;
            case "low" -> 0.3f;
            case "good" -> 0.8f;
            case "max" -> 1.0f;
            case "supernatural" -> 1.2f;
            default -> 0.6f;
        };
    }
}
