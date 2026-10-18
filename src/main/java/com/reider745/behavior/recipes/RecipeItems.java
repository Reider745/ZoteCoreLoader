package com.reider745.behavior.recipes;

import cn.nukkit.item.Item;
import com.reider745.item.CustomItem;
import org.json.JSONArray;
import org.json.JSONObject;

/** Shared item syntax used by crafting and smelting recipes in Bedrock 1.16. */
public final class RecipeItems {
    private RecipeItems() {}

    public static Item parse(Object value) {
        if (value instanceof JSONArray array) {
            if (array.length() != 1) throw new IllegalArgumentException("Expected one recipe item: " + value);
            return parse(array.get(0));
        }
        JSONObject object = value instanceof JSONObject json ? json : null;
        String identifier = object == null ? (String) value : object.getString("item");
        Integer customId = CustomItem.customItems.get(identifier);
        Item item = customId == null ? Item.fromString(identifier) : Item.get(customId);
        item = item.clone();
        item.setCount(1);
        if (object != null) {
            if (object.has("data")) {
                int data = object.getInt("data");
                if (data < -1 || data > 65535) throw new IllegalArgumentException("Invalid recipe data: " + data);
                item.setDamage(data == -1 || data == 32767 ? null : data);
            }
            item.setCount(object.optInt("count", 1));
        }
        if (item.isNull() || item.getCount() <= 0) {
            throw new IllegalArgumentException("Unknown or empty recipe item: " + value);
        }
        return item;
    }

    public static Item result(Object value) {
        Item item = parse(value);
        if (!item.hasMeta()) throw new IllegalArgumentException("Wildcard recipe result: " + value);
        return item;
    }
}
